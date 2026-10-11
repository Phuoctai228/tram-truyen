package com.tramtruyen.util;

import jakarta.servlet.http.HttpServletRequest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Tiện ích hỗ trợ mã hóa HMAC-SHA512 và xử lý tham số giao tiếp với VNPay.
 */
public final class VNPayUtil {

    private VNPayUtil() {
        // Private constructor để ngăn việc khởi tạo utility class
    }

    /**
     * Băm chuỗi dữ liệu theo thuật toán HmacSHA512 với khóa bí mật.
     *
     * @param key  Khóa bí mật (Hash Secret do VNPay cấp)
     * @param data Dữ liệu cần băm
     * @return Chuỗi hexa viết thường
     */
    public static String hmacSHA512(String key, String data) {
        if (key == null || data == null) {
            return "";
        }
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Tạo chuỗi dữ liệu băm và query string từ map tham số (đã sắp xếp A-Z).
     *
     * @param params Map chứa các tham số VNPay
     * @return Đối tượng chứa hashData và queryUrl
     */
    public static Map<String, String> buildQueryAndHashData(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            if (fieldValue != null && !fieldValue.trim().isEmpty()) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));

                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        Map<String, String> result = new HashMap<>();
        result.put("hashData", hashData.toString());
        result.put("queryString", query.toString());
        return result;
    }

    /**
     * Lấy địa chỉ IP thực tế của client từ HttpServletRequest.
     *
     * @param request HTTP request
     * @return Địa chỉ IP (hoặc 127.0.0.1 nếu local)
     */
    public static String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-FORWARDED-FOR");
        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ipAddress)) {
            ipAddress = "127.0.0.1";
        }
        return ipAddress;
    }
}
