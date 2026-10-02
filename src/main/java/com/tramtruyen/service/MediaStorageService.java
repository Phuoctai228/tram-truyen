package com.tramtruyen.service;

import org.springframework.web.multipart.MultipartFile;

/** Stores uploaded media without coupling business services to a vendor SDK. */
public interface MediaStorageService {

    /**
     * Uploads an image and returns its public secure URL.
     *
     * @param file image supplied by the user
     * @return secure URL, or {@code null} when no file was supplied
     */
    String uploadImage(MultipartFile file);
}