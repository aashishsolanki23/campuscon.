package com.campuscon.dto.deed;
import com.campuscon.model.Deed;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeedResponse {
    private Long id;
    private String title;
    private String description;
    private String bannerUrl;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime eventDate;
    
    private String venue;
    private String category;
    private Long creatorId;
    private String creatorName;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    // likesCount removed
    private Integer commentsCount;
    private Integer savesCount;
    private Integer sharesCount;
    // liked status removed
    private Boolean saved;
    
    // Registration related fields
    private Boolean registrationEnabled;
    private Integer registrationsCount;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startDateTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDateTime;
    
    /**
     * Convert a Deed entity to DeedResponse DTO
     * 
     * @param deed The deed entity
     * @return The deed response DTO
     */
    public static DeedResponse fromEntity(Deed deed) {
        return DeedResponse.builder()
                .id(deed.getId())
                .title(deed.getTitle())
                .description(deed.getDescription())
                .bannerUrl(deed.getBannerUrl())
                .eventDate(deed.getStartDateTime()) // For backward compatibility
                .venue(deed.getVenue())
                .category(deed.getCategoryDisplayName())
                .creatorId(deed.getCreator().getId())
                .creatorName(deed.getCreator().getUsername())
                .createdAt(deed.getCreatedAt())
                .commentsCount(deed.getComments() != null ? Integer.valueOf(deed.getComments().size()) : 0)
                .savesCount(Integer.valueOf(0)) // Default to 0 if saves count method doesn't exist
                .sharesCount(Integer.valueOf(0)) // Default to 0 if shares count method doesn't exist
                .registrationEnabled(deed.isRegistrationEnabled())
                // If getRegistrationsCount() doesn't exist, calculate from registrations if available
                .registrationsCount(deed.getRegistrations() != null ? Integer.valueOf(deed.getRegistrations().size()) : 0)
                .startDateTime(deed.getStartDateTime())
                .endDateTime(deed.getEndDateTime())
                .build();
    }
}
