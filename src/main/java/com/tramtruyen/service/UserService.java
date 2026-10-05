package com.tramtruyen.service;

import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    void updateProfile(String email, String fullName, MultipartFile avatar);
    void changePassword(String email, String oldPassword, String newPassword);
}
