package com.tramtruyen.util;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Utility class for formatting date and time representations.
 */
public final class DateTimeUtils {

    private DateTimeUtils() {
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
