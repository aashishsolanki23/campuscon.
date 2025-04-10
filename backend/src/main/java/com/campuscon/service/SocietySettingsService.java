package com.campuscon.service;

import com.campuscon.dto.settings.SocietySettingsRequest;
import com.campuscon.dto.settings.SocietySettingsResponse;
import com.campuscon.exception.ResourceNotFoundException;
import com.campuscon.model.SocietyRole;
import com.campuscon.model.SocietySettings;
import com.campuscon.model.User;
import com.campuscon.repository.SocietyRoleRepository;
import com.campuscon.repository.SocietySettingsRepository;
import com.campuscon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing society settings
 */
@Service
@RequiredArgsConstructor
public class SocietySettingsService {

    private final SocietySettingsRepository societySettingsRepository;
    private final SocietyRoleRepository societyRoleRepository;
    private final UserRepository userRepository;

    /**
     * Get or create settings for a society
     */
    @Transactional
    public SocietySettings getOrCreateSocietySettings(Long societyId) {
        return societySettingsRepository.findBySocietyId(societyId)
                .orElseGet(() -> {
                    User society = userRepository.findById(societyId)
                            .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
                    
                    // Verify this is actually a society account
                    boolean isSociety = society.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_SOCIETY"));
                    if (!isSociety) {
                        throw new IllegalArgumentException("User is not a society");
                    }
                    
                    SocietySettings newSettings = SocietySettings.builder()
                            .society(society)
                            .build();
                    
                    return societySettingsRepository.save(newSettings);
                });
    }

    /**
     * Get society settings
     */
    public SocietySettingsResponse getSocietySettings(Long societyId) {
        SocietySettings settings = getOrCreateSocietySettings(societyId);
        
        return SocietySettingsResponse.builder()
                .autoApproveBonding(settings.isAutoApproveBonding())
                .allowMemberPosting(settings.isAllowMemberPosting())
                .requirePostApproval(settings.isRequirePostApproval())
                .build();
    }

    /**
     * Update society settings
     */
    @Transactional
    public SocietySettingsResponse updateSocietySettings(Long societyId, SocietySettingsRequest request) {
        SocietySettings settings = getOrCreateSocietySettings(societyId);
        
        settings.setAutoApproveBonding(request.isAutoApproveBonding());
        settings.setAllowMemberPosting(request.isAllowMemberPosting());
        settings.setRequirePostApproval(request.isRequirePostApproval());
        
        societySettingsRepository.save(settings);
        
        return SocietySettingsResponse.builder()
                .autoApproveBonding(settings.isAutoApproveBonding())
                .allowMemberPosting(settings.isAllowMemberPosting())
                .requirePostApproval(settings.isRequirePostApproval())
                .build();
    }

    /**
     * Get all society members with their roles
     */
    public List<SocietyRole> getSocietyMembers(Long societyId) {
        User society = userRepository.findById(societyId)
                .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
        
        return societyRoleRepository.findBySociety(society);
    }

    /**
     * Transfer presidency to another member
     */
    @Transactional
    public void transferPresidency(Long societyId, Long currentPresidentId, Long newPresidentId) {
        User society = userRepository.findById(societyId)
                .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
        
        User newPresident = userRepository.findById(newPresidentId)
                .orElseThrow(() -> new ResourceNotFoundException("New president user not found"));
        
        // Verify current president
        SocietyRole currentPresidentRole = societyRoleRepository.findBySocietyAndIsPresidentTrue(society)
                .orElseThrow(() -> new ResourceNotFoundException("Current president role not found"));
        
        if (!currentPresidentRole.getUser().getId().equals(currentPresidentId)) {
            throw new AccessDeniedException("Only the current president can transfer presidency");
        }
        
        // Find or create role for new president
        SocietyRole newPresidentRole = societyRoleRepository.findBySocietyAndUser(society, newPresident)
                .orElseGet(() -> {
                    return SocietyRole.builder()
                            .society(society)
                            .user(newPresident)
                            .roleName("Member")
                            .build();
                });
        
        // Update roles
        currentPresidentRole.setPresident(false);
        currentPresidentRole.setRoleName("Former President");
        currentPresidentRole.setCanModifySettings(false);
        
        newPresidentRole.setPresident(true);
        newPresidentRole.setRoleName("President");
        newPresidentRole.setCanPost(true);
        newPresidentRole.setCanApprovePosts(true);
        newPresidentRole.setCanManageMembers(true);
        newPresidentRole.setCanModifySettings(true);
        
        societyRoleRepository.save(currentPresidentRole);
        societyRoleRepository.save(newPresidentRole);
    }

    /**
     * Assign role to a member
     */
    @Transactional
    public SocietyRole assignRole(Long societyId, Long userId, String roleName, 
                               boolean canPost, boolean canApprovePosts, 
                               boolean canManageMembers, boolean canModifySettings) {
        User society = userRepository.findById(societyId)
                .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Check if role already exists
        Optional<SocietyRole> existingRole = societyRoleRepository.findBySocietyAndUser(society, user);
        
        SocietyRole role;
        if (existingRole.isPresent()) {
            role = existingRole.get();
            // Don't modify president flag if it exists
            boolean isPresident = role.isPresident();
            
            role.setRoleName(roleName);
            role.setCanPost(canPost);
            role.setCanApprovePosts(canApprovePosts);
            role.setCanManageMembers(canManageMembers);
            role.setCanModifySettings(canModifySettings);
            
            // Ensure president status doesn't change
            role.setPresident(isPresident);
        } else {
            role = SocietyRole.builder()
                    .society(society)
                    .user(user)
                    .roleName(roleName)
                    .canPost(canPost)
                    .canApprovePosts(canApprovePosts)
                    .canManageMembers(canManageMembers)
                    .canModifySettings(canModifySettings)
                    .isPresident(false) // New roles can't be president
                    .build();
        }
        
        return societyRoleRepository.save(role);
    }

    /**
     * Remove a member from society
     */
    @Transactional
    public void removeMember(Long societyId, Long userId) {
        User society = userRepository.findById(societyId)
                .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Find role if exists
        Optional<SocietyRole> roleOptional = societyRoleRepository.findBySocietyAndUser(society, user);
        if (roleOptional.isPresent()) {
            SocietyRole role = roleOptional.get();
            
            // Cannot remove president
            if (role.isPresident()) {
                throw new IllegalStateException("Cannot remove society president. Transfer presidency first.");
            }
            
            societyRoleRepository.delete(role);
        }
    }
}
