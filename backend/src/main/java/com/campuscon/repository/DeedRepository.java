package com.campuscon.repository;

import com.campuscon.enums.DeedCategory;
import com.campuscon.model.Deed;
import com.campuscon.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Modifying;


public interface DeedRepository extends JpaRepository<Deed, Long> {
    
    // Find deeds with end date time before the given date time (for cleanup)
    List<Deed> findByEndDateTimeBefore(LocalDateTime dateTime);
    
    // Delete all deeds by creator ID (for account deletion)
    @Modifying
    @Transactional
    @Query("DELETE FROM Deed d WHERE d.creator.id = :creatorId")
    void deleteAllByCreatorId(@Param("creatorId") Long creatorId);
    
    // This method is now handled by the updated deleteAllByCreatorId above
    
    // Find deeds by creator (for profile page)
    Page<Deed> findByCreatorOrderByCreatedAtDesc(User creator, Pageable pageable);
    
    // Find deeds by creator ID
    Page<Deed> findByCreatorIdOrderByStartDateTimeDesc(Long creatorId, Pageable pageable);
    
    // Find deeds from a specific college
    @Query("SELECT d FROM Deed d WHERE d.creator.collegeName = :collegeName AND d.isApproved = true ORDER BY d.startDateTime DESC")
    Page<Deed> findByCollegeNameOrderByStartDateTimeDesc(@Param("collegeName") String collegeName, Pageable pageable);
    
    // Find upcoming deeds
    @Query("SELECT d FROM Deed d WHERE d.startDateTime > :now AND d.isApproved = true ORDER BY d.startDateTime ASC")
    Page<Deed> findUpcomingDeeds(@Param("now") LocalDateTime now, Pageable pageable);
    
    // Find deeds by category
    Page<Deed> findByCategoryAndIsApprovedTrueOrderByStartDateTimeDesc(DeedCategory category, Pageable pageable);
    
    // Find deeds saved by a user
    @Query("SELECT d FROM Deed d JOIN d.savedByUsers u WHERE u.id = :userId ORDER BY d.startDateTime DESC")
    Page<Deed> findSavedDeedsByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // Count deeds by creator
    long countByCreator(User creator);
    
    // isLikedByUser method has been removed
    
    // Check if a deed is saved by a user
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Deed d JOIN d.savedByUsers u WHERE d.id = :deedId AND u.id = :userId")
    boolean isSavedByUser(@Param("deedId") Long deedId, @Param("userId") Long userId);
    
    // Find most popular deeds (for home page)
    @Query("SELECT d FROM Deed d WHERE d.isApproved = true ORDER BY (d.commentsCount + d.savesCount) DESC")
    Page<Deed> findMostPopularDeeds(Pageable pageable);
    
    // Search deeds by title or description
    Page<Deed> findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByStartDateTimeDesc(
        String title, String description, Pageable pageable);
        
    // Find deeds by date range
    Page<Deed> findByStartDateTimeBetweenOrderByStartDateTimeDesc(
        LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
        
    // Find deeds after a specific date
    Page<Deed> findByStartDateTimeAfterOrderByStartDateTimeDesc(
        LocalDateTime startDate, Pageable pageable);
        
    // Find all approved deeds
    Page<Deed> findByIsApprovedTrueOrderByStartDateTimeDesc(Pageable pageable);
}
