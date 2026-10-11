package com.tramtruyen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveReportRequest {

    @NotBlank(message = "Ghi chú xử lý / Lý do từ chối không được để trống")
    @Size(max = 500, message = "Ghi chú không được vượt quá 500 ký tự")
    private String adminNote;

    private String action; // HIDE, DELETE, DRAFT_CHAPTER, RECORD_ONLY

    private String decision; // PROCESSED or REJECTED
}
