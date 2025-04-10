package com.campuscon.controller;

import com.campuscon.dto.ApiResponse;
import com.campuscon.dto.settings.AccountDeletionRequest;
import com.campuscon.dto.settings.SecuritySettingsRequest;
import com.campuscon.dto.settings.SecuritySettingsResponse;
import com.campuscon.dto.user.UserSummaryResponse;
import com.campuscon.security.CurrentUser;
import com.campuscon.security.UserPrincipal;
import com.campuscon.service.SecuritySettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing security settings
 */
@RestController
@RequestMapping("/api/settings/security")
@RequiredArgsConstructor
public class SecuritySettingsController {

    private final SecuritySettingsService securitySettingsService;

    /**
     * Get security settings for the current user
     */
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('SOCIETY')")
    public ResponseEntity<ApiResponse<SecuritySettingsResponse>> getSecuritySettings(@CurrentUser UserPrincipal currentUser) {
        SecuritySettingsResponse response = securitySettingsService.getSecuritySettings(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(response, "Security settings retrieved successfully"));
    }

    /**
     * Update security settings for the current user
     */
    @PutMapping
    @PreAuthorize("hasRole('USER') or hasRole('SOCIETY')")
    public ResponseEntity<ApiResponse<SecuritySettingsResponse>> updateSecuritySettings(
            @CurrentUser UserPrincipal currentUser,
            @RequestBody SecuritySettingsRequest request) {
        SecuritySettingsResponse response = securitySettingsService.updateSecuritySettings(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(response, "Security settings updated successfully"));
    }

    /**
     * Get blocked users for the current user
     */
    @GetMapping("/blocked-users")
    @PreAuthorize("hasRole('USER') or hasRole('SOCIETY')")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> getBlockedUsers(@CurrentUser UserPrincipal currentUser) {
        List<UserSummaryResponse> blockedUsers = securitySettingsService.getBlockedUsers(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(blockedUsers, "Blocked users retrieved successfully"));
    }

    /**
     * Block a user
     */
    @PostMapping("/block-user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('SOCIETY')")
    public ResponseEntity<ApiResponse<Void>> blockUser(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long userId,
            @RequestParam(required = false) String reason) {
        securitySettingsService.blockUser(currentUser.getId(), userId, reason);
        return ResponseEntity.ok(ApiResponse.success(null, "User blocked successfully"));
    }

    /**
     * Unblock a user
     */
    @DeleteMapping("/unblock-user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('SOCIETY')")
    public ResponseEntity<ApiResponse<Void>> unblockUser(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long userId) {
        securitySettingsService.unblockUser(currentUser.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success(null, "User unblocked successfully"));
    }

    /**
     * Check if a user is blocked
     */
    @GetMapping("/is-blocked/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('SOCIETY')")
    public ResponseEntity<ApiResponse<Boolean>> isUserBlocked(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long userId) {
        boolean isBlocked = securitySettingsService.isUserBlocked(currentUser.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success(isBlocked, isBlocked ? "User is blocked" : "User is not blocked"));
    }
    
    /**
     * Delete the current user's account
     */
    @DeleteMapping("/account")
    @PreAuthorize("hasRole('USER') or hasRole('SOCIETY')")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @CurrentUser UserPrincipal currentUser,
            @RequestBody AccountDeletionRequest request) {
        securitySettingsService.deleteUserAccount(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Account deleted successfully"));
    }
}
