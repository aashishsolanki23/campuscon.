package com.campuscon.repository;

import com.campuscon.model.ChatGroup;
import com.campuscon.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {
    // Auto-generated groups feature has been removed
    
    List<ChatGroup> findByMembersContaining(User user);
    
    /**
     * Find all chat groups that contain a specific user as a member
     * Alternative method name for the same functionality
     */
    List<ChatGroup> findByMembersContains(User user);
    
    Optional<ChatGroup> findByName(String name);
    
    // Batch year lookup method removed - auto-generated groups feature has been removed
    
    @Query("SELECT g FROM ChatGroup g JOIN g.members m WHERE m = ?1 ORDER BY g.createdAt DESC")
    List<ChatGroup> findUserGroups(User user);
    
    boolean existsByNameAndCreator(String name, User creator);
    
    /**
     * Remove a user from all chat groups
     * 
     * @param userId The ID of the user to remove
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM chat_group_members WHERE user_id = :userId", nativeQuery = true)
    void removeUserFromAllGroups(@Param("userId") Long userId);
}
