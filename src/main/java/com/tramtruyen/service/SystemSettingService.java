package com.tramtruyen.service;

import com.tramtruyen.dto.SystemSettingsDTO;
import com.tramtruyen.dto.ThemeBackgroundDTO;
import com.tramtruyen.entity.WebSetting;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface SystemSettingService {

    SystemSettingsDTO getSettings();

    boolean updateSettings(Integer adminId, SystemSettingsDTO dto, String clientIp);

    int getVndToCoinRate();

    String getVndPerCoinFormatted();

    List<WebSetting> getAllThemes();

    WebSetting getActiveTheme();

    String getActiveThemeCss();

    String getThemeTokens(String folderPath);

    java.util.Map<String, String> getThemeTokenMap(String folderPath);

    ThemeBackgroundDTO getThemeBackground(String folderPath);

    WebSetting activateTheme(Integer themeId, Integer adminId, String clientIp);

    WebSetting createTheme(String name, String folderPath, String thumbnailUrl, String tokensJson, Integer adminId, String clientIp);

    WebSetting createTheme(String name, String folderPath, String thumbnailUrl, String tokensJson,
                           MultipartFile backgroundImage, Integer bgOpacity, Integer bgBlur,
                           Integer adminId, String clientIp);

    WebSetting updateTheme(Integer id, String name, String thumbnailUrl, String tokensJson, Integer adminId, String clientIp);

    WebSetting updateTheme(Integer id, String name, String thumbnailUrl, String tokensJson,
                           MultipartFile backgroundImage, Integer bgOpacity, Integer bgBlur, Boolean removeBg,
                           Integer adminId, String clientIp);

    WebSetting updateTheme(Integer id, String name, String folderPath, String thumbnailUrl, String tokensJson,
                           MultipartFile backgroundImage, Integer bgOpacity, Integer bgBlur, Boolean removeBg,
                           Integer adminId, String clientIp);

    void deleteTheme(Integer id, Integer adminId, String clientIp);
}

