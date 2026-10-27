package com.tramtruyen.entity.enums;

/**
 * Nhà cung cấp định danh xác thực người dùng.
 */
public enum AuthProvider {
    LOCAL("Tài khoản nội bộ"),
    GOOGLE("Google OAuth2");

    private final String displayName;

    AuthProvider(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
