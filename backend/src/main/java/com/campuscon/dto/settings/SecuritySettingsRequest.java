package com.campuscon.dto.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for account security settings requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecuritySettingsRequest {
    private boolean twoFactorAuthEnabled;
    private String currentPassword;
    private String newPassword;
}
