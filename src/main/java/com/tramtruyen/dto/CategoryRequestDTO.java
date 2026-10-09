package com.tramtruyen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequestDTO {
    @NotBlank(message = "Tên thể loại không được để trống")
    @Size(max = 100, message = "Tên thể loại tối đa 100 ký tự")
    private String name;

    @NotBlank(message = "Slug không được để trống")
    @Size(max = 100, message = "Slug tối đa 100 ký tự")
    private String slug;

    private String description;
}
