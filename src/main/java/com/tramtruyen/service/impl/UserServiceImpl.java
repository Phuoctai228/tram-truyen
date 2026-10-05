package com.tramtruyen.service.impl;

import com.tramtruyen.entity.User;
import com.tramtruyen.exception.ResourceNotFoundException;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.MediaStorageService;
import com.tramtruyen.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final MediaStorageService mediaStorageService;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void updateProfile(String email, String fullName, MultipartFile avatar) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        if (fullName != null && !fullName.trim().isEmpty()) {
            user.setFullName(fullName.trim());
        }

        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = mediaStorageService.uploadImage(avatar);
            if (avatarUrl != null) {
                user.setAvatarUrl(avatarUrl);
            }
        }

        userRepository.save(user);
    }

    @Override
    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));
                
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
