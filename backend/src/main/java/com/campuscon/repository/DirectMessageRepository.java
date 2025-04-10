package com.campuscon.repository;

import com.campuscon.model.DirectMessage;
import com.campuscon.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DirectMessageRepository extends JpaRepository<DirectMessage, Long> {
    
    @Query("SELECT m FROM DirectMessage m WHERE (m.sender = ?1 AND m.receiver = ?2) OR (m.sender = ?2 AND m.receiver = ?1) ORDER BY m.sentAt DESC")
    Page<DirectMessage> findConversation(User userOne, User userTwo, Pageable pageable);
    
    @Query("SELECT m FROM DirectMessage m WHERE m.receiver = ?1 AND m.isRead = false")
    List<DirectMessage> findUnreadMessagesForUser(User user);
    
    @Query("SELECT COUNT(m) FROM DirectMessage m WHERE m.receiver = ?1 AND m.isRead = false")
    Long countUnreadMessagesForUser(User user);
    
    @Query("SELECT DISTINCT m.sender FROM DirectMessage m WHERE m.receiver = ?1 AND m.isRead = false")
    List<User> findUsersWithUnreadMessages(User user);
    
    @Query("SELECT DISTINCT CASE WHEN m.sender = ?1 THEN m.receiver ELSE m.sender END FROM DirectMessage m WHERE m.sender = ?1 OR m.receiver = ?1 ORDER BY m.sentAt DESC")
    List<User> findRecentConversationPartners(User user, Pageable pageable);
    
    List<DirectMessage> findByIsPinnedTrueAndSenderAndReceiver(User sender, User receiver);
    
    @Query("SELECT m FROM DirectMessage m WHERE ((m.sender = ?1 AND m.receiver = ?2) OR (m.sender = ?2 AND m.receiver = ?1)) AND m.isDeleted = false ORDER BY m.sentAt DESC")
    Page<DirectMessage> findActiveConversation(User userOne, User userTwo, Pageable pageable);
}
