package com.tramtruyen.service.impl;

import com.tramtruyen.service.EmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtpEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã xác nhận đăng ký tài khoản Trạm Truyện");
        message.setText("Chào bạn,\n\nMã xác nhận (OTP) của bạn là: " + otpCode + 
                "\n\nMã này có hiệu lực trong vòng 15 phút.\nVui lòng không chia sẻ mã này cho bất kỳ ai.\n\nTrân trọng,\nĐội ngũ Trạm Truyện.");
        mailSender.send(message);
    }
}
