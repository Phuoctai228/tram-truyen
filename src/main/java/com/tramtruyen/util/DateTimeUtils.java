package com.tramtruyen.util;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Utility class for formatting date and time representations.
 */
public final class DateTimeUtils {

    private DateTimeUtils() {
    }

    public static final java.time.ZoneId ZONE_UTC = java.time.ZoneId.of("UTC");
    public static final java.time.ZoneId ZONE_HANOI = java.time.ZoneId.of("Asia/Ho_Chi_Minh");

    public record DateRangeUtc(LocalDateTime startUtc, LocalDateTime endUtc, boolean isInverted) {}

    /**
     * Converts a UTC LocalDateTime to Hanoi ZonedDateTime (UTC+7).
     */
    public static java.time.ZonedDateTime toHanoiZonedDateTime(LocalDateTime utcDateTime) {
        if (utcDateTime == null) {
            return null;
        }
        return utcDateTime.atZone(ZONE_UTC).withZoneSameInstant(ZONE_HANOI);
    }

    /**
     * Formats a UTC LocalDateTime into a string in Hanoi (UTC+7) time.
     */
    public static String formatUtcToHanoi(LocalDateTime utcDateTime, String pattern) {
        if (utcDateTime == null) {
            return "—";
        }
        return toHanoiZonedDateTime(utcDateTime).format(java.time.format.DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Formats a SQL Timestamp (assumed stored in UTC) into a string in Hanoi (UTC+7) time.
     */
    public static String formatUtcToHanoi(java.sql.Timestamp utcTimestamp, String pattern) {
        if (utcTimestamp == null) {
            return "—";
        }
        return formatUtcToHanoi(utcTimestamp.toLocalDateTime(), pattern);
    }

    /**
     * Returns today's date in Hanoi timezone (Asia/Ho_Chi_Minh).
     */
    public static java.time.LocalDate getHanoiToday() {
        return java.time.LocalDate.now(ZONE_HANOI);
    }

    /**
     * Returns today's date string in Hanoi timezone as dd/MM/yyyy.
     */
    public static String getHanoiTodayString() {
        return getHanoiToday().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    /**
     * Converts a Hanoi LocalDateTime to a UTC LocalDateTime.
     */
    public static LocalDateTime hanoiToUtc(LocalDateTime hanoiDateTime) {
        if (hanoiDateTime == null) {
            return null;
        }
        return hanoiDateTime.atZone(ZONE_HANOI).withZoneSameInstant(ZONE_UTC).toLocalDateTime();
    }

    /**
     * Calculates the UTC start and end bounds based on Hanoi calendar intervals.
     */
    public static DateRangeUtc calculateUtcRangeFromHanoi(String timeRange, String startDateStr, String endDateStr) {
        java.time.LocalDate today = getHanoiToday();

        if ("today".equalsIgnoreCase(timeRange)) {
            LocalDateTime startHanoi = today.atStartOfDay();
            LocalDateTime endHanoi = today.atTime(java.time.LocalTime.MAX);
            return new DateRangeUtc(hanoiToUtc(startHanoi), hanoiToUtc(endHanoi), false);
        } else if ("7days".equalsIgnoreCase(timeRange)) {
            LocalDateTime startHanoi = today.minusDays(6).atStartOfDay();
            LocalDateTime endHanoi = today.atTime(java.time.LocalTime.MAX);
            return new DateRangeUtc(hanoiToUtc(startHanoi), hanoiToUtc(endHanoi), false);
        } else if ("month".equalsIgnoreCase(timeRange)) {
            LocalDateTime startHanoi = today.withDayOfMonth(1).atStartOfDay();
            LocalDateTime endHanoi = today.atTime(java.time.LocalTime.MAX);
            return new DateRangeUtc(hanoiToUtc(startHanoi), hanoiToUtc(endHanoi), false);
        }

        // Custom date interval or explicit start/end dates
        java.time.LocalDate startDate = null;
        java.time.LocalDate endDate = null;

        if (startDateStr != null && !startDateStr.trim().isEmpty()) {
            try {
                startDate = java.time.LocalDate.parse(startDateStr.trim());
            } catch (Exception ignored) {
            }
        }
        if (endDateStr != null && !endDateStr.trim().isEmpty()) {
            try {
                endDate = java.time.LocalDate.parse(endDateStr.trim());
            } catch (Exception ignored) {
            }
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            return new DateRangeUtc(null, null, true); // Inverted range detected
        }

        LocalDateTime startUtc = (startDate != null) ? hanoiToUtc(startDate.atStartOfDay()) : null;
        LocalDateTime endUtc = (endDate != null) ? hanoiToUtc(endDate.atTime(java.time.LocalTime.MAX)) : null;

        return new DateRangeUtc(startUtc, endUtc, false);
    }

    /**
     * Formats a LocalDateTime into a human-friendly relative time string in Vietnamese.
     * E.g. "Vừa xong", "15 phút trước", "2 giờ trước", "Hôm qua", "3 ngày trước".
     *
     * @param dateTime the past date time to format
     * @return relative time string
     */
    public static String formatRelativeTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "Gần đây";
        }
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long seconds = Math.max(0, duration.getSeconds());
        if (seconds < 60) {
            return "Vừa xong";
        }
        long minutes = duration.toMinutes();
        if (minutes < 60) {
            return minutes + " phút trước";
        }
        long hours = duration.toHours();
        if (hours < 24) {
            return hours + " giờ trước";
        }
        long days = duration.toDays();
        if (days == 1) {
            return "Hôm qua";
        }
        if (days < 30) {
            return days + " ngày trước";
        }
        long months = days / 30;
        if (months < 12) {
            return months + " tháng trước";
        }
        return (months / 12) + " năm trước";
    }
}
