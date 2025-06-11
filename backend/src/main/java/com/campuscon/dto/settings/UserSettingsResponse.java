package com.campuscon.dto.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user management settings responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettingsResponse {
    private boolean autoApproveRequests;
    private boolean allowFollowerPosting;
    private boolean requirePostApproval;
    private boolean isOrganizer;
}
