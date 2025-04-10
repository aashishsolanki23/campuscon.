package com.campuscon.service;

import com.campuscon.dto.settings.*;
import com.campuscon.exception.ResourceNotFoundException;
import com.campuscon.model.Settings;
import com.campuscon.model.User;
import com.campuscon.repository.SettingsRepository;
import com.campuscon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing user settings
 */
@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SettingsRepository settingsRepository;
    private final UserRepository userRepository;

    /**
     * Get or create settings for a user
     */
    @Transactional
    public Settings getOrCreateSettings(Long userId) {
        return settingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    
                    Settings newSettings = Settings.builder()
                            .user(user)
                            .build();
                    
                    return settingsRepository.save(newSettings);
                });
    }

    /**
     * Get profile settings for a user
     */
    public ProfileSettingsResponse getProfileSettings(Long userId) {
        Settings settings = getOrCreateSettings(userId);
        
        return ProfileSettingsResponse.builder()
                .profileVisibility(settings.getProfileVisibility())
                .bondRequestPermission(settings.getBondRequestPermission())
                .commentPermission(settings.getCommentPermission())
                .build();
    }

    /**
     * Update profile settings for a user
     */
    @Transactional
    public ProfileSettingsResponse updateProfileSettings(Long userId, ProfileSettingsRequest request) {
        Settings settings = getOrCreateSettings(userId);
        
        settings.setProfileVisibility(request.getProfileVisibility());
        settings.setBondRequestPermission(request.getBondRequestPermission());
        settings.setCommentPermission(request.getCommentPermission());
        
        settingsRepository.save(settings);
        
        return ProfileSettingsResponse.builder()
                .profileVisibility(settings.getProfileVisibility())
                .bondRequestPermission(settings.getBondRequestPermission())
                .commentPermission(settings.getCommentPermission())
                .build();
    }

    /**
     * Get notification settings for a user
     */
    public NotificationSettingsResponse getNotificationSettings(Long userId) {
        Settings settings = getOrCreateSettings(userId);
        
        return NotificationSettingsResponse.builder()
                .pushNotificationsEnabled(settings.isPushNotificationsEnabled())
                .bondRequestNotifications(settings.isBondRequestNotifications())
                .messageNotifications(settings.isMessageNotifications())
                .interactionNotifications(settings.isInteractionNotifications())
                .eventNotifications(settings.isEventNotifications())
                .emailNotificationsEnabled(settings.isEmailNotificationsEnabled())
                .build();
    }

    /**
     * Update notification settings for a user
     */
    @Transactional
    public NotificationSettingsResponse updateNotificationSettings(Long userId, NotificationSettingsRequest request) {
        Settings settings = getOrCreateSettings(userId);
        
        settings.setPushNotificationsEnabled(request.isPushNotificationsEnabled());
        settings.setBondRequestNotifications(request.isBondRequestNotifications());
        settings.setMessageNotifications(request.isMessageNotifications());
        settings.setInteractionNotifications(request.isInteractionNotifications());
        settings.setEventNotifications(request.isEventNotifications());
        settings.setEmailNotificationsEnabled(request.isEmailNotificationsEnabled());
        
        settingsRepository.save(settings);
        
        return NotificationSettingsResponse.builder()
                .pushNotificationsEnabled(settings.isPushNotificationsEnabled())
                .bondRequestNotifications(settings.isBondRequestNotifications())
                .messageNotifications(settings.isMessageNotifications())
                .interactionNotifications(settings.isInteractionNotifications())
                .eventNotifications(settings.isEventNotifications())
                .emailNotificationsEnabled(settings.isEmailNotificationsEnabled())
                .build();
    }

    /**
     * Get app preferences for a user
     */
    public AppPreferencesResponse getAppPreferences(Long userId) {
        Settings settings = getOrCreateSettings(userId);
        
        return AppPreferencesResponse.builder()
                .themePreference(settings.getThemePreference())
                .language(settings.getLanguage())
                .autoLoadMedia(settings.isAutoLoadMedia())
                .build();
    }

    /**
     * Update app preferences for a user
     */
    @Transactional
    public AppPreferencesResponse updateAppPreferences(Long userId, AppPreferencesRequest request) {
        Settings settings = getOrCreateSettings(userId);
        
        settings.setThemePreference(request.getThemePreference());
        settings.setLanguage(request.getLanguage());
        settings.setAutoLoadMedia(request.isAutoLoadMedia());
        
        settingsRepository.save(settings);
        
        return AppPreferencesResponse.builder()
                .themePreference(settings.getThemePreference())
                .language(settings.getLanguage())
                .autoLoadMedia(settings.isAutoLoadMedia())
                .build();
    }
}
