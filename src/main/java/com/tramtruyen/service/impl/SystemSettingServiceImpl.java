package com.tramtruyen.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tramtruyen.dto.SystemSettingsDTO;
import com.tramtruyen.entity.SystemAuditLog;
import com.tramtruyen.entity.SystemSetting;
import com.tramtruyen.entity.User;
import com.tramtruyen.repository.SystemAuditLogRepository;
import com.tramtruyen.repository.SystemSettingRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemSettingServiceImpl implements SystemSettingService {

    public static final String KEY_VND_TO_COIN_RATE = "vnd_to_coin_rate";
    public static final String KEY_CONTACT_PHONE = "contact_phone";
    public static final String KEY_SUPPORT_EMAIL = "support_email";
    public static final String KEY_TERMS_OF_SERVICE = "terms_of_service";
    public static final String KEY_PRIVACY_POLICY = "privacy_policy";

    private final SystemSettingRepository systemSettingRepository;
    private final UserRepository userRepository;
    private final SystemAuditLogRepository systemAuditLogRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional(readOnly = true)
    public SystemSettingsDTO getSettings() {
        Map<String, String> map = new HashMap<>();
        List<SystemSetting> all = systemSettingRepository.findAll();
        LocalDateTime latestUpdate = null;
        Integer lastUpdatedBy = null;

        for (SystemSetting s : all) {
            map.put(s.getSettingKey(), s.getSettingValue());
            if (s.getUpdatedAt() != null) {
                if (latestUpdate == null || s.getUpdatedAt().isAfter(latestUpdate)) {
                    latestUpdate = s.getUpdatedAt();
                    lastUpdatedBy = s.getUpdatedBy();
                }
            }
        }

        String adminName = "Ban Quản trị (SuperAdmin)";
        if (lastUpdatedBy != null) {
            Optional<User> uOpt = userRepository.findById(lastUpdatedBy);
            if (uOpt.isPresent()) {
                adminName = uOpt.get().getFullName();
            }
        }

        String formattedTime = "Chưa cập nhật";
        if (latestUpdate != null) {
            formattedTime = latestUpdate.format(DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy"));
        }

        int vndRate = getVndToCoinRate();

        return SystemSettingsDTO.builder()
                .vndToCoinRate(vndRate)
                .contactPhone(map.getOrDefault(KEY_CONTACT_PHONE, "1900-1234"))
                .supportEmail(map.getOrDefault(KEY_SUPPORT_EMAIL, "support@tramtruyen.com"))
                .termsOfService(map.getOrDefault(KEY_TERMS_OF_SERVICE,
                        "Chào mừng bạn đến với Trạm Truyện. Bằng việc truy cập hoặc sử dụng trang web, bạn đồng ý tuân thủ các quy định về bản quyền tác phẩm văn học, ứng xử văn minh trong bình luận, không sao chép hoặc phân phối lại nội dung chương VIP khi chưa có sự đồng ý bằng văn bản của Ban Quản Trị."))
                .privacyPolicy(map.getOrDefault(KEY_PRIVACY_POLICY,
                        "Trạm Truyện cam kết bảo mật tuyệt đối thông tin cá nhân của người dùng bao gồm địa chỉ email, lịch sử đọc truyện và thông tin giao dịch nạp Coin. Chúng tôi không chia sẻ hoặc cung cấp thông tin người dùng cho bất kỳ bên thứ ba nào ngoại trừ các trường hợp theo yêu cầu của cơ quan pháp luật có thẩm quyền."))
                .lastUpdatedFormatted(formattedTime)
                .lastUpdatedByAdmin(adminName)
                .build();
    }

    @Override
    @Transactional
    public boolean updateSettings(Integer adminId, SystemSettingsDTO dto, String clientIp) {
        if (dto == null) {
            throw new IllegalArgumentException("Dữ liệu cấu hình không hợp lệ");
        }

        SystemSettingsDTO current = getSettings();

        Map<String, String> newValues = new HashMap<>();
        newValues.put(KEY_VND_TO_COIN_RATE, String.valueOf(dto.getVndToCoinRate()));
        newValues.put(KEY_CONTACT_PHONE, dto.getContactPhone().trim());
        newValues.put(KEY_SUPPORT_EMAIL, dto.getSupportEmail().trim());
        newValues.put(KEY_TERMS_OF_SERVICE, dto.getTermsOfService().trim());
        newValues.put(KEY_PRIVACY_POLICY, dto.getPrivacyPolicy().trim());

        Map<String, String> oldValues = new HashMap<>();
        oldValues.put(KEY_VND_TO_COIN_RATE, String.valueOf(current.getVndToCoinRate()));
        oldValues.put(KEY_CONTACT_PHONE, current.getContactPhone());
        oldValues.put(KEY_SUPPORT_EMAIL, current.getSupportEmail());
        oldValues.put(KEY_TERMS_OF_SERVICE, current.getTermsOfService());
        oldValues.put(KEY_PRIVACY_POLICY, current.getPrivacyPolicy());

        Map<String, String> changedKeysOld = new HashMap<>();
        Map<String, String> changedKeysNew = new HashMap<>();

        for (Map.Entry<String, String> entry : newValues.entrySet()) {
            String key = entry.getKey();
            String newVal = entry.getValue();
            String oldVal = oldValues.get(key);
            if (!Objects.equals(newVal, oldVal)) {
                changedKeysOld.put(key, oldVal);
                changedKeysNew.put(key, newVal);
            }
        }

        if (changedKeysNew.isEmpty()) {
            return false; // No change
        }

        LocalDateTime now = LocalDateTime.now();
        for (Map.Entry<String, String> entry : changedKeysNew.entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();

            SystemSetting setting = systemSettingRepository.findBySettingKey(key)
                    .orElse(SystemSetting.builder().settingKey(key).build());

            setting.setSettingValue(val);
            setting.setUpdatedBy(adminId);
            setting.setUpdatedAt(now);
            systemSettingRepository.save(setting);
        }

        // Audit Log
        try {
            systemAuditLogRepository.save(SystemAuditLog.builder()
                    .adminId(adminId != null ? adminId : 1)
                    .actionType("UPDATE_SYSTEM_SETTINGS")
                    .targetEntity("SYSTEM_SETTINGS")
                    .targetId(null)
                    .oldValue(objectMapper.writeValueAsString(changedKeysOld))
                    .newValue(objectMapper.writeValueAsString(changedKeysNew))
                    .ipAddress(clientIp)
                    .build());
        } catch (Exception e) {
            log.error("Lỗi khi ghi system audit log cấu hình hệ thống", e);
        }

        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public int getVndToCoinRate() {
        try {
            Optional<SystemSetting> settingOpt = systemSettingRepository.findById(KEY_VND_TO_COIN_RATE);
            if (settingOpt.isPresent()) {
                String val = settingOpt.get().getSettingValue();
                if (val != null && !val.trim().isEmpty()) {
                    int parsed = Integer.parseInt(val.trim());
                    if (parsed >= 1 && parsed <= 1000) {
                        return parsed;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Không thể đọc tỷ giá vnd_to_coin_rate từ DB, dùng fallback 10: {}", e.getMessage());
        }
        return 10;
    }

    @Override
    @Transactional(readOnly = true)
    public String getVndPerCoinFormatted() {
        int rate = getVndToCoinRate();
        try {
            BigDecimal thousand = new BigDecimal("1000");
            BigDecimal rateBd = new BigDecimal(rate);
            BigDecimal vndPerCoin = thousand.divide(rateBd, 2, RoundingMode.HALF_UP).stripTrailingZeros();

            DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("vi", "VN"));
            symbols.setGroupingSeparator('.');
            symbols.setDecimalSeparator(',');
            DecimalFormat df = new DecimalFormat("#,##0.##", symbols);
            return df.format(vndPerCoin) + "đ / 1 Coin";
        } catch (Exception e) {
            log.warn("Lỗi tính chuỗi vndPerCoinFormatted: {}", e.getMessage());
            return "100đ / 1 Coin";
        }
    }
}
