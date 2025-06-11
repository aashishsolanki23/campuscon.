package com.campuscon.service;

import com.campuscon.model.Deed;
import com.campuscon.model.DeedRegistration;
import com.campuscon.model.User;
import com.campuscon.repository.DeedRegistrationRepository;
import com.campuscon.repository.DeedRepository;
import com.campuscon.repository.UserRepository;
import com.campuscon.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class DeedRegistrationService {

    private final DeedRegistrationRepository deedRegistrationRepository;
    private final DeedRepository deedRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    
    public DeedRegistrationService(
            DeedRegistrationRepository deedRegistrationRepository,
            DeedRepository deedRepository,
            UserRepository userRepository,
            @Lazy NotificationService notificationService) {
        this.deedRegistrationRepository = deedRegistrationRepository;
        this.deedRepository = deedRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;

    }

    /**
     * Register for a deed - single click registration
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
                .teamName(teamName)
                .teamSize(teamSize)
                .additionalInfo(additionalInfo)
                .build();
        
        DeedRegistration savedRegistration = deedRegistrationRepository.save(registration);
        
        // Notify creator about the new registration
        notificationService.sendDeedRegistrationNotification(deed.getCreator(), user, deed);
        
        // InGroup system removed
        
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

    // Status-specific methods removed with single-click registration

    /**
     * Get user's registrations
     */
    public Page<DeedRegistration> getUserRegistrations(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return deedRegistrationRepository.findByUser(user, pageable);
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
        
        // Delete the registration
        deedRegistrationRepository.delete(registration);
        
        // Notify creator about the cancellation
        notificationService.sendDeedRegistrationCancelledNotification(deed.getCreator(), user, deed);
        
        // InGroup system removal code removed
    }

    /**
     * Check if user has registered for a deed
     */
    public boolean isUserRegisteredForDeed(Long deedId, Long userId) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return deedRegistrationRepository.existsByDeedAndUser(deed, user);
    }

    // getRegistrationStatus method removed with single-click registration

    /**
     * Count registrations for a deed
     */
    public long countRegistrations(Long deedId) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        
        return deedRegistrationRepository.countByDeed(deed);
    }

    // countRegistrationsByStatus method removed with single-click registration
    
    /**
     * Count registrations for a user
     * @param userId The user ID
     * @return Count of registrations for the user
     */
    public Long countRegistrationsForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        return deedRegistrationRepository.countByUser(user);
    }
    
    /**
     * Remove user from all registrations - used when deleting an account
     * @param userId The user ID
     */
    @Transactional
    public void removeUserFromAllRegistrations(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Find all user's registrations
        List<DeedRegistration> registrations = deedRegistrationRepository.findAllByUser(user);
        
        if (!registrations.isEmpty()) {
            log.info("Removing {} registrations for user ID: {}", registrations.size(), userId);
            
            // InGroup system removed - no group operations needed
            
            // Delete all registrations
            deedRegistrationRepository.deleteAll(registrations);
        }
    }
}
