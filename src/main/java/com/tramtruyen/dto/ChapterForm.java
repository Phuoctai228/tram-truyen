package com.tramtruyen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Request data for creating a chapter. */
@Getter
@Setter
@NoArgsConstructor
public class ChapterForm {

    @NotNull(message = "Số chương không được để trống")
    @Positive(message = "Số chương phải lớn hơn 0")
    private Integer chapterNumber;

    @NotBlank(message = "Tiêu đề chương không được để trống")
    @Size(max = 255, message = "Tiêu đề chương không được vượt quá 255 ký tự")
    private String title;

    @NotBlank(message = "Nội dung chương không được để trống")
    private String content;
}