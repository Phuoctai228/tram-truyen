package com.tramtruyen.service;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otpCode);
}
