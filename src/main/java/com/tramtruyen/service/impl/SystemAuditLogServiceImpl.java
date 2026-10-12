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
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemAuditLogServiceImpl implements SystemAuditLogService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SystemAuditLogRepository systemAuditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // Bảng quy đổi nhãn hành động tập trung (Centralized Action Type Dictionary)
    private static final Map<String, ActionMeta> ACTION_REGISTRY = new HashMap<>();

    private record ActionMeta(String label, String chipClass) {}

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

    @Override
    @Transactional(readOnly = true)
    public SystemAuditLogStatsDTO getStats() {
        String totalSql = "SELECT COUNT(*) FROM system_audit_logs";
        String todaySql = "SELECT COUNT(*) FROM system_audit_logs WHERE created_at >= :todayStart";
        String activeAdminsSql = "SELECT COUNT(DISTINCT admin_id) FROM system_audit_logs WHERE created_at >= :last24h";

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime last24h = LocalDateTime.now().minusHours(24);

        Long total = jdbcTemplate.getJdbcTemplate().queryForObject(totalSql, Long.class);
        total = total != null ? total : 0L;

        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("todayStart", Timestamp.valueOf(todayStart));
        Long todayCount = jdbcTemplate.queryForObject(todaySql, params, Long.class);
        todayCount = todayCount != null ? todayCount : 0L;

        params = new MapSqlParameterSource();
        params.addValue("last24h", Timestamp.valueOf(last24h));
        Long activeAdmins = jdbcTemplate.queryForObject(activeAdminsSql, params, Long.class);
        activeAdmins = activeAdmins != null ? activeAdmins : 0L;

        return SystemAuditLogStatsDTO.builder()
                .totalRecords(total)
                .todayRecords(todayCount)
                .activeAdminsLast24h(activeAdmins)
                .todayGrowthText("Hôm nay")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SystemAuditLogDTO> getAuditLogs(String startDate, String endDate, String actionType,
                                               String performer, String targetEntity, Integer targetId,
                                               int page, int size) {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;

        StringBuilder whereClause = new StringBuilder(" WHERE 1=1 ");
        MapSqlParameterSource params = new MapSqlParameterSource();

        // 1. Khoảng ngày
        LocalDate sDate = parseDate(startDate);
        LocalDate eDate = parseDate(endDate);
        if (sDate != null && eDate != null && !sDate.isAfter(eDate)) {
            whereClause.append(" AND l.created_at >= :startDate AND l.created_at <= :endDate ");
            params.addValue("startDate", Timestamp.valueOf(sDate.atStartOfDay()));
            params.addValue("endDate", Timestamp.valueOf(eDate.atTime(23, 59, 59, 999999000)));
        } else if (sDate != null && eDate == null) {
            whereClause.append(" AND l.created_at >= :startDate ");
            params.addValue("startDate", Timestamp.valueOf(sDate.atStartOfDay()));
        } else if (sDate == null && eDate != null) {
            whereClause.append(" AND l.created_at <= :endDate ");
            params.addValue("endDate", Timestamp.valueOf(eDate.atTime(23, 59, 59, 999999000)));
        }

        // 2. Loại hành động
        if (actionType != null && !actionType.trim().isEmpty() && !"ALL".equalsIgnoreCase(actionType.trim())) {
            whereClause.append(" AND l.action_type = :actionType ");
            params.addValue("actionType", actionType.trim());
        }

        // 3. Người thực hiện (email hoặc full_name)
        if (performer != null && !performer.trim().isEmpty()) {
            whereClause.append(" AND (LOWER(u.email) LIKE :performer OR LOWER(u.full_name) LIKE :performer) ");
            params.addValue("performer", "%" + performer.trim().toLowerCase() + "%");
        }

        // 4. Đối tượng (target_entity)
        if (targetEntity != null && !targetEntity.trim().isEmpty() && !"ALL".equalsIgnoreCase(targetEntity.trim())) {
            whereClause.append(" AND l.target_entity = :targetEntity ");
            params.addValue("targetEntity", targetEntity.trim());
        }

        // 5. Mã đối tượng (target_id)
        if (targetId != null) {
            whereClause.append(" AND l.target_id = :targetId ");
            params.addValue("targetId", targetId);
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

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

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

            // Quy tắc đặc biệt: AUTO_UNBAN
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
                    .formattedCreatedAt(createdAt.format(dtf))
                    .build();
        });

        return new PageImpl<>(list, PageRequest.of(page, size), totalElements);
    }

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

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
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

        // Định dạng pretty print JSON cho oldValue & newValue
        String rawOld = log.getOldValue();
        String rawNew = log.getNewValue();
        String prettyOld = prettyPrintJson(rawOld);
        String prettyNew = prettyPrintJson(rawNew);

        // Tính diff các trường thay đổi nếu cả 2 đều là JSON object
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
                .formattedCreatedAt(log.getCreatedAt() != null ? log.getCreatedAt().format(dtf) : "—")
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

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        String trimmed = dateStr.trim();
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy")
        );
        for (DateTimeFormatter dtf : formatters) {
            try {
                return LocalDate.parse(trimmed, dtf);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
