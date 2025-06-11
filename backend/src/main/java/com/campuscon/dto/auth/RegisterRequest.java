package com.campuscon.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    // Core fields - required for traditional auth
    private String username;  // Required for traditional auth, generated for Google auth

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String password;  // Required for traditional auth, not needed for Google auth
    private String confirmPassword;  // Required for traditional auth, not needed for Google auth
    
    @NotBlank(message = "Name is required")
    private String displayName;  // Full name of the user
    
    // College information
    private String collegeName;  // Name of the user's college
    
    // Contact information
    private String mobileNumber;  // Added for user contact
    
    // Profile information
    private String profilePictureUrl;
    private String bio;
    
    // Custom user URLs
    private Map<String, String> customUrls = new HashMap<String, String>();
    
    // Authentication method
    private boolean googleAuth = false;  // Flag to indicate if user signed up with Google
    
    // User types (system will set default USER type)
    private List<String> userTypes;
}

