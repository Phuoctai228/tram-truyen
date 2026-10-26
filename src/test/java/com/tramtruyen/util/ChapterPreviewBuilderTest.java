package com.tramtruyen.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChapterPreviewBuilderTest {

    private ChapterPreviewBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new ChapterPreviewBuilder();
    }

    @Test
    void buildPreviewLimitsToAtMostTwoBlocks() {
        String content = "<p>Đoạn 1</p><p>Đoạn 2</p><p>Đoạn 3</p><p>Đoạn 4</p>";
        List<String> preview = builder.buildPreview(content);

        assertThat(preview).hasSize(2);
        assertThat(preview.get(0)).contains("Đoạn 1");
        assertThat(preview.get(1)).contains("Đoạn 2");
    }

    @Test
    void buildPreviewTruncatesWhenContentExceedsBudget() {
        String longText = "A".repeat(800);
        String content = "<p>" + longText + "</p>";
        List<String> preview = builder.buildPreview(content);

        assertThat(preview).hasSize(1);
        assertThat(preview.get(0).length()).isLessThanOrEqualTo(ChapterPreviewBuilder.PREVIEW_CHAR_LIMIT + 10);
    }

    @Test
    void splitParagraphsSplitsCleanLines() {
        String content = "Dòng 1\r\n\r\nDòng 2\r\n   \r\nDòng 3";
        List<String> paras = builder.splitParagraphs(content);

        assertThat(paras).containsExactly("Dòng 1", "Dòng 2", "Dòng 3");
    }
}
