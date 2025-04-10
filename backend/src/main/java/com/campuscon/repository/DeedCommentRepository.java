package com.campuscon.repository;

import com.campuscon.model.Deed;
import com.campuscon.model.DeedComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DeedCommentRepository extends JpaRepository<DeedComment, Long> {
    
    // Find comments for a deed
    Page<DeedComment> findByDeedAndParentCommentIsNullOrderByCreatedAtDesc(Deed deed, Pageable pageable);
    
    // Find replies to a comment
    List<DeedComment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);
    
    // Count comments for a deed
    long countByDeed(Deed deed);
    
    // Check if a comment is liked by a user
    @Query("SELECT CASE WHEN COUNT(dc) > 0 THEN true ELSE false END FROM DeedComment dc " +
           "JOIN dc.likes l WHERE dc.id = :commentId AND l.id = :userId")
    boolean isLikedByUser(@Param("commentId") Long commentId, @Param("userId") Long userId);
}
