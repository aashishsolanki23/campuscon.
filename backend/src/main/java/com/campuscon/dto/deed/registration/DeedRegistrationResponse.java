package com.campuscon.dto.deed.registration;

// Removed DeedRegistration import as it's no longer needed
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for deed registration responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeedRegistrationResponse {
    
    private Long id;
    private Long deedId;
    private String deedTitle;
    private Long userId;
    private String username;
    private LocalDateTime registeredAt;
    private String teamName;
    private Integer teamSize;
    private String additionalInfo;
}
