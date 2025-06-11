package com.campuscon.repository;

import com.campuscon.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Basic user lookup methods
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsernameOrEmail(String username, String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    // OAuth2 related methods
    Optional<User> findByProviderAndProviderId(String provider, String providerId);
    
    // User types related methods (replacing role-based queries)
    @Query("SELECT u FROM User u JOIN u.userTypes t WHERE t = :userType")
    List<User> findByUserType(@Param("userType") String userType);
    
    // Location-based queries for college detection
    List<User> findByCollegeNameContainingIgnoreCase(String collegeName);
    
    // Geographic queries for location-based matching
    @Query("SELECT u FROM User u WHERE u.city = :city OR u.state = :state")
    List<User> findByCityOrState(@Param("city") String city, @Param("state") String state);
    
    // Combined search for users across different parameters
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.displayName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(u.collegeName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<User> searchUsers(@Param("searchTerm") String searchTerm);
    
    // Academic-specific queries
    List<User> findByBatchYearAndCourseCode(String batchYear, String courseCode);
    
    // Legacy method transformed for compatibility
    @Query("SELECT u FROM User u JOIN u.userTypes t WHERE t = 'SOCIETY'")
    List<User> findSocieties();
    
    // General search
    List<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(String username, String email);
    
    /**
     * Find all users belonging to a specific team
     * 
     * @param teamId The team ID
     * @return List of users in the team
     */
    @Query("SELECT u FROM User u WHERE u.team.id = :teamId")
    List<User> findByTeamId(@Param("teamId") Long teamId);
}
