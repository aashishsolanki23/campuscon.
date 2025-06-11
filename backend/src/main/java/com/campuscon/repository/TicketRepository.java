package com.campuscon.repository;

import com.campuscon.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    Optional<Ticket> findByTicketCode(String ticketCode);
    
    List<Ticket> findByUserId(Long userId);
    
    List<Ticket> findByDeedId(Long deedId);
    
    Optional<Ticket> findByUserIdAndDeedId(Long userId, Long deedId);
    
    boolean existsByUserIdAndDeedId(Long userId, Long deedId);
}
