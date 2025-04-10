package com.campuscon.dto.deed.registration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for deed registration requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeedRegistrationRequest {
    
    @NotNull(message = "Deed ID is required")
    private Long deedId;
    
    @Size(max = 100, message = "Team name must be less than 100 characters")
    private String teamName;
    
    private Integer teamSize;
    
    @Size(max = 1000, message = "Additional info must be less than 1000 characters")
    private String additionalInfo;
}
