package com.campuscon.service;

import com.campuscon.dto.user.ChangeUsernameRequest;
import com.campuscon.exception.ResourceNotFoundException;

import com.campuscon.model.User;
import com.campuscon.repository.ChatGroupRepository;
import com.campuscon.repository.DeedRepository;

import com.campuscon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



/**
 * Service for managing user accounts, including username changes and account deletion
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserAccountService {

    private final UserRepository userRepository;

    private final DeedRepository deedRepository;
    private final ChatGroupRepository chatGroupRepository;
    
    /**
     * Change the username of a user
     * 
     * @param userId The user ID
     * @param request The change username request
     * @return The updated user
     */
    @Transactional
    public User changeUsername(Long userId, ChangeUsernameRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Update the username
        user.setUsername(request.getNewUsername());
        
        // Save and return the updated user
        return userRepository.save(user);
    }
    
    /**
     * Update a user's organization role or display name
     * 
     * @param userId The user ID
     * @param request The change username request
     * @return The updated user
     */
    @Transactional
    public User updateUserOrganizationInfo(Long userId, ChangeUsernameRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Update display name for all users
        // This provides flexibility regardless of user type
        user.setDisplayName(request.getNewUsername());
        
        // Save and return the updated user
        return userRepository.save(user);
    }
    
    /**
     * Delete a user account and all associated data
     * 
     * @param userId The user ID to delete
     * @return true if deletion was successful
     */
    @Transactional
    public boolean deleteUserAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Step 1: InGroup system removed
        
        // Step 2: Delete all deeds created by this user
        deedRepository.deleteAllByCreatorId(userId);
        
        // Step 3: Delete user from chat groups
        chatGroupRepository.removeUserFromAllGroups(userId);
        
        // Step 4: Delete the user
        userRepository.delete(user);
        
        log.info("User account with ID {} successfully deleted", userId);
        return true;
    }
}
