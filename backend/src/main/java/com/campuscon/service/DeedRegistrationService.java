package com.campuscon.service;

import com.campuscon.model.Deed;
import com.campuscon.model.DeedRegistration;
import com.campuscon.model.User;
import com.campuscon.repository.DeedRegistrationRepository;
import com.campuscon.repository.DeedRepository;
import com.campuscon.repository.UserRepository;
import com.campuscon.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DeedRegistrationService {

    private final DeedRegistrationRepository deedRegistrationRepository;
    private final DeedRepository deedRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Register for a deed
     */
    @Transactional
    public DeedRegistration registerForDeed(Long deedId, Long userId, String teamName, Integer teamSize, String additionalInfo) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Check if registration is enabled for this deed
        if (!deed.isRegistrationEnabled()) {
            throw new IllegalArgumentException("Registration is not enabled for this deed");
        }
        
        // Check if user has already registered
        if (deedRegistrationRepository.existsByDeedAndUser(deed, user)) {
            throw new IllegalArgumentException("You have already registered for this deed");
        }
        
        // Create registration
        DeedRegistration registration = DeedRegistration.builder()
                .deed(deed)
                .user(user)
                .registeredAt(LocalDateTime.now())
                .status(DeedRegistration.RegistrationStatus.PENDING)
                .teamName(teamName)
                .teamSize(teamSize)
                .additionalInfo(additionalInfo)
                .build();
        
        DeedRegistration savedRegistration = deedRegistrationRepository.save(registration);
        
        // Notify society about the new registration
        notificationService.sendDeedRegistrationNotification(deed.getSociety(), user, deed);
        
        return savedRegistration;
    }

    /**
     * Get registrations for a deed
     */
    public Page<DeedRegistration> getDeedRegistrations(Long deedId, Pageable pageable) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        
        return deedRegistrationRepository.findByDeed(deed, pageable);
    }

    /**
     * Get registrations for a deed by status
     */
    public Page<DeedRegistration> getDeedRegistrationsByStatus(Long deedId, DeedRegistration.RegistrationStatus status, Pageable pageable) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        
        return deedRegistrationRepository.findByDeedAndStatus(deed, status, pageable);
    }

    /**
     * Get user's registrations
     */
    public Page<DeedRegistration> getUserRegistrations(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return deedRegistrationRepository.findByUser(user, pageable);
    }

    /**
     * Update registration status (for society)
     */
    @Transactional
    public DeedRegistration updateRegistrationStatus(Long registrationId, DeedRegistration.RegistrationStatus status, 
                                                     String rejectionReason, Long societyId) {
        DeedRegistration registration = deedRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Registration not found"));
        
        // Check if the society owns this deed
        if (!registration.getDeed().getSociety().getId().equals(societyId)) {
            throw new IllegalArgumentException("Not authorized to update this registration");
        }
        
        // Update status
        registration.setStatus(status);
        
        // Set rejection reason if applicable
        if (status == DeedRegistration.RegistrationStatus.REJECTED && rejectionReason != null) {
            registration.setRejectionReason(rejectionReason);
        }
        
        DeedRegistration updatedRegistration = deedRegistrationRepository.save(registration);
        
        // Send notification to user about registration status update
        if (status == DeedRegistration.RegistrationStatus.APPROVED) {
            notificationService.sendDeedRegistrationApprovedNotification(registration.getUser(), registration.getDeed());
        } else if (status == DeedRegistration.RegistrationStatus.REJECTED) {
            notificationService.sendDeedRegistrationRejectedNotification(registration.getUser(), registration.getDeed(), rejectionReason);
        }
        
        return updatedRegistration;
    }

    /**
     * Cancel registration (for user)
     */
    @Transactional
    public void cancelRegistration(Long deedId, Long userId) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        DeedRegistration registration = deedRegistrationRepository.findByDeedAndUser(deed, user);
        
        if (registration == null) {
            throw new ResourceNotFoundException("Registration not found");
        }
        
        // Only allow cancellation if status is pending
        if (registration.getStatus() != DeedRegistration.RegistrationStatus.PENDING) {
            throw new IllegalArgumentException("Cannot cancel registration with status: " + registration.getStatus());
        }
        
        deedRegistrationRepository.delete(registration);
        
        // Notify society about the cancellation
        notificationService.sendDeedRegistrationCancelledNotification(deed.getSociety(), user, deed);
    }

    /**
     * Check if user has registered for a deed
     */
    public boolean hasUserRegistered(Long deedId, Long userId) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return deedRegistrationRepository.existsByDeedAndUser(deed, user);
    }

    /**
     * Get registration status for a user and deed
     */
    public DeedRegistration.RegistrationStatus getRegistrationStatus(Long deedId, Long userId) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        DeedRegistration registration = deedRegistrationRepository.findByDeedAndUser(deed, user);
        
        if (registration == null) {
            return null;
        }
        
        return registration.getStatus();
    }

    /**
     * Count registrations for a deed
     */
    public long countRegistrations(Long deedId) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        
        return deedRegistrationRepository.countByDeed(deed);
    }

    /**
     * Count registrations for a deed by status
     */
    public long countRegistrationsByStatus(Long deedId, DeedRegistration.RegistrationStatus status) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        
        return deedRegistrationRepository.countByDeedAndStatus(deed, status);
    }
}
