package com.tramtruyen.entity.enums;

/**
 * Trạng thái vòng đời và phát hành của bộ truyện.
 */
public enum NovelStatus {
    ONGOING("Đang ra"),
    COMPLETED("Hoàn thành"),
    ON_HOLD("Tạm ngưng"),
    ARCHIVED("Lưu trữ");

    private final String displayName;

    NovelStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
