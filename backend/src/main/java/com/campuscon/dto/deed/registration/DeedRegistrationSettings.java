package com.campuscon.dto.deed.registration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for managing deed registration settings
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeedRegistrationSettings {
    
    @NotNull(message = "Registration enabled flag is required")
    private Boolean registrationEnabled;
    
    @Size(max = 1000, message = "Eligibility criteria must be less than 1000 characters")
    private String eligibilityCriteria;
    
    private Integer maxRegistrations;
    
    private Boolean requireApproval;
    
    private Boolean allowTeamRegistration;
    
    private Integer maxTeamSize;
    
    @Size(max = 500, message = "Additional fields configuration must be less than 500 characters")
    private String additionalFieldsConfig;
}
