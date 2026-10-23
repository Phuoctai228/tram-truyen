package com.tramtruyen.util;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

/**
 * Lớp tiện ích phân trang chuẩn hóa cho toàn bộ hệ thống Trạm Truyện.
 * Hỗ trợ parse page/size an toàn, clamp page vượt ngưỡng, và build query string giữ nguyên bộ lọc.
 */
public final class PaginationUtils {

    public static final Set<Integer> ALLOWED_SIZES = Set.of(7, 10, 15, 20);

    private PaginationUtils() {
        // Utility class
    }

    /**
     * Chuyển đổi tham số page sang số nguyên an toàn.
     * Nếu null, rỗng, không phải số hoặc âm thì trả về 0.
     */
    public static int parsePage(String pageStr) {
        if (pageStr == null || pageStr.isBlank()) {
            return 0;
        }
        try {
            int p = Integer.parseInt(pageStr.trim());
            return Math.max(0, p);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Chuyển đổi tham số size sang số nguyên an toàn.
     * Chỉ chấp nhận các giá trị trong tập {7, 10, 15, 20}.
     * Nếu không hợp lệ thì trả về defaultSize.
     */
    public static int parseSize(String sizeStr, int defaultSize) {
        if (sizeStr == null || sizeStr.isBlank()) {
            return defaultSize;
        }
        try {
            int s = Integer.parseInt(sizeStr.trim());
            if (ALLOWED_SIZES.contains(s)) {
                return s;
            }
            return defaultSize;
        } catch (NumberFormatException e) {
            return defaultSize;
        }
    }

    /**
     * Đưa trang về trang cuối hợp lệ nếu page >= totalPages.
     */
    public static int clampPage(int page, int totalPages) {
        if (totalPages <= 0) {
            return 0;
        }
        if (page >= totalPages) {
            return totalPages - 1;
        }
        return Math.max(0, page);
    }

    /**
     * Tạo chuỗi query string từ Map các tham số lọc.
     * - Bỏ qua các giá trị null, chuỗi rỗng.
     * - Bỏ qua các key 'page' và 'size' (fragment tự gắn).
     * - Bỏ qua 'selectedId' (dành riêng cho notifications).
     * - Mã hóa từng giá trị an toàn theo UTF-8 (hỗ trợ tiếng Việt, khoảng trắng, &, %, +, #).
     */
    public static String buildFilterQuery(Map<String, ?> params) {
        if (params == null || params.isEmpty()) {
            return "";
        }
        StringJoiner joiner = new StringJoiner("&");
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            String key = entry.getKey();
            if (key == null || key.isBlank() || "page".equalsIgnoreCase(key) || "size".equalsIgnoreCase(key) || "selectedId".equalsIgnoreCase(key)) {
                continue;
            }
            Object val = entry.getValue();
            if (val == null) {
                continue;
            }
            String strVal = val.toString().trim();
            if (strVal.isEmpty()) {
                continue;
            }
            // Không đưa false của Boolean vào query nếu không cần thiết
            if (val instanceof Boolean b && !b) {
                continue;
            }
            String encodedKey = URLEncoder.encode(key.trim(), StandardCharsets.UTF_8);
            String encodedVal = URLEncoder.encode(strVal, StandardCharsets.UTF_8);
            joiner.add(encodedKey + "=" + encodedVal);
        }
        return joiner.toString();
    }
}
