package com.tramtruyen.security;

import com.tramtruyen.dto.AccountLockedInfoDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom authentication failure handler for formLogin and oauth2Login.
 * - LockedException / OAuth2 account_locked -> store info in session and redirect to /account-locked
 * - DisabledException -> /login?error=disabled
 * - Other authentication errors -> /login?error
 */
@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final AccountLockService accountLockService;

    public CustomAuthenticationFailureHandler(AccountLockService accountLockService) {
        this.accountLockService = accountLockService;
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

        // 3. Các lỗi xác thực khác
        String targetUrl = "/login?error";
        if (exception instanceof DisabledException) {
            targetUrl = "/login?error=disabled";
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
