package com.tramtruyen.controller;

import com.tramtruyen.dto.SystemAuditLogDTO;
import com.tramtruyen.dto.SystemAuditLogDetailDTO;
import com.tramtruyen.dto.SystemAuditLogStatsDTO;
import com.tramtruyen.service.SystemAuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String showAuditLogPage(@RequestParam(required = false) String startDate,
                                   @RequestParam(required = false) String endDate,
                                   @RequestParam(required = false) String actionType,
                                   @RequestParam(required = false) String performer,
                                   @RequestParam(required = false) String targetEntity,
                                   @RequestParam(required = false) Integer targetId,
                                   @RequestParam(required = false, defaultValue = "0") int page,
                                   @RequestParam(required = false, defaultValue = "20") int size,
                                   Model model) {

        String effectiveStartDate = startDate != null ? startDate.trim() : "";
        String effectiveEndDate = endDate != null ? endDate.trim() : "";

        LocalDate sDate = parseDate(effectiveStartDate);
        LocalDate eDate = parseDate(effectiveEndDate);
        if (sDate != null && eDate != null && sDate.isAfter(eDate)) {
            model.addAttribute("errorMessage", "Khoảng ngày không hợp lệ: 'Từ ngày' không được lớn hơn 'Đến ngày'.");
            effectiveStartDate = "";
            effectiveEndDate = "";
        }

        SystemAuditLogStatsDTO stats = systemAuditLogService.getStats();
        Page<SystemAuditLogDTO> logPage = systemAuditLogService.getAuditLogs(
                effectiveStartDate, effectiveEndDate, actionType, performer, targetEntity, targetId, page, size
        );

        List<String> actionTypes = systemAuditLogService.getAvailableActionTypes();
        List<String> targetEntities = systemAuditLogService.getAvailableTargetEntities();

        model.addAttribute("stats", stats);
        model.addAttribute("logs", logPage);
        model.addAttribute("availableActionTypes", actionTypes);
        model.addAttribute("availableTargetEntities", targetEntities);
        model.addAttribute("startDate", effectiveStartDate);
        model.addAttribute("endDate", effectiveEndDate);
        model.addAttribute("actionType", actionType != null ? actionType.trim() : "");
        model.addAttribute("performer", performer != null ? performer.trim() : "");
        model.addAttribute("targetEntity", targetEntity != null ? targetEntity.trim() : "");
        model.addAttribute("targetId", targetId);
        model.addAttribute("currentPage", logPage.getNumber());
        model.addAttribute("totalPages", logPage.getTotalPages());
        model.addAttribute("totalElements", logPage.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("pageTitle", "Nhật ký hệ thống | Trạm Truyện Admin");

        return "admin/audit-logs/index";
    }

    @GetMapping("/{id}/detail")
    @ResponseBody
    public ResponseEntity<SystemAuditLogDetailDTO> getLogDetail(@PathVariable Integer id) {
        try {
            SystemAuditLogDetailDTO detail = systemAuditLogService.getAuditLogDetail(id);
            return ResponseEntity.ok(detail);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
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
