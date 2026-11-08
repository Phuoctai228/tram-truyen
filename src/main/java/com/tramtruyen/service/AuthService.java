package com.tramtruyen.service;

import com.tramtruyen.dto.RegisterRequestDTO;

public interface AuthService {
    void registerUser(RegisterRequestDTO requestDTO);
    void verifyOtp(String email, String otp);
    void resendVerificationOtp(String email);
    void sendForgotPasswordOtp(String email);
    void verifyForgotPasswordOtp(String email, String otp);
    void resetPassword(String email, String newPassword);
}
