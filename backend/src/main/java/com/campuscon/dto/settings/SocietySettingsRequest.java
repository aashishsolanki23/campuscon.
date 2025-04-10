package com.campuscon.dto.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for society management settings requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocietySettingsRequest {
    private boolean autoApproveBonding;
    private boolean allowMemberPosting;
    private boolean requirePostApproval;
}
