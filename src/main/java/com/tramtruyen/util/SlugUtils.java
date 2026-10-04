package com.tramtruyen.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility class for converting Vietnamese / Unicode strings into URL-friendly slugs.
 */
public final class SlugUtils {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s_]+");
    private static final Pattern EDGES_DASHES = Pattern.compile("(^-+|-+$)");
    private static final Pattern MULTI_DASHES = Pattern.compile("-{2,}");

    private SlugUtils() {
    }

    /**
     * Converts an input title string to an SEO-friendly URL slug.
     * E.g.: "novel 1" -> "novel-1"
     *       "Đấu Phá Thương Khung" -> "dau-pha-thuong-khung"
     */
    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        // Replace Vietnamese 'đ', 'Đ'
        String text = input.trim()
                .replace("đ", "d")
                .replace("Đ", "d");

        // Decompose unicode characters into base letters + combining marks
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        // Strip out diacritical marks
        String withoutDiacritics = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(normalized).replaceAll("");

        // Replace whitespace and underscores with hyphens
        String hyphens = WHITESPACE.matcher(withoutDiacritics).replaceAll("-");

        // Remove non-alphanumeric, non-hyphen characters
        String clean = NONLATIN.matcher(hyphens).replaceAll("");

        // Convert to lowercase
        String lower = clean.toLowerCase(Locale.ENGLISH);

        // Deduplicate consecutive hyphens
        String deduplicated = MULTI_DASHES.matcher(lower).replaceAll("-");

        // Trim leading and trailing hyphens
        return EDGES_DASHES.matcher(deduplicated).replaceAll("");
    }
}
