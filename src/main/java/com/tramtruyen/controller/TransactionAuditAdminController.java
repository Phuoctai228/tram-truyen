package com.tramtruyen.controller;

import com.tramtruyen.dto.PaymentLogDTO;
import com.tramtruyen.dto.TransactionAuditDTO;
import com.tramtruyen.dto.TransactionAuditStatsDTO;
import com.tramtruyen.service.TransactionAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/admin/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionAuditAdminController {

    private final TransactionAuditService transactionAuditService;

    @GetMapping
    public String showAuditPage(@RequestParam(required = false) String startDate,
                                @RequestParam(required = false) String endDate,
                                @RequestParam(required = false, defaultValue = "ALL") String type,
                                @RequestParam(required = false) String orderCode,
                                @RequestParam(required = false) String email,
                                @RequestParam(required = false) String page,
                                @RequestParam(required = false) String size,
                                Model model) {

        String effectiveStartDate = startDate != null ? startDate.trim() : "";
        String effectiveEndDate = endDate != null ? endDate.trim() : "";

        // Kiểm tra tính hợp lệ của khoảng ngày
        LocalDate sDate = parseDate(effectiveStartDate);
        LocalDate eDate = parseDate(effectiveEndDate);
        if (sDate != null && eDate != null && sDate.isAfter(eDate)) {
            model.addAttribute("errorMessage", "Khoảng ngày không hợp lệ: 'Từ ngày' không được lớn hơn 'Đến ngày'.");
            effectiveStartDate = "";
            effectiveEndDate = "";
        }

        int parsedPage = com.tramtruyen.util.PaginationUtils.parsePage(page);
        int parsedSize = com.tramtruyen.util.PaginationUtils.parseSize(size, 10);

        TransactionAuditStatsDTO stats = transactionAuditService.getAuditStats();
        Page<TransactionAuditDTO> txPage = transactionAuditService.getAuditTransactions(
                effectiveStartDate, effectiveEndDate, type, orderCode, email, parsedPage, parsedSize
        );

        if (parsedPage >= txPage.getTotalPages() && txPage.getTotalPages() > 0) {
            parsedPage = txPage.getTotalPages() - 1;
            txPage = transactionAuditService.getAuditTransactions(
                    effectiveStartDate, effectiveEndDate, type, orderCode, email, parsedPage, parsedSize
            );
        }

        String filterQuery = com.tramtruyen.util.PaginationUtils.buildFilterQuery(java.util.Map.of(
                "startDate", effectiveStartDate,
                "endDate", effectiveEndDate,
                "type", type != null && !type.isBlank() ? type : "ALL",
                "orderCode", orderCode != null ? orderCode.trim() : "",
                "email", email != null ? email.trim() : ""
        ));

        model.addAttribute("stats", stats);
        model.addAttribute("transactions", txPage);
        model.addAttribute("startDate", effectiveStartDate);
        model.addAttribute("endDate", effectiveEndDate);
        model.addAttribute("type", type != null && !type.isBlank() ? type : "ALL");
        model.addAttribute("orderCode", orderCode != null ? orderCode.trim() : "");
        model.addAttribute("email", email != null ? email.trim() : "");
        model.addAttribute("filterQuery", filterQuery);
        model.addAttribute("currentPage", txPage.getNumber());
        model.addAttribute("totalPages", txPage.getTotalPages());
        model.addAttribute("totalElements", txPage.getTotalElements());
        model.addAttribute("pageSize", parsedSize);
        model.addAttribute("pageTitle", "Đối soát giao dịch tài chính | Trạm Truyện Admin");

        return "admin/transactions/index";
    }

    @GetMapping("/ipn-logs")
    @ResponseBody
    public ResponseEntity<List<PaymentLogDTO>> getIpnLogs(@RequestParam String orderCode) {
        List<PaymentLogDTO> logs = transactionAuditService.getIpnLogsByOrderCode(orderCode);
        return ResponseEntity.ok(logs);
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
