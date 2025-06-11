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
     * Get deed registration settings
     */
    @GetMapping("/{deedId}/settings/registration")
    public ResponseEntity<?> getDeedRegistrationSettings(
            @PathVariable Long deedId,
            @CurrentUser UserPrincipal userPrincipal
    ) {
        Deed deed = deedService.getDeedById(deedId);
        
        // Check if the current user is the creator of the deed
        if (!deed.getCreator().getId().equals(userPrincipal.getId())) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("You don't have permission to access these settings")
            );
        }
        
        DeedRegistrationSettings settings = DeedRegistrationSettings.builder()
                .registrationEnabled(deed.isRegistrationEnabled())
                .eligibilityCriteria(deed.getEligibilityCriteria())
                .maxRegistrations(deed.getMaxRegistrations())
                .requireApproval(deed.isRequireApproval())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(settings));
    }
    
    /**
     * Update deed registration settings
     */
    @PutMapping("/{deedId}/settings/registration")
    public ResponseEntity<?> updateDeedRegistrationSettings(
            @PathVariable Long deedId,
            @Valid @RequestBody DeedRegistrationSettings settings,
            @CurrentUser UserPrincipal userPrincipal
    ) {
        Deed deed = deedService.getDeedById(deedId);
        
        // Check if the current user is the creator of the deed
        if (!deed.getCreator().getId().equals(userPrincipal.getId())) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("You don't have permission to update these settings")
            );
        }
        
        deedSettingsService.updateRegistrationSettings(deedId, settings);
        
        return ResponseEntity.ok(
            ApiResponse.success(settings, "Registration settings updated successfully")
        );
    }
    
    /**
     * Get deed general settings
     */
    @GetMapping("/{deedId}/settings")
    public ResponseEntity<?> getDeedSettings(
            @PathVariable Long deedId,
            @CurrentUser UserPrincipal userPrincipal
    ) {
        Deed deed = deedService.getDeedById(deedId);
        
        // Check if the current user is the creator of the deed
        if (!deed.getCreator().getId().equals(userPrincipal.getId())) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("You don't have permission to access these settings")
            );
        }
        
        DeedSettingsResponse settings = deedSettingsService.getDeedSettings(deedId);
        return ResponseEntity.ok(ApiResponse.success(settings));
    }
    
    /**
     * Update deed general settings
     */
    @PutMapping("/{deedId}/settings")
    public ResponseEntity<?> updateDeedSettings(
            @PathVariable Long deedId,
            @Valid @RequestBody DeedSettingsRequest settingsRequest,
            @CurrentUser UserPrincipal userPrincipal
    ) {
        Deed deed = deedService.getDeedById(deedId);
        
        // Check if the current user is the creator of the deed
        if (!deed.getCreator().getId().equals(userPrincipal.getId())) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("You don't have permission to update these settings")
            );
        }
        
        deedSettingsService.updateDeedSettings(deedId, settingsRequest);
        
        return ResponseEntity.ok(
            ApiResponse.success(settingsRequest, "Deed settings updated successfully")
        );
    }
    
    /**
     * Process waitlisted registrations in bulk 
     * and notifies users of their approval
     */
    @PostMapping("/{deedId}/waitlist/process")
    public ResponseEntity<?> processWaitlistedRegistrations(
            @PathVariable Long deedId,
            @CurrentUser UserPrincipal userPrincipal
    ) {
        Deed deed = deedService.getDeedById(deedId);
        
        // Check if the current user is the creator of the deed
        if (!deed.getCreator().getId().equals(userPrincipal.getId())) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("You don't have permission to process waitlisted registrations")
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
