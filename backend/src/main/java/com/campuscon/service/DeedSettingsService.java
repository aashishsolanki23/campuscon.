package com.campuscon.service;

import com.campuscon.dto.deed.settings.DeedSettingsRequest;
import com.campuscon.dto.deed.settings.DeedSettingsResponse;
import com.campuscon.dto.deed.registration.DeedRegistrationSettings;
import com.campuscon.exception.ResourceNotFoundException;
import com.campuscon.model.Deed;
import com.campuscon.repository.DeedRegistrationRepository;
import com.campuscon.repository.DeedRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing deed settings
 */
@Service
public class DeedSettingsService {

    private final DeedRepository deedRepository;
    private final DeedRegistrationRepository deedRegistrationRepository;
    // Kept for backward compatibility but no longer used with single-click registration
    @SuppressWarnings("unused")
    private final NotificationService notificationService;
    
    public DeedSettingsService(
            DeedRepository deedRepository,
            DeedRegistrationRepository deedRegistrationRepository,
            @org.springframework.context.annotation.Lazy NotificationService notificationService) {
        this.deedRepository = deedRepository;
        this.deedRegistrationRepository = deedRegistrationRepository;
        this.notificationService = notificationService;
    }

    /**
     * Get settings for a deed
     */
    public DeedSettingsResponse getDeedSettings(Long deedId) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        
        // Count all registrations with single-click registration system
        int totalCount = (int) deedRegistrationRepository.countByDeed(deed);
        // With single-click registration, there's no waitlist or approval process
        
        return DeedSettingsResponse.builder()
                .requireApprovalForRegistration(false) // Always false with single-click registration
                .maxRegistrations(deed.getMaxRegistrations())
                .allowWaitlist(false) // Always false with single-click registration
                .notifyOnRegistration(deed.isNotifyOnRegistration())
                .currentRegistrationsCount(totalCount)
                .waitlistCount(0) // Always 0 with single-click registration
                .build();
    }

    /**
     * Update settings for a deed
     */
    @Transactional
    public DeedSettingsResponse updateDeedSettings(Long deedId, DeedSettingsRequest request) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        
        // Update deed settings
        if (request.getRequireApprovalForRegistration() != null) {
            deed.setRequireApproval(request.getRequireApprovalForRegistration());
        }
        
        if (request.getMaxRegistrations() != null) {
            deed.setMaxRegistrations(request.getMaxRegistrations());
        }
        
        // Waitlist is no longer supported with single-click registration
        // Keep this line for backward compatibility
        // if (request.getAllowWaitlist() != null) {
            // deed.setAllowWaitlist(request.getAllowWaitlist());
        // }
        
        if (request.getNotifyOnRegistration() != null) {
            deed.setNotifyOnRegistration(request.getNotifyOnRegistration());
        }
        
        deedRepository.save(deed);
        
        return getDeedSettings(deedId);
    }

    /**
     * Check if a deed has reached its maximum registrations
     */
    public boolean hasDeedReachedMaxRegistrations(Long deedId) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        
        // If max registrations is not set, return false
        if (deed.getMaxRegistrations() == null || deed.getMaxRegistrations() <= 0) {
            return false;
        }
        
        // Count all registrations with single-click registration
        long totalRegistrations = deedRegistrationRepository.countByDeed(deed);
        
        return totalRegistrations >= deed.getMaxRegistrations();
    }

    /**
     * Process registrations from waitlist - no longer needed with single-click registration
     * This method is kept as a stub for backward compatibility
     * @return Always returns 0 since there's no waitlist in single-click registration
     */
    @Transactional
    public int processWaitlist(Long deedId) {
        // With single-click registration, there's no waitlist to process
        // All registrations are automatically approved upon creation
        return 0;
    }
    
    /**
     * Update registration settings for a deed
     * @param deedId ID of the deed to update
     * @param settings Registration settings to apply
     */
    @Transactional
    public void updateRegistrationSettings(Long deedId, DeedRegistrationSettings settings) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        
        // Update registration settings
        if (settings.getRegistrationEnabled() != null) {
            deed.setRegistrationEnabled(settings.getRegistrationEnabled());
        }
        
        if (settings.getEligibilityCriteria() != null) {
            deed.setEligibilityCriteria(settings.getEligibilityCriteria());
        }
        
        if (settings.getMaxRegistrations() != null) {
            deed.setMaxRegistrations(settings.getMaxRegistrations());
        }
        
        if (settings.getRequireApproval() != null) {
            deed.setRequireApproval(settings.getRequireApproval());
        }
        
        deedRepository.save(deed);
    }
}
