package com.campuscon.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for Deed creation and update requests.
 * Contains validation constraints for all required fields.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeedFormRequest {
    
    @NotBlank(message = "Category is required")
    private String category;
    
    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 20, message = "Title must be between 5 and 255 characters")
    private String title;
    
    @NotBlank(message = "Description is required")
    @Size(min = 10, max =300, message = "Description must be at least 10 characters")
    private String description;
    
    @NotBlank(message = "Venue is required")
    private String venue;
    
    private String url;
    
    private boolean isOpenForAll;
    
    private boolean isRegistrationOnly;
    
    @NotNull(message = "Start date time is required")
    private String startDateTime; // ISO 8601 format
    
    @NotNull(message = "End date time is required")
    private String endDateTime; // ISO 8601 format
    
    private boolean isTeamEvent;
    
    private Integer minTeamSize;
    
    private Integer maxTeamSize;
    
    @Min(value = 1, message = "Maximum registrations must be at least 1")
    private Integer maxRegistrations;
    
    @NotBlank(message = "Thumbnail URL is required")
    private String thumbnailUrl;
    
    private String firstPrize;
    
    private String secondPrize;
    
    private String thirdPrize;
    
    private boolean certificatesProvided;
    
    @Valid
    @Builder.Default
    private List<DeedRoundDto> rounds = new ArrayList<>();
}
