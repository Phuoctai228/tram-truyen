package com.tramtruyen.controller;

import com.tramtruyen.dto.CoinPackageRequestDTO;
import com.tramtruyen.service.CoinPackageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller quản lý CRUD các gói nạp Coin dành cho Quản trị viên (Admin).
 */
@Controller
@RequestMapping("/admin/settings/packages")
@RequiredArgsConstructor
@Slf4j
public class CoinPackageAdminController {

    private final CoinPackageService coinPackageService;

    /**
     * Tạo mới một gói nạp Coin.
     */
    @PostMapping
    public String createPackage(@Valid @ModelAttribute("coinPackageForm") CoinPackageRequestDTO dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/settings";
        }

        try {
            coinPackageService.createPackage(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm mới gói nạp Coin thành công!");
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn("Không thể tạo gói nạp Coin: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi không xác định khi tạo gói nạp Coin", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể tạo gói nạp Coin: " + e.getMessage());
        }

        return "redirect:/admin/settings";
    }

    /**
     * Cập nhật thông tin gói nạp Coin.
     */
    @PostMapping("/{id}/edit")
    public String updatePackage(@PathVariable("id") Integer id,
                                @Valid @ModelAttribute("coinPackageForm") CoinPackageRequestDTO dto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/settings";
        }

        try {
            coinPackageService.updatePackage(id, dto);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật gói nạp Coin thành công!");
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn("Không thể cập nhật gói nạp Coin ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật gói nạp Coin ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể cập nhật gói nạp: " + e.getMessage());
        }

        return "redirect:/admin/settings";
    }

    /**
     * Bật / Tắt trạng thái Ẩn / Hiện (ACTIVE ↔ INACTIVE) của gói nạp.
     */
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable("id") Integer id,
                               RedirectAttributes redirectAttributes) {
        try {
            coinPackageService.togglePackageStatus(id);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái hiển thị gói nạp thành công!");
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.warn("Không thể chuyển đổi trạng thái gói nạp ID {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi khi đổi trạng thái gói nạp ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể thay đổi trạng thái: " + e.getMessage());
        }

        return "redirect:/admin/settings";
    }

    /**
     * Xóa một gói nạp Coin.
     */
    @PostMapping("/{id}/delete")
    public String deletePackage(@PathVariable("id") Integer id,
                                RedirectAttributes redirectAttributes) {
        try {
            coinPackageService.deletePackage(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xóa gói nạp Coin thành công!");
        } catch (Exception e) {
            log.error("Lỗi khi xóa gói nạp Coin ID: {}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Không thể xóa gói nạp: " + e.getMessage());
        }

        return "redirect:/admin/settings";
    }
}

