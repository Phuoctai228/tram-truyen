package com.tramtruyen.controller;

import com.tramtruyen.dto.ReportCommentRequest;
import com.tramtruyen.dto.ReportContentRequest;
import com.tramtruyen.entity.User;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.security.CustomUserDetails;
import com.tramtruyen.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    @PostMapping("/comment")
    @ResponseBody
    public ResponseEntity<?> reportComment(@Valid @RequestBody ReportCommentRequest request,
                                           BindingResult bindingResult,
                                           Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Vui lòng đăng nhập để gửi báo cáo bình luận."));
        }

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", bindingResult.getAllErrors().get(0).getDefaultMessage()));
        }

        try {
            Integer userId = getUserId(authentication);
            reportService.reportComment(userId, request);
            return ResponseEntity.ok(Map.of("success", true, "message", "Báo cáo bình luận của bạn đã được gửi và đang chờ xử lý."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi xử lý báo cáo bình luận", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Đã có lỗi xảy ra khi gửi báo cáo."));
        }
    }

    @PostMapping("/content")
    @ResponseBody
    public ResponseEntity<?> reportContent(@Valid @RequestBody ReportContentRequest request,
                                           BindingResult bindingResult,
                                           Authentication authentication) {
        if (!isAuthenticated(authentication)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Vui lòng đăng nhập để gửi báo cáo nội dung."));
        }

        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", bindingResult.getAllErrors().get(0).getDefaultMessage()));
        }

        try {
            Integer userId = getUserId(authentication);
            reportService.reportContent(userId, request);
            return ResponseEntity.ok(Map.of("success", true, "message", "Báo cáo nội dung chương của bạn đã được gửi. Ban biên tập sẽ kiểm tra trong 24 giờ."));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Lỗi khi xử lý báo cáo nội dung", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Đã có lỗi xảy ra khi gửi báo cáo."));
        }
    }

    private boolean isAuthenticated(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken);
    }

    private Integer getUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.getUser().getId();
        }
        return userRepository.findByEmail(authentication.getName())
                .map(User::getId)
                .orElse(null);
    }
}
