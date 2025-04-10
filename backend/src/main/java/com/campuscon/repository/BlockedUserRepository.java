package com.campuscon.repository;

import com.campuscon.model.BlockedUser;
import com.campuscon.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing blocked user relationships
 */
@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, Long> {
    
    /**
     * Find all users blocked by a specific user
     */
    List<BlockedUser> findByBlocker(User blocker);
    
    /**
     * Find a specific blocked user relationship
     */
    Optional<BlockedUser> findByBlockerAndBlocked(User blocker, User blocked);
    
    /**
     * Check if a user is blocked by another user
     */
    boolean existsByBlockerAndBlocked(User blocker, User blocked);
}
