package com.campuscon.repository;

import com.campuscon.model.Deed;
import com.campuscon.model.DeedRegistration;
import com.campuscon.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public interface DeedRegistrationRepository extends JpaRepository<DeedRegistration, Long> {
    
    /**
     * Find all registrations for a specific deed
     */
    Page<DeedRegistration> findByDeed(Deed deed, Pageable pageable);
    
    /**
     * Find all registrations by a user
     */
    Page<DeedRegistration> findByUser(User user, Pageable pageable);
    
    // Status-related methods removed for single-click registration
    
    
    // Status filtering methods removed for single-click registration
    
    /**
     * Find registrations for a deed ordered by creation date
     */
    List<DeedRegistration> findByDeedOrderByRegisteredAtAsc(Deed deed);
    
    /**
     * Check if a user has registered for a deed
     */
    boolean existsByDeedAndUser(Deed deed, User user);
    
    /**
     * Find registration by deed and user
     */
    DeedRegistration findByDeedAndUser(Deed deed, User user);
    
    /**
     * Count registrations for a deed
     */
    long countByDeed(Deed deed);
    
    /**
     * Count registrations for a user
     */
    long countByUser(User user);
    
    /**
     * Find all registrations by a user (not paginated)
     */
    List<DeedRegistration> findAllByUser(User user);
    
    // Status-specific counting method removed for single-click registration
}
