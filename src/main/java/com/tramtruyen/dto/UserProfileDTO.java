package com.tramtruyen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data transfer object for user profile summary displayed in sidebar and header.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {

    private Integer id;
    private String fullName;
    private String email;
    private String avatarUrl;
    private Integer walletBalance;
    private LocalDateTime createdAt;
    private boolean isEmailVerified;
}
