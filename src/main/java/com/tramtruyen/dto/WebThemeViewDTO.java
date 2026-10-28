package com.tramtruyen.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebThemeViewDTO {
    private Integer id;
    private String name;
    private String folderPath;
    private Boolean isActive;
    private Integer uploadedBy;
    private String uploaderName;
    private LocalDateTime updatedAt;
    private String formattedUpdatedAt;
    private String tokensJson;
    private java.util.Map<String, String> tokenMap;
    private String bgUrl;
    private Integer bgOpacity;
    private Integer bgBlur;

    public boolean hasBackground() {
        return bgUrl != null && !bgUrl.isBlank();
    }

    public String getToken(String key, String fallback) {
        if (tokenMap != null && tokenMap.containsKey(key)) {
            String val = tokenMap.get(key);
            if (val != null && !val.isBlank()) {
                return val.trim();
            }
        }
        return fallback;
    }
}
