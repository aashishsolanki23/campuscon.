package com.campuscon.dto.deed;

import com.campuscon.model.DeedRound;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.format.DateTimeFormatter;

/**
 * DTO for deed round information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeedRoundDto {

    private Long id;
    
    @NotBlank(message = "Round name is required")
    private String roundName;
    
    @NotNull(message = "Round number is required")
    private Integer roundNumber;
    
    private String roundUrl;
    
    @NotBlank(message = "Round date time is required")
    private String roundDateTime; // ISO 8601 format
    
    private String roundVenue;
    
    private String roundDescription;
    
    /**
     * Convert a DeedRound entity to DeedRoundDto
     * 
     * @param round The deed round entity
     * @return The deed round DTO
     */
    public static DeedRoundDto fromEntity(DeedRound round) {
        if (round == null) {
            return null;
        }
        
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        
        return DeedRoundDto.builder()
                .id(round.getId())
                .roundName(round.getRoundName())
                .roundNumber(round.getRoundNumber())
                .roundUrl(round.getRoundUrl())
                .roundDateTime(round.getRoundDateTime() != null ? round.getRoundDateTime().format(formatter) : null)
                .roundVenue(round.getRoundVenue())
                .roundDescription(round.getRoundDescription())
                .build();
    }
}
