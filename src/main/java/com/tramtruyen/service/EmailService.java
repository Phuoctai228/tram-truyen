package com.tramtruyen.service;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otpCode);
    void sendForgotPasswordOtpEmail(String toEmail, String otpCode);
}
