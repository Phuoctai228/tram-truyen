package com.tramtruyen.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

/**
 * Global exception handler that intercepts uncaught exceptions across all controllers.
 * Prevents raw error pages from showing to users by providing:
 * - JSON error responses for AJAX/API requests
 * - Redirect with flash messages for form/page requests
 */
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ─── ResourceNotFoundException → 404 ─────────────────────────────────

    @ExceptionHandler(ResourceNotFoundException.class)
    public Object handleResourceNotFound(ResourceNotFoundException ex,
                                         HttpServletRequest request,
                                         RedirectAttributes redirectAttributes) {
        log.warn("Resource not found: {} [URI={}]", ex.getMessage(), request.getRequestURI());

        if (isAjaxOrApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("success", false, "message", ex.getMessage()));
        }

        // For page requests, redirect back with error message
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:" + extractPath(referer);
        }
        redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/";
    }

    // ─── DuplicateResourceException → 409 ────────────────────────────────

    @ExceptionHandler(DuplicateResourceException.class)
    public Object handleDuplicateResource(DuplicateResourceException ex,
                                          HttpServletRequest request,
                                          RedirectAttributes redirectAttributes) {
        log.warn("Duplicate resource: {} [URI={}]", ex.getMessage(), request.getRequestURI());

        if (isAjaxOrApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("success", false, "message", ex.getMessage()));
        }

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:" + extractPath(referer);
        }
        redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/";
    }

    // ─── InsufficientBalanceException → 400 ──────────────────────────────

    @ExceptionHandler(InsufficientBalanceException.class)
    public Object handleInsufficientBalance(InsufficientBalanceException ex,
                                            HttpServletRequest request,
                                            RedirectAttributes redirectAttributes) {
        log.warn("Insufficient balance: {} [URI={}]", ex.getMessage(), request.getRequestURI());

        if (isAjaxOrApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("success", false, "insufficientBalance", true, "message", ex.getMessage()));
        }

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("insufficientBalance", true);
            return "redirect:" + extractPath(referer);
        }
        redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        redirectAttributes.addFlashAttribute("insufficientBalance", true);
        return "redirect:/";
    }

    // ─── IllegalArgumentException → 400 ──────────────────────────────────

    @ExceptionHandler(IllegalArgumentException.class)
    public Object handleIllegalArgument(IllegalArgumentException ex,
                                        HttpServletRequest request,
                                        RedirectAttributes redirectAttributes) {
        log.warn("Invalid argument: {} [URI={}]", ex.getMessage(), request.getRequestURI());

        if (isAjaxOrApiRequest(request)) {
            return ResponseEntity.badRequest()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("success", false, "message", ex.getMessage()));
        }

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:" + extractPath(referer);
        }
        redirectAttributes.addFlashAttribute("errorMsg", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/";
    }

    // ─── 405 Method Not Allowed ──────────────────────────────────────────
    @ExceptionHandler(org.springframework.web.HttpRequestMethodNotSupportedException.class)
    public Object handleMethodNotSupported(org.springframework.web.HttpRequestMethodNotSupportedException ex,
                                           HttpServletRequest request,
                                           RedirectAttributes redirectAttributes) {
        log.warn("Method not supported: {} [URI={}]", ex.getMessage(), request.getRequestURI());
        if (isAjaxOrApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("success", false, "message", "Phương thức không được hỗ trợ."));
        }
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Phương thức không được hỗ trợ.");
            redirectAttributes.addFlashAttribute("errorMessage", "Phương thức không được hỗ trợ.");
            return "redirect:" + extractPath(referer);
        }
        redirectAttributes.addFlashAttribute("errorMsg", "Phương thức không được hỗ trợ.");
        redirectAttributes.addFlashAttribute("errorMessage", "Phương thức không được hỗ trợ.");
        return "redirect:/";
    }

    // ─── 403 Access Denied ──────────────────────────────────────────────
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public Object handleAccessDenied(org.springframework.security.access.AccessDeniedException ex,
                                     HttpServletRequest request,
                                     RedirectAttributes redirectAttributes) {
        log.warn("Access denied: {} [URI={}]", ex.getMessage(), request.getRequestURI());
        if (isAjaxOrApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("success", false, "message", "Bạn không có quyền thực hiện hành động này."));
        }
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Bạn không có quyền thực hiện hành động này.");
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện hành động này.");
            return "redirect:" + extractPath(referer);
        }
        redirectAttributes.addFlashAttribute("errorMsg", "Bạn không có quyền thực hiện hành động này.");
        redirectAttributes.addFlashAttribute("errorMessage", "Bạn không có quyền thực hiện hành động này.");
        return "redirect:/";
    }

    // ─── Validation Errors (400) ──────────────────────────────────────────
    @ExceptionHandler({
        org.springframework.web.bind.MethodArgumentNotValidException.class,
        org.springframework.web.bind.MissingServletRequestParameterException.class,
        org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class
    })
    public Object handleValidationExceptions(Exception ex,
                                             HttpServletRequest request,
                                             RedirectAttributes redirectAttributes) {
        log.warn("Validation/Parameter error: {} [URI={}]", ex.getMessage(), request.getRequestURI());
        
        String userMessage = "Dữ liệu cung cấp không hợp lệ hoặc thiếu tham số.";
        
        if (isAjaxOrApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("success", false, "message", userMessage));
        }
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMsg", userMessage);
            redirectAttributes.addFlashAttribute("errorMessage", userMessage);
            return "redirect:" + extractPath(referer);
        }
        redirectAttributes.addFlashAttribute("errorMsg", userMessage);
        redirectAttributes.addFlashAttribute("errorMessage", userMessage);
        return "redirect:/";
    }

    // ─── NoResourceFoundException (Spring Boot 3.2+) → 404 ───────────────

    @ExceptionHandler(NoResourceFoundException.class)
    public Object handleNoResourceFound(NoResourceFoundException ex,
                                        HttpServletRequest request,
                                        RedirectAttributes redirectAttributes) {
        log.warn("Resource not found: {} [URI={}]", ex.getMessage(), request.getRequestURI());

        // For static resource requests (e.g., /favicon.ico, styles, scripts, images), return 404 with no body
        if (isStaticResourceRequest(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        // For AJAX or API endpoints, return JSON 404
        if (isAjaxOrApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("success", false, "message", "Tài nguyên không tồn tại."));
        }

        // For page requests, redirect to home page with warning
        redirectAttributes.addFlashAttribute("errorMsg", "Trang hoặc tài nguyên không tồn tại.");
        return "redirect:/";
    }

    // ─── Generic Exception → 500 ────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public Object handleGenericException(Exception ex,
                                         HttpServletRequest request,
                                         RedirectAttributes redirectAttributes) {
        // Handle client connection abortions (Broken pipe, ClientAbortException, etc.)
        if (isClientAbortException(ex)) {
            log.debug("Client connection aborted for URI={}: {}", request.getRequestURI(), ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        log.error("Unhandled exception at URI={}: {}", request.getRequestURI(), ex.getMessage(), ex);

        // Never redirect or set flash message for static resource requests
        if (isStaticResourceRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        if (isAjaxOrApiRequest(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("success", false, "message", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau."));
        }

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            redirectAttributes.addFlashAttribute("errorMsg", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
            return "redirect:" + extractPath(referer);
        }
        redirectAttributes.addFlashAttribute("errorMsg", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
        redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi hệ thống. Vui lòng thử lại sau.");
        return "redirect:/";
    }

    // ─── Helpers ─────────────────────────────────────────────────────────

    /**
     * Determines if the request is an AJAX call or an API endpoint.
     */
    private boolean isAjaxOrApiRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String accept = request.getHeader("Accept");
        String xRequestedWith = request.getHeader("X-Requested-With");

        return (uri != null && uri.startsWith("/api/"))
                || "XMLHttpRequest".equals(xRequestedWith)
                || (accept != null && accept.contains("application/json"));
    }

    /**
     * Checks if the request is targeting static web resources (images, icons, styles, scripts, fonts).
     */
    private boolean isStaticResourceRequest(HttpServletRequest request) {
        String uri = request.getRequestURI();
        if (uri == null) {
            return false;
        }
        return uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")
                || uri.equals("/favicon.ico")
                || uri.matches(".*\\.(ico|png|jpg|jpeg|gif|svg|webp|css|js|map|woff|woff2|ttf|eot)$");
    }

    /**
     * Checks if the exception or any of its causes is a client connection termination
     * (e.g. ClientAbortException, Broken pipe, Connection reset by peer).
     */
    private boolean isClientAbortException(Throwable ex) {
        Throwable current = ex;
        while (current != null) {
            String name = current.getClass().getName();
            String msg = current.getMessage() != null ? current.getMessage().toLowerCase() : "";
            if (name.contains("ClientAbortException")
                    || msg.contains("broken pipe")
                    || msg.contains("connection reset by peer")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    /**
     * Extracts only the path portion from a full URL (strips scheme + host + port).
     * Falls back to "/" if parsing fails.
     */
    private String extractPath(String fullUrl) {
        try {
            java.net.URI uri = java.net.URI.create(fullUrl);
            String path = uri.getPath();
            return (path != null && !path.isBlank()) ? path : "/";
        } catch (Exception e) {
            return "/";
        }
    }
}
