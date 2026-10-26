package com.tramtruyen.config;

import com.tramtruyen.entity.WebSetting;
import com.tramtruyen.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.LocalDate;

/**
 * ControllerAdvice tự động cung cấp chủ đề giao diện đang kích hoạt (activeTheme)
 * và thông số năm âm lịch Can Chi động (tái sử dụng chủ đề Tết qua các năm)
 * cho toàn bộ các trang giao diện (Admin & User) trên website Trạm Truyện.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class WebThemeAdvice {

    private final SystemSettingService systemSettingService;

    private static final String[] CAN = {"Canh", "Tân", "Nhâm", "Quý", "Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ"};
    private static final String[] CHI = {"Thân", "Dậu", "Tuất", "Hợi", "Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi"};

    @ModelAttribute("activeTheme")
    public WebSetting populateActiveTheme() {
        try {
            return systemSettingService.getActiveTheme();
        } catch (Exception e) {
            return null;
        }
    }

    @ModelAttribute("currentYear")
    public int populateCurrentYear() {
        return LocalDate.now().getYear();
    }

    @ModelAttribute("lunarYearName")
    public String populateLunarYearName() {
        int year = LocalDate.now().getYear();
        return computeCanChi(year);
    }

    @ModelAttribute("tetSpringTitle")
    public String populateTetSpringTitle() {
        int year = LocalDate.now().getYear();
        return "XUÂN " + computeCanChi(year).toUpperCase() + " " + year;
    }

    /**
     * Tự động tính toán Can Chi theo năm Dương/Âm lịch Việt Nam.
     * Ví dụ: 2024 -> Giáp Thìn, 2025 -> Ất Tỵ, 2026 -> Bính Ngọ, 2027 -> Đinh Mùi...
     * Giúp chủ đề Tết có thể tái sử dụng vĩnh viễn qua mọi năm mà không bị gán cứng.
     */
    public static String computeCanChi(int year) {
        String can = CAN[Math.floorMod(year, 10)];
        String chi = CHI[Math.floorMod(year, 12)];
        return can + " " + chi;
    }
}
