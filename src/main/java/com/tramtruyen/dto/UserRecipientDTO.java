package com.tramtruyen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRecipientDTO {
    private Integer id;
    private String fullName;
    private String email;
    private String avatarUrl;
    private String initial;
}
