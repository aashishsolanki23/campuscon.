package com.campuscon.service;

import com.campuscon.dto.deed.settings.DeedSettingsRequest;
import com.campuscon.dto.deed.settings.DeedSettingsResponse;
import com.campuscon.exception.ResourceNotFoundException;
import com.campuscon.model.Deed;
import com.campuscon.model.DeedRegistration;
import com.campuscon.model.User;
import com.campuscon.repository.DeedRegistrationRepository;
import com.campuscon.repository.DeedRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for managing deed settings
 */
@Service
@RequiredArgsConstructor
public class DeedSettingsService {

    private final DeedRepository deedRepository;
    private final DeedRegistrationRepository deedRegistrationRepository;
    private final NotificationService notificationService;

    /**
     * Get settings for a deed
     */
    public DeedSettingsResponse getDeedSettings(Long deedId) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        
        // Count registrations using repository methods for better performance
        int approvedCount = (int) deedRegistrationRepository.countByDeedAndStatus(deed, DeedRegistration.RegistrationStatus.APPROVED);
        int waitlistCount = (int) deedRegistrationRepository.countByDeedAndStatus(deed, DeedRegistration.RegistrationStatus.PENDING);
        
        return DeedSettingsResponse.builder()
                .requireApprovalForRegistration(deed.isRequireRegistrationApproval())
                .maxRegistrations(deed.getMaxRegistrations())
                .allowWaitlist(deed.isAllowWaitlist())
                .notifyOnRegistration(deed.isNotifyOnRegistration())
                .currentRegistrationsCount(approvedCount)
                .waitlistCount(waitlistCount)
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
            deed.setRequireRegistrationApproval(request.getRequireApprovalForRegistration());
        }
        
        if (request.getMaxRegistrations() != null) {
            deed.setMaxRegistrations(request.getMaxRegistrations());
        }
        
        if (request.getAllowWaitlist() != null) {
            deed.setAllowWaitlist(request.getAllowWaitlist());
        }
        
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
        
        // Count approved registrations
        long approvedRegistrationsCount = deedRegistrationRepository.countByDeedAndStatus(deed, DeedRegistration.RegistrationStatus.APPROVED);
        
        return approvedRegistrationsCount >= deed.getMaxRegistrations();
    }

    /**
     * Process registrations from waitlist
     * This can be called after someone cancels their registration
     * @return Number of registrations that were approved from the waitlist
     */
    @Transactional
    public int processWaitlist(Long deedId) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        
        // If max registrations is not set or auto-approval is not enabled, do nothing
        if (deed.getMaxRegistrations() == null || deed.getMaxRegistrations() <= 0 || deed.isRequireRegistrationApproval()) {
            return 0;
        }
        
        // Count approved registrations
        long approvedRegistrationsCount = deedRegistrationRepository.countByDeedAndStatus(deed, DeedRegistration.RegistrationStatus.APPROVED);
        
        // If there's space for more registrations
        int processed = 0;
        if (approvedRegistrationsCount < deed.getMaxRegistrations()) {
            // Get the oldest pending registrations
            List<DeedRegistration> pendingRegistrations = deedRegistrationRepository.findByDeedAndStatusOrderByRegisteredAtAsc(deed, DeedRegistration.RegistrationStatus.PENDING);
            
            // Calculate how many can be approved
            int slotsAvailable = deed.getMaxRegistrations() - (int) approvedRegistrationsCount;
            int toApprove = Math.min(slotsAvailable, pendingRegistrations.size());
            processed = toApprove;
            
            // Approve the oldest pending registrations
            for (int i = 0; i < toApprove; i++) {
                DeedRegistration registration = pendingRegistrations.get(i);
                registration.setStatus(DeedRegistration.RegistrationStatus.APPROVED);
                deedRegistrationRepository.save(registration);
                
                // Notify user about registration approval
                User user = registration.getUser();
                notificationService.sendDeedRegistrationApprovalNotification(
                    user.getId(),
                    deed.getId(),
                    deed.getTitle(),
                    "Your registration for " + deed.getTitle() + " has been approved!"
                );
            }
        }
        return processed;
    }
}
