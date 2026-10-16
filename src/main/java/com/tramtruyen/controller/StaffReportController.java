package com.tramtruyen.controller;

import com.tramtruyen.dto.ResolveReportRequest;
import com.tramtruyen.entity.CommentReport;
import com.tramtruyen.entity.ContentReport;
import com.tramtruyen.entity.User;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.security.CustomUserDetails;
import com.tramtruyen.service.ReportService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

/**
 * Handles Staff moderation and report resolution views and operations.
 */
@Controller
@RequestMapping("/staff/reports")
@RequiredArgsConstructor
@Slf4j
public class StaffReportController {

    private final ReportService reportService;
    private final UserRepository userRepository;

    @GetMapping
    public String listReports(@RequestParam(value = "tab", defaultValue = "comment") String tab,
                              @RequestParam(value = "status", defaultValue = "RECEIVED") String status,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size,
                              Model model) {

        String safeTab = "content".equalsIgnoreCase(tab) ? "content" : "comment";
        String safeStatus = (status == null || status.isBlank()) ? "RECEIVED" : status.toUpperCase();

        Page<CommentReport> commentReports = null;
        Page<ContentReport> contentReports = null;

        if ("comment".equals(safeTab)) {
            commentReports = reportService.getCommentReports(safeStatus, page, size);
        } else {
            contentReports = reportService.getContentReports(safeStatus, page, size);
        }

        Map<String, Long> counts = reportService.getReportCounts();

        model.addAttribute("activeTab", safeTab);
        model.addAttribute("currentStatus", safeStatus);
        model.addAttribute("commentReports", commentReports);
        model.addAttribute("contentReports", contentReports);
        model.addAttribute("counts", counts);
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("pageTitle", "Quản lý & Xử lý báo cáo vi phạm | Trạm Truyện Staff");

        return "staff/reports/list";
    }

    @PostMapping("/comment/{id}/resolve")
    public String resolveCommentReport(@PathVariable("id") Integer id,
                                       @Valid @ModelAttribute ResolveReportRequest request,
                                       BindingResult bindingResult,
                                       @RequestParam(value = "status", defaultValue = "RECEIVED") String currentStatus,
                                       @RequestParam(value = "page", defaultValue = "0") int page,
                                       Authentication authentication,
                                       HttpServletRequest httpRequest,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/staff/reports?tab=comment&status=" + currentStatus + "&page=" + page;
        }

        try {
            Integer staffId = getStaffId(authentication);
            String clientIp = getClientIp(httpRequest);
            reportService.resolveCommentReport(staffId, id, request, clientIp);
            redirectAttributes.addFlashAttribute("successMessage",
                    "REJECTED".equalsIgnoreCase(request.getDecision()) ? "Đã từ chối báo cáo bình luận." : "Đã xử lý vi phạm bình luận thành công.");
        } catch (Exception e) {
            log.error("Lỗi khi xử lý báo cáo bình luận #" + id, e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/staff/reports?tab=comment&status=" + currentStatus + "&page=" + page;
    }

    @PostMapping("/content/{id}/resolve")
    public String resolveContentReport(@PathVariable("id") Integer id,
                                       @Valid @ModelAttribute ResolveReportRequest request,
                                       BindingResult bindingResult,
                                       @RequestParam(value = "status", defaultValue = "RECEIVED") String currentStatus,
                                       @RequestParam(value = "page", defaultValue = "0") int page,
                                       Authentication authentication,
                                       HttpServletRequest httpRequest,
                                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/staff/reports?tab=content&status=" + currentStatus + "&page=" + page;
        }

        try {
            Integer staffId = getStaffId(authentication);
            String clientIp = getClientIp(httpRequest);
            reportService.resolveContentReport(staffId, id, request, clientIp);
            redirectAttributes.addFlashAttribute("successMessage",
                    "REJECTED".equalsIgnoreCase(request.getDecision()) ? "Đã từ chối báo cáo nội dung." : "Đã xử lý vi phạm nội dung chương thành công.");
        } catch (Exception e) {
            log.error("Lỗi khi xử lý báo cáo nội dung #" + id, e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/staff/reports?tab=content&status=" + currentStatus + "&page=" + page;
    }

    @GetMapping("/comment/{id}/resolve")
    public String getResolveCommentReportRedirect(@RequestParam(value = "status", defaultValue = "RECEIVED") String currentStatus,
                                                  @RequestParam(value = "page", defaultValue = "0") int page) {
        return "redirect:/staff/reports?tab=comment&status=" + currentStatus + "&page=" + page;
    }

    @GetMapping("/content/{id}/resolve")
    public String getResolveContentReportRedirect(@RequestParam(value = "status", defaultValue = "RECEIVED") String currentStatus,
                                                  @RequestParam(value = "page", defaultValue = "0") int page) {
        return "redirect:/staff/reports?tab=content&status=" + currentStatus + "&page=" + page;
    }

    private Integer getStaffId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails cud) {
            return cud.getUser().getId();
        }
        if (authentication != null) {
            return userRepository.findByEmail(authentication.getName())
                    .map(User::getId)
                    .orElse(1);
        }
        return 1;
    }

    private String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
