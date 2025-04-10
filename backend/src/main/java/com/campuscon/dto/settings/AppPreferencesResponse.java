package com.campuscon.dto.settings;

import com.campuscon.model.Settings.ThemePreference;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for app preferences settings responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppPreferencesResponse {
    private ThemePreference themePreference;
    private String language;
    private boolean autoLoadMedia;
}
