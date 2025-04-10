package com.campuscon.repository;

import com.campuscon.model.Settings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing user settings
 */
@Repository
public interface SettingsRepository extends JpaRepository<Settings, Long> {
    
    /**
     * Find settings by user ID
     */
    Optional<Settings> findByUserId(Long userId);
}
