package com.campuscon.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for user profile information
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private Long id;
    private String username;
    private String displayName;
    private String email;
    private String bio;
    private String profilePictureUrl;
    
    // College information
    private String collegeName;
    
    // Custom URLs (name -> url mapping)
    private Map<String, String> customUrls;
    
    // Stats
    private Long bondCount;
    private Long registrationCount;
    
    // Email verification status
    private boolean isVerified;
}
