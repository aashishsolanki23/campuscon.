package com.campuscon.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for event rounds in the Deed form.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeedRoundDto {
    
    @NotBlank(message = "Round name is required")
    private String roundName;
    
    private String roundUrl;
    
    private String roundDateTime; // ISO 8601 format
    
    private String roundVenue;
    
    private String roundDescription;
}
