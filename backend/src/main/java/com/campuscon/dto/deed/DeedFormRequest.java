package com.campuscon.dto.deed;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for deed creation and update requests
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeedFormRequest {
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must be less than 255 characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    private String description;
    
    @NotBlank(message = "Venue is required")
    @Size(max = 255, message = "Venue must be less than 255 characters")
    private String venue;
    
    @NotBlank(message = "Category is required")
    private String category;
    
    private String url;
    
    @NotBlank(message = "Start date and time is required")
    private String startDateTime; // ISO 8601 format
    
    @NotBlank(message = "End date and time is required")
    private String endDateTime; // ISO 8601 format
    
    @Builder.Default
    private boolean openForAll = true;
    
    @Builder.Default
    private boolean registrationOnly = false;
    
    @Builder.Default
    private boolean teamEvent = false;
    
    @Builder.Default
    private int minTeamSize = 1;
    
    @Builder.Default
    private int maxTeamSize = 10;
    
    private String eligibilityCriteria;
    
    @Builder.Default
    private int maxRegistrations = 0; // 0 means unlimited
    
    @Builder.Default
    private boolean requireApproval = false;
    
    private String additionalFields;
    
    // Additional fields required by DeedFormService
    private String thumbnailUrl;
    private String firstPrize;
    private String secondPrize;
    private String thirdPrize;
    
    @Builder.Default
    private boolean certificatesProvided = false;
    
    // For competition rounds
    private List<DeedRoundDto> rounds;
}
