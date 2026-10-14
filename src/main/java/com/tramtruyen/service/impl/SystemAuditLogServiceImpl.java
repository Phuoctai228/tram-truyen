package com.tramtruyen.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
    // 2. MỨC ĐỘ CẢNH BÁO (SEVERITY)
    // =========================================================================
    public enum Severity {
        INFO("Thông tin", "bg-emerald-50 text-emerald-700 border-emerald-200", "bg-emerald-500"),
        WARNING("Cảnh báo", "bg-amber-50 text-amber-700 border-amber-200", "bg-amber-500"),
        CRITICAL("Nguy cơ cao", "bg-rose-50 text-rose-700 border-rose-200", "bg-rose-500");

        private final String label;
        private final String badgeClass;
        private final String dotClass;

        Severity(String label, String badgeClass, String dotClass) {
            this.label = label;
            this.badgeClass = badgeClass;
            this.dotClass = dotClass;
        }

        public String label() { return label; }
        public String badgeClass() { return badgeClass; }
        public String dotClass() { return dotClass; }
    }

    private static final Map<String, Severity> SEVERITY_REGISTRY = new HashMap<>();
    static {
        // Info: SEND_NOTIFICATION, RESOLVE_COMMENT_REPORT, RESOLVE_CONTENT_REPORT, UNBAN_USER, AUTO_UNBAN
        SEVERITY_REGISTRY.put("SEND_NOTIFICATION", Severity.INFO);
        SEVERITY_REGISTRY.put("RESOLVE_COMMENT_REPORT", Severity.INFO);
        SEVERITY_REGISTRY.put("RESOLVE_CONTENT_REPORT", Severity.INFO);
        SEVERITY_REGISTRY.put("UNBAN_USER", Severity.INFO);
        SEVERITY_REGISTRY.put("AUTO_UNBAN", Severity.INFO);

        // Warning: REJECT_COMMENT_REPORT, REJECT_CONTENT_REPORT
        SEVERITY_REGISTRY.put("REJECT_COMMENT_REPORT", Severity.WARNING);
        SEVERITY_REGISTRY.put("REJECT_CONTENT_REPORT", Severity.WARNING);

        // Critical: BAN_USER, CHANGE_USER_ROLE, UPDATE_SYSTEM_SETTINGS
        SEVERITY_REGISTRY.put("BAN_USER", Severity.CRITICAL);
        SEVERITY_REGISTRY.put("CHANGE_USER_ROLE", Severity.CRITICAL);
        SEVERITY_REGISTRY.put("UPDATE_SYSTEM_SETTINGS", Severity.CRITICAL);
    }

    // =========================================================================
    // 3. NHÓM HÀNH ĐỘNG (ACTION GROUPS - DATA-DRIVEN)
    // =========================================================================
    public record ActionGroupDef(String code, String name, List<String> actionTypes) {}

    private static final List<ActionGroupDef> ACTION_GROUPS = List.of(
            new ActionGroupDef("ACCOUNT_ROLE", "Phân quyền & Tài khoản", List.of(
                    "BAN_USER", "UNBAN_USER", "AUTO_UNBAN", "CHANGE_USER_ROLE"
            )),
            new ActionGroupDef("SYSTEM_CONFIG", "Cấu hình hệ thống", List.of(
                    "UPDATE_SYSTEM_SETTINGS"
            )),
            new ActionGroupDef("CONTENT_REPORT", "Quản lý nội dung / Truyện", List.of(
                    "RESOLVE_COMMENT_REPORT", "REJECT_COMMENT_REPORT", "RESOLVE_CONTENT_REPORT", "REJECT_CONTENT_REPORT", "SEND_NOTIFICATION"
            )),
            // Khai báo sẵn sàng mở rộng khi có action_type mới:
            new ActionGroupDef("FINANCE_TRANSACTION", "Tiền tệ & Giao dịch", List.of(
                    // Chưa có action_type nào trong system_audit_logs -> Tự động ẩn trên giao diện
            )),
            new ActionGroupDef("SECURITY_LOGIN", "Bảo mật & Đăng nhập", List.of(
                    // Chưa có action_type nào trong system_audit_logs -> Tự động ẩn trên giao diện
            ))
    );

    @Override
    public List<ActionGroupItem> getActiveActionGroups() {
        return ACTION_GROUPS.stream()
                .filter(g -> g.actionTypes() != null && !g.actionTypes().isEmpty())
                .map(g -> new ActionGroupItem(g.code(), g.name()))
                .toList();
    }

    // =========================================================================
    // 4. STATS (CHỈ SỐ TỔNG QUAN - TÍNH THEO GIỜ HÀ NỘI UTC+7)
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public SystemAuditLogStatsDTO getStats() {
        String totalSql = "SELECT COUNT(*) FROM system_audit_logs";
        String todaySql = "SELECT COUNT(*) FROM system_audit_logs WHERE created_at >= :todayStartUtc AND created_at <= :todayEndUtc";
        String activeAdminsSql = "SELECT COUNT(DISTINCT admin_id) FROM system_audit_logs WHERE created_at >= :last24hUtc";

        LocalDate todayHanoi = DateTimeUtils.getHanoiToday();
        LocalDateTime todayStartUtc = DateTimeUtils.hanoiToUtc(todayHanoi.atStartOfDay());
        LocalDateTime todayEndUtc = DateTimeUtils.hanoiToUtc(todayHanoi.atTime(LocalTime.MAX));
        LocalDateTime last24hUtc = DateTimeUtils.hanoiToUtc(LocalDateTime.now().minusHours(24));

        Long total = jdbcTemplate.getJdbcTemplate().queryForObject(totalSql, Long.class);
        total = total != null ? total : 0L;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("todayStartUtc", Timestamp.valueOf(todayStartUtc));
        params.addValue("todayEndUtc", Timestamp.valueOf(todayEndUtc));
        Long todayCount = jdbcTemplate.queryForObject(todaySql, params, Long.class);
        todayCount = todayCount != null ? todayCount : 0L;

        params = new MapSqlParameterSource();
        params.addValue("last24hUtc", Timestamp.valueOf(last24hUtc));
        Long activeAdmins = jdbcTemplate.queryForObject(activeAdminsSql, params, Long.class);
        activeAdmins = activeAdmins != null ? activeAdmins : 0L;

        return SystemAuditLogStatsDTO.builder()
                .totalRecords(total)
                .todayRecords(todayCount)
                .activeAdminsLast24h(activeAdmins)
                .todayGrowthText("Hôm nay (" + DateTimeUtils.getHanoiTodayString() + ")")
                .build();
    }

    // =========================================================================
    // 5. QUERY DANH SÁCH BỘ LỌC MỚI THEO MOCK
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public Page<SystemAuditLogDTO> getAuditLogsFiltered(String keyword, String actionGroup, String severity,
                                                         String timeRange, String startDate, String endDate,
                                                         Boolean onlyAdmin, int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;

        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");
        MapSqlParameterSource params = new MapSqlParameterSource();

        // 5.1. Lọc theo khoảng thời gian (tính theo giờ Hà Nội UTC+7 rồi đổi sang UTC)
        DateTimeUtils.DateRangeUtc dateRange = DateTimeUtils.calculateUtcRangeFromHanoi(timeRange, startDate, endDate);
        if (!dateRange.isInverted()) {
            if (dateRange.startUtc() != null) {
                whereClause.append(" AND l.created_at >= :startUtc ");
                params.addValue("startUtc", Timestamp.valueOf(dateRange.startUtc()));
            }
            if (dateRange.endUtc() != null) {
                whereClause.append(" AND l.created_at <= :endUtc ");
                params.addValue("endUtc", Timestamp.valueOf(dateRange.endUtc()));
            }
        }

        // 5.2. Lọc theo Nhóm hành động (Action Group)
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

        // 5.3. Lọc theo Mức độ cảnh báo (Severity)
        if (severity != null && !severity.trim().isEmpty() && !"ALL".equalsIgnoreCase(severity.trim())) {
            String normSeverity = severity.trim().toUpperCase();
            List<String> matchingActions = SEVERITY_REGISTRY.entrySet().stream()
                    .filter(e -> e.getValue().name().equalsIgnoreCase(normSeverity))
                    .map(Map.Entry::getKey)
                    .toList();
            if (matchingActions.isEmpty()) {
                whereClause.append(" AND 1 = 0 ");
            } else {
                whereClause.append(" AND l.action_type IN (:severityActions) ");
                params.addValue("severityActions", matchingActions);
            }
        }

        // 5.4. Chip "Chỉ Admin": người thực hiện đang mang ROLE_ADMIN
        if (Boolean.TRUE.equals(onlyAdmin)) {
            whereClause.append("""
                     AND EXISTS (
                        SELECT 1 FROM user_roles ur
                        JOIN roles r ON ur.role_id = r.id
                        WHERE ur.user_id = l.admin_id AND r.name = 'ROLE_ADMIN'
                    ) 
            """);
        }

        // 5.5. Ô tìm kiếm thông minh (keyword)
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
                "u.full_name, u.email, u.avatar_url " +
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
            String avatarUrl = rs.getString("avatar_url");

            boolean isAutoUnban = "AUTO_UNBAN".equalsIgnoreCase(action);
            if (isAutoUnban) {
                fullName = "Hệ thống (tự động)";
                email = "system.scheduler@internal";
                avatarUrl = null;
            } else {
                if (fullName == null || fullName.isBlank()) {
                    fullName = "Quản trị viên #" + adminId;
                }
                if (email == null || email.isBlank()) {
                    email = "admin_" + adminId + "@tramtruyen.com";
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

            // Severity calculation
            Severity sev = SEVERITY_REGISTRY.get(action);
            String sevCode = sev != null ? sev.name() : "NEUTRAL";
            String sevLabel = sev != null ? sev.label() : "Bình thường";
            String sevBadge = sev != null ? sev.badgeClass() : "bg-gray-100 text-gray-700 border-gray-200";
            String sevDot = sev != null ? sev.dotClass() : "bg-gray-400";

            return SystemAuditLogDTO.builder()
                    .id(id)
                    .adminId(adminId)
                    .adminName(fullName)
                    .adminEmail(email)
                    .adminAvatar(avatarUrl)
                    .adminRoleBadge(isAutoUnban ? "System" : "Admin")
                    .actionType(action)
                    .actionTypeDisplay(meta.label())
                    .actionChipClass(meta.chipClass())
                    .targetEntity(entity)
                    .targetId(isTargetIdNull ? null : tId)
                    .targetDisplay(targetDisplay)
                    .ipAddress(ip != null ? ip : "—")
                    .createdAt(createdAt)
                    .formattedCreatedAt(DateTimeUtils.formatUtcToHanoi(createdAt, "dd/MM/yyyy HH:mm:ss"))
                    .severity(sevCode)
                    .severityDisplay(sevLabel)
                    .severityBadgeClass(sevBadge)
                    .severityDotClass(sevDot)
                    .build();
        });

        return new PageImpl<>(list, PageRequest.of(page, size), totalElements);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SystemAuditLogDTO> getAuditLogs(String startDate, String endDate, String actionType,
                                                String performer, String targetEntity, Integer targetId,
                                                int page, int size) {
        // Chuyển tiếp an toàn sang getAuditLogsFiltered
        return getAuditLogsFiltered(performer, null, null, null, startDate, endDate, false, page, size);
    }

    // =========================================================================
    // 6. CHI TIẾT NHẬT KÝ (FIELD DIFFS)
    // =========================================================================
    @Override
    @Transactional(readOnly = true)
    public SystemAuditLogDetailDTO getAuditLogDetail(Integer logId) {
        SystemAuditLog log = systemAuditLogRepository.findById(logId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bản ghi nhật ký với ID " + logId));

        String adminName;
        String adminEmail;
        if ("AUTO_UNBAN".equalsIgnoreCase(log.getActionType())) {
            adminName = "Hệ thống (tự động)";
            adminEmail = "system.scheduler@internal";
        } else {
            Optional<User> userOpt = userRepository.findById(log.getAdminId());
            if (userOpt.isPresent()) {
                adminName = userOpt.get().getFullName();
                adminEmail = userOpt.get().getEmail();
            } else {
                adminName = "Quản trị viên #" + log.getAdminId();
                adminEmail = "admin_" + log.getAdminId() + "@tramtruyen.com";
            }
        }

        ActionMeta meta = ACTION_REGISTRY.getOrDefault(log.getActionType(),
                new ActionMeta(log.getActionType(), "bg-[#f3f0ec] text-[#6d5b4a] border-[#ddd3c8]"));

        String targetDisplay;
        if (log.getTargetEntity() != null && !log.getTargetEntity().isBlank()) {
            if (log.getTargetId() != null) {
                targetDisplay = formatTargetEntity(log.getTargetEntity()) + ": #" + log.getTargetId();
            } else {
                targetDisplay = formatTargetEntity(log.getTargetEntity());
            }
        } else {
            targetDisplay = log.getTargetId() != null ? ("#" + log.getTargetId()) : "—";
        }

        String rawOld = log.getOldValue();
        String rawNew = log.getNewValue();
        String prettyOld = prettyPrintJson(rawOld);
        String prettyNew = prettyPrintJson(rawNew);

        List<AuditLogFieldDiffDTO> diffs = computeFieldDiffs(rawOld, rawNew);
        boolean isDiffAvailable = !diffs.isEmpty();

        return SystemAuditLogDetailDTO.builder()
                .id(log.getId())
                .adminId(log.getAdminId())
                .adminName(adminName)
                .adminEmail(adminEmail)
                .actionType(log.getActionType())
                .actionTypeDisplay(meta.label())
                .actionChipClass(meta.chipClass())
                .targetEntity(log.getTargetEntity())
                .targetId(log.getTargetId())
                .targetDisplay(targetDisplay)
                .ipAddress(log.getIpAddress() != null ? log.getIpAddress() : "—")
                .createdAt(log.getCreatedAt())
                .formattedCreatedAt(DateTimeUtils.formatUtcToHanoi(log.getCreatedAt(), "dd/MM/yyyy HH:mm:ss"))
                .rawOldValue(rawOld)
                .prettyOldValue(prettyOld)
                .rawNewValue(rawNew)
                .prettyNewValue(prettyNew)
                .isJsonDiffAvailable(isDiffAvailable)
                .fieldDiffs(diffs)
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
    // 7. CÁC HÀM TIỆN ÍCH NỘI BỘ
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

    private String prettyPrintJson(String jsonStr) {
        if (jsonStr == null || jsonStr.isBlank()) {
            return "(Trống)";
        }
        try {
            Object obj = objectMapper.readValue(jsonStr, Object.class);
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (Exception e) {
            return jsonStr;
        }
    }

    private List<AuditLogFieldDiffDTO> computeFieldDiffs(String oldJson, String newJson) {
        if (oldJson == null || oldJson.isBlank() || newJson == null || newJson.isBlank()) {
            return Collections.emptyList();
        }
        try {
            Map<String, Object> oldMap = objectMapper.readValue(oldJson, new TypeReference<Map<String, Object>>() {});
            Map<String, Object> newMap = objectMapper.readValue(newJson, new TypeReference<Map<String, Object>>() {});

            Set<String> allKeys = new TreeSet<>();
            allKeys.addAll(oldMap.keySet());
            allKeys.addAll(newMap.keySet());

            List<AuditLogFieldDiffDTO> diffList = new ArrayList<>();
            for (String key : allKeys) {
                Object oldVal = oldMap.get(key);
                Object newVal = newMap.get(key);

                if (!Objects.equals(oldVal, newVal)) {
                    diffList.add(AuditLogFieldDiffDTO.builder()
                            .fieldName(key)
                            .oldValue(oldVal != null ? String.valueOf(oldVal) : "null (không có)")
                            .newValue(newVal != null ? String.valueOf(newVal) : "null (đã bỏ)")
                            .build());
                }
            }
            return diffList;
        } catch (Exception e) {
            return Collections.emptyList();
        }
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
