package com.campuscon.repository;

import com.campuscon.model.Brick;
import com.campuscon.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BrickRepository extends JpaRepository<Brick, Long> {
    
    // Find bricks by user (for profile page)
    Page<Brick> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);
    
    // Find bricks by users (for bricks page - showing bricks from bonded users)
    @Query("SELECT b FROM Brick b WHERE b.user IN :users AND b.isApproved = true ORDER BY b.createdAt DESC")
    Page<Brick> findByUserInOrderByCreatedAtDesc(@Param("users") List<User> users, Pageable pageable);
    
    // Find bricks saved by a user
    @Query("SELECT b FROM Brick b JOIN b.savedByUsers u WHERE u.id = :userId ORDER BY b.createdAt DESC")
    Page<Brick> findSavedBricksByUserId(@Param("userId") Long userId, Pageable pageable);
    
    // Count bricks by user
    long countByUser(User user);
    
    // Check if a brick is liked by a user
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Brick b JOIN b.likedByUsers u WHERE b.id = :brickId AND u.id = :userId")
    boolean isLikedByUser(@Param("brickId") Long brickId, @Param("userId") Long userId);
    
    // Check if a brick is saved by a user
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Brick b JOIN b.savedByUsers u WHERE b.id = :brickId AND u.id = :userId")
    boolean isSavedByUser(@Param("brickId") Long brickId, @Param("userId") Long userId);
    
    // Find most popular bricks (for discovery)
    @Query(value = "SELECT * FROM bricks WHERE is_approved = true ORDER BY (likes_count + comments_count + saves_count) DESC", nativeQuery = true)
    Page<Brick> findMostPopularBricks(Pageable pageable);
}
