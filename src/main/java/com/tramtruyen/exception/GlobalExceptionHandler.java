package com.tramtruyen.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    // ─── Generic Exception → 500 ────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public Object handleGenericException(Exception ex,
                                         HttpServletRequest request,
                                         RedirectAttributes redirectAttributes) {
        log.error("Unhandled exception at URI={}: {}", request.getRequestURI(), ex.getMessage(), ex);

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

        return uri.startsWith("/api/")
                || "XMLHttpRequest".equals(xRequestedWith)
                || (accept != null && accept.contains("application/json"));
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
