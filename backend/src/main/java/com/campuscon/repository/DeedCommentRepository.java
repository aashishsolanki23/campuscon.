package com.campuscon.repository;

import com.campuscon.model.Deed;
import com.campuscon.model.DeedComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
// Query and Param imports removed as they are no longer needed

import java.util.List;

public interface DeedCommentRepository extends JpaRepository<DeedComment, Long> {
    
    // Find comments for a deed
    Page<DeedComment> findByDeedAndParentCommentIsNullOrderByCreatedAtDesc(Deed deed, Pageable pageable);
    
    // Find replies to a comment
    List<DeedComment> findByParentCommentIdOrderByCreatedAtAsc(Long parentCommentId);
    
    // Count comments for a deed
    long countByDeed(Deed deed);
    
    // isLikedByUser method has been removed
}
