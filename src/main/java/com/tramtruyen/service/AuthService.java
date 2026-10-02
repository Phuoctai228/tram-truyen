package com.tramtruyen.service;

import com.tramtruyen.dto.RegisterRequestDTO;

public interface AuthService {
    void registerUser(RegisterRequestDTO requestDTO);
    void verifyOtp(String email, String otp);
}
