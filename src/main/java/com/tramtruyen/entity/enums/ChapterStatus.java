package com.tramtruyen.entity.enums;

/**
 * Trạng thái biên tập, bảo mật và quyền truy cập của chương truyện.
 */
public enum ChapterStatus {
    DRAFT("Bản nháp"),
    PUBLISHED_REGULAR("Xuất bản thường"),
    PUBLISHED_VIP("Xuất bản VIP"),
    LOCKED("Đã khóa");

    private final String displayName;

    ChapterStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
