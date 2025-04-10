package com.campuscon.repository;

import com.campuscon.model.Brick;
import com.campuscon.model.BrickComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BrickCommentRepository extends JpaRepository<BrickComment, Long> {
    
    // Find comments for a brick
    Page<BrickComment> findByBrickAndParentCommentIsNullOrderByCreatedAtDesc(Brick brick, Pageable pageable);
    
    // Find replies to a comment
    List<BrickComment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);
    
    // Count comments for a brick
    long countByBrick(Brick brick);
    
    // Check if a comment is liked by a user
    @Query("SELECT CASE WHEN COUNT(bc) > 0 THEN true ELSE false END FROM BrickComment bc " +
           "JOIN bc.likes l WHERE bc.id = :commentId AND l.id = :userId")
    boolean isLikedByUser(@Param("commentId") Long commentId, @Param("userId") Long userId);
}
