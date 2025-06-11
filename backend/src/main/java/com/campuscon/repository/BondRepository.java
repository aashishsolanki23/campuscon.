package com.campuscon.repository;

import com.campuscon.model.Bond;
import com.campuscon.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BondRepository extends JpaRepository<Bond, Long> {
    
    @Query("SELECT b FROM Bond b WHERE (b.requester = ?1 AND b.receiver = ?2) OR (b.requester = ?2 AND b.receiver = ?1)")
    Optional<Bond> findBondBetweenUsers(User userOne, User userTwo);
    
    @Query("SELECT b FROM Bond b WHERE (b.requester = ?1 OR b.receiver = ?1) AND b.status = 'ACCEPTED'")
    List<Bond> findAllAcceptedBondsByUser(User user);
    
    @Query("SELECT b FROM Bond b WHERE b.receiver = ?1 AND b.status = 'PENDING'")
    List<Bond> findPendingBondRequestsForUser(User user);
    
    @Query("SELECT b FROM Bond b WHERE b.requester = ?1 AND b.status = 'PENDING'")
    List<Bond> findPendingBondRequestsSentByUser(User user);
    
    @Query("SELECT COUNT(b) FROM Bond b WHERE (b.requester = ?1 OR b.receiver = ?1) AND b.status = 'ACCEPTED'")
    Long countAcceptedBonds(User user);
    
    boolean existsByRequesterAndReceiverAndStatus(User requester, User receiver, Bond.BondStatus status);
    
    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Bond b " +
           "WHERE ((b.requester = ?1 AND b.receiver = ?2) OR (b.requester = ?2 AND b.receiver = ?1)) " +
           "AND b.status = 'ACCEPTED'")
    boolean areUsersBonded(User userOne, User userTwo);
    
    @Query("SELECT u FROM User u WHERE u <> ?1 AND EXISTS " +
           "(SELECT b FROM Bond b WHERE ((b.requester = u AND b.receiver = ?1) OR (b.requester = ?1 AND b.receiver = u)) " +
           "AND b.status = 'ACCEPTED')")
    List<User> findBondedUsers(User user);
    
    /**
     * Find all bonds (both as requester and receiver) for a specific user
     */
    @Query("SELECT b FROM Bond b WHERE (b.requester = ?1 OR b.receiver = ?1)")
    List<Bond> findAllBondsForUser(User user);
}
