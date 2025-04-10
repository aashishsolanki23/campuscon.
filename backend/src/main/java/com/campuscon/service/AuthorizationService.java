package com.campuscon.service;

import com.campuscon.exception.UnauthorizedException;
import com.campuscon.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service responsible for authorization checks related to content creation and manipulation
 * Enforces CampusCon's role-based content creation rules:
 * - Students can only create bricks
 * - Societies can create both bricks and deeds
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationService {

    private final UserService userService;
    
    /**
     * Checks if a user is authorized to create a brick
     * Both students and societies can create bricks
     * 
     * @param userId The ID of the user attempting to create a brick
     * @throws UnauthorizedException if the user is not authorized
     */
    public void checkBrickCreationPermission(Long userId) {
        // Both students and societies can create bricks, so no additional checks needed
        // Just verify the user exists
        userService.getUserById(userId);
    }
    
    /**
     * Checks if a user is authorized to create a deed
     * Only societies can create deeds
     * 
     * @param userId The ID of the user attempting to create a deed
     * @throws UnauthorizedException if the user is not authorized
     */
    public void checkDeedCreationPermission(Long userId) {
        User user = userService.getUserById(userId);
        
        if (user.getRole() != User.UserRole.SOCIETY) {
            log.warn("User {} attempted to create a deed but is not a society", userId);
            throw new UnauthorizedException("Only societies can create deeds");
        }
    }
    
    /**
     * Checks if the user has permission to modify the specified brick
     * 
     * @param userId The ID of the user attempting to modify the brick
     * @param creatorId The ID of the brick creator
     * @throws UnauthorizedException if the user is not authorized
     */
    public void checkBrickModificationPermission(Long userId, Long creatorId) {
        User user = userService.getUserById(userId);
        
        // Allow if the user is the creator or an admin
        if (!userId.equals(creatorId) && user.getRole() != User.UserRole.ADMIN) {
            log.warn("User {} attempted to modify brick created by {}", userId, creatorId);
            throw new UnauthorizedException("You don't have permission to modify this brick");
        }
    }
    
    /**
     * Checks if the user has permission to modify the specified deed
     * 
     * @param userId The ID of the user attempting to modify the deed
     * @param creatorId The ID of the deed creator
     * @throws UnauthorizedException if the user is not authorized
     */
    public void checkDeedModificationPermission(Long userId, Long creatorId) {
        User user = userService.getUserById(userId);
        
        // Allow if the user is the creator society or an admin
        if (!userId.equals(creatorId) && user.getRole() != User.UserRole.ADMIN) {
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
        User user = userService.getUserById(userId);
        
        // Only the creator society or an admin can enable registration
        if (!userId.equals(creatorId) && user.getRole() != User.UserRole.ADMIN) {
            log.warn("User {} attempted to modify registration settings for deed created by {}", userId, creatorId);
            throw new UnauthorizedException("Only the society that created this deed can enable registration");
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
        User user = userService.getUserById(userId);
        
        // Only the creator society or an admin can view registrations
        if (!userId.equals(creatorId) && user.getRole() != User.UserRole.ADMIN) {
            log.warn("User {} attempted to view registrations for deed created by {}", userId, creatorId);
            throw new UnauthorizedException("Only the society that created this deed can view registrations");
        }
    }
}
