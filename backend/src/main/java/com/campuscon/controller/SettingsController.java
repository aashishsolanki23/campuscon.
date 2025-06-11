package com.campuscon.controller;

import com.campuscon.dto.ApiResponse;
import com.campuscon.dto.settings.*;
import com.campuscon.security.CurrentUser;
import com.campuscon.security.UserPrincipal;
import com.campuscon.service.SettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing user settings
 */
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    /**
     * Get profile settings for the current user
     */
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ProfileSettingsResponse>> getProfileSettings(@CurrentUser UserPrincipal currentUser) {
        ProfileSettingsResponse response = settingsService.getProfileSettings(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Profile settings retrieved successfully"));
    }

    /**
     * Update profile settings for the current user
     */
    @PutMapping("/profile")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<ProfileSettingsResponse>> updateProfileSettings(
            @CurrentUser UserPrincipal currentUser,
            @RequestBody ProfileSettingsRequest request) {
        ProfileSettingsResponse response = settingsService.updateProfileSettings(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile settings updated successfully"));
    }

    /**
     * Get notification settings for the current user
     */
    @GetMapping("/notifications")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<NotificationSettingsResponse>> getNotificationSettings(@CurrentUser UserPrincipal currentUser) {
        NotificationSettingsResponse response = settingsService.getNotificationSettings(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Notification settings retrieved successfully"));
    }

    /**
     * Update notification settings for the current user
     */
    @PutMapping("/notifications")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<NotificationSettingsResponse>> updateNotificationSettings(
            @CurrentUser UserPrincipal currentUser,
            @RequestBody NotificationSettingsRequest request) {
        NotificationSettingsResponse response = settingsService.updateNotificationSettings(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Notification settings updated successfully"));
    }

    /**
     * Get app preferences for the current user
     */
    @GetMapping("/preferences")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<AppPreferencesResponse>> getAppPreferences(@CurrentUser UserPrincipal currentUser) {
        AppPreferencesResponse response = settingsService.getAppPreferences(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "App preferences retrieved successfully"));
    }

    /**
     * Update app preferences for the current user
     */
    @PutMapping("/preferences")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<AppPreferencesResponse>> updateAppPreferences(
            @CurrentUser UserPrincipal currentUser,
            @RequestBody AppPreferencesRequest request) {
        AppPreferencesResponse response = settingsService.updateAppPreferences(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "App preferences updated successfully"));
    }
}
