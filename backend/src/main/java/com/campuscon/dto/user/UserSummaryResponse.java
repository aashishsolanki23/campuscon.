package com.campuscon.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning summarized user information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {
    
    private Long id;
    private String username;
    private String name;
    private String profilePictureUrl;
    // Removed societyRole field as part of unified user model
    private Long bondsCount;
    // likesCount field has been removed
    private String role; // User role (USER, ADMIN, MODERATOR)
}
