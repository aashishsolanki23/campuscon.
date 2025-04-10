package com.campuscon.controller;

import com.campuscon.dto.ApiResponse;
import com.campuscon.dto.settings.SocietySettingsRequest;
import com.campuscon.dto.settings.SocietySettingsResponse;
import com.campuscon.model.SocietyRole;
import com.campuscon.security.CurrentUser;
import com.campuscon.security.UserPrincipal;
import com.campuscon.service.SocietySettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for managing society-specific settings
 */
@RestController
@RequestMapping("/api/society/settings")
@RequiredArgsConstructor
public class SocietySettingsController {

    private final SocietySettingsService societySettingsService;

    /**
     * Get society settings
     */
    @GetMapping("/{societyId}")
    @PreAuthorize("hasRole('SOCIETY') and @securityService.isSocietyMember(#societyId, authentication)")
    public ResponseEntity<ApiResponse<SocietySettingsResponse>> getSocietySettings(@PathVariable Long societyId) {
        SocietySettingsResponse response = societySettingsService.getSocietySettings(societyId);
        return ResponseEntity.ok(ApiResponse.success(response, "Society settings retrieved successfully"));
    }

    /**
     * Update society settings
     */
    @PutMapping("/{societyId}")
    @PreAuthorize("hasRole('SOCIETY') and @securityService.canModifySocietySettings(#societyId, authentication)")
    public ResponseEntity<ApiResponse<SocietySettingsResponse>> updateSocietySettings(
            @PathVariable Long societyId,
            @RequestBody SocietySettingsRequest request) {
        SocietySettingsResponse response = societySettingsService.updateSocietySettings(societyId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Society settings updated successfully"));
    }

    /**
     * Get society members
     */
    @GetMapping("/{societyId}/members")
    @PreAuthorize("hasRole('SOCIETY') and @securityService.isSocietyMember(#societyId, authentication)")
    public ResponseEntity<ApiResponse<List<SocietyRole>>> getSocietyMembers(@PathVariable Long societyId) {
        List<SocietyRole> members = societySettingsService.getSocietyMembers(societyId);
        return ResponseEntity.ok(ApiResponse.success(members, "Society members retrieved successfully"));
    }

    /**
     * Transfer society presidency
     */
    @PostMapping("/{societyId}/transfer-presidency")
    @PreAuthorize("hasRole('SOCIETY') and @securityService.isSocietyPresident(#societyId, authentication)")
    public ResponseEntity<ApiResponse<Void>> transferPresidency(
            @PathVariable Long societyId,
            @CurrentUser UserPrincipal currentUser,
            @RequestParam Long newPresidentId) {
        societySettingsService.transferPresidency(societyId, currentUser.getId(), newPresidentId);
        return ResponseEntity.ok(ApiResponse.success(null, "Presidency transferred successfully"));
    }

    /**
     * Assign role to society member
     */
    @PostMapping("/{societyId}/members/{userId}/role")
    @PreAuthorize("hasRole('SOCIETY') and @securityService.canManageSocietyMembers(#societyId, authentication)")
    public ResponseEntity<ApiResponse<SocietyRole>> assignRole(
            @PathVariable Long societyId,
            @PathVariable Long userId,
            @RequestParam String roleName,
            @RequestParam boolean canPost,
            @RequestParam boolean canApprovePosts,
            @RequestParam boolean canManageMembers,
            @RequestParam boolean canModifySettings) {
        SocietyRole role = societySettingsService.assignRole(societyId, userId, roleName, 
                                                       canPost, canApprovePosts, 
                                                       canManageMembers, canModifySettings);
        return ResponseEntity.ok(ApiResponse.success(role, "Role assigned successfully"));
    }

    /**
     * Remove member from society
     */
    @DeleteMapping("/{societyId}/members/{userId}")
    @PreAuthorize("hasRole('SOCIETY') and @securityService.canManageSocietyMembers(#societyId, authentication)")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @PathVariable Long societyId,
            @PathVariable Long userId) {
        societySettingsService.removeMember(societyId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Member removed successfully"));
    }
}
