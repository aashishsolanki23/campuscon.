package com.campuscon.repository;

import com.campuscon.enums.DeedGroupType;
import com.campuscon.model.Deed;
import com.campuscon.model.DeedChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DeedChatGroupRepository extends JpaRepository<DeedChatGroup, Long> {
    
    /**
     * Find all chat groups associated with a deed
     */
    List<DeedChatGroup> findByDeedOrderByGroupTypeAsc(Deed deed);
    
    /**
     * Find a specific type of chat group for a deed
     */
    Optional<DeedChatGroup> findByDeedAndGroupType(Deed deed, DeedGroupType groupType);
    
    /**
     * Find a team-specific chat group
     */
    Optional<DeedChatGroup> findByDeedAndGroupTypeAndTeamId(Deed deed, DeedGroupType groupType, Long teamId);
    
    /**
     * Find all chat groups where a user is a member
     */
    @Query("SELECT dcg FROM DeedChatGroup dcg JOIN dcg.chatGroup.members m WHERE m.id = :userId")
    List<DeedChatGroup> findAllByMemberId(@Param("userId") Long userId);
    
    /**
     * Delete all chat groups associated with a deed
     */
    void deleteAllByDeed(Deed deed);
}
