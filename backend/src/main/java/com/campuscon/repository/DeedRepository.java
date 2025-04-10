package com.campuscon.repository;

import com.campuscon.model.Deed;
import com.campuscon.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;


public interface DeedRepository extends JpaRepository<Deed, Long> {
    
    // Find deeds by society (for society profile page)
    Page<Deed> findBySocietyOrderByCreatedAtDesc(User society, Pageable pageable);
    
    // Find deeds from a specific college
    @Query("SELECT d FROM Deed d WHERE d.society.collegeName = :collegeName AND d.isApproved = true ORDER BY d.eventDate DESC")
    Page<Deed> findByCollegeNameOrderByEventDateDesc(@Param("collegeName") String collegeName, Pageable pageable);
    
    // Find deeds from a specific university
    @Query("SELECT d FROM Deed d WHERE d.society.universityName = :universityName AND d.isApproved = true ORDER BY d.eventDate DESC")
    Page<Deed> findByUniversityNameOrderByEventDateDesc(@Param("universityName") String universityName, Pageable pageable);
    
    // Find upcoming deeds
    @Query("SELECT d FROM Deed d WHERE d.eventDate > :now AND d.isApproved = true ORDER BY d.eventDate ASC")
    Page<Deed> findUpcomingDeeds(@Param("now") LocalDateTime now, Pageable pageable);
    
    // Find deeds by category
    Page<Deed> findByCategoryAndIsApprovedTrueOrderByEventDateDesc(String category, Pageable pageable);
    
    // Find deeds saved by a user
    @Query("SELECT d FROM Deed d JOIN d.savedByUsers u WHERE u.id = :userId ORDER BY d.eventDate DESC")
    Page<Deed> findSavedDeedsByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // Count deeds by society
    long countBySociety(User society);
    
    // Check if a deed is liked by a user
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Deed d JOIN d.likedByUsers u WHERE d.id = :deedId AND u.id = :userId")
    boolean isLikedByUser(@Param("deedId") Long deedId, @Param("userId") Long userId);
    
    // Check if a deed is saved by a user
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Deed d JOIN d.savedByUsers u WHERE d.id = :deedId AND u.id = :userId")
    boolean isSavedByUser(@Param("deedId") Long deedId, @Param("userId") Long userId);
    
    // Find most popular deeds (for home page)
    @Query("SELECT d FROM Deed d WHERE d.isApproved = true ORDER BY (d.likesCount + d.commentsCount + d.savesCount) DESC")
    Page<Deed> findMostPopularDeeds(Pageable pageable);
}
