package com.tramtruyen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO holding account locked metadata to display on /account-locked page.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountLockedInfoDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String title;
    private String subtitle;
    private String reasonDisplay;
    private String bannedAtFormatted;
    private String durationFormatted;
    private String reviewer;
    private String ticketNumber;
    private String supportEmail;
    private boolean permanent;
    private boolean generic;
}
