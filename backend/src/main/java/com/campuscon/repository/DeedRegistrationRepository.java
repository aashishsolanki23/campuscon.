package com.campuscon.repository;

import com.campuscon.model.Deed;
import com.campuscon.model.DeedRegistration;
import com.campuscon.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

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
    
    /**
     * Find all registrations by status
     */
    Page<DeedRegistration> findByStatus(DeedRegistration.RegistrationStatus status, Pageable pageable);
    
    /**
     * Find all registrations for a deed with a specific status
     */
    Page<DeedRegistration> findByDeedAndStatus(Deed deed, DeedRegistration.RegistrationStatus status, Pageable pageable);
    
    /**
     * Find pending registrations for a deed ordered by creation date
     */
    List<DeedRegistration> findByDeedAndStatusOrderByRegisteredAtAsc(Deed deed, DeedRegistration.RegistrationStatus status);
    
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
     * Count registrations for a deed by status
     */
    long countByDeedAndStatus(Deed deed, DeedRegistration.RegistrationStatus status);
}
