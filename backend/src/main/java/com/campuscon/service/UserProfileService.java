package com.campuscon.service;

import com.campuscon.dto.user.ProfileUpdateRequest;
import com.campuscon.dto.user.UserProfileResponse;
import com.campuscon.exception.ResourceNotFoundException;
import com.campuscon.model.User;
import com.campuscon.model.UserCustomUrl;
import com.campuscon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing user profiles
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BondService bondService;
    private final DeedRegistrationService deedRegistrationService;
    private final UserCustomUrlService userCustomUrlService;


    /**
     * Get a user's profile
     *
     * @param userId The ID of the user to get the profile for
     * @return The user's profile response
     */
    public UserProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        return mapToProfileResponse(user);
    }

    /**
     * Update a user's profile
     *
     * @param userId The ID of the user to update
     * @param request The profile update request
     * @return The updated user profile
     */
    @Transactional
    public UserProfileResponse updateProfile(Long userId, ProfileUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Update basic profile information
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            // Check if username is already taken
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username is already taken");
            }
            user.setUsername(request.getUsername());
        }
        
        if (request.getDisplayName() != null) {
            user.setDisplayName(request.getDisplayName());
        }
        
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        
        if (request.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(request.getProfilePictureUrl());
        }
        
        // Update custom URLs if provided
        if (request.getCustomUrls() != null && !request.getCustomUrls().isEmpty()) {
            userCustomUrlService.saveUserCustomUrls(user, request.getCustomUrls());
        }
        
        // Save the user
        User updatedUser = userRepository.save(user);
        
        return mapToProfileResponse(updatedUser);
    }
    
    /**
     * Change a user's password
     *
     * @param userId The ID of the user
     * @param currentPassword The current password
     * @param newPassword The new password
     * @return True if the password was changed successfully
     */
    @Transactional
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new AccessDeniedException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return true;
    }
    
    /**
     * Permanently delete a user's account
     *
     * @param userId The ID of the user to delete
     * @param password The user's password for verification
     */
    @Transactional
    public void deleteAccount(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Verify password for security
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AccessDeniedException("Password is incorrect");
        }
        
        // Clean up all associated data
        // 1. Remove all bonds
        bondService.removeAllBondsForUser(userId);
        
        // 2. Remove user from all registrations
        deedRegistrationService.removeUserFromAllRegistrations(userId);
        
        // 3. InGroup system removed
        
        // 4. Finally delete the user
        userRepository.delete(user);
        
        log.info("User account deleted: {}", userId);
    }
    
    /**
     * Map a User entity to a UserProfileResponse DTO
     *
     * @param user The user entity
     * @return The user profile response
     */
    private UserProfileResponse mapToProfileResponse(User user) {
        UserProfileResponse response = new UserProfileResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setDisplayName(user.getDisplayName());
        response.setEmail(user.getEmail());
        response.setBio(user.getBio());
        response.setProfilePictureUrl(user.getProfilePictureUrl());
        
        // Add college info if available
        response.setCollegeName(user.getCollegeName());
        
        // Add custom URLs
        Map<String, String> customUrls = userCustomUrlService.getCustomUrlsByUserId(user.getId()).stream()
                .collect(Collectors.toMap(UserCustomUrl::getUrlName, UserCustomUrl::getUrl));
        response.setCustomUrls(customUrls);
        
        // Add count of bonds and registrations
        response.setBondCount(bondService.countBondsForUser(user.getId()));
        response.setRegistrationCount(deedRegistrationService.countRegistrationsForUser(user.getId()));
        
        return response;
    }
}
