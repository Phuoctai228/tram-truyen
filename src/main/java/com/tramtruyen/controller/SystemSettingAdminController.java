package com.tramtruyen.controller;

import com.tramtruyen.dto.SystemSettingsDTO;
import com.tramtruyen.entity.User;
import com.tramtruyen.entity.WebSetting;
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
import org.springframework.web.bind.annotation.*;
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
        model.addAttribute("themes", systemSettingService.getAllThemes());
        model.addAttribute("activeTheme", systemSettingService.getActiveTheme());
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

    @PostMapping("/themes/{id}/activate")
    public String activateTheme(@PathVariable("id") Integer themeId,
                                Authentication authentication,
                                HttpServletRequest httpRequest,
                                RedirectAttributes redirectAttributes) {
        try {
            Integer adminId = getAdminId(authentication);
            String clientIp = getClientIp(httpRequest);
            WebSetting activated = systemSettingService.activateTheme(themeId, adminId, clientIp);
            redirectAttributes.addFlashAttribute("successMessage", "Kích hoạt chủ đề giao diện \"" + activated.getName() + "\" thành công!");
        } catch (Exception e) {
            log.error("Lỗi khi kích hoạt chủ đề giao diện ID: {}", themeId, e);
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/settings#themes-section";
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
