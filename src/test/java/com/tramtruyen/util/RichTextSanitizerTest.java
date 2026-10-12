package com.tramtruyen.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RichTextSanitizerTest {

    private final RichTextSanitizer sanitizer = new RichTextSanitizer();

    @Test
    void preservesFontSizeWhenStyleContainsEditorLineHeight() {
        String sanitized = sanitizer.sanitize(
                "<p><span style=\"font-size: 20px; line-height: inherit; color: red\">Selected text</span></p>");

        assertThat(sanitized)
                .contains("font-size: 20px")
                .contains("line-height: inherit")
                .doesNotContain("color");
    }

    @Test
    void removesOutOfRangeFontSize() {
        String sanitized = sanitizer.sanitize("<span style=\"font-size: 120px\">Invalid size</span>");

        assertThat(sanitized)
                .doesNotContain("font-size")
                .contains("Invalid size");
    }
}
