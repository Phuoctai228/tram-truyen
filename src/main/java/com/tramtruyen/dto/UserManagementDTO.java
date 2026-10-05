package com.tramtruyen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserManagementDTO {
    private Integer id;
    private String email;
    private String fullName;
    private String avatarUrl;
    private Integer walletBalance;
    private String formattedBalance;
    private String status;
    private String statusDisplayName;
    private LocalDateTime createdAt;
    private String createdAtFormatted;
    private Set<String> roles;
    private String highestRole;
    private String roleDisplayName;
    private boolean banned;
    private boolean admin;
    private boolean staff;
    private String initial;

    // Latest ban info for modal unban
    private String lastBanReason;
    private String lastBanReasonKey;
    private String lastBanDate;
    private String lastBanNote;
}
