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

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        boolean isStaff = authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_STAFF".equals(authority.getAuthority()));

        SavedRequest savedRequest = this.requestCache.getRequest(request, response);

        // Case 1: ROLE_ADMIN -> Direct to admin dashboard (or specific admin sub-page if requested)
        if (isAdmin) {
            if (redirectUrl != null && redirectUrl.startsWith("/admin") && isValidRedirectUrl(redirectUrl)) {
                clearAuthenticationAttributes(request);
                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
                return;
            }
            if (savedRequest != null) {
                String targetUrl = extractRelativePath(savedRequest.getRedirectUrl());
                if (targetUrl.startsWith("/admin") && isValidRedirectUrl(targetUrl)) {
                    clearAuthenticationAttributes(request);
                    getRedirectStrategy().sendRedirect(request, response, targetUrl);
                    return;
                }
            }
            clearAuthenticationAttributes(request);
            getRedirectStrategy().sendRedirect(request, response, "/admin/novels");
            return;
        }

        // Case 2: ROLE_STAFF -> Direct to staff dashboard (or specific staff sub-page if requested)
        if (isStaff) {
            if (redirectUrl != null && redirectUrl.startsWith("/staff") && isValidRedirectUrl(redirectUrl)) {
                clearAuthenticationAttributes(request);
                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
                return;
            }
            if (savedRequest != null) {
                String targetUrl = extractRelativePath(savedRequest.getRedirectUrl());
                if (targetUrl.startsWith("/staff") && isValidRedirectUrl(targetUrl)) {
                    clearAuthenticationAttributes(request);
                    getRedirectStrategy().sendRedirect(request, response, targetUrl);
                    return;
                }
            }
            clearAuthenticationAttributes(request);
            getRedirectStrategy().sendRedirect(request, response, "/staff/novels");
            return;
        }

        // Case 3: Standard Member / User -> Redirect to requested page or home page
        if (redirectUrl != null && !redirectUrl.isBlank() && isValidRedirectUrl(redirectUrl)) {
            if (!redirectUrl.startsWith("/admin") && !redirectUrl.startsWith("/staff")) {
                clearAuthenticationAttributes(request);
                getRedirectStrategy().sendRedirect(request, response, redirectUrl);
                return;
            }
        }

        if (savedRequest != null) {
            String targetUrl = extractRelativePath(savedRequest.getRedirectUrl());
            if (!targetUrl.startsWith("/admin") && !targetUrl.startsWith("/staff") && isValidRedirectUrl(targetUrl)) {
                clearAuthenticationAttributes(request);
                getRedirectStrategy().sendRedirect(request, response, targetUrl);
                return;
            }
        }

        clearAuthenticationAttributes(request);
        getRedirectStrategy().sendRedirect(request, response, "/");
    }

    private boolean isUrlAllowedForRole(String url, boolean isAdmin, boolean isStaff) {
        if (url == null) return false;
        if (url.startsWith("/admin")) {
            return isAdmin;
        }
        if (url.startsWith("/staff")) {
            return isStaff || isAdmin;
        }
        return true;
    }

    private String extractRelativePath(String fullUrl) {
        if (fullUrl == null) return "/";
        try {
            java.net.URI uri = java.net.URI.create(fullUrl);
            String path = uri.getPath();
            return path != null ? path : fullUrl;
        } catch (Exception e) {
            return fullUrl;
        }
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
