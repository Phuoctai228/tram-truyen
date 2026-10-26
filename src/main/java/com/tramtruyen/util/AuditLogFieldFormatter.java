package com.tramtruyen.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tramtruyen.dto.AuditLogFieldDiffDTO;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Lớp tiện ích ánh xạ tập trung cho Nhật ký hệ thống (System Audit Logs):
 * - Ánh xạ tên trường (khóa JSON) sang nhãn tiếng Việt thân thiện.
 * - Định dạng giá trị sang tiếng Việt chuẩn theo tài liệu nhóm (RDS).
 * - Làm phẳng JSON diff (so sánh Trước/Sau) và xử lý văn bản dài.
 */
public final class AuditLogFieldFormatter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Pattern ISO_DATETIME_PATTERN = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}.*");
    private static final DateTimeFormatter HANOI_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // 1. Ánh xạ khóa JSON sang nhãn tiếng Việt thân thiện
    private static final Map<String, String> FIELD_LABEL_MAP = Map.ofEntries(
            Map.entry("vnd_to_coin_rate", "Tỷ giá quy đổi (VNĐ / Coin)"),
            Map.entry("support_email", "Email hỗ trợ"),
            Map.entry("contact_phone", "Số điện thoại liên hệ"),
            Map.entry("terms_of_service", "Điều khoản dịch vụ"),
            Map.entry("privacy_policy", "Chính sách bảo mật"),
            Map.entry("status", "Trạng thái"),
            Map.entry("reason", "Mã lý do"),
            Map.entry("reasonDisplay", "Lý do khóa"),
            Map.entry("duration", "Thời hạn khóa"),
            Map.entry("note", "Ghi chú xử lý"),
            Map.entry("bannedAt", "Thời điểm khóa"),
            Map.entry("banExpiresAt", "Thời điểm hết hạn khóa"),
            Map.entry("unbanReason", "Lý do mở khóa"),
            Map.entry("unbannedAt", "Thời điểm mở khóa"),
            Map.entry("auto", "Tự động"),
            Map.entry("originalBanExpiresAt", "Hạn khóa ban đầu"),
            Map.entry("roles", "Danh sách vai trò"),
            Map.entry("commentStatus", "Trạng thái bình luận"),
            Map.entry("chapterStatus", "Trạng thái chương"),
            Map.entry("action", "Biện pháp xử lý"),
            Map.entry("adminNote", "Ghi chú kiểm duyệt"),
            Map.entry("scope", "Phạm vi thông báo"),
            Map.entry("recipientId", "ID người nhận"),
            Map.entry("title", "Tiêu đề thông báo"),
            Map.entry("affectedReplyCount", "Số trả lời bị ảnh hưởng"),
            Map.entry("affectedReplyIds", "Danh sách ID trả lời bị ảnh hưởng"),
            Map.entry("active_theme_id", "ID chủ đề giao diện"),
            Map.entry("active_theme_name", "Tên chủ đề giao diện"),
            Map.entry("previous_active_theme_name", "Giao diện trước đó"),
            Map.entry("new_active_theme_name", "Giao diện mới kích hoạt"),
            Map.entry("folder_path", "Mã giao diện")
    );

    // 2. Giá trị thời hạn khóa thực tế (UserManagementServiceImpl)
    private static final Map<String, String> DURATION_MAP = Map.of(
            "3", "3 ngày",
            "7", "7 ngày",
            "30", "30 ngày",
            "forever", "Vĩnh viễn"
    );

    // 3. Giá trị lý do khóa thực tế (UserManagementServiceImpl)
    private static final Map<String, String> BAN_REASON_MAP = com.tramtruyen.service.impl.UserManagementServiceImpl.BAN_REASON_MAP;

    // 4. Trạng thái theo Dữ liệu chuẩn
    private static final Map<String, String> STATUS_MAP = Map.ofEntries(
            Map.entry("ACTIVE", "Đang hoạt động"),
            Map.entry("BANNED", "Bị khóa"),
            Map.entry("PENDING_VERIFICATION", "Chờ kích hoạt"),
            Map.entry("RECEIVED", "Chờ xử lý"),
            Map.entry("PENDING", "Chờ xử lý"),
            Map.entry("PROCESSED", "Đã xử lý"),
            Map.entry("REJECTED", "Từ chối"),
            Map.entry("VISIBLE", "Hiển thị"),
            Map.entry("HIDDEN", "Ẩn"),
            Map.entry("DELETED", "Đã xóa"),
            Map.entry("DRAFT", "Bản nháp"),
            Map.entry("PUBLISHED_REGULAR", "Xuất bản thường"),
            Map.entry("PUBLISHED_VIP", "Xuất bản VIP"),
            Map.entry("ONGOING", "Đang ra"),
            Map.entry("COMPLETED", "Hoàn thành"),
            Map.entry("ON_HOLD", "Tạm dừng"),
            Map.entry("ARCHIVED", "Tạm ẩn")
    );

    // 5. Biện pháp xử lý vi phạm báo cáo
    private static final Map<String, String> ACTION_MAP = Map.of(
            "HIDE", "Ẩn nội dung",
            "DELETE", "Xóa nội dung",
            "NO_CHANGE", "Không thay đổi"
    );

    // 6. Vai trò hệ thống
    private static final Map<String, String> ROLE_MAP = Map.of(
            "ROLE_ADMIN", "Quản trị viên",
            "ROLE_STAFF", "Nhân viên",
            "ROLE_MEMBER", "Độc giả"
    );

    // 7. Phạm vi thông báo
    private static final Map<String, String> NOTIF_SCOPE_MAP = Map.of(
            "ALL", "Toàn hệ thống",
            "SPECIFIC", "Người dùng cụ thể"
    );

    private AuditLogFieldFormatter() {
    }

    public static String getFieldLabel(String fieldName) {
        if (fieldName == null) return "Trường dữ liệu";
        return FIELD_LABEL_MAP.getOrDefault(fieldName, fieldName);
    }

    public static String formatValue(String key, Object rawValue) {
        if (rawValue == null) {
            return "(trống)";
        }
        String strVal = String.valueOf(rawValue).trim();
        if (strVal.isEmpty() || "null".equalsIgnoreCase(strVal)) {
            return "(trống)";
        }

        // 1. Theo key cụ thể
        if ("duration".equals(key)) {
            return DURATION_MAP.getOrDefault(strVal.toLowerCase(), strVal);
        }
        if ("reason".equals(key)) {
            return BAN_REASON_MAP.getOrDefault(strVal.toLowerCase(), strVal);
        }
        if ("action".equals(key)) {
            return ACTION_MAP.getOrDefault(strVal.toUpperCase(), strVal);
        }
        if ("scope".equals(key)) {
            return NOTIF_SCOPE_MAP.getOrDefault(strVal.toUpperCase(), strVal);
        }
        if ("vnd_to_coin_rate".equals(key)) {
            try {
                long rate = Long.parseLong(strVal);
                DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.GERMANY);
                symbols.setGroupingSeparator('.');
                return new DecimalFormat("#,###", symbols).format(rate) + " VNĐ / Coin";
            } catch (Exception ignored) {
            }
        }

        // 2. Boolean
        if ("true".equalsIgnoreCase(strVal)) {
            return "Có";
        }
        if ("false".equalsIgnoreCase(strVal)) {
            return "Không";
        }

        // 3. Status map (áp dụng cho status, commentStatus, chapterStatus...)
        if (STATUS_MAP.containsKey(strVal.toUpperCase())) {
            return STATUS_MAP.get(strVal.toUpperCase());
        }

        // 4. Role list (dạng collection [ROLE_ADMIN, ROLE_STAFF] hoặc chuỗi)
        if (rawValue instanceof Collection<?> coll) {
            List<String> labels = new ArrayList<>();
            for (Object item : coll) {
                String roleKey = String.valueOf(item);
                labels.add(ROLE_MAP.getOrDefault(roleKey, roleKey));
            }
            return String.join(", ", labels);
        }
        if (ROLE_MAP.containsKey(strVal.toUpperCase())) {
            return ROLE_MAP.get(strVal.toUpperCase());
        }

        // 5. ISO DateTime (đã là giờ Hà Nội) -> dd/MM/yyyy HH:mm
        if (ISO_DATETIME_PATTERN.matcher(strVal).matches()) {
            try {
                LocalDateTime ldt = LocalDateTime.parse(strVal);
                return ldt.format(HANOI_FORMATTER);
            } catch (Exception ignored) {
            }
        }

        return strVal;
    }

    /**
     * So sánh 2 chuỗi rawOld và rawNew để tạo danh sách AuditLogFieldDiffDTO.
     */
    public static List<AuditLogFieldDiffDTO> computeFieldDiffs(String rawOld, String rawNew) {
        Map<String, Object> oldMap = parseJsonObject(rawOld);
        Map<String, Object> newMap = parseJsonObject(rawNew);

        if (oldMap == null && newMap == null) {
            return Collections.emptyList();
        }

        Map<String, Object> safeOld = oldMap != null ? oldMap : Collections.emptyMap();
        Map<String, Object> safeNew = newMap != null ? newMap : Collections.emptyMap();

        Set<String> allKeys = new LinkedHashSet<>();
        allKeys.addAll(safeOld.keySet());
        allKeys.addAll(safeNew.keySet());

        List<AuditLogFieldDiffDTO> diffs = new ArrayList<>();

        for (String key : allKeys) {
            Object oVal = safeOld.get(key);
            Object nVal = safeNew.get(key);

            // Bỏ qua nếu giá trị cũ và mới giống hệt nhau
            if (Objects.equals(oVal, nVal)) {
                continue;
            }

            String formattedOld = formatValue(key, oVal);
            String formattedNew = formatValue(key, nVal);

            // Bỏ qua nếu sau khi định dạng vẫn hoàn toàn giống nhau
            if (Objects.equals(formattedOld, formattedNew)) {
                continue;
            }

            String diffType;
            String diffTypeLabel;
            if (oVal == null || "(trống)".equals(formattedOld)) {
                diffType = "ADDED";
                diffTypeLabel = "Thêm mới";
            } else if (nVal == null || "(trống)".equals(formattedNew)) {
                diffType = "DELETED";
                diffTypeLabel = "Đã xóa";
            } else {
                diffType = "MODIFIED";
                diffTypeLabel = "Đã sửa";
            }

            boolean isLong = (formattedOld != null && (formattedOld.length() > 150 || formattedOld.contains("\n")))
                    || (formattedNew != null && (formattedNew.length() > 150 || formattedNew.contains("\n")));

            diffs.add(AuditLogFieldDiffDTO.builder()
                    .fieldName(key)
                    .fieldLabel(getFieldLabel(key))
                    .oldValue(formattedOld)
                    .newValue(formattedNew)
                    .diffType(diffType)
                    .diffTypeLabel(diffTypeLabel)
                    .longText(isLong)
                    .build());
        }

        return diffs;
    }

    public static Map<String, Object> parseJsonObject(String json) {
        if (json == null || json.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception ignored) {
            return null;
        }
    }

    public static String resolveTargetUrl(String targetEntity) {
        if (targetEntity == null) return null;
        String e = targetEntity.trim().toLowerCase();
        return switch (e) {
            case "users" -> "/admin/users";
            case "system_settings" -> "/admin/settings";
            case "comment_reports" -> "/admin/reports?tab=comment";
            case "content_reports" -> "/admin/reports?tab=content";
            case "notifications" -> "/admin/notifications";
            default -> null;
        };
    }
}
