package com.tramtruyen.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tramtruyen.dto.AuditLogFieldDiffDTO;
import com.tramtruyen.dto.SystemAuditLogDTO;
import com.tramtruyen.dto.SystemAuditLogDetailDTO;
import com.tramtruyen.dto.SystemAuditLogStatsDTO;
import com.tramtruyen.entity.SystemAuditLog;
import com.tramtruyen.entity.User;
import com.tramtruyen.repository.SystemAuditLogRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.SystemAuditLogService;
import com.tramtruyen.util.AuditLogFieldFormatter;
import com.tramtruyen.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemAuditLogServiceImpl implements SystemAuditLogService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SystemAuditLogRepository systemAuditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private static final java.time.format.DateTimeFormatter AUDIT_DATE_TIME_FORMATTER =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    // =========================================================================
    // 1. ACTION REGISTRY (Tập trung danh mục nhãn hành động)
    // =========================================================================
    private static final Map<String, ActionMeta> ACTION_REGISTRY = new HashMap<>();

    public record ActionMeta(String label, String chipClass) {}

    static {
        ACTION_REGISTRY.put("BAN_USER", new ActionMeta("Khóa tài khoản", "bg-[#feebe8] text-[#c53d26] border-[#f7c8c0]"));
        ACTION_REGISTRY.put("UNBAN_USER", new ActionMeta("Mở khóa tài khoản", "bg-[#e8f7ee] text-[#1b7a42] border-[#b8e8cb]"));
        ACTION_REGISTRY.put("AUTO_UNBAN", new ActionMeta("Tự động mở khóa", "bg-[#e8f7ee] text-[#1b7a42] border-[#b8e8cb]"));
        ACTION_REGISTRY.put("CHANGE_USER_ROLE", new ActionMeta("Phân quyền thành viên", "bg-[#fef4e8] text-[#c07c2a] border-[#f5d9b8]"));
        ACTION_REGISTRY.put("UPDATE_SYSTEM_SETTINGS", new ActionMeta("Cập nhật cấu hình", "bg-[#fef4e8] text-[#c07c2a] border-[#f5d9b8]"));
        ACTION_REGISTRY.put("RESOLVE_COMMENT_REPORT", new ActionMeta("Duyệt báo cáo bình luận", "bg-[#e8f7ee] text-[#1b7a42] border-[#b8e8cb]"));
        ACTION_REGISTRY.put("REJECT_COMMENT_REPORT", new ActionMeta("Từ chối báo cáo bình luận", "bg-[#feebe8] text-[#c53d26] border-[#f7c8c0]"));
        ACTION_REGISTRY.put("RESOLVE_CONTENT_REPORT", new ActionMeta("Duyệt báo cáo nội dung", "bg-[#e8f7ee] text-[#1b7a42] border-[#b8e8cb]"));
        ACTION_REGISTRY.put("REJECT_CONTENT_REPORT", new ActionMeta("Từ chối báo cáo nội dung", "bg-[#feebe8] text-[#c53d26] border-[#f7c8c0]"));
        ACTION_REGISTRY.put("SEND_NOTIFICATION", new ActionMeta("Gửi thông báo hệ thống", "bg-[#eff6ff] text-[#1d4ed8] border-[#bfdbfe]"));
    }

    // =========================================================================
    // 2. NHÓM HÀNH ĐỘNG (ACTION GROUPS - Theo module nghiệp vụ tài liệu)
    // =========================================================================
    public record ActionGroupDef(String code, String name, List<String> actionTypes) {}

    private static final List<ActionGroupDef> ACTION_GROUPS = List.of(
            new ActionGroupDef("USER_ROLE", "Tài khoản & Phân quyền", List.of(
                    "BAN_USER", "UNBAN_USER", "AUTO_UNBAN", "CHANGE_USER_ROLE"
            )),
            new ActionGroupDef("REPORT_MODERATION", "Kiểm duyệt báo cáo", List.of(
                    "RESOLVE_COMMENT_REPORT", "REJECT_COMMENT_REPORT", "RESOLVE_CONTENT_REPORT", "REJECT_CONTENT_REPORT"
            )),
            new ActionGroupDef("NOTIFICATION", "Thông báo", List.of(
                    "SEND_NOTIFICATION"
            )),
            new ActionGroupDef("SYSTEM_SETTING", "Cấu hình hệ thống", List.of(
                    "UPDATE_SYSTEM_SETTINGS"
            )),
            // Khai báo sẵn sàng mở rộng khi có action_type mới (tự động ẩn khi rỗng):
            new ActionGroupDef("NOVEL_CONTENT", "Nội dung truyện & chương", List.of())
    );

    @Override
    public List<ActionGroupItem> getActiveActionGroups() {
        return ACTION_GROUPS.stream()
                .filter(g -> g.actionTypes() != null && !g.actionTypes().isEmpty())
                .map(g -> new ActionGroupItem(g.code(), g.name()))
                .toList();
    }

    // =========================================================================
    // 3. STATS (CHỈ SỐ TỔNG QUAN)
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public SystemAuditLogStatsDTO getStats() {
        DateTimeUtils.DateRangeUtc todayBounds = DateTimeUtils.calculateUtcRangeFromHanoi("today", null, null);
        LocalDateTime todayStartUtc = todayBounds.startUtc();
        LocalDateTime todayEndUtc = todayBounds.endUtc();

        LocalDateTime last24hUtc = DateTimeUtils.hanoiToUtc(LocalDateTime.now().minusHours(24));

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("todayStartUtc", todayStartUtc);
        params.addValue("todayEndUtc", todayEndUtc);
        params.addValue("last24hUtc", last24hUtc);

        String totalSql = "SELECT COUNT(*) FROM system_audit_logs";
        String todaySql = "SELECT COUNT(*) FROM system_audit_logs WHERE created_at >= :todayStartUtc AND created_at <= :todayEndUtc";
        String activeAdminsSql = "SELECT COUNT(DISTINCT admin_id) FROM system_audit_logs WHERE created_at >= :last24hUtc";

        Long total = jdbcTemplate.queryForObject(totalSql, params, Long.class);
        Long today = jdbcTemplate.queryForObject(todaySql, params, Long.class);
        Long activeAdmins = jdbcTemplate.queryForObject(activeAdminsSql, params, Long.class);

        return SystemAuditLogStatsDTO.builder()
                .totalRecords(total != null ? total : 0L)
                .todayRecords(today != null ? today : 0L)
                .activeAdminsLast24h(activeAdmins != null ? activeAdmins : 0L)
                .build();
    }

    // =========================================================================
    // 4. DANH SÁCH BẢN GHI (LỌC THEO VAI TRÒ NGƯỜI THỰC HIỆN, NHÓM HÀNH ĐỘNG...)
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public Page<SystemAuditLogDTO> getAuditLogsFiltered(String keyword, String actionGroup, String performerRole,
                                                        String timeRange, String startDate, String endDate,
                                                        int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;

        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");
        MapSqlParameterSource params = new MapSqlParameterSource();

        // 4.1. Lọc theo Khoảng thời gian
        DateTimeUtils.DateRangeUtc dateRange = DateTimeUtils.calculateUtcRangeFromHanoi(timeRange, startDate, endDate);
        if (dateRange.isInverted()) {
            whereClause.append(" AND 1 = 0 ");
        } else {
            if (dateRange.startUtc() != null) {
                whereClause.append(" AND l.created_at >= :startUtc ");
                params.addValue("startUtc", dateRange.startUtc());
            }
            if (dateRange.endUtc() != null) {
                whereClause.append(" AND l.created_at <= :endUtc ");
                params.addValue("endUtc", dateRange.endUtc());
            }
        }

        // 4.2. Lọc theo Nhóm hành động (Action Group)
        if (actionGroup != null && !actionGroup.trim().isEmpty() && !"ALL".equalsIgnoreCase(actionGroup.trim())) {
            String normGroup = actionGroup.trim();
            Optional<ActionGroupDef> groupOpt = ACTION_GROUPS.stream()
                    .filter(g -> g.code().equalsIgnoreCase(normGroup))
                    .findFirst();
            if (groupOpt.isPresent()) {
                List<String> actions = groupOpt.get().actionTypes();
                if (actions.isEmpty()) {
                    whereClause.append(" AND 1 = 0 ");
                } else {
                    whereClause.append(" AND l.action_type IN (:groupActions) ");
                    params.addValue("groupActions", actions);
                }
            }
        }

        // 4.3. Lọc theo Vai trò người thực hiện (ROLE_ADMIN, ROLE_STAFF)
        if (performerRole != null && !performerRole.trim().isEmpty() && !"ALL".equalsIgnoreCase(performerRole.trim())) {
            String targetRole = performerRole.trim().toUpperCase();
            whereClause.append("""
                     AND EXISTS (
                        SELECT 1 FROM user_roles ur
                        JOIN roles r ON ur.role_id = r.id
                        WHERE ur.user_id = l.admin_id AND r.name = :performerRole
                    ) 
            """);
            params.addValue("performerRole", targetRole);
        }

        // 4.4. Ô tìm kiếm thông minh (keyword)
        if (keyword != null && !keyword.trim().isEmpty()) {
            String rawKw = keyword.trim();
            String escapedKw = escapeLike(rawKw.toLowerCase());
            String kwPattern = "%" + escapedKw + "%";
            params.addValue("kwPattern", kwPattern);

            StringBuilder searchOr = new StringBuilder();
            searchOr.append(" (LOWER(u.full_name) LIKE :kwPattern ESCAPE '\\' ");
            searchOr.append(" OR LOWER(u.email) LIKE :kwPattern ESCAPE '\\' ");
            searchOr.append(" OR LOWER(l.action_type) LIKE :kwPattern ESCAPE '\\' ");
            searchOr.append(" OR l.ip_address LIKE :kwPattern ESCAPE '\\' ");
            searchOr.append(" OR LOWER(l.target_entity) LIKE :kwPattern ESCAPE '\\' ");

            // Tìm theo ID đối tượng nếu có chuỗi số (#123, US-123, 123)
            Pattern digitPattern = Pattern.compile("\\d+");
            Matcher matcher = digitPattern.matcher(rawKw);
            if (matcher.find()) {
                try {
                    int parsedId = Integer.parseInt(matcher.group());
                    searchOr.append(" OR l.target_id = :kwTargetId ");
                    params.addValue("kwTargetId", parsedId);
                } catch (NumberFormatException ignored) {
                }
            }

            // Dò tìm theo nhãn tiếng Việt của hành động
            List<String> matchingActionCodes = new ArrayList<>();
            for (Map.Entry<String, ActionMeta> entry : ACTION_REGISTRY.entrySet()) {
                if (entry.getValue().label().toLowerCase().contains(rawKw.toLowerCase())) {
                    matchingActionCodes.add(entry.getKey());
                }
            }
            if (!matchingActionCodes.isEmpty()) {
                searchOr.append(" OR l.action_type IN (:matchingActionCodes) ");
                params.addValue("matchingActionCodes", matchingActionCodes);
            }

            // Dò tìm theo tên đối tượng tiếng Việt
            List<String> matchingEntities = getMatchingEntitiesForKeyword(rawKw.toLowerCase());
            if (!matchingEntities.isEmpty()) {
                searchOr.append(" OR LOWER(l.target_entity) IN (:matchingEntities) ");
                params.addValue("matchingEntities", matchingEntities);
            }

            searchOr.append(") ");
            whereClause.append(" AND ").append(searchOr);
        }

        String countSql = "SELECT COUNT(*) FROM system_audit_logs l LEFT JOIN users u ON l.admin_id = u.id " + whereClause;
        Long totalElements = jdbcTemplate.queryForObject(countSql, params, Long.class);
        totalElements = totalElements != null ? totalElements : 0L;

        String querySql = "SELECT l.id, l.admin_id, l.action_type, l.target_entity, l.target_id, l.ip_address, l.created_at, " +
                "u.full_name, u.email, " +
                "(SELECT string_agg(r.name, ',') FROM user_roles ur JOIN roles r ON ur.role_id = r.id WHERE ur.user_id = u.id) AS role_names " +
                "FROM system_audit_logs l " +
                "LEFT JOIN users u ON l.admin_id = u.id " +
                whereClause +
                "ORDER BY l.created_at DESC " +
                "LIMIT :limit OFFSET :offset ";

        params.addValue("limit", size);
        params.addValue("offset", page * size);

        List<SystemAuditLogDTO> list = jdbcTemplate.query(querySql, params, (rs, rowNum) -> {
            int id = rs.getInt("id");
            int adminId = rs.getInt("admin_id");
            String action = rs.getString("action_type");
            String entity = rs.getString("target_entity");
            int tId = rs.getInt("target_id");
            boolean isTargetIdNull = rs.wasNull();
            String ip = rs.getString("ip_address");
            Timestamp createdAtTs = rs.getTimestamp("created_at");
            LocalDateTime createdAt = createdAtTs != null ? createdAtTs.toLocalDateTime() : LocalDateTime.now();

            String fullName = rs.getString("full_name");
            String email = rs.getString("email");
            String roleNames = rs.getString("role_names");

            boolean isAutoUnban = "AUTO_UNBAN".equalsIgnoreCase(action);
            String adminRoleBadge;

            if (isAutoUnban) {
                fullName = "Hệ thống (tự động)";
                email = "system.scheduler@internal";
                adminRoleBadge = null; // AUTO_UNBAN không hiển thị nhãn vai trò
            } else {
                if (fullName == null || fullName.isBlank()) {
                    fullName = "Quản trị viên #" + adminId;
                }
                if (email == null || email.isBlank()) {
                    email = "admin_" + adminId + "@tramtruyen.com";
                }
                if (roleNames != null && roleNames.contains("ROLE_ADMIN")) {
                    adminRoleBadge = "Quản trị viên";
                } else if (roleNames != null && roleNames.contains("ROLE_STAFF")) {
                    adminRoleBadge = "Nhân viên";
                } else {
                    adminRoleBadge = "Độc giả";
                }
            }

            ActionMeta meta = ACTION_REGISTRY.getOrDefault(action,
                    new ActionMeta(action, "bg-[#f3f0ec] text-[#6d5b4a] border-[#ddd3c8]"));

            String targetDisplay;
            if (entity != null && !entity.isBlank()) {
                if (!isTargetIdNull) {
                    targetDisplay = formatTargetEntity(entity) + ": #" + tId;
                } else {
                    targetDisplay = formatTargetEntity(entity);
                }
            } else {
                targetDisplay = isTargetIdNull ? "—" : ("#" + tId);
            }

            return SystemAuditLogDTO.builder()
                    .id(id)
                    .adminId(adminId)
                    .adminName(fullName)
                    .adminEmail(email)
                    .adminRoleBadge(adminRoleBadge)
                    .actionType(action)
                    .actionTypeDisplay(meta.label())
                    .actionChipClass(meta.chipClass())
                    .targetEntity(entity)
                    .targetId(isTargetIdNull ? null : tId)
                    .targetDisplay(targetDisplay)
                    .ipAddress(ip != null ? ip : "—")
                    .createdAt(createdAt)
                    .formattedCreatedAt(createdAt != null ? createdAt.format(AUDIT_DATE_TIME_FORMATTER) : "—")
                    .build();
        });

        return new PageImpl<>(list, PageRequest.of(page, size), totalElements);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SystemAuditLogDTO> getAuditLogs(String startDate, String endDate, String actionType,
                                                String performer, String targetEntity, Integer targetId,
                                                int page, int size) {
        return getAuditLogsFiltered(performer, null, null, null, startDate, endDate, page, size);
    }

    // =========================================================================
    // 5. CHI TIẾT NHẬT KÝ (SERVER-RENDERED PAGE DTO)
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public SystemAuditLogDetailDTO getAuditLogDetail(Integer logId) {
        SystemAuditLog log = systemAuditLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi nhật ký với ID " + logId));

        String adminName;
        String adminEmail;
        String adminRoleBadge = null;

        if ("AUTO_UNBAN".equalsIgnoreCase(log.getActionType())) {
            adminName = "Hệ thống (tự động)";
            adminEmail = "system.scheduler@internal";
            adminRoleBadge = null; // AUTO_UNBAN không hiển thị nhãn vai trò
        } else {
            Optional<User> userOpt = userRepository.findById(log.getAdminId());
            if (userOpt.isPresent()) {
                User u = userOpt.get();
                adminName = u.getFullName();
                adminEmail = u.getEmail();
                if (u.getRoles() != null && !u.getRoles().isEmpty()) {
                    boolean isAdmin = u.getRoles().stream().anyMatch(r -> "ROLE_ADMIN".equalsIgnoreCase(r.getName()));
                    boolean isStaff = u.getRoles().stream().anyMatch(r -> "ROLE_STAFF".equalsIgnoreCase(r.getName()));
                    if (isAdmin) {
                        adminRoleBadge = "Quản trị viên";
                    } else if (isStaff) {
                        adminRoleBadge = "Nhân viên";
                    } else {
                        adminRoleBadge = "Độc giả";
                    }
                }
            } else {
                adminName = "Quản trị viên #" + log.getAdminId();
                adminEmail = "admin_" + log.getAdminId() + "@tramtruyen.com";
                adminRoleBadge = "Quản trị viên";
            }
        }

        ActionMeta meta = ACTION_REGISTRY.getOrDefault(log.getActionType(),
                new ActionMeta(log.getActionType(), "bg-[#f3f0ec] text-[#6d5b4a] border-[#ddd3c8]"));

        String targetDisplay;
        if (log.getTargetEntity() != null && !log.getTargetEntity().isBlank()) {
            if (log.getTargetId() != null) {
                targetDisplay = formatTargetEntity(log.getTargetEntity()) + " #" + log.getTargetId();
            } else {
                targetDisplay = formatTargetEntity(log.getTargetEntity());
            }
        } else {
            targetDisplay = log.getTargetId() != null ? ("#" + log.getTargetId()) : "—";
        }

        String rawOld = log.getOldValue();
        String rawNew = log.getNewValue();

        List<AuditLogFieldDiffDTO> diffs = AuditLogFieldFormatter.computeFieldDiffs(rawOld, rawNew);
        boolean isDiffAvailable = !diffs.isEmpty();

        String plainFallbackNote = null;
        if (!isDiffAvailable) {
            boolean oldBlank = rawOld == null || rawOld.isBlank();
            boolean newBlank = rawNew == null || rawNew.isBlank();
            if (oldBlank && newBlank) {
                plainFallbackNote = "Bản ghi này không có dữ liệu thay đổi chi tiết.";
            } else {
                // Không phải JSON object, hiển thị nội dung dạng text thường
                StringBuilder sb = new StringBuilder();
                if (!oldBlank) {
                    sb.append("Giá trị cũ: ").append(rawOld).append("\n");
                }
                if (!newBlank) {
                    sb.append("Giá trị mới: ").append(rawNew);
                }
                plainFallbackNote = sb.toString().trim();
            }
        }

        String targetUrl = AuditLogFieldFormatter.resolveTargetUrl(log.getTargetEntity());

        return SystemAuditLogDetailDTO.builder()
                .id(log.getId())
                .adminId(log.getAdminId())
                .adminName(adminName)
                .adminEmail(adminEmail)
                .adminRoleBadge(adminRoleBadge)
                .actionType(log.getActionType())
                .actionTypeDisplay(meta.label())
                .actionChipClass(meta.chipClass())
                .targetEntity(log.getTargetEntity())
                .targetId(log.getTargetId())
                .targetDisplay(targetDisplay)
                .targetUrl(targetUrl)
                .ipAddress(log.getIpAddress() != null ? log.getIpAddress() : "—")
                .createdAt(log.getCreatedAt())
                .formattedCreatedAt(log.getCreatedAt() != null ? log.getCreatedAt().format(AUDIT_DATE_TIME_FORMATTER) : "—")
                .isJsonDiffAvailable(isDiffAvailable)
                .fieldDiffs(diffs)
                .plainFallbackNote(plainFallbackNote)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableActionTypes() {
        String sql = "SELECT DISTINCT action_type FROM system_audit_logs WHERE action_type IS NOT NULL ORDER BY action_type";
        return jdbcTemplate.getJdbcTemplate().queryForList(sql, String.class);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableTargetEntities() {
        String sql = "SELECT DISTINCT target_entity FROM system_audit_logs WHERE target_entity IS NOT NULL ORDER BY target_entity";
        return jdbcTemplate.getJdbcTemplate().queryForList(sql, String.class);
    }

    // =========================================================================
    // 6. CÁC HÀM TIỆN ÍCH NỘI BỘ
    // =========================================================================
    private String escapeLike(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }

    private List<String> getMatchingEntitiesForKeyword(String kwLower) {
        List<String> matches = new ArrayList<>();
        if ("người dùng".contains(kwLower) || "thành viên".contains(kwLower) || "tài khoản".contains(kwLower) || "user".contains(kwLower)) {
            matches.addAll(List.of("users", "USERS", "user", "USER"));
        }
        if ("cấu hình".contains(kwLower) || "hệ thống".contains(kwLower) || "setting".contains(kwLower) || "config".contains(kwLower)) {
            matches.addAll(List.of("system_settings", "SYSTEM_SETTINGS"));
        }
        if ("bình luận".contains(kwLower) || "comment".contains(kwLower)) {
            matches.addAll(List.of("comment_reports", "COMMENT_REPORTS"));
        }
        if ("nội dung".contains(kwLower) || "chương".contains(kwLower) || "content".contains(kwLower)) {
            matches.addAll(List.of("content_reports", "CONTENT_REPORTS"));
        }
        if ("thông báo".contains(kwLower) || "notification".contains(kwLower)) {
            matches.addAll(List.of("notifications", "NOTIFICATIONS"));
        }
        if ("truyện".contains(kwLower) || "novel".contains(kwLower)) {
            matches.addAll(List.of("novels", "NOVELS"));
        }
        return matches;
    }

    private String formatTargetEntity(String entity) {
        if (entity == null) return "";
        return switch (entity.toUpperCase()) {
            case "USERS" -> "Người dùng";
            case "SYSTEM_SETTINGS" -> "Cấu hình hệ thống";
            case "COMMENT_REPORTS" -> "Báo cáo bình luận";
            case "CONTENT_REPORTS" -> "Báo cáo nội dung";
            case "NOTIFICATIONS" -> "Thông báo";
            case "NOVELS" -> "Truyện";
            case "CHAPTERS" -> "Chương truyện";
            default -> entity;
        };
    }
}
