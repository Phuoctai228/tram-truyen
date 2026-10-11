package com.tramtruyen.service;

import com.tramtruyen.dto.SystemSettingsDTO;

public interface SystemSettingService {

    SystemSettingsDTO getSettings();

    boolean updateSettings(Integer adminId, SystemSettingsDTO dto, String clientIp);

    int getVndToCoinRate();

    String getVndPerCoinFormatted();
}
