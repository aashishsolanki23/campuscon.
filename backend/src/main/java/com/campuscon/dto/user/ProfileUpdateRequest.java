package com.campuscon.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for updating user profile information
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileUpdateRequest {
    private String username;
    private String displayName;
    private String bio;
    private String profilePictureUrl;
    
    // Custom URLs map (name -> url)  
    private Map<String, String> customUrls;
}
