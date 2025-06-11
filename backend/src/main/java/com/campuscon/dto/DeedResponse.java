package com.campuscon.dto;
import com.campuscon.model.Deed;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for Deed responses.
 * Contains all deed details for API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeedResponse {
    private Long id;
    private String category;
    private String title;
    private String description;
    private String venue;
    private String url;
    private boolean isOpenForAll;
    private boolean isRegistrationOnly;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private boolean isTeamEvent;
    private Integer minTeamSize;
    private Integer maxTeamSize;
    private Integer maxRegistrations;
    private String thumbnailUrl;
    private String bannerUrl;
    private String firstPrize;
    private String secondPrize;
    private String thirdPrize;
    private boolean certificatesProvided;
    private Long creatorId;
    private String creatorName;
    // likesCount removed
    private long commentsCount;
    private long registrationsCount;
    private boolean isApproved;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<DeedRoundResponse> rounds;
    
    /**
     * Static factory method to convert a Deed entity to a DeedResponse DTO
     * 
     * @param deed The Deed entity to convert
     * @return DeedResponse DTO
     */
    public static DeedResponse fromEntity(Deed deed) {
        return DeedResponse.builder()
                .id(deed.getId())
                .category(deed.getCategoryDisplayName())
                .title(deed.getTitle())
                .description(deed.getDescription())
                .venue(deed.getVenue())
                .url(deed.getUrl())
                .isOpenForAll(deed.isOpenForAll())
                .isRegistrationOnly(deed.isRegistrationOnly())
                .startDateTime(deed.getStartDateTime())
                .endDateTime(deed.getEndDateTime())
                .isTeamEvent(deed.isTeamEvent())
                .minTeamSize(deed.getMinTeamSize())
                .maxTeamSize(deed.getMaxTeamSize())
                .maxRegistrations(deed.getMaxRegistrations())
                .thumbnailUrl(deed.getThumbnailUrl())
                .bannerUrl(deed.getBannerUrl())
                .firstPrize(deed.getFirstPrize())
                .secondPrize(deed.getSecondPrize())
                .thirdPrize(deed.getThirdPrize())
                .certificatesProvided(deed.isCertificatesProvided())
                .creatorId(deed.getCreator().getId())
                .creatorName(deed.getCreator().getUsername())
                // likesCount removed
                .commentsCount(deed.getCommentsCount())
                .registrationsCount(deed.getRegistrations().size())
                .isApproved(deed.isApproved())
                .isActive(true) // Active by default when retrieving
                .createdAt(deed.getCreatedAt())
                .updatedAt(deed.getUpdatedAt())
                .rounds(deed.getRounds().stream()
                        .map(DeedRoundResponse::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }
    
    /**
     * Convert a list of Deed entities to a list of DeedResponse DTOs
     * 
     * @param deeds List of Deed entities
     * @return List of DeedResponse DTOs
     */
    public static List<DeedResponse> fromEntities(List<Deed> deeds) {
        return deeds.stream()
                .map(DeedResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
