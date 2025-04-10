package com.campuscon.dto.deed.registration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for deed registration counts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeedRegistrationCountsResponse {
    
    private long totalCount;
    private long pendingCount;
    private long approvedCount;
    private long rejectedCount;
}
