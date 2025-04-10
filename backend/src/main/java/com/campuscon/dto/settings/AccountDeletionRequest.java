package com.campuscon.dto.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for account deletion requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountDeletionRequest {
    private String password;
    private String otpCode;
    private String reason;
}
