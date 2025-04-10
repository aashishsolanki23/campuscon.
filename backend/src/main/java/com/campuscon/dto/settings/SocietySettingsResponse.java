package com.campuscon.dto.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for society management settings responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocietySettingsResponse {
    private boolean autoApproveBonding;
    private boolean allowMemberPosting;
    private boolean requirePostApproval;
}
