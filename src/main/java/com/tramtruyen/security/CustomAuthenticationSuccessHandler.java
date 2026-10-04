package com.tramtruyen.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom authentication success handler that preserves the user's intended destination.
 * Priority order:
 * 1. Explicit 'redirect' parameter in request (from login form hidden field or query string)
 * 2. Session attribute 'REDIRECT_URL' (saved before redirecting to login page)
 * 3. Spring Security SavedRequest (intercepted protected resource request)
 * 4. Default URL ("/")
 */
@Component
public class CustomAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final RequestCache requestCache = new HttpSessionRequestCache();

    public CustomAuthenticationSuccessHandler() {
        setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {

        // 1. Check if an explicit redirect parameter was passed
        String redirectUrl = request.getParameter("redirect");

        // 2. Check if REDIRECT_URL is stored in the session
        HttpSession session = request.getSession(false);
        if ((redirectUrl == null || redirectUrl.isBlank()) && session != null) {
            Object sessionRedirect = session.getAttribute("REDIRECT_URL");
            if (sessionRedirect instanceof String s && !s.isBlank()) {
                redirectUrl = s;
                session.removeAttribute("REDIRECT_URL");
            }
        }

        // 3. If redirectUrl is a safe internal relative path, redirect to it
        if (redirectUrl != null && !redirectUrl.isBlank() && isValidRedirectUrl(redirectUrl)) {
            clearAuthenticationAttributes(request);
            getRedirectStrategy().sendRedirect(request, response, redirectUrl);
            return;
        }

        // 4. Check Spring Security's saved request (for intercepted requests like /user/bookshelf)
        SavedRequest savedRequest = this.requestCache.getRequest(request, response);
        if (savedRequest != null) {
            String targetUrl = savedRequest.getRedirectUrl();
            if (targetUrl != null && !targetUrl.contains("/login") && !targetUrl.contains("/register")) {
                clearAuthenticationAttributes(request);
                getRedirectStrategy().sendRedirect(request, response, targetUrl);
                return;
            }
        }

        // 5. Default fallback to home
        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, "/");
    }

    /**
     * Prevents open-redirect security vulnerabilities by ensuring the URL is a safe local relative path.
     */
    public static boolean isValidRedirectUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }
        return url.startsWith("/")
                && !url.startsWith("//")
                && !url.contains("\\")
                && !url.contains("://")
                && !url.equals("/login")
                && !url.equals("/logout")
                && !url.equals("/register")
                && !url.startsWith("/login?")
                && !url.startsWith("/logout?")
                && !url.startsWith("/register?");
    }
}
