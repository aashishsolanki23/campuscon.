package com.campuscon.dto.deed.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for deed settings requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeedSettingsRequest {
    private Boolean requireApprovalForRegistration;
    private Integer maxRegistrations;
    private Boolean allowWaitlist;
    private Boolean notifyOnRegistration;
}
