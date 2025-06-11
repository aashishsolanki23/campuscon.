package com.campuscon.dto.bond;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for returning bond request information
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BondRequestResponse {
    private Long bondId;
    private Long requesterId;
    private String requesterUsername;
    private String requesterDisplayName;
    private String requesterProfilePictureUrl;
    private LocalDateTime requestDate;
}
