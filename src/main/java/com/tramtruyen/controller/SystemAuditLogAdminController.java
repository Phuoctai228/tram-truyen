package com.tramtruyen.controller;

import com.tramtruyen.dto.SystemAuditLogDTO;
import com.tramtruyen.dto.SystemAuditLogDetailDTO;
import com.tramtruyen.dto.SystemAuditLogStatsDTO;
import com.tramtruyen.service.SystemAuditLogService;
import com.tramtruyen.util.DateTimeUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin/audit-logs")
@RequiredArgsConstructor
@Slf4j
public class SystemAuditLogAdminController {

    private final SystemAuditLogService systemAuditLogService;

    @GetMapping
    public String showAuditLogPage(@RequestParam(required = false) String keyword,
                                   @RequestParam(required = false) String actionGroup,
                                   @RequestParam(required = false) String performerRole,
                                   @RequestParam(required = false, defaultValue = "all") String timeRange,
                                   @RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate,
                                   // Tham số cũ: bỏ qua an toàn, không lỗi 500
                                   @RequestParam(required = false) String severity,
                                   @RequestParam(required = false) Boolean onlyAdmin,
                                   @RequestParam(required = false) String actionType,
                                   @RequestParam(required = false) String performer,
                                   @RequestParam(required = false) String targetEntity,
                                   @RequestParam(required = false) Integer targetId,
                                   @RequestParam(required = false) String page,
                                   @RequestParam(required = false) String size,
                                   Model model) {

        String effectiveKeyword = keyword != null ? keyword.trim() : (performer != null ? performer.trim() : "");
        String effectiveGroup = actionGroup != null ? actionGroup.trim() : "";
        
        // Vai trò người thực hiện (tương thích backward nếu gửi onlyAdmin=true)
        String effectiveRole = performerRole != null ? performerRole.trim() : "";
        if (effectiveRole.isEmpty() && Boolean.TRUE.equals(onlyAdmin)) {
            effectiveRole = "ROLE_ADMIN";
        }

        String effectiveTimeRange = timeRange != null && !timeRange.trim().isEmpty() ? timeRange.trim() : "all";
        String effectiveStartDate = startDate != null ? startDate.trim() : "";
        String effectiveEndDate = endDate != null ? endDate.trim() : "";

        // Kiểm tra khoảng ngày ngược
        LocalDate sDate = parseDate(effectiveStartDate);
        LocalDate eDate = parseDate(effectiveEndDate);
        if (sDate != null && eDate != null && sDate.isAfter(eDate)) {
            model.addAttribute("errorMessage", "Khoảng ngày không hợp lệ: 'Từ ngày' không được lớn hơn 'Đến ngày'.");
            effectiveStartDate = "";
            effectiveEndDate = "";
            effectiveTimeRange = "all";
        }

        int parsedPage = com.tramtruyen.util.PaginationUtils.parsePage(page);
        int parsedSize = com.tramtruyen.util.PaginationUtils.parseSize(size, 10);

        Page<SystemAuditLogDTO> logPage = systemAuditLogService.getAuditLogsFiltered(
                effectiveKeyword, effectiveGroup, effectiveRole, effectiveTimeRange,
                effectiveStartDate, effectiveEndDate, parsedPage, parsedSize
        );

        // Clamp nếu page vượt quá tổng số trang
        if (parsedPage >= logPage.getTotalPages() && logPage.getTotalPages() > 0) {
            parsedPage = logPage.getTotalPages() - 1;
            logPage = systemAuditLogService.getAuditLogsFiltered(
                    effectiveKeyword, effectiveGroup, effectiveRole, effectiveTimeRange,
                    effectiveStartDate, effectiveEndDate, parsedPage, parsedSize
            );
        }

        SystemAuditLogStatsDTO stats = systemAuditLogService.getStats();
        List<SystemAuditLogService.ActionGroupItem> actionGroups = systemAuditLogService.getActiveActionGroups();

        String filterQuery = com.tramtruyen.util.PaginationUtils.buildFilterQuery(java.util.Map.of(
                "keyword", effectiveKeyword,
                "actionGroup", effectiveGroup,
                "performerRole", effectiveRole,
                "timeRange", effectiveTimeRange,
                "startDate", effectiveStartDate,
                "endDate", effectiveEndDate
        ));

        model.addAttribute("stats", stats);
        model.addAttribute("logs", logPage);
        model.addAttribute("actionGroups", actionGroups);
        model.addAttribute("hanoiToday", DateTimeUtils.getHanoiTodayString());

        model.addAttribute("keyword", effectiveKeyword);
        model.addAttribute("actionGroup", effectiveGroup);
        model.addAttribute("performerRole", effectiveRole);
        model.addAttribute("timeRange", effectiveTimeRange);
        model.addAttribute("startDate", effectiveStartDate);
        model.addAttribute("endDate", effectiveEndDate);
        model.addAttribute("filterQuery", filterQuery);

        model.addAttribute("currentPage", logPage.getNumber());
        model.addAttribute("totalPages", logPage.getTotalPages());
        model.addAttribute("totalElements", logPage.getTotalElements());
        model.addAttribute("pageSize", parsedSize);
        model.addAttribute("pageTitle", "Nhật ký hệ thống | Trạm Truyện Admin");

        return "admin/audit-logs/index";
    }

    /**
     * Trang chi tiết bản ghi nhật ký (dựng hoàn toàn ở server, NO JavaScript, NO modal).
     */
    @GetMapping("/{id}")
    public String showAuditLogDetail(@PathVariable String id,
                                     @RequestParam(required = false, defaultValue = "0") String page,
                                     @RequestParam(required = false, defaultValue = "10") String size,
                                     @RequestParam(required = false, defaultValue = "") String keyword,
                                     @RequestParam(required = false, defaultValue = "") String actionGroup,
                                     @RequestParam(required = false, defaultValue = "") String performerRole,
                                     @RequestParam(required = false, defaultValue = "all") String timeRange,
                                     @RequestParam(required = false, defaultValue = "") String startDate,
                                     @RequestParam(required = false, defaultValue = "") String endDate,
                                     RedirectAttributes redirectAttributes,
                                     Model model) {
        Integer parsedId;
        try {
            parsedId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy bản ghi nhật ký.");
            return "redirect:/admin/audit-logs";
        }

        try {
            SystemAuditLogDetailDTO detail = systemAuditLogService.getAuditLogDetail(parsedId);

            // Tham số giữ lại bộ lọc cho nút "Quay lại danh sách"
            model.addAttribute("detail", detail);
            model.addAttribute("returnPage", page);
            model.addAttribute("returnSize", size);
            model.addAttribute("returnKeyword", keyword);
            model.addAttribute("returnActionGroup", actionGroup);
            model.addAttribute("returnPerformerRole", performerRole);
            model.addAttribute("returnTimeRange", timeRange);
            model.addAttribute("returnStartDate", startDate);
            model.addAttribute("returnEndDate", endDate);
            model.addAttribute("pageTitle", "Chi tiết nhật ký #LOG-" + id + " | Trạm Truyện Admin");

            return "admin/audit-logs/detail";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy bản ghi nhật ký.");
            return "redirect:/admin/audit-logs";
        } catch (Exception e) {
            log.error("Lỗi khi tải chi tiết nhật ký ID " + id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy bản ghi nhật ký.");
            return "redirect:/admin/audit-logs";
        }
    }

    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy")
        );
        for (DateTimeFormatter dtf : formatters) {
            try {
                return LocalDate.parse(dateStr.trim(), dtf);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
