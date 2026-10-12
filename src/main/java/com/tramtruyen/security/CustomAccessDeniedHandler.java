package com.tramtruyen.security;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Handles 403 Forbidden scenarios when an authenticated user attempts to access
 * an administrative URL without sufficient privileges.
 */
@Component
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String uri = request.getRequestURI();

        if (auth != null) {
            log.warn("Access Denied: User '{}' with authorities {} attempted to access unauthorized URL '{}'",
                    auth.getName(), auth.getAuthorities(), uri);
        } else {
            log.warn("Access Denied: Anonymous user attempted to access '{}'", uri);
        }

        // Differentiate message based on attempted area and user authorities
        String detailMessage;
        boolean isStaff = auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_STAFF".equals(a.getAuthority()));

        if (uri.startsWith("/admin") && isStaff) {
            detailMessage = "Tài khoản Nhân viên (Staff) không có quyền truy cập vào khu vực Quản trị viên (Admin).";
        } else if (uri.startsWith("/admin") || uri.startsWith("/staff")) {
            detailMessage = "Bạn không có quyền truy cập vào giao diện quản lý này.";
        } else {
            detailMessage = "Bạn không có quyền hạn để thực hiện thao tác này.";
        }

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        request.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpServletResponse.SC_FORBIDDEN);
        request.setAttribute("status", 403);
        request.setAttribute("error", "Từ chối truy cập");
        request.setAttribute("message", detailMessage);

        request.getRequestDispatcher("/error").forward(request, response);
    }
}
