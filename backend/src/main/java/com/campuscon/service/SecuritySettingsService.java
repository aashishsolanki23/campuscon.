package com.campuscon.service;

import com.campuscon.dto.settings.AccountDeletionRequest;
import com.campuscon.dto.settings.SecuritySettingsRequest;
import com.campuscon.dto.settings.SecuritySettingsResponse;
import com.campuscon.dto.user.UserSummaryResponse;
import com.campuscon.exception.BadRequestException;
import com.campuscon.exception.ResourceNotFoundException;
import com.campuscon.model.BlockedUser;
import com.campuscon.model.Settings;
import com.campuscon.model.User;
import com.campuscon.repository.BlockedUserRepository;
import com.campuscon.repository.SettingsRepository;
import com.campuscon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing security settings
 */
@Service
@RequiredArgsConstructor
public class SecuritySettingsService {

    private final UserRepository userRepository;
    private final SettingsRepository settingsRepository;
    private final BlockedUserRepository blockedUserRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Get security settings for a user
     */
    public SecuritySettingsResponse getSecuritySettings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Check if settings exist, create if not (for future use)
        settingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Settings newSettings = Settings.builder()
                            .user(user)
                            .build();
                    return settingsRepository.save(newSettings);
                });
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String lastPasswordChange = user.getUpdatedAt() != null 
                ? user.getUpdatedAt().format(formatter) 
                : user.getCreatedAt().format(formatter);
        
        String lastLogin = user.getLastSeenAt() != null 
                ? user.getLastSeenAt().format(formatter) 
                : "Never";
        
        return SecuritySettingsResponse.builder()
                .twoFactorAuthEnabled(false) // Future update feature
                .lastPasswordChangeDate(lastPasswordChange)
                .lastLoginDate(lastLogin)
                .build();
    }

    /**
     * Update security settings for a user
     */
    @Transactional
    public SecuritySettingsResponse updateSecuritySettings(Long userId, SecuritySettingsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Update password if requested
        if (request.getNewPassword() != null && !request.getNewPassword().isEmpty()) {
            // Verify current password
            if (request.getCurrentPassword() == null || !passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new BadRequestException("Current password is incorrect");
            }
            
            // Update password
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(user);
        }
        
        // Two-factor authentication would be set here in future updates
        
        return getSecuritySettings(userId);
    }

    /**
     * Get blocked users for a user
     */
    public List<UserSummaryResponse> getBlockedUsers(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<BlockedUser> blockedUsers = blockedUserRepository.findByBlocker(user);
        
        return blockedUsers.stream()
                .map(blockedUser -> mapToUserSummaryResponse(blockedUser.getBlocked()))
                .collect(Collectors.toList());
    }

    /**
     * Block a user
     */
    @Transactional
    public void blockUser(Long userId, Long blockedUserId, String reason) {
        if (userId.equals(blockedUserId)) {
            throw new BadRequestException("Cannot block yourself");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        User blockedUser = userRepository.findById(blockedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User to block not found"));
        
        // Check if already blocked
        if (blockedUserRepository.existsByBlockerAndBlocked(user, blockedUser)) {
            throw new BadRequestException("User is already blocked");
        }
        
        // Create blocked user relationship
        BlockedUser blockRecord = BlockedUser.builder()
                .blocker(user)
                .blocked(blockedUser)
                .reason(reason)
                .build();
        
        blockedUserRepository.save(blockRecord);
    }

    /**
     * Unblock a user
     */
    @Transactional
    public void unblockUser(Long userId, Long blockedUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        User blockedUser = userRepository.findById(blockedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User to unblock not found"));
        
        // Find and remove blocked relationship
        BlockedUser blockRecord = blockedUserRepository.findByBlockerAndBlocked(user, blockedUser)
                .orElseThrow(() -> new BadRequestException("User is not blocked"));
        
        blockedUserRepository.delete(blockRecord);
    }
    
    /**
     * Check if a user is blocked by another user
     */
    public boolean isUserBlocked(Long blockerUserId, Long blockedUserId) {
        User blocker = userRepository.findById(blockerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Blocker user not found"));
        
        User blocked = userRepository.findById(blockedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Blocked user not found"));
        
        return blockedUserRepository.existsByBlockerAndBlocked(blocker, blocked);
    }
    
    /**
     * Delete user account
     */
    @Transactional
    public void deleteUserAccount(Long userId, AccountDeletionRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Password is incorrect");
        }
        
        // Verify OTP (this would involve an OTP verification service)
        // Assuming verifyOtp method exists in an OtpService
        // otpService.verifyOtp(user.getEmail(), request.getOtpCode());
        
        // All users are now treated equally, no special handling required for different user types
        
        // Remove related records
        // Note: This assumes cascade delete is set up in the database
        // or you need to manually delete related records here
        
        // Delete blocked user records
        blockedUserRepository.findByBlocker(user).forEach(blockedUserRepository::delete);
        
        // Delete user settings
        settingsRepository.findByUserId(userId).ifPresent(settingsRepository::delete);
        
        // Finally delete the user
        userRepository.delete(user);
        
        // Additional cleanup could be done here
        // e.g., invalidate sessions, tokens, etc.
    }

    /**
     * Helper method to map User entity to UserSummaryResponse DTO
     */
    private UserSummaryResponse mapToUserSummaryResponse(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getUsername())
                .profilePictureUrl(user.getProfilePictureUrl())
                .role("USER") // Default role in unified user model
                .build();
    }
}
