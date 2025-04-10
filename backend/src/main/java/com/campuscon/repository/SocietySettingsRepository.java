package com.campuscon.repository;

import com.campuscon.model.SocietySettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing society settings
 */
@Repository
public interface SocietySettingsRepository extends JpaRepository<SocietySettings, Long> {
    
    /**
     * Find society settings by society ID
     */
    Optional<SocietySettings> findBySocietyId(Long societyId);
}
