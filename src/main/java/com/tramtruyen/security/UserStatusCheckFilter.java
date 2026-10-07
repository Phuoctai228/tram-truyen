package com.tramtruyen.security;

import com.tramtruyen.dto.AccountLockedInfoDTO;
import com.tramtruyen.entity.User;
import com.tramtruyen.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Filter that verifies the active database status of authenticated users on each request.
 * If an authenticated user has been banned (status BANNED):
 * 1. Checks if ban duration expired (lazy auto-unban).
 * 2. If still banned, invalidates current session, clears SecurityContext, clears remember-me cookie,
 *    creates a new session with LOCKED_USER_INFO, and redirects to /account-locked.
 */
@RequiredArgsConstructor
public class UserStatusCheckFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final AccountLockService accountLockService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/images/")
                || path.startsWith("/favicon.ico")
                || "/account-locked".equals(path)
                || "/login".equals(path)
                || "/logout".equals(path)
                || "/register".equals(path)
                || "/verify-otp".equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 1. Chỉ xử lý khi đã xác thực và principal đúng là CustomUserDetails
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof CustomUserDetails userDetails)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Lấy thông tin user hiện tại
        Integer userId = userDetails.getUser() != null ? userDetails.getUser().getId() : null;
        if (userId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Đọc nhanh status từ DB bằng câu query trên khóa chính
        Optional<String> statusOpt = userRepository.findStatusById(userId);

        // 3. Chỉ chặn khi status đúng là BANNED. Optional rỗng hoặc status khác thì cho đi tiếp
        if (statusOpt.isEmpty() || !"BANNED".equalsIgnoreCase(statusOpt.get())) {
            filterChain.doFilter(request, response);
            return;
        }

        // 4. Nếu status là BANNED, kiểm tra xem đã hết hạn khóa chưa (cơ chế Lazy Unban)
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            boolean autoUnbanned = accountLockService.checkAndAutoUnban(user);
            if (autoUnbanned) {
                // Đã tự động mở khóa thành công, cho phép tiếp tục request
                filterChain.doFilter(request, response);
                return;
            }
        }

        // 5. User vẫn đang bị khóa: Tiến hành hủy phiên và đăng xuất
        String email = userDetails.getUsername();
        AccountLockedInfoDTO lockedInfo = accountLockService.buildAccountLockedInfo(email);

        // Invalidate session cũ
        HttpSession oldSession = request.getSession(false);
        if (oldSession != null) {
            oldSession.invalidate();
        }

        // Xóa SecurityContext
        SecurityContextHolder.clearContext();

        // Xóa cookie remember-me (path là contextPath hoặc /)
        Cookie rememberMeCookie = new Cookie("remember-me", "");
        rememberMeCookie.setMaxAge(0);
        String contextPath = request.getContextPath();
        rememberMeCookie.setPath((contextPath == null || contextPath.isEmpty()) ? "/" : contextPath);
        response.addCookie(rememberMeCookie);

        // Cấp session mới từ container và lưu LOCKED_USER_INFO
        HttpSession newSession = request.getSession(true);
        newSession.setAttribute("LOCKED_USER_INFO", lockedInfo);

        // 6. Phân biệt AJAX request bằng X-Requested-With hoặc Accept không chứa text/html
        String requestedWith = request.getHeader("X-Requested-With");
        String acceptHeader = request.getHeader("Accept");
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(requestedWith)
                || (acceptHeader != null && !acceptHeader.contains("text/html"));

        if (isAjax) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\":\"account_locked\",\"redirectUrl\":\"/account-locked\"}");
            return;
        }

        // Request điều hướng thông thường -> redirect 302 tới /account-locked
        response.sendRedirect(request.getContextPath() + "/account-locked");
    }
}
