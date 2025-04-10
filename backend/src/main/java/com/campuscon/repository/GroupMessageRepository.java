package com.campuscon.repository;

import com.campuscon.model.ChatGroup;
import com.campuscon.model.GroupMessage;
import com.campuscon.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {
    
    Page<GroupMessage> findByGroupOrderBySentAtDesc(ChatGroup group, Pageable pageable);
    
    List<GroupMessage> findByGroupAndIsPinnedTrue(ChatGroup group);
    
    @Query("SELECT m FROM GroupMessage m WHERE m.group = ?1 AND m.isDeleted = false ORDER BY m.sentAt DESC")
    Page<GroupMessage> findActiveMessagesByGroup(ChatGroup group, Pageable pageable);
    
    @Query("SELECT COUNT(m) FROM GroupMessage m WHERE m.group = ?1 AND m.sender <> ?2 AND ?2 NOT MEMBER OF m.readBy")
    Long countUnreadMessages(ChatGroup group, User user);
    
    List<GroupMessage> findBySender(User sender);
}
