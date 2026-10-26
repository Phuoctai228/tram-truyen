package com.tramtruyen.entity.enums;

/**
 * Trạng thái kích hoạt và kỷ luật của tài khoản người dùng.
 */
public enum UserStatus {
    ACTIVE("Đang hoạt động"),
    PENDING_VERIFICATION("Chờ kích hoạt"),
    BANNED("Bị khóa");

    private final String displayName;

    UserStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
