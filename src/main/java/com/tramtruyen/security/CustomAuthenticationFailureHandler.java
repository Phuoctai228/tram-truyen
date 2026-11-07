package com.tramtruyen.security;

import com.tramtruyen.dto.AccountLockedInfoDTO;
import com.tramtruyen.entity.User;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Custom authentication failure handler for formLogin and oauth2Login.
 * - LockedException / OAuth2 account_locked -> store info in session and redirect to /account-locked
 * - DisabledException:
 *     If valid credentials for PENDING_VERIFICATION user: resend OTP and redirect to /verify-otp?email=...&resent=true
 *     If invalid credentials: redirect to /login?error
 *     Otherwise: redirect to /login?error=disabled
 * - Other authentication errors -> /login?error
 */
@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final AccountLockService accountLockService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    public CustomAuthenticationFailureHandler(AccountLockService accountLockService,
                                              UserRepository userRepository,
                                              PasswordEncoder passwordEncoder,
                                              @Lazy AuthService authService) {
        this.accountLockService = accountLockService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authService = authService;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        // 1. User bị khóa khi đăng nhập Form (Email & Password)
        if (exception instanceof LockedException) {
            String email = request.getParameter("username");
            AccountLockedInfoDTO lockedInfo = accountLockService.buildAccountLockedInfo(email);
            request.getSession().setAttribute("LOCKED_USER_INFO", lockedInfo);
            getRedirectStrategy().sendRedirect(request, response, "/account-locked");
            return;
        }

        // 2. User bị khóa khi đăng nhập Google OAuth2
        if (exception instanceof OAuth2AuthenticationException oauth2Ex) {
            if (oauth2Ex.getError() != null && "account_locked".equals(oauth2Ex.getError().getErrorCode())) {
                String email = oauth2Ex.getError().getDescription();
                AccountLockedInfoDTO lockedInfo = accountLockService.buildAccountLockedInfo(email);
                request.getSession().setAttribute("LOCKED_USER_INFO", lockedInfo);
                getRedirectStrategy().sendRedirect(request, response, "/account-locked");
                return;
            }
        }

        // 3. User chưa kích hoạt tài khoản (status: PENDING_VERIFICATION)
        if (exception instanceof DisabledException) {
            String email = request.getParameter("username");
            String rawPassword = request.getParameter("password");

            if (email != null && !email.isBlank()) {
                Optional<User> userOpt = userRepository.findByEmail(email.trim());
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    if ("PENDING_VERIFICATION".equalsIgnoreCase(user.getStatus())) {
                        if (rawPassword != null && user.getPassword() != null && passwordEncoder.matches(rawPassword, user.getPassword())) {
                            // Mật khẩu chính xác -> Sinh mã OTP mới, gửi email và chuyển hướng sang trang xác thực
                            authService.resendVerificationOtp(user.getEmail());
                            String targetUrl = "/verify-otp?email=" + URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8) + "&resent=true";
                            getRedirectStrategy().sendRedirect(request, response, targetUrl);
                            return;
                        } else {
                            // Sai mật khẩu -> Chuyển hướng báo lỗi sai mật khẩu bình thường
                            getRedirectStrategy().sendRedirect(request, response, "/login?error");
                            return;
                        }
                    }
                }
            }
            getRedirectStrategy().sendRedirect(request, response, "/login?error=disabled");
            return;
        }

        // 4. Các lỗi xác thực khác
        getRedirectStrategy().sendRedirect(request, response, "/login?error");
    }
}
