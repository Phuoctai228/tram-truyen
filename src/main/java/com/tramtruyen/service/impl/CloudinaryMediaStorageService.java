package com.tramtruyen.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.tramtruyen.config.CloudinaryProperties;
import com.tramtruyen.service.MediaStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/** Cloudinary implementation of the media storage boundary. */
@Service
@RequiredArgsConstructor
public class CloudinaryMediaStorageService implements MediaStorageService {

    private static final String NOVEL_COVER_FOLDER = "tram-truyen/novels";

    private final Cloudinary cloudinary;
    private final CloudinaryProperties properties;

    @Override
    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "resource_type", "image",
                    "folder", NOVEL_COVER_FOLDER,
                    "upload_preset", properties.getUploadPreset()));
            return String.valueOf(result.get("secure_url"));
        } catch (IOException exception) {
            throw new IllegalStateException("Không thể tải ảnh bìa lên kho lưu trữ", exception);
        }
    }
}