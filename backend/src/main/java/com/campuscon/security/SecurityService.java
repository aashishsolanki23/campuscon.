package com.campuscon.security;

import com.campuscon.model.SocietyRole;
import com.campuscon.repository.SocietyRoleRepository;
import com.campuscon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Service for authorization and security checks
 */
@Service
@RequiredArgsConstructor
public class SecurityService {

    private final SocietyRoleRepository societyRoleRepository;
    private final UserRepository userRepository;

    /**
     * Check if user is a member of the society
     */
    public boolean isSocietyMember(Long societyId, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof UserPrincipal)) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Society owner can access its own settings
        if (societyId.equals(userPrincipal.getId())) {
            return true;
        }
        
        // Check if user has a role in the society
        return societyRoleRepository.findBySocietyAndUser(
                userRepository.findById(societyId).orElseThrow(),
                userRepository.findById(userPrincipal.getId()).orElseThrow()
        ).isPresent();
    }

    /**
     * Check if user can modify society settings
     */
    public boolean canModifySocietySettings(Long societyId, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof UserPrincipal)) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Society owner can modify its own settings
        if (societyId.equals(userPrincipal.getId())) {
            return true;
        }
        
        // Check if user has permission to modify settings
        return societyRoleRepository.findBySocietyAndUser(
                userRepository.findById(societyId).orElseThrow(),
                userRepository.findById(userPrincipal.getId()).orElseThrow()
        ).map(SocietyRole::isCanModifySettings).orElse(false);
    }

    /**
     * Check if user is the society president
     */
    public boolean isSocietyPresident(Long societyId, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof UserPrincipal)) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Society owner is considered the president
        if (societyId.equals(userPrincipal.getId())) {
            return true;
        }
        
        // Check if user is the president
        return societyRoleRepository.findBySocietyAndUser(
                userRepository.findById(societyId).orElseThrow(),
                userRepository.findById(userPrincipal.getId()).orElseThrow()
        ).map(SocietyRole::isPresident).orElse(false);
    }

    /**
     * Check if user can manage society members
     */
    public boolean canManageSocietyMembers(Long societyId, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof UserPrincipal)) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Society owner can manage members
        if (societyId.equals(userPrincipal.getId())) {
            return true;
        }
        
        // Check if user has permission to manage members
        return societyRoleRepository.findBySocietyAndUser(
                userRepository.findById(societyId).orElseThrow(),
                userRepository.findById(userPrincipal.getId()).orElseThrow()
        ).map(SocietyRole::isCanManageMembers).orElse(false);
    }

    /**
     * Check if user can post content for society
     */
    public boolean canPostForSociety(Long societyId, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof UserPrincipal)) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Society owner can post content
        if (societyId.equals(userPrincipal.getId())) {
            return true;
        }
        
        // Check if user has permission to post
        return societyRoleRepository.findBySocietyAndUser(
                userRepository.findById(societyId).orElseThrow(),
                userRepository.findById(userPrincipal.getId()).orElseThrow()
        ).map(SocietyRole::isCanPost).orElse(false);
    }

    /**
     * Check if user can approve posts for society
     */
    public boolean canApprovePostsForSociety(Long societyId, Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof UserPrincipal)) {
            return false;
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Society owner can approve posts
        if (societyId.equals(userPrincipal.getId())) {
            return true;
        }
        
        // Check if user has permission to approve posts
        return societyRoleRepository.findBySocietyAndUser(
                userRepository.findById(societyId).orElseThrow(),
                userRepository.findById(userPrincipal.getId()).orElseThrow()
        ).map(SocietyRole::isCanApprovePosts).orElse(false);
    }
}
