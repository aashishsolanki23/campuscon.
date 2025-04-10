package com.campuscon.dto.deed.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for deed settings responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeedSettingsResponse {
    private Boolean requireApprovalForRegistration;
    private Integer maxRegistrations;
    private Boolean allowWaitlist;
    private Boolean notifyOnRegistration;
    private Integer currentRegistrationsCount;
    private Integer waitlistCount;
}
