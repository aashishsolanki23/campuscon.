package com.campuscon.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import java.util.Arrays;

/**
 * Service for authorization and security checks using the unified user model
 */
@Service
@RequiredArgsConstructor
public class SecurityService {

    // No repository dependencies needed

    /**
     * Check if a user is the owner of or has specific relationship with an entity
     */
    public boolean isOwnerOfResource(Long resourceOwnerId, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof UserPrincipal)) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Resource owner can access its own resources
        return resourceOwnerId.equals(userPrincipal.getId());
    }
    
    /**
     * Check if user has specific roles or userTypes
     */
    public boolean hasUserTypes(Authentication authentication, String... requiredTypes) {
        if (!(authentication.getPrincipal() instanceof UserPrincipal)) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        if (userPrincipal.getUserTypes() == null || userPrincipal.getUserTypes().isEmpty()) {
            return false;
        }
        
        // Check if user has any of the required types
        return Arrays.stream(requiredTypes)
                .anyMatch(type -> userPrincipal.getUserTypes().contains(type));
    }
    
    /**
     * Check if user has permission to act on a resource based on their userTypes and relationship
     */
    public boolean hasPermission(Long resourceOwnerId, Authentication authentication, String... permissionRoles) {
        if (!(authentication.getPrincipal() instanceof UserPrincipal)) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Resource owner always has permission
        if (resourceOwnerId.equals(userPrincipal.getId())) {
            return true;
        }
        
        // Check if user has any of the required permission roles
        return hasUserTypes(authentication, permissionRoles);
    }
    
    /**
     * Check if user can edit content created by another user
     */
    public boolean canManageContent(Long contentCreatorId, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof UserPrincipal)) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Creator can manage their own content
        if (contentCreatorId.equals(userPrincipal.getId())) {
            return true;
        }
        
        // Admins can manage all content
        return userPrincipal.getUserTypes() != null &&
               userPrincipal.getUserTypes().contains("ADMIN");
    }
    
    /**
     * Check if user has organizational management permissions
     */
    public boolean canManageOrganization(Long organizationId, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof UserPrincipal)) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Organization owner can manage the organization
        if (organizationId.equals(userPrincipal.getId())) {
            return true;
        }
        
        // Check for specific management roles
        return userPrincipal.getUserTypes() != null &&
               (userPrincipal.getUserTypes().contains("ADMIN") ||
                userPrincipal.getUserTypes().contains("ORG_MANAGER") ||
                ("PRESIDENT".equals(userPrincipal.getOrganizationRole()) && 
                 userPrincipal.getUserTypes().contains("ORG_MEMBER")));
    }
}
