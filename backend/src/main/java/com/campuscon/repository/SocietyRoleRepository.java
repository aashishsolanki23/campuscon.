package com.campuscon.repository;

import com.campuscon.model.SocietyRole;
import com.campuscon.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing society roles
 */
@Repository
public interface SocietyRoleRepository extends JpaRepository<SocietyRole, Long> {
    
    /**
     * Find society roles by society
     */
    List<SocietyRole> findBySociety(User society);
    
    /**
     * Find society role by society and user
     */
    Optional<SocietyRole> findBySocietyAndUser(User society, User user);
    
    /**
     * Find president role for a society
     */
    Optional<SocietyRole> findBySocietyAndIsPresidentTrue(User society);
    
    /**
     * Find all roles for a user across all societies
     */
    List<SocietyRole> findByUser(User user);
}
