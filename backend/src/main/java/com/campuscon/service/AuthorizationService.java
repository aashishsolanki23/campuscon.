package com.campuscon.service;

import com.campuscon.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for authorization checks related to content creation and manipulation
 * Enforces CampusCon's permission rules for creating and managing deeds
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {

    private final UserService userService;
    
    // Brick creation permission check has been removed
    
    /**
     * Checks if a user is authorized to create a deed
     * Any authenticated user can create deeds
     * 
     * @param userId The ID of the user attempting to create a deed
     * @throws UnauthorizedException if the user is not authorized
     */
    public void checkDeedCreationPermission(Long userId) {
        // Verify the user exists - will throw exception if not found
        userService.getUserById(userId);
        
        // All authenticated users can create deeds now
        // No additional authorization check needed
    }
    
    // Brick modification permission check has been removed
    
    /**
     * Checks if the user has permission to modify the specified deed
     * 
     * @param userId The ID of the user attempting to modify the deed
     * @param creatorId The ID of the deed creator
     * @throws UnauthorizedException if the user is not authorized
     */
    public void checkDeedModificationPermission(Long userId, Long creatorId) {
        // Verify the user exists
        userService.getUserById(userId);
        
        // Allow if the user is the creator
        if (!userId.equals(creatorId)) {
            log.warn("User {} attempted to modify deed created by {}", userId, creatorId);
            throw new UnauthorizedException("You don't have permission to modify this deed");
        }
    }
    
    /**
     * Checks if the user has permission to enable registration for a deed
     * Only the society that created the deed can enable registration
     * 
     * @param userId The ID of the user attempting to enable registration
     * @param creatorId The ID of the deed creator
     * @throws UnauthorizedException if the user is not authorized
     */
    public void checkDeedRegistrationEnablePermission(Long userId, Long creatorId) {
        // Verify the user exists
        userService.getUserById(userId);
        
        // Only the creator can enable registration
        if (!userId.equals(creatorId)) {
            log.warn("User {} attempted to modify registration settings for deed created by {}", userId, creatorId);
            throw new UnauthorizedException("Only the user that created this deed can enable registration");
        }
    }
    
    /**
     * Checks if the user has permission to view or manage registrations for a deed
     * Only the society that created the deed can view or manage registrations
     * 
     * @param userId The ID of the user attempting to view registrations
     * @param creatorId The ID of the deed creator
     * @throws UnauthorizedException if the user is not authorized
     */
    public void checkDeedRegistrationViewPermission(Long userId, Long creatorId) {
        // Verify the user exists
        userService.getUserById(userId);
        
        // Only the creator can view registrations
        if (!userId.equals(creatorId)) {
            log.warn("User {} attempted to view registrations for deed created by {}", userId, creatorId);
            throw new UnauthorizedException("Only the user that created this deed can view registrations");
        }
    }
}
