package com.tramtruyen.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tramtruyen.config.CloudinaryProperties;
import com.tramtruyen.dto.SystemSettingsDTO;
import com.tramtruyen.dto.ThemeBackgroundDTO;
import com.tramtruyen.entity.SystemAuditLog;
import com.tramtruyen.entity.SystemSetting;
import com.tramtruyen.entity.User;
import com.tramtruyen.entity.WebSetting;
import com.tramtruyen.repository.SystemAuditLogRepository;
import com.tramtruyen.repository.SystemSettingRepository;
import com.tramtruyen.repository.UserRepository;
import com.tramtruyen.repository.WebSettingRepository;
import com.tramtruyen.service.MediaStorageService;
import com.tramtruyen.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

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
    private final WebSettingRepository webSettingRepository;
    private final ObjectMapper objectMapper;
    private final MediaStorageService mediaStorageService;
    private final CloudinaryProperties cloudinaryProperties;

    private static final Pattern THEME_FOLDER_PATTERN = Pattern.compile("^theme-[a-z0-9-]{1,55}$");
    private final AtomicReference<WebSetting> cachedActiveTheme = new AtomicReference<>(null);
    private volatile boolean themeCacheInitialized = false;
    private volatile long lastThemeCacheTime = 0;
    private static final long THEME_CACHE_TTL_MS = 60000L; // 60 giây TTL cho active theme cache

    @Override
    @Transactional(readOnly = true)
    public SystemSettingsDTO getSettings() {
        Map<String, String> map = new HashMap<>();
        List<SystemSetting> all = systemSettingRepository.findAll();
        LocalDateTime latestUpdate = null;
        Integer lastUpdatedBy = null;

        for (SystemSetting s : all) {
            if (s.getSettingKey() != null && (s.getSettingKey().startsWith("web_theme_tokens:") || s.getSettingKey().startsWith("web_theme_bg:"))) {
                continue;
            }
            map.put(s.getSettingKey(), s.getSettingValue());
            if (s.getUpdatedAt() != null) {
                if (latestUpdate == null || s.getUpdatedAt().isAfter(latestUpdate)) {
                    latestUpdate = s.getUpdatedAt();
                    lastUpdatedBy = s.getUpdatedBy();
                }
            }
        }

        String adminName = "Ban Quản trị";
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

    @Override
    @Transactional(readOnly = true)
    public List<WebSetting> getAllThemes() {
        return webSettingRepository.findAllByOrderByIdAsc();
    }

    @Override
    @Transactional(readOnly = true)
    public WebSetting getActiveTheme() {
        long now = System.currentTimeMillis();
        if (themeCacheInitialized && (now - lastThemeCacheTime < THEME_CACHE_TTL_MS) && cachedActiveTheme.get() != null) {
            return cachedActiveTheme.get();
        }
        try {
            WebSetting active = webSettingRepository.findFirstByIsActiveTrueOrderByIdAsc().orElse(null);
            if (active == null) {
                active = webSettingRepository.findByFolderPath("theme-default").orElse(null);
            }
            if (active != null) {
                cachedActiveTheme.set(active);
                lastThemeCacheTime = now;
                themeCacheInitialized = true;
            }
            return active;
        } catch (Exception e) {
            log.warn("Lỗi khi tải active theme từ CSDL: {}", e.getMessage());
            return cachedActiveTheme.get();
        }
    }

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("^#[0-9A-Fa-f]{6}$");
    private static final Pattern HTTP_URL_PATTERN = Pattern.compile("^https?://.+", Pattern.CASE_INSENSITIVE);

    public static final Set<String> CORE_REQUIRED_TOKENS = Set.of(
            "background", "on_background",
            "surface", "on_surface",
            "primary", "secondary"
    );

    public static final Set<String> THEME_TOKEN_WHITELIST = Set.of(
            "background", "on_background",
            "surface", "on_surface", "on_surface_variant",
            "surface_container_lowest", "surface_container_low", "surface_container", "surface_container_high", "surface_container_highest",
            "primary", "primary_container", "on_primary", "on_primary_container",
            "secondary", "on_secondary",
            "outline", "outline_variant"
    );

    private record ContrastCheckPair(String fgKey, String bgKey, String name) {}

    private static final List<ContrastCheckPair> CONTRAST_PAIRS = List.of(
            new ContrastCheckPair("on_background", "background", "Chữ trên nền chính / Nền chính (on_background / background)"),
            new ContrastCheckPair("on_surface", "surface", "Chữ trên bề mặt / Bề mặt (on_surface / surface)"),
            new ContrastCheckPair("on_surface_variant", "surface", "Chữ phụ trên bề mặt / Bề mặt (on_surface_variant / surface)"),
            new ContrastCheckPair("on_surface", "surface_container_lowest", "Chữ trên thẻ / Nền thẻ (on_surface / surface_container_lowest)"),
            new ContrastCheckPair("on_surface_variant", "surface_container_lowest", "Chữ phụ trên thẻ / Nền thẻ (on_surface_variant / surface_container_lowest)"),
            new ContrastCheckPair("on_primary", "primary", "Chữ trên màu chủ đạo / Màu chủ đạo (on_primary / primary)"),
            new ContrastCheckPair("on_primary_container", "primary_container", "Chữ trên hộp chủ đạo / Hộp chủ đạo (on_primary_container / primary_container)"),
            new ContrastCheckPair("on_secondary", "secondary", "Chữ trên huy hiệu phụ / Màu huy hiệu (on_secondary / secondary)"),
            new ContrastCheckPair("primary", "background", "Chữ/icon chủ đạo trên nền chính (primary / background)"),
            new ContrastCheckPair("primary", "surface", "Chữ/icon chủ đạo trên bề mặt (primary / surface)"),
            new ContrastCheckPair("secondary", "surface", "Chữ màu phụ trên bề mặt (secondary / surface)"),
            new ContrastCheckPair("primary", "surface_container", "Chữ/icon chủ đạo trên khối (Pill Coin / primary trên surface_container)"),
            new ContrastCheckPair("secondary", "surface_container", "Màu phụ trên khối (secondary / surface_container)"),
            new ContrastCheckPair("on_surface_variant", "surface_container", "Chữ phụ trên khối (on_surface_variant / surface_container)")
    );

    private static int[] hexToRgb(String hex) {
        if (hex == null || hex.length() != 7 || !hex.startsWith("#")) {
            return new int[]{0, 0, 0};
        }
        try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);
            return new int[]{r, g, b};
        } catch (Exception e) {
            return new int[]{0, 0, 0};
        }
    }

    private static String rgbToHex(int r, int g, int b) {
        int cr = Math.max(0, Math.min(255, r));
        int cg = Math.max(0, Math.min(255, g));
        int cb = Math.max(0, Math.min(255, b));
        return String.format("#%02X%02X%02X", cr, cg, cb);
    }

    private static String mixColors(String hex1, String hex2, double weight) {
        int[] c1 = hexToRgb(hex1);
        int[] c2 = hexToRgb(hex2);
        double w = Math.max(0.0, Math.min(1.0, weight));
        int r = (int) Math.round(c1[0] * (1.0 - w) + c2[0] * w);
        int g = (int) Math.round(c1[1] * (1.0 - w) + c2[1] * w);
        int b = (int) Math.round(c1[2] * (1.0 - w) + c2[2] * w);
        return rgbToHex(r, g, b);
    }

    private static String toRgbTriplet(String hex) {
        int[] rgb = hexToRgb(hex);
        return rgb[0] + " " + rgb[1] + " " + rgb[2];
    }

    @Override
    @Transactional(readOnly = true)
    public String getActiveThemeCss() {
        WebSetting active = getActiveTheme();
        if (active == null || "theme-default".equals(active.getFolderPath()) || active.getFolderPath() == null) {
            return null;
        }

        String folderPath = active.getFolderPath();

        // 1. Sinh CSS bộ mã màu tokens (nếu có)
        String tokenKey = "web_theme_tokens:" + folderPath;
        Optional<SystemSetting> settingOpt = systemSettingRepository.findById(tokenKey);
        String tokenCss = null;
        boolean isDark = false;
        if (settingOpt.isPresent() && settingOpt.get().getSettingValue() != null && !settingOpt.get().getSettingValue().isBlank()) {
            String tokenJson = settingOpt.get().getSettingValue();
            tokenCss = formatCssVariables(tokenJson);
            isDark = isDarkThemeTokens(tokenJson);
        } else {
            log.info("Giao diện đang kích hoạt '{}' chưa có bộ mã màu (tokens) trong system_settings. Bỏ qua phần CSS màu sắc.", folderPath);
        }

        // 2. Sinh CSS ảnh nền mờ (UC-52) - Hoàn toàn độc lập với phần tokens
        String backgroundCss = generateThemeBackgroundCss(folderPath, isDark);

        // 3. Kiểm tra phòng vệ (Option A): nếu CSS chứa ký tự cấm <>"'& thì loại phần đó và log WARN
        if (tokenCss != null && containsForbiddenCssChars(tokenCss)) {
            log.warn("Phát hiện ký tự cấm trong tokenCss của giao diện '{}'. Bỏ qua tokenCss để tránh lỗi HTML entities.", folderPath);
            tokenCss = null;
        }
        if (backgroundCss != null && containsForbiddenCssChars(backgroundCss)) {
            log.warn("Phát hiện ký tự cấm trong backgroundCss của giao diện '{}'. Bỏ qua backgroundCss để tránh lỗi HTML entities.", folderPath);
            backgroundCss = null;
        }

        if ((tokenCss == null || tokenCss.isBlank()) && (backgroundCss == null || backgroundCss.isBlank())) {
            return null;
        }

        StringBuilder combined = new StringBuilder();
        if (tokenCss != null && !tokenCss.isBlank()) {
            combined.append(tokenCss);
            if (!tokenCss.endsWith("\n")) {
                combined.append("\n");
            }
        }
        if (backgroundCss != null && !backgroundCss.isBlank()) {
            combined.append(backgroundCss);
        }
        return combined.toString();
    }

    private boolean containsForbiddenCssChars(String css) {
        if (css == null) return false;
        for (int i = 0; i < css.length(); i++) {
            char c = css.charAt(i);
            if (c == '<' || c == '>' || c == '"' || c == '\'' || c == '&') {
                return true;
            }
        }
        return false;
    }

    private boolean isDarkThemeTokens(String tokensJson) {
        if (tokensJson == null || tokensJson.isBlank()) return false;
        try {
            Map<String, String> map = objectMapper.readValue(tokensJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
            String bg = map.get("background");
            if (bg == null) bg = map.get("--c-background");
            if (bg != null) {
                bg = bg.trim();
                if (bg.startsWith("#") && bg.length() == 7) {
                    return calculateLuminance(bg) < 0.2;
                }
                String[] parts = bg.split("\\s+");
                if (parts.length == 3) {
                    int r = Integer.parseInt(parts[0]);
                    int g = Integer.parseInt(parts[1]);
                    int b = Integer.parseInt(parts[2]);
                    return calculateLuminance(rgbToHex(r, g, b)) < 0.2;
                }
            }
        } catch (Exception ignored) {}
        return false;
    }

    @Override
    @Transactional(readOnly = true)
    public String getThemeTokens(String folderPath) {
        if (folderPath == null || "theme-default".equals(folderPath)) {
            return "";
        }
        return systemSettingRepository.findById("web_theme_tokens:" + folderPath)
                .map(SystemSetting::getSettingValue)
                .map(this::normalizeToHexTokensJson)
                .orElse("");
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, String> getThemeTokenMap(String folderPath) {
        if (folderPath == null || "theme-default".equals(folderPath)) {
            return Collections.emptyMap();
        }
        return systemSettingRepository.findById("web_theme_tokens:" + folderPath)
                .map(SystemSetting::getSettingValue)
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
                    } catch (Exception e) {
                        log.warn("Lỗi đọc theme token map cho {}: {}", folderPath, e.getMessage());
                        return Collections.<String, String>emptyMap();
                    }
                })
                .orElse(Collections.emptyMap());
    }

    @Override
    @Transactional(readOnly = true)
    public ThemeBackgroundDTO getThemeBackground(String folderPath) {
        if (folderPath == null || "theme-default".equals(folderPath)) {
            return null;
        }
        return systemSettingRepository.findById("web_theme_bg:" + folderPath)
                .map(SystemSetting::getSettingValue)
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, ThemeBackgroundDTO.class);
                    } catch (Exception e) {
                        log.warn("Lỗi đọc cấu hình ảnh nền cho {}: {}", folderPath, e.getMessage());
                        return null;
                    }
                })
                .orElse(null);
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return;
        }
        if (file.getSize() > 3 * 1024 * 1024) {
            throw new IllegalArgumentException("Ảnh nền không được vượt quá 3MB (kích thước file: "
                    + String.format(Locale.US, "%.2f", file.getSize() / (1024.0 * 1024.0)) + "MB). Vui lòng chọn ảnh nhỏ hơn.");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("Không thể đọc nội dung file ảnh: " + e.getMessage());
        }
        if (bytes == null || bytes.length < 12) {
            throw new IllegalArgumentException("File ảnh không hợp lệ hoặc dữ liệu bị hỏng.");
        }

        boolean isJpeg = (bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF);
        boolean isPng = (bytes[0] == (byte) 0x89 && bytes[1] == (byte) 0x50 && bytes[2] == (byte) 0x4E && bytes[3] == (byte) 0x47);
        boolean isWebp = (bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F'
                && bytes[8] == 'W' && bytes[9] == 'E' && bytes[10] == 'B' && bytes[11] == 'P');

        if (!isJpeg && !isPng && !isWebp) {
            throw new IllegalArgumentException("Định dạng file không được hỗ trợ. Chỉ chấp nhận ảnh JPG, PNG hoặc WEBP hợp lệ.");
        }
    }

    private String applyCloudinaryTransformation(String url) {
        if (url == null || url.isBlank()) return url;
        String marker = "/image/upload/";
        int idx = url.indexOf(marker);
        if (idx != -1 && !url.contains("c_limit")) {
            return url.substring(0, idx + marker.length()) + "c_limit,w_1920,q_auto,f_auto/" + url.substring(idx + marker.length());
        }
        return url;
    }

    private boolean isValidCloudinaryUrl(String url) {
        if (url == null || url.isBlank()) return false;
        String trimmed = url.trim();
        if (trimmed.contains("'") || trimmed.contains("\"") || trimmed.contains("(") || trimmed.contains(")")
                || trimmed.contains(";") || trimmed.contains(" ") || trimmed.contains("<") || trimmed.contains(">")) {
            return false;
        }
        String cloudName = (cloudinaryProperties != null && cloudinaryProperties.getCloudName() != null && !cloudinaryProperties.getCloudName().isBlank())
                ? Pattern.quote(cloudinaryProperties.getCloudName().trim())
                : "[a-zA-Z0-9_-]+";
        String regex = "^https://res\\.cloudinary\\.com/" + cloudName + "/image/upload/[a-zA-Z0-9_,/-]+\\.(jpg|jpeg|png|webp)$";
        return Pattern.matches(regex, trimmed);
    }

    @Override
    @Transactional
    public WebSetting activateTheme(Integer themeId, Integer adminId, String clientIp) {
        if (themeId == null) {
            throw new IllegalArgumentException("Mã chủ đề không hợp lệ.");
        }

        WebSetting targetTheme = webSettingRepository.findById(themeId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chủ đề giao diện yêu cầu."));

        if (Boolean.TRUE.equals(targetTheme.getIsActive())) {
            throw new com.tramtruyen.exception.ThemeAlreadyActiveException("Giao diện này đang được sử dụng.");
        }

        if (targetTheme.getFolderPath() == null || !THEME_FOLDER_PATTERN.matcher(targetTheme.getFolderPath()).matches()) {
            throw new IllegalArgumentException("Mã thư mục giao diện không hợp lệ (phải theo định dạng ^theme-[a-z0-9-]{1,55}$).");
        }

        WebSetting previousActive = webSettingRepository.findFirstByIsActiveTrueOrderByIdAsc().orElse(null);

        // Deactivate all currently active themes
        webSettingRepository.deactivateAllActiveThemes();

        // Activate target theme
        targetTheme.setIsActive(true);
        targetTheme.setUpdatedAt(LocalDateTime.now());
        WebSetting saved = webSettingRepository.save(targetTheme);

        // Invalidate and update cache
        cachedActiveTheme.set(saved);
        lastThemeCacheTime = System.currentTimeMillis();
        themeCacheInitialized = true;

        // Audit Log ACTIVATE_WEB_THEME
        try {
            Map<String, Object> oldValues = new LinkedHashMap<>();
            oldValues.put("themeId", previousActive != null ? previousActive.getId() : null);
            oldValues.put("themeName", previousActive != null ? previousActive.getName() : "Trạm Truyện Chuẩn (Mặc định)");
            oldValues.put("folderPath", previousActive != null ? previousActive.getFolderPath() : "theme-default");

            Map<String, Object> newValues = new LinkedHashMap<>();
            newValues.put("themeId", saved.getId());
            newValues.put("themeName", saved.getName());
            newValues.put("folderPath", saved.getFolderPath());

            systemAuditLogRepository.save(SystemAuditLog.builder()
                    .adminId(adminId != null ? adminId : 1)
                    .actionType("ACTIVATE_WEB_THEME")
                    .targetEntity("web_settings")
                    .targetId(saved.getId())
                    .oldValue(objectMapper.writeValueAsString(oldValues))
                    .newValue(objectMapper.writeValueAsString(newValues))
                    .ipAddress(clientIp)
                    .build());
        } catch (Exception e) {
            log.error("Lỗi khi ghi system audit log kích hoạt giao diện", e);
        }

        return saved;
    }

    @Override
    @Transactional
    public WebSetting createTheme(String name, String folderPath, String tokensJson,
                                  MultipartFile backgroundImage, Integer bgOpacity, Integer bgBlur,
                                  Integer adminId, String clientIp) {
        // 1. Validate toàn bộ trước
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên giao diện không được để trống.");
        }
        if (folderPath == null || !THEME_FOLDER_PATTERN.matcher(folderPath.trim()).matches()) {
            throw new IllegalArgumentException("Mã thư mục giao diện không hợp lệ (phải bắt đầu bằng 'theme-' và chỉ gồm chữ thường, số, dấu gạch ngang, 1-55 ký tự).");
        }
        String cleanFolder = folderPath.trim();
        if (webSettingRepository.existsByFolderPath(cleanFolder)) {
            throw new IllegalArgumentException("Mã thư mục giao diện '" + cleanFolder + "' đã tồn tại.");
        }
        String validatedTokensJson = validateAndNormalizeTokensJson(tokensJson);

        int opacity = bgOpacity != null ? Math.max(5, Math.min(30, bgOpacity)) : 12;
        int blur = bgBlur != null ? Math.max(0, Math.min(10, bgBlur)) : 4;
        validateImageFile(backgroundImage);

        // 2. Upload Cloudinary sau khi validate hoàn tất (trước khi ghi DB)
        String uploadedBgUrl = null;
        if (backgroundImage != null && !backgroundImage.isEmpty()) {
            uploadedBgUrl = mediaStorageService.uploadImage(backgroundImage);
            uploadedBgUrl = applyCloudinaryTransformation(uploadedBgUrl);
            if (!isValidCloudinaryUrl(uploadedBgUrl)) {
                log.warn("URL tải lên Cloudinary không khớp định dạng hợp lệ: {}", uploadedBgUrl);
                throw new IllegalArgumentException("Không thể xác thực URL ảnh sau khi tải lên Cloudinary.");
            }
        }

        // 3. Ghi DB
        WebSetting saved;
        try {
            WebSetting newTheme = WebSetting.builder()
                    .name(name.trim())
                    .folderPath(cleanFolder)
                    .thumbnailUrl(null)
                    .isActive(false)
                    .uploadedBy(adminId)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            saved = webSettingRepository.save(newTheme);

            SystemSetting tokenSetting = SystemSetting.builder()
                    .settingKey("web_theme_tokens:" + cleanFolder)
                    .settingValue(validatedTokensJson)
                    .description("Bộ mã màu hex cho giao diện " + saved.getName())
                    .updatedBy(adminId)
                    .updatedAt(LocalDateTime.now())
                    .build();
            systemSettingRepository.save(tokenSetting);

            if (uploadedBgUrl != null) {
                ThemeBackgroundDTO bgDto = ThemeBackgroundDTO.builder()
                        .url(uploadedBgUrl)
                        .opacity(opacity)
                        .blur(blur)
                        .build();
                SystemSetting bgSetting = SystemSetting.builder()
                        .settingKey("web_theme_bg:" + cleanFolder)
                        .settingValue(objectMapper.writeValueAsString(bgDto))
                        .description("Cấu hình ảnh nền cho giao diện " + saved.getName())
                        .updatedBy(adminId)
                        .updatedAt(LocalDateTime.now())
                        .build();
                systemSettingRepository.save(bgSetting);
            }
        } catch (Exception e) {
            if (uploadedBgUrl != null) {
                log.warn("Lỗi lưu DB sau khi upload ảnh Cloudinary, URL mồ côi: {}", uploadedBgUrl);
            }
            throw (e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e));
        }

        // 4. Audit Log CREATE_WEB_THEME
        try {
            Map<String, Object> newValues = new LinkedHashMap<>();
            newValues.put("themeId", saved.getId());
            newValues.put("name", saved.getName());
            newValues.put("folderPath", saved.getFolderPath());
            newValues.put("thumbnailUrl", saved.getThumbnailUrl());
            newValues.put("hasBackground", uploadedBgUrl != null);
            if (uploadedBgUrl != null) {
                newValues.put("bgOpacity", opacity);
                newValues.put("bgBlur", blur);
            }

            systemAuditLogRepository.save(SystemAuditLog.builder()
                    .adminId(adminId != null ? adminId : 1)
                    .actionType("CREATE_WEB_THEME")
                    .targetEntity("web_settings")
                    .targetId(saved.getId())
                    .oldValue(null)
                    .newValue(objectMapper.writeValueAsString(newValues))
                    .ipAddress(clientIp)
                    .build());
        } catch (Exception e) {
            log.error("Lỗi khi ghi system audit log tạo mới giao diện", e);
        }

        return saved;
    }

    @Override
    @Transactional
    public WebSetting updateTheme(Integer id, String name, String folderPath, String tokensJson,
                                  MultipartFile backgroundImage, Integer bgOpacity, Integer bgBlur, Boolean removeBg,
                                  Integer adminId, String clientIp) {
        // 1. Validate toàn bộ trước
        if (id == null) {
            throw new IllegalArgumentException("Mã chủ đề không hợp lệ.");
        }
        WebSetting theme = webSettingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chủ đề giao diện yêu cầu."));

        if ("theme-default".equals(theme.getFolderPath())) {
            throw new IllegalArgumentException("Không thể chỉnh sửa giao diện mặc định hệ thống.");
        }
        if (folderPath != null && !folderPath.isBlank() && !folderPath.trim().equals(theme.getFolderPath())) {
            throw new IllegalArgumentException("Mã thư mục giao diện không khớp với bản ghi đang chỉnh sửa (ID=" + id + ").");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên giao diện không được để trống.");
        }
        String validatedTokensJson = validateAndNormalizeTokensJson(tokensJson);

        int opacity = bgOpacity != null ? Math.max(5, Math.min(30, bgOpacity)) : 12;
        int blur = bgBlur != null ? Math.max(0, Math.min(10, bgBlur)) : 4;
        validateImageFile(backgroundImage);

        ThemeBackgroundDTO oldBg = getThemeBackground(theme.getFolderPath());
        String oldName = theme.getName();
        String oldThumbnail = theme.getThumbnailUrl();

        // 2. Upload Cloudinary nếu có ảnh mới
        String newUploadedBgUrl = null;
        if (Boolean.TRUE.equals(removeBg)) {
            // Admin chọn xoá ảnh nền
        } else if (backgroundImage != null && !backgroundImage.isEmpty()) {
            newUploadedBgUrl = mediaStorageService.uploadImage(backgroundImage);
            newUploadedBgUrl = applyCloudinaryTransformation(newUploadedBgUrl);
            if (!isValidCloudinaryUrl(newUploadedBgUrl)) {
                log.warn("URL tải lên Cloudinary không khớp định dạng hợp lệ: {}", newUploadedBgUrl);
                throw new IllegalArgumentException("Không thể xác thực URL ảnh sau khi tải lên Cloudinary.");
            }
        }

        // Xác định URL ảnh nền cuối cùng
        String finalBgUrl = null;
        if (!Boolean.TRUE.equals(removeBg)) {
            if (newUploadedBgUrl != null) {
                finalBgUrl = newUploadedBgUrl;
            } else if (oldBg != null && oldBg.getUrl() != null) {
                finalBgUrl = oldBg.getUrl();
            }
        }

        // 3. Ghi DB
        WebSetting saved;
        try {
            theme.setName(name.trim());
            theme.setThumbnailUrl(null);
            theme.setUpdatedAt(LocalDateTime.now());
            saved = webSettingRepository.save(theme);

            String tokenKey = "web_theme_tokens:" + theme.getFolderPath();
            SystemSetting tokenSetting = systemSettingRepository.findById(tokenKey)
                    .orElse(SystemSetting.builder().settingKey(tokenKey).build());
            tokenSetting.setSettingValue(validatedTokensJson);
            tokenSetting.setDescription("Bộ mã màu hex cho giao diện " + saved.getName());
            tokenSetting.setUpdatedBy(adminId);
            tokenSetting.setUpdatedAt(LocalDateTime.now());
            systemSettingRepository.save(tokenSetting);

            String bgKey = "web_theme_bg:" + theme.getFolderPath();
            if (finalBgUrl != null) {
                ThemeBackgroundDTO bgDto = ThemeBackgroundDTO.builder()
                        .url(finalBgUrl)
                        .opacity(opacity)
                        .blur(blur)
                        .build();
                SystemSetting bgSetting = systemSettingRepository.findById(bgKey)
                        .orElse(SystemSetting.builder().settingKey(bgKey).build());
                bgSetting.setSettingValue(objectMapper.writeValueAsString(bgDto));
                bgSetting.setDescription("Cấu hình ảnh nền cho giao diện " + saved.getName());
                bgSetting.setUpdatedBy(adminId);
                bgSetting.setUpdatedAt(LocalDateTime.now());
                systemSettingRepository.save(bgSetting);
            } else {
                systemSettingRepository.deleteById(bgKey);
            }

            if (Boolean.TRUE.equals(theme.getIsActive())) {
                cachedActiveTheme.set(saved);
                lastThemeCacheTime = System.currentTimeMillis();
                themeCacheInitialized = true;
            }
        } catch (Exception e) {
            if (newUploadedBgUrl != null) {
                log.warn("Lỗi lưu DB sau khi upload ảnh Cloudinary, URL mồ côi: {}", newUploadedBgUrl);
            }
            throw (e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e));
        }

        // 4. Audit Log UPDATE_WEB_THEME
        try {
            Map<String, Object> oldValues = new LinkedHashMap<>();
            oldValues.put("name", oldName);
            oldValues.put("thumbnailUrl", oldThumbnail);
            oldValues.put("hasBackground", oldBg != null && oldBg.getUrl() != null);
            if (oldBg != null && oldBg.getUrl() != null) {
                oldValues.put("bgOpacity", oldBg.getOpacity());
                oldValues.put("bgBlur", oldBg.getBlur());
            }

            Map<String, Object> newValues = new LinkedHashMap<>();
            newValues.put("name", saved.getName());
            newValues.put("thumbnailUrl", saved.getThumbnailUrl());
            newValues.put("hasBackground", finalBgUrl != null);
            if (finalBgUrl != null) {
                newValues.put("bgOpacity", opacity);
                newValues.put("bgBlur", blur);
            }

            systemAuditLogRepository.save(SystemAuditLog.builder()
                    .adminId(adminId != null ? adminId : 1)
                    .actionType("UPDATE_WEB_THEME")
                    .targetEntity("web_settings")
                    .targetId(saved.getId())
                    .oldValue(objectMapper.writeValueAsString(oldValues))
                    .newValue(objectMapper.writeValueAsString(newValues))
                    .ipAddress(clientIp)
                    .build());
        } catch (Exception e) {
            log.error("Lỗi khi ghi system audit log cập nhật giao diện", e);
        }

        return saved;
    }

    @Override
    @Transactional
    public void deleteTheme(Integer id, Integer adminId, String clientIp) {
        if (id == null) {
            throw new IllegalArgumentException("Mã chủ đề không hợp lệ.");
        }
        WebSetting theme = webSettingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chủ đề giao diện yêu cầu."));

        if ("theme-default".equals(theme.getFolderPath())) {
            throw new IllegalArgumentException("Không thể xóa giao diện mặc định hệ thống.");
        }
        if (Boolean.TRUE.equals(theme.getIsActive())) {
            throw new IllegalStateException("Không thể xóa giao diện đang được kích hoạt.");
        }

        String tokenKey = "web_theme_tokens:" + theme.getFolderPath();
        String bgKey = "web_theme_bg:" + theme.getFolderPath();
        systemSettingRepository.deleteById(tokenKey);
        systemSettingRepository.deleteById(bgKey);
        webSettingRepository.delete(theme);

        // Audit Log DELETE_WEB_THEME
        try {
            Map<String, Object> oldValues = new LinkedHashMap<>();
            oldValues.put("themeId", theme.getId());
            oldValues.put("name", theme.getName());
            oldValues.put("folderPath", theme.getFolderPath());

            systemAuditLogRepository.save(SystemAuditLog.builder()
                    .adminId(adminId != null ? adminId : 1)
                    .actionType("DELETE_WEB_THEME")
                    .targetEntity("web_settings")
                    .targetId(theme.getId())
                    .oldValue(objectMapper.writeValueAsString(oldValues))
                    .newValue(null)
                    .ipAddress(clientIp)
                    .build());
        } catch (Exception e) {
            log.error("Lỗi khi ghi system audit log xóa giao diện", e);
        }
    }

    private String formatCssVariables(String json) {
        return formatCssVariables(json, null);
    }

    private String formatCssVariables(String json, String folderPath) {
        if (json == null || json.isBlank()) return null;
        try {
            Map<String, String> rawMap = objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
            if (rawMap.isEmpty()) return null;

            Map<String, String> hexMap = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : rawMap.entrySet()) {
                String key = entry.getKey().trim();
                if (key.startsWith("--c-")) {
                    key = key.substring(4).replace('-', '_');
                }
                String val = entry.getValue() != null ? entry.getValue().trim() : "";
                if (val.startsWith("#") && val.length() == 7) {
                    hexMap.put(key, val.toUpperCase());
                } else {
                    String[] parts = val.split("\\s+");
                    if (parts.length == 3) {
                        try {
                            int r = Integer.parseInt(parts[0]);
                            int g = Integer.parseInt(parts[1]);
                            int b = Integer.parseInt(parts[2]);
                            hexMap.put(key, String.format("#%02X%02X%02X", r, g, b));
                        } catch (Exception ignored) {}
                    }
                }
            }

            String bg = hexMap.getOrDefault("background", "#FFF8F5");
            String onBg = hexMap.getOrDefault("on_background", "#231A11");
            String surface = hexMap.getOrDefault("surface", bg);
            String onSurface = hexMap.getOrDefault("on_surface", onBg);
            String primary = hexMap.getOrDefault("primary", "#875200");
            String secondary = hexMap.getOrDefault("secondary", "#9E422C");
            boolean isDark = calculateLuminance(bg) < 0.2;

            String surfaceMed = hexMap.computeIfAbsent("surface_container", k -> mixColors(bg, onBg, 0.06));
            String surfaceLowest = hexMap.computeIfAbsent("surface_container_lowest", k -> isDark ? mixColors(bg, "#000000", 0.25) : "#FFFFFF");
            String surfaceLow = hexMap.computeIfAbsent("surface_container_low", k -> mixColors(bg, surfaceMed, 0.5));
            String surfaceHigh = hexMap.computeIfAbsent("surface_container_high", k -> mixColors(bg, onBg, 0.09));
            String surfaceHighest = hexMap.computeIfAbsent("surface_container_highest", k -> mixColors(bg, onBg, 0.12));
            String onSurfaceVariant = hexMap.computeIfAbsent("on_surface_variant", k -> mixColors(onBg, bg, 0.28));
            String outline = hexMap.computeIfAbsent("outline", k -> mixColors(bg, onBg, 0.45));
            String outlineVariant = hexMap.computeIfAbsent("outline_variant", k -> mixColors(bg, onBg, 0.20));

            String onPrimary = hexMap.computeIfAbsent("on_primary", k -> 
                calculateContrast("#FFFFFF", primary) >= calculateContrast("#121212", primary) ? "#FFFFFF" : "#121212");
            String primaryContainer = hexMap.computeIfAbsent("primary_container", k -> {
                if (calculateContrast("#FFFFFF", primary) >= 4.5) return primary;
                for (double w = 0.05; w <= 0.8; w += 0.05) {
                    String cand = mixColors(primary, "#000000", w);
                    if (calculateContrast("#FFFFFF", cand) >= 4.5) return cand;
                }
                return primary;
            });
            String onPrimaryContainer = hexMap.computeIfAbsent("on_primary_container", k -> "#FFFFFF");
            String onSecondary = hexMap.computeIfAbsent("on_secondary", k -> 
                calculateContrast("#FFFFFF", secondary) >= calculateContrast("#121212", secondary) ? "#FFFFFF" : "#121212");

            Map<String, String> resolved = new LinkedHashMap<>();

            // 1. Primary group
            resolved.put("--c-primary", toRgbTriplet(primary));
            resolved.put("--c-on-primary", toRgbTriplet(onPrimary));
            resolved.put("--c-primary-container", toRgbTriplet(primaryContainer));
            resolved.put("--c-on-primary-container", toRgbTriplet(onPrimaryContainer));
            resolved.put("--c-primary-fixed", toRgbTriplet(surfaceHigh));
            resolved.put("--c-primary-fixed-dim", toRgbTriplet(primaryContainer));
            resolved.put("--c-on-primary-fixed", toRgbTriplet(onSurface));
            resolved.put("--c-on-primary-fixed-variant", toRgbTriplet(onSurfaceVariant));
            resolved.put("--c-inverse-primary", toRgbTriplet(mixColors(primary, "#FFFFFF", 0.35)));

            // 2. Secondary group
            resolved.put("--c-secondary", toRgbTriplet(secondary));
            resolved.put("--c-on-secondary", toRgbTriplet(onSecondary));
            resolved.put("--c-secondary-container", toRgbTriplet(surfaceHigh));
            resolved.put("--c-on-secondary-container", toRgbTriplet(onSurface));
            resolved.put("--c-secondary-fixed", toRgbTriplet(surfaceHigh));
            resolved.put("--c-secondary-fixed-dim", toRgbTriplet(surfaceMed));
            resolved.put("--c-on-secondary-fixed", toRgbTriplet(onSurface));
            resolved.put("--c-on-secondary-fixed-variant", toRgbTriplet(onSurfaceVariant));

            // 3. Background & Surface group
            resolved.put("--c-background", toRgbTriplet(bg));
            resolved.put("--c-on-background", toRgbTriplet(onBg));
            resolved.put("--c-surface", toRgbTriplet(surface));
            resolved.put("--c-on-surface", toRgbTriplet(onSurface));
            resolved.put("--c-surface-variant", toRgbTriplet(surfaceHighest));
            resolved.put("--c-on-surface-variant", toRgbTriplet(onSurfaceVariant));
            resolved.put("--c-surface-dim", toRgbTriplet(surfaceLow));
            resolved.put("--c-surface-bright", toRgbTriplet(surface));
            resolved.put("--c-surface-container-lowest", toRgbTriplet(surfaceLowest));
            resolved.put("--c-surface-container-low", toRgbTriplet(surfaceLow));
            resolved.put("--c-surface-container", toRgbTriplet(surfaceMed));
            resolved.put("--c-surface-container-high", toRgbTriplet(surfaceHigh));
            resolved.put("--c-surface-container-highest", toRgbTriplet(surfaceHighest));
            resolved.put("--c-surface-tint", toRgbTriplet(primary));
            resolved.put("--c-inverse-surface", toRgbTriplet(onSurface));
            resolved.put("--c-inverse-on-surface", toRgbTriplet(surface));

            // 4. Outline group
            resolved.put("--c-outline", toRgbTriplet(outline));
            resolved.put("--c-outline-variant", toRgbTriplet(outlineVariant));

            // 5. Tertiary group
            resolved.put("--c-tertiary", toRgbTriplet(onSurfaceVariant));
            resolved.put("--c-on-tertiary", toRgbTriplet(surfaceLowest));
            resolved.put("--c-tertiary-container", toRgbTriplet(surfaceLow));
            resolved.put("--c-on-tertiary-container", toRgbTriplet(onSurface));
            resolved.put("--c-tertiary-fixed", toRgbTriplet(surfaceHigh));
            resolved.put("--c-tertiary-fixed-dim", toRgbTriplet(surfaceMed));
            resolved.put("--c-on-tertiary-fixed", toRgbTriplet(onSurface));
            resolved.put("--c-on-tertiary-fixed-variant", toRgbTriplet(onSurfaceVariant));

            // 6. Error group
            resolved.put("--c-error", "186 26 26");
            resolved.put("--c-on-error", "255 255 255");
            resolved.put("--c-error-container", "255 218 214");
            resolved.put("--c-on-error-container", "147 0 10");

            StringBuilder sb = new StringBuilder(":root {\n");
            for (Map.Entry<String, String> entry : resolved.entrySet()) {
                if (entry.getValue() != null && !entry.getValue().isBlank()) {
                    sb.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append(";\n");
                }
            }
            sb.append("}\n\n");

            // Danh sách trắng các quy tắc CSS ghi đè các class hardcode trên giao diện công khai (VIỆC 2 & 3)
            sb.append("""
            /* Phủ màu nền trang và chữ chính */
            body, body.bg-\\[\\#faf8f5\\], body.bg-\\[\\#FAF7F2\\], body.bg-\\[\\#faf7f2\\] {
              background-color: rgb(var(--c-background)) !important;
              color: rgb(var(--c-on-surface)) !important;
            }

            /* Gán lại thẻ sáng / nền trắng về surface_container_lowest */
            body .bg-white,
            body .bg-\\[\\#faf7f2\\], body .bg-\\[\\#FAF7F2\\], body .bg-\\[\\#faf8f5\\],
            body .bg-\\[\\#FAFAFA\\], body .bg-\\[\\#FCFAF6\\], body .bg-\\[\\#fdfaf7\\],
            body .bg-\\[\\#F9F7F5\\], body .bg-\\[\\#F7F3EB\\], body .bg-\\[\\#F8F5EE\\],
            body .bg-\\[\\#f8f5ee\\], body .bg-\\[\\#faf6f0\\], body .bg-\\[\\#faf3e8\\],
            body .bg-\\[\\#faf4ec\\] {
              background-color: rgb(var(--c-surface-container-lowest)) !important;
            }

            /* Gán lại nền phụ / dải màu về surface_container_low */
            body .bg-\\[\\#FFF6EF\\], body .bg-\\[\\#f4ece1\\], body .bg-\\[\\#f5eae0\\],
            body .bg-\\[\\#F5EAD9\\], body .bg-\\[\\#F5EEDB\\], body .bg-\\[\\#F3EDE2\\],
            body .bg-\\[\\#f3e8d5\\], body .bg-\\[\\#FFF5E6\\], body .bg-\\[\\#FFF8EE\\],
            body .bg-\\[\\#FFF9EC\\], body .bg-\\[\\#FFF9EE\\], body .bg-\\[\\#fff8ed\\],
            body .bg-\\[\\#fffaf4\\], body .bg-\\[\\#fffaf8\\], body .bg-\\[\\#fdf3e2\\],
            body .bg-\\[\\#FDF0E1\\], body .bg-\\[\\#f0dfd1\\], body .bg-gray-50 {
              background-color: rgb(var(--c-surface-container-low)) !important;
            }

            /* Nền thẻ tag / tint */
            body .bg-\\[\\#ECFDF5\\], body .bg-\\[\\#eef7ee\\], body .bg-\\[\\#f6fbf6\\], body .bg-\\[\\#f0f4ff\\] {
              background-color: rgb(var(--c-surface-container)) !important;
            }
            body .bg-\\[\\#FEF3C7\\], body .bg-\\[\\#FEF2F2\\], body .bg-\\[\\#FEE2E2\\], body .bg-\\[\\#ffebee\\],
            body .bg-\\[\\#875200\\]\\/10, body .bg-\\[\\#bc7d39\\]\\/10 {
              background-color: rgb(var(--c-surface-container-high)) !important;
            }

            /* Hover states cho nền sáng */
            body .hover\\:bg-\\[\\#faf7f2\\]:hover, body .hover\\:bg-\\[\\#FAF7F2\\]:hover,
            body .hover\\:bg-\\[\\#faf8f5\\]:hover, body .hover\\:bg-\\[\\#f5eae0\\]:hover,
            body .hover\\:bg-\\[\\#f4ece1\\]:hover, body .hover\\:bg-\\[\\#feeadc\\]:hover,
            body .hover\\:bg-\\[\\#fbedd5\\]:hover, body .hover\\:bg-\\[\\#fff9f0\\]:hover,
            body .hover\\:bg-gray-50:hover, body .hover\\:bg-\\[\\#eae2d5\\]:hover,
            body .hover\\:bg-\\[\\#e9decb\\]:hover, body .hover\\:bg-\\[\\#ebd9c7\\]:hover {
              background-color: rgb(var(--c-surface-container-high)) !important;
            }

            /* Chữ tối màu hardcode (tiêu đề, nội dung chính): on_surface */
            body .text-\\[\\#2b2118\\], body .text-\\[\\#2B231D\\], body .text-\\[\\#231a11\\],
            body .text-\\[\\#231A11\\], body .text-\\[\\#2e261f\\], body .text-\\[\\#333333\\],
            body .text-\\[\\#3d3025\\], body .text-\\[\\#3e342b\\], body .text-\\[\\#4D3B2A\\],
            body .text-\\[\\#4d3b2a\\], body .text-\\[\\#4d4843\\], body .text-\\[\\#524437\\],
            body .text-\\[\\#5c4a3b\\] {
              color: rgb(var(--c-on-surface)) !important;
            }

            /* Chữ phụ, ngày tháng, metadata: on_surface_variant */
            body .text-\\[\\#5c3e07\\], body .text-\\[\\#666666\\], body .text-\\[\\#6b5849\\],
            body .text-\\[\\#6B5B4B\\], body .text-\\[\\#6b5b4b\\], body .text-\\[\\#6b7280\\],
            body .text-\\[\\#734825\\], body .text-\\[\\#75695d\\], body .text-\\[\\#7d6b59\\],
            body .text-\\[\\#7D7065\\], body .text-\\[\\#7d7367\\], body .text-\\[\\#8a7664\\],
            body .text-\\[\\#8C642E\\], body .text-\\[\\#8C6D3F\\], body .text-\\[\\#8C7A68\\],
            body .text-\\[\\#8c7a68\\], body .text-\\[\\#8C7E72\\], body .text-\\[\\#9c7a4b\\],
            body .text-\\[\\#9d8975\\], body .text-\\[\\#a09081\\], body .text-\\[\\#a4917f\\],
            body .text-\\[\\#A59585\\], body .text-\\[\\#A89A8E\\], body .text-\\[\\#A89F91\\],
            body .text-gray-300, body .text-gray-400, body .text-gray-500,
            body .text-slate-400, body .text-slate-500,
            body .placeholder\\:text-\\[\\#a08b79\\]::placeholder {
              color: rgb(var(--c-on-surface-variant)) !important;
            }

            /* Chữ màu thương hiệu / liên kết: primary */
            body .text-\\[\\#875200\\], body .text-\\[\\#92400E\\], body .text-\\[\\#B45309\\],
            body .text-\\[\\#b5803a\\], body .text-\\[\\#b88636\\], body .text-\\[\\#bc7d39\\],
            body .text-\\[\\#c08d41\\], body .text-\\[\\#D97706\\], body .text-\\[\\#15803D\\],
            body .text-\\[\\#2d6a4f\\], body .text-\\[\\#2e7d32\\], body .text-\\[\\#48BB78\\] {
              color: rgb(var(--c-primary)) !important;
            }

            /* Hover text */
            body .hover\\:text-\\[\\#2b2118\\]:hover, body .hover\\:text-\\[\\#2B231D\\]:hover {
              color: rgb(var(--c-on-surface)) !important;
            }
            body .hover\\:text-\\[\\#875200\\]:hover, body .hover\\:text-\\[\\#9c7a4b\\]:hover,
            body .hover\\:text-\\[\\#B45309\\]:hover, body .hover\\:text-\\[\\#c08d41\\]:hover {
              color: rgb(var(--c-primary)) !important;
            }

            /* Viền hardcode: outline_variant */
            body .border-\\[\\#ebd9c7\\], body .border-\\[\\#eae2d5\\], body .border-\\[\\#EAE3D2\\],
            body .border-\\[\\#E8DFC8\\], body .border-\\[\\#E8DFC9\\], body .border-\\[\\#ead8cb\\],
            body .border-\\[\\#EFE9DC\\], body .border-\\[\\#f0d6a8\\], body .border-\\[\\#f0deca\\],
            body .border-\\[\\#f0e4d7\\], body .border-\\[\\#f0e8dc\\], body .border-\\[\\#f2ebe1\\],
            body .border-\\[\\#F2ECE1\\], body .border-\\[\\#f4ece1\\], body .border-\\[\\#f5dfb8\\],
            body .border-\\[\\#F5E6CC\\], body .border-\\[\\#f5eae0\\], body .border-\\[\\#F6D8B6\\],
            body .border-\\[\\#FBEAC8\\], body .border-\\[\\#D4C8B5\\], body .border-\\[\\#D5C7B0\\],
            body .border-\\[\\#c4a47c\\], body .border-\\[\\#d2edd2\\], body .border-\\[\\#e2f0e2\\],
            body .border-\\[\\#d4a054\\], body .border-gray-200, body .border-white {
              border-color: rgb(var(--c-outline-variant)) !important;
            }
            body .focus\\:border-\\[\\#875200\\]:focus, body .focus\\:border-\\[\\#B45309\\]:focus,
            body .focus\\:border-\\[\\#c08d41\\]:focus {
              border-color: rgb(var(--c-primary)) !important;
            }

            /* Nút bấm chủ đạo hardcode */
            body .bg-\\[\\#c08d41\\], body .bg-\\[\\#d4a054\\], body .bg-\\[\\#b88636\\],
            body .bg-\\[\\#875200\\], body .bg-\\[\\#B45309\\], body .bg-\\[\\#d97706\\],
            body .bg-\\[\\#e53935\\], body .bg-\\[\\#bd5d38\\],
            body .peer-checked\\:bg-\\[\\#c08d41\\], body .peer-checked\\:bg-\\[\\#a64032\\] {
              background-color: rgb(var(--c-primary)) !important;
              color: rgb(var(--c-on-primary)) !important;
            }
            body .hover\\:bg-\\[\\#6b4100\\]:hover, body .hover\\:bg-\\[\\#92400E\\]:hover,
            body .hover\\:bg-\\[\\#a34b29\\]:hover, body .hover\\:bg-\\[\\#a67a38\\]:hover,
            body .hover\\:bg-\\[\\#b06f20\\]:hover, body .hover\\:bg-\\[\\#b45309\\]:hover,
            body .hover\\:bg-\\[\\#c08d41\\]:hover, body .hover\\:bg-\\[\\#d32f2f\\]:hover {
              background-color: rgb(var(--c-primary-container)) !important;
            }

            /* Hero banner strip không còn nền sáng trên Dạ Khúc Đêm (VIỆC 2) */
            body #heroBannerSection {
              background-color: rgb(var(--c-surface-container-low)) !important;
            }
            body #heroBannerSection .bg-primary-fixed-dim\\/20 {
              background-color: rgb(var(--c-primary) / 0.15) !important;
            }
            body #heroBannerSection .bg-secondary-fixed\\/20 {
              background-color: rgb(var(--c-secondary) / 0.15) !important;
            }

            /* Ép màu chữ chuẩn cho badge và nút (VIỆC 3) */
            body .bg-secondary,
            body .bg-secondary *,
            body .bg-secondary.text-white,
            body .bg-secondary .text-white {
              color: rgb(var(--c-on-secondary)) !important;
            }

            body .bg-primary,
            body .bg-primary *,
            body .bg-primary.text-white,
            body .bg-primary .text-white {
              color: rgb(var(--c-on-primary)) !important;
            }

            body .bg-primary-container,
            body .bg-primary-container *,
            body .bg-primary-container.text-white,
            body .bg-primary-container .text-white,
            body .bg-primary-container.text-surface-container-lowest,
            body .bg-primary-container .text-surface-container-lowest {
              color: rgb(var(--c-on-primary-container)) !important;
            }

            /* Pill Coin trên Header (trỏ link nạp coin) */
            body a[href*=/wallet/topup] {
              background-color: rgb(var(--c-surface-container-high)) !important;
              color: rgb(var(--c-on-surface)) !important;
            }
            body a[href*=/wallet/topup]:hover {
              background-color: rgb(var(--c-surface-container-highest)) !important;
            }
            body a[href*=/wallet/topup] .text-on-primary-container {
              color: rgb(var(--c-primary)) !important;
            }

            /* Ô gợi ý tìm kiếm searchSuggestions */
            body #searchSuggestions {
              background-color: rgb(var(--c-surface)) !important;
              border-color: rgb(var(--c-outline-variant)) !important;
              color: rgb(var(--c-on-surface)) !important;
            }
            body #searchSuggestions a:hover {
              background-color: rgb(var(--c-surface-container-high)) !important;
            }
            body #searchSuggestions .text-\\[\\#2B231D\\], body #searchSuggestions .text-\\[\\#2b2118\\] {
              color: rgb(var(--c-on-surface)) !important;
            }
            body #searchSuggestions .text-\\[\\#7D7065\\], body #searchSuggestions .text-\\[\\#8a7664\\] {
              color: rgb(var(--c-on-surface-variant)) !important;
            }

            /* Gradients trên trang chủ / leaderboard */
            body .from-\\[\\#c08d41\\], body .from-\\[\\#d4a054\\],
            body .from-\\[\\#FFF3EB\\], body .from-\\[\\#fffbf4\\] {
              --tw-gradient-from: rgb(var(--c-surface)) !important;
              --tw-gradient-to: rgb(var(--c-surface-container)) !important;
            }

            /* Custom scrollbar matching theme */
            ::-webkit-scrollbar {
              width: 8px;
            }
            ::-webkit-scrollbar-track {
              background: rgb(var(--c-surface-container));
            }
            ::-webkit-scrollbar-thumb {
              background: rgb(var(--c-outline-variant));
              border-radius: 4px;
            }
            ::-webkit-scrollbar-thumb:hover {
              background: rgb(var(--c-outline));
            }
            """);

            return sb.toString();
        } catch (Exception e) {
            log.warn("Lỗi format theme tokens CSS: {}", e.getMessage());
            return null;
        }
    }

    private String generateThemeBackgroundCss(String folderPath, boolean isDark) {
        if (folderPath == null || "theme-default".equals(folderPath)) {
            return null;
        }
        String bgKey = "web_theme_bg:" + folderPath;
        Optional<SystemSetting> bgSettingOpt = systemSettingRepository.findById(bgKey);
        if (bgSettingOpt.isEmpty() || bgSettingOpt.get().getSettingValue() == null || bgSettingOpt.get().getSettingValue().isBlank()) {
            return null;
        }

        try {
            ThemeBackgroundDTO bgDto = objectMapper.readValue(bgSettingOpt.get().getSettingValue(), ThemeBackgroundDTO.class);
            if (bgDto == null || bgDto.getUrl() == null || bgDto.getUrl().isBlank()) {
                return null;
            }

            String url = bgDto.getUrl().trim();
            String cloudName = (cloudinaryProperties != null && cloudinaryProperties.getCloudName() != null && !cloudinaryProperties.getCloudName().isBlank())
                    ? cloudinaryProperties.getCloudName().trim()
                    : "";

            if (!isValidCloudinaryUrl(url)) {
                String reason;
                if (!url.startsWith("https://res.cloudinary.com/")) {
                    reason = "URL không bắt đầu bằng https://res.cloudinary.com/";
                } else if (!cloudName.isBlank() && !url.startsWith("https://res.cloudinary.com/" + cloudName + "/")) {
                    reason = "cloud_name trong URL không khớp với cloud_name cấu hình dự án (" + cloudName + ")";
                } else if (!url.contains("/image/upload/")) {
                    reason = "URL thiếu tiền tố /image/upload/";
                } else if (!url.matches(".*\\.(jpg|jpeg|png|webp)$")) {
                    reason = "Đuôi file không thuộc định dạng cho phép (.jpg, .jpeg, .png, .webp)";
                } else {
                    reason = "URL chứa ký tự không hợp lệ hoặc không vượt qua biểu thức chính quy";
                }
                log.warn("Bỏ qua ảnh nền cho giao diện '{}': URL '{}' không hợp lệ. Lý do: {}", folderPath, url, reason);
                return null;
            }

            int opacity = bgDto.getOpacity() != null ? Math.max(5, Math.min(30, bgDto.getOpacity())) : 12;
            int blur = bgDto.getBlur() != null ? Math.max(0, Math.min(10, bgDto.getBlur())) : 4;
            double opacityFraction = opacity / 100.0;
            String blurRule = blur > 0 ? "  filter: blur(" + blur + "px);\n" : "";

            String urlHost = "res.cloudinary.com";
            try {
                java.net.URI uri = java.net.URI.create(url);
                if (uri.getHost() != null) {
                    urlHost = uri.getHost();
                }
            } catch (Exception ignored) {}

            log.info("Sinh CSS ảnh nền cho giao diện '{}': opacity={}%, blur={}px, URL host={}",
                    folderPath, opacity, blur, urlHost);

            StringBuilder sb = new StringBuilder();
            sb.append("/* Web Theme Background Image (UC-52) */\n");
            sb.append("html {\n");
            sb.append("  background-color: rgb(var(--c-background, 255 248 245));\n");
            sb.append("}\n");
            sb.append("body {\n");
            sb.append("  background: transparent !important;\n");
            sb.append("  position: relative;\n");
            sb.append("}\n");
            sb.append("body::before {\n");
            sb.append("  content: attr(data-theme);\n");
            sb.append("  position: fixed;\n");
            sb.append("  inset: 0;\n");
            sb.append("  z-index: -1;\n");
            sb.append("  pointer-events: none;\n");
            sb.append("  background: url(").append(url).append(") center/cover no-repeat;\n");
            sb.append("  opacity: ").append(String.format(Locale.US, "%.2f", opacityFraction)).append(";\n");
            sb.append(blurRule);
            sb.append("  transform: scale(1.05);\n");
            sb.append("}\n\n");

            if (isDark) {
                sb.append("/* Dark Theme Overlay: bao dam tuong phan toi thieu 4.5:1 */\n");
                sb.append("body::after {\n");
                sb.append("  content: attr(data-theme);\n");
                sb.append("  position: fixed;\n");
                sb.append("  inset: 0;\n");
                sb.append("  z-index: -1;\n");
                sb.append("  pointer-events: none;\n");
                sb.append("  background-color: rgb(var(--c-background, 18 18 18) / 0.65);\n");
                sb.append("}\n\n");
            }

            sb.append("/* Header bán trong suốt cho theme có ảnh nền */\n");
            sb.append("header.fixed.top-0.left-0.right-0.z-50.bg-surface\\/90 {\n");
            sb.append("  background-color: rgb(var(--c-surface, 255 248 245) / 0.88) !important;\n");
            sb.append("  backdrop-filter: blur(16px) !important;\n");
            sb.append("  -webkit-backdrop-filter: blur(16px) !important;\n");
            sb.append("}\n\n");

            sb.append("/* Làm trong suốt các wrapper full-page để lộ ảnh nền */\n");
            sb.append("body main,\n");
            sb.append("body main.bg-surface,\n");
            sb.append("body main.bg-background,\n");
            sb.append("body main[class*=bg-gradient-to-b],\n");
            sb.append("body section.bg-\\[\\#fdfaf7\\] {\n");
            sb.append("  background-color: transparent !important;\n");
            sb.append("  background-image: none !important;\n");
            sb.append("}\n");

            return sb.toString();
        } catch (Exception e) {
            log.warn("Lỗi phân tích JSON ảnh nền '{}': {}", bgKey, e.getMessage());
            return null;
        }
    }

    private String validateAndNormalizeTokensJson(String json) {
        if (json == null || json.isBlank()) {
            throw new IllegalArgumentException("Bộ mã màu giao diện không được để trống.");
        }
        Map<String, String> tokens;
        try {
            tokens = objectMapper.readValue(json, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            throw new IllegalArgumentException("Dữ liệu bộ mã màu tokens phải là chuỗi JSON hợp lệ: " + e.getMessage());
        }

        if (tokens == null || tokens.isEmpty()) {
            throw new IllegalArgumentException("Bộ mã màu giao diện không được để trống.");
        }

        // 1. Kiểm tra tối thiểu 6 token cơ bản bắt buộc
        for (String coreKey : CORE_REQUIRED_TOKENS) {
            if (!tokens.containsKey(coreKey) || tokens.get(coreKey) == null || tokens.get(coreKey).isBlank()) {
                throw new IllegalArgumentException("Bộ mã màu thiếu token cơ bản bắt buộc: " + coreKey);
            }
        }

        Map<String, String> normalizedTokens = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : tokens.entrySet()) {
            String key = entry.getKey().trim();
            if (THEME_TOKEN_WHITELIST.contains(key) && entry.getValue() != null && !entry.getValue().isBlank()) {
                String val = entry.getValue().trim();
                if (HEX_COLOR_PATTERN.matcher(val).matches()) {
                    normalizedTokens.put(key, val.toUpperCase());
                }
            }
        }

        String bg = normalizedTokens.get("background");
        String onBg = normalizedTokens.get("on_background");
        String surface = normalizedTokens.get("surface");
        String onSurface = normalizedTokens.get("on_surface");
        String primary = normalizedTokens.get("primary");
        String secondary = normalizedTokens.get("secondary");
        boolean isDark = calculateLuminance(bg) < 0.2;

        // 2. Tự động suy ra các token whitelist còn thiếu
        normalizedTokens.computeIfAbsent("surface_container_lowest", k -> isDark ? mixColors(bg, "#000000", 0.25) : "#FFFFFF");
        normalizedTokens.computeIfAbsent("surface_container", k -> mixColors(bg, onBg, 0.06));
        normalizedTokens.computeIfAbsent("surface_container_low", k -> mixColors(bg, normalizedTokens.get("surface_container"), 0.5));
        normalizedTokens.computeIfAbsent("surface_container_high", k -> mixColors(bg, onBg, 0.09));
        normalizedTokens.computeIfAbsent("surface_container_highest", k -> mixColors(bg, onBg, 0.12));
        normalizedTokens.computeIfAbsent("on_surface_variant", k -> mixColors(onBg, bg, 0.28));

        normalizedTokens.computeIfAbsent("primary_container", k -> {
            if (calculateContrast("#FFFFFF", primary) >= 4.5) return primary;
            for (double w = 0.05; w <= 0.8; w += 0.05) {
                String cand = mixColors(primary, "#000000", w);
                if (calculateContrast("#FFFFFF", cand) >= 4.5) return cand;
            }
            return primary;
        });

        normalizedTokens.computeIfAbsent("on_primary", k -> 
            calculateContrast("#FFFFFF", primary) >= calculateContrast("#121212", primary) ? "#FFFFFF" : "#121212"
        );
        normalizedTokens.computeIfAbsent("on_primary_container", k -> "#FFFFFF");
        normalizedTokens.computeIfAbsent("on_secondary", k -> 
            calculateContrast("#FFFFFF", secondary) >= calculateContrast("#121212", secondary) ? "#FFFFFF" : "#121212"
        );
        normalizedTokens.computeIfAbsent("outline", k -> mixColors(bg, onBg, 0.45));
        normalizedTokens.computeIfAbsent("outline_variant", k -> mixColors(bg, onBg, 0.20));

        // 3. Kiểm tra định dạng hex ^#[0-9A-Fa-f]{6}$ cho tất cả 18 token
        for (String key : THEME_TOKEN_WHITELIST) {
            String val = normalizedTokens.get(key);
            if (val == null || !HEX_COLOR_PATTERN.matcher(val).matches()) {
                throw new IllegalArgumentException("Mã màu của token '" + key + "' không đúng định dạng hex 6 ký tự (VD: #B91C1C). Nhận được: " + val);
            }
        }

        // 4. Kiểm tra độ tương phản >= 4.5:1 cho các cặp thực tế
        for (ContrastCheckPair pair : CONTRAST_PAIRS) {
            String fgHex = normalizedTokens.get(pair.fgKey());
            String bgHex = normalizedTokens.get(pair.bgKey());
            double ratio = calculateContrast(fgHex, bgHex);
            if (ratio < 4.5) {
                throw new IllegalArgumentException(String.format(
                        "Tỉ lệ tương phản của cặp '%s' là %.2f:1 (%s vs %s), không đạt chuẩn tối thiểu 4.5:1 (WCAG AA). Vui lòng chọn màu có độ tương phản cao hơn.",
                        pair.name(), ratio, fgHex, bgHex
                ));
            }
        }

        // 5. Kiểm tra tương phản giữa chữ trắng (#FFFFFF) và primary_container
        double whiteContrast = calculateContrast("#FFFFFF", normalizedTokens.get("primary_container"));
        if (whiteContrast < 4.5) {
            throw new IllegalArgumentException(String.format(
                    "Tỉ lệ tương phản giữa chữ trắng (#FFFFFF) và hộp chủ đạo 'primary_container' (%s) là %.2f:1, không đạt chuẩn tối thiểu 4.5:1 (WCAG AA).",
                    normalizedTokens.get("primary_container"), whiteContrast
            ));
        }

        // 6. Kiểm tra tương phản chữ thực tế trên secondary
        String realSecText = normalizedTokens.get("on_secondary");
        double secTextContrast = calculateContrast(realSecText, normalizedTokens.get("secondary"));
        if (secTextContrast < 4.5) {
            throw new IllegalArgumentException(String.format(
                    "Tỉ lệ tương phản giữa chữ '%s' và huy hiệu phụ 'secondary' (%s) là %.2f:1, không đạt chuẩn tối thiểu 4.5:1 (WCAG AA).",
                    realSecText, normalizedTokens.get("secondary"), secTextContrast
            ));
        }

        try {
            return objectMapper.writeValueAsString(normalizedTokens);
        } catch (Exception e) {
            throw new IllegalArgumentException("Lỗi chuyển đổi dữ liệu mã màu: " + e.getMessage());
        }
    }

    private String normalizeToHexTokensJson(String rawJson) {
        if (rawJson == null || rawJson.isBlank()) return "";
        try {
            Map<String, String> map = objectMapper.readValue(rawJson, new com.fasterxml.jackson.core.type.TypeReference<Map<String, String>>() {});
            Map<String, String> hexMap = new LinkedHashMap<>();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey().trim();
                if (key.startsWith("--c-")) {
                    key = key.substring(4).replace('-', '_');
                }
                String val = entry.getValue() != null ? entry.getValue().trim() : "";
                if (val.startsWith("#") && val.length() == 7) {
                    hexMap.put(key, val.toUpperCase());
                } else {
                    String[] parts = val.split("\\s+");
                    if (parts.length == 3) {
                        try {
                            int r = Integer.parseInt(parts[0]);
                            int g = Integer.parseInt(parts[1]);
                            int b = Integer.parseInt(parts[2]);
                            hexMap.put(key, String.format("#%02X%02X%02X", r, g, b));
                        } catch (Exception e) {
                            hexMap.put(key, val);
                        }
                    } else {
                        hexMap.put(key, val);
                    }
                }
            }
            return objectMapper.writeValueAsString(hexMap);
        } catch (Exception e) {
            return rawJson;
        }
    }

    private double calculateLuminance(String hex) {
        int r = Integer.parseInt(hex.substring(1, 3), 16);
        int g = Integer.parseInt(hex.substring(3, 5), 16);
        int b = Integer.parseInt(hex.substring(5, 7), 16);
        double rLin = (r / 255.0 <= 0.04045) ? (r / 255.0) / 12.92 : Math.pow(((r / 255.0) + 0.055) / 1.055, 2.4);
        double gLin = (g / 255.0 <= 0.04045) ? (g / 255.0) / 12.92 : Math.pow(((g / 255.0) + 0.055) / 1.055, 2.4);
        double bLin = (b / 255.0 <= 0.04045) ? (b / 255.0) / 12.92 : Math.pow(((b / 255.0) + 0.055) / 1.055, 2.4);
        return 0.2126 * rLin + 0.7152 * gLin + 0.0722 * bLin;
    }

    private double calculateContrast(String hex1, String hex2) {
        double l1 = calculateLuminance(hex1);
        double l2 = calculateLuminance(hex2);
        double max = Math.max(l1, l2);
        double min = Math.min(l1, l2);
        return (max + 0.05) / (min + 0.05);
    }
}
