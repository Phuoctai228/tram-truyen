package com.tramtruyen.controller;

import com.tramtruyen.dto.SystemSettingsDTO;
import com.tramtruyen.entity.User;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.security.CustomUserDetails;
import com.tramtruyen.service.SystemSettingService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
@Slf4j
public class SystemSettingAdminController {

    private final SystemSettingService systemSettingService;
    private final UserRepository userRepository;

    @GetMapping
    public String showSettingsPage(Model model) {
        if (!model.containsAttribute("settings")) {
            SystemSettingsDTO currentSettings = systemSettingService.getSettings();
            model.addAttribute("settings", currentSettings);
        }
        model.addAttribute("pageTitle", "Cấu hình hệ thống & chính sách | Trạm Truyện Admin");
        return "admin/settings/config";
    }

    @PostMapping
    public String saveSettings(@Valid @ModelAttribute("settings") SystemSettingsDTO settingsDTO,
                               BindingResult bindingResult,
                               Authentication authentication,
                               HttpServletRequest httpRequest,
                               RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.settings", bindingResult);
            redirectAttributes.addFlashAttribute("settings", settingsDTO);
            redirectAttributes.addFlashAttribute("errorMessage", bindingResult.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/admin/settings";
        }

        try {
            Integer adminId = getAdminId(authentication);
            String clientIp = getClientIp(httpRequest);
            boolean changed = systemSettingService.updateSettings(adminId, settingsDTO, clientIp);

            if (!changed) {
                redirectAttributes.addFlashAttribute("infoMessage", "Không có thay đổi nào so với cấu hình hiện tại.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật cấu hình hệ thống & chính sách thành công!");
            }
        } catch (Exception e) {
            log.error("Lỗi khi lưu cấu hình hệ thống", e);
            redirectAttributes.addFlashAttribute("settings", settingsDTO);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/settings";
    }

    private Integer getAdminId(Authentication authentication) {
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
