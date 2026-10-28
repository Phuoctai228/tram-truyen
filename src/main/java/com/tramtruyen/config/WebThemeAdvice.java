package com.tramtruyen.config;

import com.tramtruyen.entity.WebSetting;
import com.tramtruyen.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class WebThemeAdvice {

    private final SystemSettingService systemSettingService;

    @ModelAttribute("activeTheme")
    public WebSetting populateActiveTheme() {
        try {
            return systemSettingService.getActiveTheme();
        } catch (Exception e) {
            log.warn("Lỗi nạp activeTheme trong WebThemeAdvice: {}", e.getMessage());
            return null;
        }
    }

    @ModelAttribute("activeThemeCss")
    public String populateActiveThemeCss() {
        try {
            return systemSettingService.getActiveThemeCss();
        } catch (Exception e) {
            log.warn("Lỗi nạp activeThemeCss trong WebThemeAdvice: {}", e.getMessage());
            return null;
        }
    }
}
