package com.tramtruyen.controller;

import com.tramtruyen.dto.SystemSettingsDTO;
import com.tramtruyen.dto.ThemeBackgroundDTO;
import com.tramtruyen.dto.WebThemeViewDTO;
import com.tramtruyen.entity.User;
import com.tramtruyen.entity.WebSetting;
import com.tramtruyen.exception.ThemeAlreadyActiveException;
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
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
@Slf4j
public class SystemSettingAdminController {

    private final SystemSettingService systemSettingService;
    private final UserRepository userRepository;

    private static final java.time.format.DateTimeFormatter THEME_DATE_FORMATTER = java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy");

    @GetMapping
    public String showSettingsPage(Model model) {
        if (!model.containsAttribute("settings")) {
            SystemSettingsDTO currentSettings = systemSettingService.getSettings();
            model.addAttribute("settings", currentSettings);
        }

        List<WebSetting> rawThemes = systemSettingService.getAllThemes();
        Set<Integer> uploaderIds = rawThemes.stream()
                .map(WebSetting::getUploadedBy)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Integer, String> uploaderNameMap = new HashMap<>();
        if (!uploaderIds.isEmpty()) {
            userRepository.findAllById(uploaderIds).forEach(u -> uploaderNameMap.put(u.getId(), u.getFullName()));
        }

        List<WebThemeViewDTO> themeDTOs = rawThemes.stream().map(t -> {
            String uploaderName = t.getUploadedBy() != null ? uploaderNameMap.getOrDefault(t.getUploadedBy(), "—") : "—";
            String formattedTime = t.getUpdatedAt() != null ? t.getUpdatedAt().format(THEME_DATE_FORMATTER) : "—";
            String tokens = systemSettingService.getThemeTokens(t.getFolderPath());
            Map<String, String> tokenMap = systemSettingService.getThemeTokenMap(t.getFolderPath());
            ThemeBackgroundDTO bgDto = systemSettingService.getThemeBackground(t.getFolderPath());
            return WebThemeViewDTO.builder()
                    .id(t.getId())
                    .name(t.getName())
                    .folderPath(t.getFolderPath())
                    .isActive(Boolean.TRUE.equals(t.getIsActive()))
                    .uploadedBy(t.getUploadedBy())
                    .uploaderName(uploaderName)
                    .updatedAt(t.getUpdatedAt())
                    .formattedUpdatedAt(formattedTime)
                    .tokensJson(tokens)
                    .tokenMap(tokenMap)
                    .bgUrl(bgDto != null ? bgDto.getUrl() : null)
                    .bgOpacity(bgDto != null ? bgDto.getOpacity() : 12)
                    .bgBlur(bgDto != null ? bgDto.getBlur() : 4)
                    .build();
        }).toList();

        model.addAttribute("themes", themeDTOs);
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
    public String activateTheme(@PathVariable("id") Integer id,
                                Authentication authentication,
                                HttpServletRequest httpRequest,
                                RedirectAttributes redirectAttributes) {
        try {
            Integer adminId = getAdminId(authentication);
            String clientIp = getClientIp(httpRequest);
            WebSetting activated = systemSettingService.activateTheme(id, adminId, clientIp);
            redirectAttributes.addFlashAttribute("successMessage", "Kích hoạt chủ đề giao diện \"" + activated.getName() + "\" thành công!");
        } catch (ThemeAlreadyActiveException e) {
            redirectAttributes.addFlashAttribute("infoMessage", "Giao diện này đang được sử dụng.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi khi kích hoạt chủ đề giao diện id={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi kích hoạt giao diện. Vui lòng thử lại!");
        }
        return "redirect:/admin/settings";
    }

    @PostMapping("/themes")
    public String createTheme(@RequestParam("name") String name,
                              @RequestParam("folderPath") String folderPath,
                              @RequestParam(value = "tokensJson", required = false) String tokensJson,
                              @RequestParam(value = "backgroundImage", required = false) MultipartFile backgroundImage,
                              @RequestParam(value = "bgOpacity", required = false, defaultValue = "12") Integer bgOpacity,
                              @RequestParam(value = "bgBlur", required = false, defaultValue = "4") Integer bgBlur,
                              Authentication authentication,
                              HttpServletRequest httpRequest,
                              RedirectAttributes redirectAttributes) {
        try {
            Integer adminId = getAdminId(authentication);
            String clientIp = getClientIp(httpRequest);
            WebSetting created = systemSettingService.createTheme(name, folderPath, tokensJson,
                    backgroundImage, bgOpacity, bgBlur, adminId, clientIp);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm giao diện \"" + created.getName() + "\" thành công!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("themeFormError", true);
            redirectAttributes.addFlashAttribute("reopenModal", "add");
            redirectAttributes.addFlashAttribute("reopenBgNote", "Vui lòng chọn lại ảnh nền nếu có (trình duyệt không tự lưu lại file sau khi gửi biểu mẫu).");
            redirectAttributes.addFlashAttribute("failedName", name);
            redirectAttributes.addFlashAttribute("failedFolderPath", folderPath);
            redirectAttributes.addFlashAttribute("failedTokensJson", tokensJson);
            redirectAttributes.addFlashAttribute("failedBgOpacity", bgOpacity);
            redirectAttributes.addFlashAttribute("failedBgBlur", bgBlur);
        } catch (Exception e) {
            log.error("Lỗi khi tạo mới chủ đề giao diện", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi thêm giao diện mới: " + e.getMessage());
            redirectAttributes.addFlashAttribute("themeFormError", true);
            redirectAttributes.addFlashAttribute("reopenModal", "add");
            redirectAttributes.addFlashAttribute("reopenBgNote", "Vui lòng chọn lại ảnh nền nếu có (trình duyệt không tự lưu lại file sau khi gửi biểu mẫu).");
            redirectAttributes.addFlashAttribute("failedName", name);
            redirectAttributes.addFlashAttribute("failedFolderPath", folderPath);
            redirectAttributes.addFlashAttribute("failedTokensJson", tokensJson);
            redirectAttributes.addFlashAttribute("failedBgOpacity", bgOpacity);
            redirectAttributes.addFlashAttribute("failedBgBlur", bgBlur);
        }
        return "redirect:/admin/settings";
    }

    @PostMapping("/themes/{id}/edit")
    public String editTheme(@PathVariable("id") Integer id,
                            @RequestParam("name") String name,
                            @RequestParam(value = "folderPath", required = false) String folderPath,
                            @RequestParam(value = "tokensJson", required = false) String tokensJson,
                            @RequestParam(value = "backgroundImage", required = false) MultipartFile backgroundImage,
                            @RequestParam(value = "bgOpacity", required = false, defaultValue = "12") Integer bgOpacity,
                            @RequestParam(value = "bgBlur", required = false, defaultValue = "4") Integer bgBlur,
                            @RequestParam(value = "removeBg", required = false, defaultValue = "false") Boolean removeBg,
                            Authentication authentication,
                            HttpServletRequest httpRequest,
                            RedirectAttributes redirectAttributes) {
        try {
            Integer adminId = getAdminId(authentication);
            String clientIp = getClientIp(httpRequest);
            WebSetting updated = systemSettingService.updateTheme(id, name, folderPath, tokensJson,
                    backgroundImage, bgOpacity, bgBlur, removeBg, adminId, clientIp);
            if (Boolean.TRUE.equals(updated.getIsActive())) {
                redirectAttributes.addFlashAttribute("successMessage", "Cập nhật giao diện \"" + updated.getName() + "\" thành công!");
            } else {
                redirectAttributes.addFlashAttribute("infoMessage", "Đã lưu giao diện " + updated.getName() + ". Giao diện này chưa được áp dụng; bấm Kích hoạt để hiển thị.");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            redirectAttributes.addFlashAttribute("themeFormError", true);
            redirectAttributes.addFlashAttribute("reopenModal", "edit");
            redirectAttributes.addFlashAttribute("reopenBgNote", "Vui lòng chọn lại ảnh nền nếu có (trình duyệt không tự lưu lại file sau khi gửi biểu mẫu).");
            redirectAttributes.addFlashAttribute("failedThemeId", id);
            redirectAttributes.addFlashAttribute("failedName", name);
            redirectAttributes.addFlashAttribute("failedTokensJson", tokensJson);
            redirectAttributes.addFlashAttribute("failedBgOpacity", bgOpacity);
            redirectAttributes.addFlashAttribute("failedBgBlur", bgBlur);
        } catch (Exception e) {
            log.error("Lỗi khi cập nhật giao diện id={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi cập nhật giao diện: " + e.getMessage());
            redirectAttributes.addFlashAttribute("themeFormError", true);
            redirectAttributes.addFlashAttribute("reopenModal", "edit");
            redirectAttributes.addFlashAttribute("reopenBgNote", "Vui lòng chọn lại ảnh nền nếu có (trình duyệt không tự lưu lại file sau khi gửi biểu mẫu).");
            redirectAttributes.addFlashAttribute("failedThemeId", id);
            redirectAttributes.addFlashAttribute("failedName", name);
            redirectAttributes.addFlashAttribute("failedTokensJson", tokensJson);
            redirectAttributes.addFlashAttribute("failedBgOpacity", bgOpacity);
            redirectAttributes.addFlashAttribute("failedBgBlur", bgBlur);
        }
        return "redirect:/admin/settings";
    }

    @PostMapping("/themes/{id}/delete")
    public String deleteTheme(@PathVariable("id") Integer id,
                              Authentication authentication,
                              HttpServletRequest httpRequest,
                              RedirectAttributes redirectAttributes) {
        try {
            Integer adminId = getAdminId(authentication);
            String clientIp = getClientIp(httpRequest);
            systemSettingService.deleteTheme(id, adminId, clientIp);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa giao diện thành công!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            log.error("Lỗi khi xóa giao diện id={}", id, e);
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi khi xóa giao diện: " + e.getMessage());
        }
        return "redirect:/admin/settings";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex, RedirectAttributes redirectAttributes) {
        log.warn("Kích thước file tải lên vượt quá giới hạn cấu hình: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", "Dung lượng file vượt quá giới hạn cho phép (tối đa 3MB cho ảnh nền). Vui lòng chọn ảnh nhỏ hơn.");
        redirectAttributes.addFlashAttribute("themeFormError", true);
        return "redirect:/admin/settings";
    }

    @ExceptionHandler(MultipartException.class)
    public String handleMultipartException(MultipartException ex, RedirectAttributes redirectAttributes) {
        log.warn("Lỗi upload file multipart: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", "Lỗi tải file lên hệ thống: " + ex.getMessage());
        redirectAttributes.addFlashAttribute("themeFormError", true);
        return "redirect:/admin/settings";
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public String handleTypeMismatch(MethodArgumentTypeMismatchException ex, RedirectAttributes redirectAttributes) {
        log.warn("Tham số không đúng định dạng: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", "Mã chủ đề giao diện không hợp lệ.");
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
