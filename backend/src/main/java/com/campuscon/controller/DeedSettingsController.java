package com.campuscon.controller;

import com.campuscon.security.CurrentUser;
import com.campuscon.security.UserPrincipal;
import com.campuscon.dto.ApiResponse;
import com.campuscon.dto.deed.registration.DeedRegistrationSettings;
import com.campuscon.dto.deed.settings.DeedSettingsRequest;
import com.campuscon.dto.deed.settings.DeedSettingsResponse;
import com.campuscon.model.Deed;
import com.campuscon.service.DeedService;
import com.campuscon.service.DeedSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Controller for managing deed settings related to registrations
 */
@RestController
@RequestMapping("/api/deeds")
@RequiredArgsConstructor
public class DeedSettingsController {

    private final DeedService deedService;
    private final DeedSettingsService deedSettingsService;

    /**
     * Get registration settings for a deed
     */
    @GetMapping("/{deedId}/settings/registration")
    @PreAuthorize("hasRole('SOCIETY')")
    public ResponseEntity<ApiResponse<DeedRegistrationSettings>> getRegistrationSettings(
            @PathVariable Long deedId,
            @CurrentUser UserPrincipal currentUser) {
        
        Deed deed = deedService.getDeedById(deedId);
        
        // Check if the current user is the society that created the deed
        if (!deed.getSociety().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("You don't have permission to access these settings")
            );
        }
        
        DeedRegistrationSettings settings = DeedRegistrationSettings.builder()
                .registrationEnabled(deed.isRegistrationEnabled())
                .eligibilityCriteria(deed.getEligibilityCriteria())
                .maxRegistrations(deed.getMaxRegistrations())
                .requireApproval(deed.isRequireRegistrationApproval())
                .allowTeamRegistration(deed.isAllowTeamRegistration())
                .maxTeamSize(deed.getMaxTeamSize())
                .additionalFieldsConfig(deed.getAdditionalFieldsConfig())
                .build();
        
        return ResponseEntity.ok(
            ApiResponse.success(settings, "Registration settings retrieved successfully")
        );
    }

    /**
     * Update registration settings for a deed
     */
    @PutMapping("/{deedId}/settings/registration")
    @PreAuthorize("hasRole('SOCIETY')")
    public ResponseEntity<ApiResponse<DeedRegistrationSettings>> updateRegistrationSettings(
            @PathVariable Long deedId,
            @Valid @RequestBody DeedRegistrationSettings settingsRequest,
            @CurrentUser UserPrincipal currentUser) {
        
        Deed deed = deedService.getDeedById(deedId);
        
        // Check if the current user is the society that created the deed
        if (!deed.getSociety().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("You don't have permission to update these settings")
            );
        }
        
        // Update deed with new settings
        deed.setRegistrationEnabled(settingsRequest.getRegistrationEnabled());
        deed.setEligibilityCriteria(settingsRequest.getEligibilityCriteria());
        deed.setMaxRegistrations(settingsRequest.getMaxRegistrations());
        deed.setRequireRegistrationApproval(settingsRequest.getRequireApproval());
        deed.setAllowTeamRegistration(settingsRequest.getAllowTeamRegistration());
        deed.setMaxTeamSize(settingsRequest.getMaxTeamSize());
        deed.setAdditionalFieldsConfig(settingsRequest.getAdditionalFieldsConfig());
        
        // Save updated deed
        deedService.updateDeed(deed);
        
        return ResponseEntity.ok(
            ApiResponse.success(settingsRequest, "Registration settings updated successfully")
        );
    }
    
    /**
     * Get deed settings
     */
    @GetMapping("/{deedId}/settings")
    @PreAuthorize("hasRole('SOCIETY')")
    public ResponseEntity<ApiResponse<DeedSettingsResponse>> getDeedSettings(
            @PathVariable Long deedId,
            @CurrentUser UserPrincipal currentUser) {
        
        Deed deed = deedService.getDeedById(deedId);
        
        // Check if the current user is the society that created the deed
        if (!deed.getSociety().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("You don't have permission to access these settings")
            );
        }
        
        DeedSettingsResponse settings = deedSettingsService.getDeedSettings(deedId);
        
        return ResponseEntity.ok(
            ApiResponse.success(settings, "Deed settings retrieved successfully")
        );
    }
    
    /**
     * Update deed settings
     */
    @PutMapping("/{deedId}/settings")
    @PreAuthorize("hasRole('SOCIETY')")
    public ResponseEntity<ApiResponse<DeedSettingsResponse>> updateDeedSettings(
            @PathVariable Long deedId,
            @Valid @RequestBody DeedSettingsRequest settingsRequest,
            @CurrentUser UserPrincipal currentUser) {
        
        Deed deed = deedService.getDeedById(deedId);
        
        // Check if the current user is the society that created the deed
        if (!deed.getSociety().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("You don't have permission to update these settings")
            );
        }
        
        DeedSettingsResponse settings = deedSettingsService.updateDeedSettings(deedId, settingsRequest);
        
        return ResponseEntity.ok(
            ApiResponse.success(settings, "Deed settings updated successfully")
        );
    }
    
    /**
     * Process registrations from the waitlist
     * Approves registrations from the waitlist based on available slots
     * and notifies users of their approval
     */
    @PostMapping("/{deedId}/settings/process-waitlist")
    @PreAuthorize("hasRole('SOCIETY')")
    public ResponseEntity<ApiResponse<Integer>> processWaitlist(
            @PathVariable Long deedId,
            @CurrentUser UserPrincipal currentUser) {
        
        Deed deed = deedService.getDeedById(deedId);
        
        // Check if the current user is the society that created the deed
        if (!deed.getSociety().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("You don't have permission to process the waitlist")
            );
        }
        
        int processedCount = deedSettingsService.processWaitlist(deedId);
        
        String message = processedCount > 0 
            ? processedCount + " registration" + (processedCount > 1 ? "s" : "") + " approved successfully" 
            : "No registrations were processed";
            
        return ResponseEntity.ok(
            ApiResponse.success(processedCount, message)
        );
    }
}
