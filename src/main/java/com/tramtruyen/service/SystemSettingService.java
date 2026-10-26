package com.tramtruyen.service;

import com.tramtruyen.dto.SystemSettingsDTO;
import com.tramtruyen.entity.WebSetting;

import java.util.List;

public interface SystemSettingService {

    SystemSettingsDTO getSettings();

    boolean updateSettings(Integer adminId, SystemSettingsDTO dto, String clientIp);

    int getVndToCoinRate();

    String getVndPerCoinFormatted();

    List<WebSetting> getAllThemes();

    WebSetting getActiveTheme();

    WebSetting activateTheme(Integer themeId, Integer adminId, String clientIp);
}
