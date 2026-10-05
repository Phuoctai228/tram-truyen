package com.tramtruyen.security;

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
 * - LockedException / OAuth2 account_locked -> /login?error=locked
 * - DisabledException -> /login?error=disabled
 * - Other authentication errors -> /login?error
 */
@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String targetUrl = "/login?error";

        if (exception instanceof LockedException) {
            targetUrl = "/login?error=locked";
        } else if (exception instanceof DisabledException) {
            targetUrl = "/login?error=disabled";
        } else if (exception instanceof OAuth2AuthenticationException oauth2Ex) {
            if (oauth2Ex.getError() != null && "account_locked".equals(oauth2Ex.getError().getErrorCode())) {
                targetUrl = "/login?error=locked";
            }
        }

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
