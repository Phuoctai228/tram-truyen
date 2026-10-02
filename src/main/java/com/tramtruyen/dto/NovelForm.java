package com.tramtruyen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/** Form data used to create or update a novel. */
@Getter
@Setter
@NoArgsConstructor
public class NovelForm {

    @NotBlank(message = "Tên truyện không được để trống")
    @Size(max = 255, message = "Tên truyện không được vượt quá 255 ký tự")
    private String title;

    @NotBlank(message = "Tác giả không được để trống")
    @Size(max = 100, message = "Tác giả không được vượt quá 100 ký tự")
    private String author;

    @NotBlank(message = "Tóm tắt không được để trống")
    private String summary;

    @NotBlank(message = "Trạng thái không được để trống")
    @Pattern(regexp = "ONGOING|COMPLETED|ON_HOLD|ARCHIVED", message = "Trạng thái không hợp lệ")
    private String status = "ONGOING";

    private MultipartFile cover;
}