package com.campuscon.service;

import com.campuscon.model.Deed;
import com.campuscon.repository.DeedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for cleaning up expired deeds
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeedCleanupService {

    private final DeedRepository deedRepository;
    
    /**
     * Scheduled task to clean up expired deeds
     * Runs once a day at midnight
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupExpiredDeeds() {
        log.info("Starting scheduled cleanup of expired deeds");
        
        // Find all deeds that have ended (endDateTime is in the past)
        LocalDateTime now = LocalDateTime.now();
        List<Deed> expiredDeeds = deedRepository.findByEndDateTimeBefore(now);
        
        log.info("Found {} expired deeds to clean up", expiredDeeds.size());
        
        for (Deed deed : expiredDeeds) {
            try {
                // InGroup system removed - no group cleanup needed
                
                // Delete the deed
                deedRepository.delete(deed);
                
                log.info("Successfully cleaned up deed: {} (ID: {})", deed.getTitle(), deed.getId());
            } catch (Exception e) {
                log.error("Error cleaning up deed with ID: {}", deed.getId(), e);
            }
        }
        
        log.info("Completed scheduled cleanup of expired deeds");
    }
    
    /**
     * Manual trigger to clean up expired deeds
     * 
     * @return The number of deeds cleaned up
     */
    @Transactional
    public int manualCleanupExpiredDeeds() {
        log.info("Starting manual cleanup of expired deeds");
        
        // Find all deeds that have ended (endDateTime is in the past)
        LocalDateTime now = LocalDateTime.now();
        List<Deed> expiredDeeds = deedRepository.findByEndDateTimeBefore(now);
        
        log.info("Found {} expired deeds to clean up", expiredDeeds.size());
        int cleanedCount = 0;
        
        for (Deed deed : expiredDeeds) {
            try {
                // InGroup system removed - no group cleanup needed
                
                // Delete the deed
                deedRepository.delete(deed);
                
                cleanedCount++;
                log.info("Successfully cleaned up deed: {} (ID: {})", deed.getTitle(), deed.getId());
            } catch (Exception e) {
                log.error("Error cleaning up deed with ID: {}", deed.getId(), e);
            }
        }
        
        log.info("Completed manual cleanup of {} expired deeds", cleanedCount);
        return cleanedCount;
    }
}
