package com.campuscon.dto.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for account security settings responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecuritySettingsResponse {
    private boolean twoFactorAuthEnabled;
    private String lastPasswordChangeDate;
    private String lastLoginDate;
}
