package com.tramtruyen.service.impl;

import com.tramtruyen.dto.RegisterRequestDTO;
import com.tramtruyen.entity.EmailVerificationToken;
import com.tramtruyen.entity.Role;
import com.tramtruyen.entity.User;
import com.tramtruyen.repository.EmailVerificationTokenRepository;
import com.tramtruyen.repository.RoleRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.AuthService;
import com.tramtruyen.service.EmailService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public AuthServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           EmailVerificationTokenRepository tokenRepository, PasswordEncoder passwordEncoder,
                           EmailService emailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void registerUser(RegisterRequestDTO requestDTO) {
        if (userRepository.existsByEmail(requestDTO.getEmail())) {
            throw new IllegalArgumentException("Email đã được sử dụng");
        }

        if (!requestDTO.getPassword().equals(requestDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp");
        }

        User user = User.builder()
                .email(requestDTO.getEmail())
                .password(passwordEncoder.encode(requestDTO.getPassword()))
                .fullName(requestDTO.getFullName())
                .status("PENDING_VERIFICATION")
                .authProvider("LOCAL")
                .walletBalance(0)
                .build();

        // Assign role MEMBER
        Role memberRole = roleRepository.findByName("ROLE_MEMBER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("ROLE_MEMBER");
                    return roleRepository.save(role);
                });
        user.getRoles().add(memberRole);

        user = userRepository.save(user);

        // Generate OTP
        String otp = String.format("%06d", new Random().nextInt(1000000));
        
        EmailVerificationToken token = EmailVerificationToken.builder()
                .user(user)
                .otpCode(otp)
                .expiryDate(LocalDateTime.now().plusMinutes(15))
                .build();
                
        tokenRepository.save(token);

        // Send Email
        emailService.sendOtpEmail(user.getEmail(), otp);
    }

    @Override
    @Transactional
    public void verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với email này."));

        if ("ACTIVE".equals(user.getStatus())) {
            throw new IllegalArgumentException("Tài khoản đã được xác thực.");
        }

        EmailVerificationToken token = tokenRepository.findByOtpCodeAndUser(otp, user)
                .orElseThrow(() -> new IllegalArgumentException("Mã OTP không chính xác."));

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Mã OTP đã hết hạn.");
        }

        user.setStatus("ACTIVE");
        userRepository.save(user);

        tokenRepository.deleteByUser(user);
    }
}
