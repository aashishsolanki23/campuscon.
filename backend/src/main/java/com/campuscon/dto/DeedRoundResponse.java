package com.campuscon.dto;

import com.campuscon.model.DeedRound;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for DeedRound responses.
 * Contains round details for API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeedRoundResponse {
    private Long id;
    private String roundName;
    private String roundUrl;
    private LocalDateTime roundDateTime;
    private String roundVenue;
    private String roundDescription;
    
    /**
     * Static factory method to convert a DeedRound entity to a DeedRoundResponse DTO
     * 
     * @param round The DeedRound entity to convert
     * @return DeedRoundResponse DTO
     */
    public static DeedRoundResponse fromEntity(DeedRound round) {
        return DeedRoundResponse.builder()
                .id(round.getId())
                .roundName(round.getRoundName())
                .roundUrl(round.getRoundUrl())
                .roundDateTime(round.getRoundDateTime())
                .roundVenue(round.getRoundVenue())
                .roundDescription(round.getRoundDescription())
                .build();
    }
}
