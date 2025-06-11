package com.campuscon.service;

import com.campuscon.model.Ticket;
import com.campuscon.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TicketService {

    private final TicketRepository ticketRepository;
    private final QRCodeService qrCodeService;

    /**
     * Generate ticket for deed registration
     */
    @Transactional
    public Ticket generateTicket(Long userId, Long deedId) {
        // Check if ticket already exists
        Optional<Ticket> existingTicket = ticketRepository.findByUserIdAndDeedId(userId, deedId);
        if (existingTicket.isPresent()) {
            log.info("Ticket already exists for user {} and deed {}", userId, deedId);
            return existingTicket.get();
        }

        try {
            // Generate unique ticket code
            String ticketCode = qrCodeService.generateTicketCode(deedId, userId);
            
            // Generate QR code
            String qrContent = String.format("TICKET:%s|USER:%d|DEED:%d", ticketCode, userId, deedId);
            String qrCodePath = qrCodeService.generateQRCode(qrContent, ticketCode);

            // Create and save ticket
            Ticket ticket = Ticket.builder()
                    .userId(userId)
                    .deedId(deedId)
                    .ticketCode(ticketCode)
                    .qrCodePath(qrCodePath)
                    .createdAt(LocalDateTime.now())
                    .build();

            Ticket savedTicket = ticketRepository.save(ticket);
            log.info("Generated ticket {} for user {} and deed {}", ticketCode, userId, deedId);
            
            return savedTicket;
        } catch (Exception e) {
            log.error("Error generating ticket for user {} and deed {}", userId, deedId, e);
            throw new RuntimeException("Failed to generate ticket", e);
        }
    }

    /**
     * Get ticket by code
     */
    public Optional<Ticket> getTicketByCode(String ticketCode) {
        return ticketRepository.findByTicketCode(ticketCode);
    }

    /**
     * Get all tickets for a user
     */
    public List<Ticket> getUserTickets(Long userId) {
        return ticketRepository.findByUserId(userId);
    }

    /**
     * Get all tickets for a deed
     */
    public List<Ticket> getDeedTickets(Long deedId) {
        return ticketRepository.findByDeedId(deedId);
    }

    /**
     * Check if user has ticket for deed
     */
    public boolean hasTicket(Long userId, Long deedId) {
        return ticketRepository.existsByUserIdAndDeedId(userId, deedId);
    }

    /**
     * Get user's ticket for specific deed
     */
    public Optional<Ticket> getUserDeedTicket(Long userId, Long deedId) {
        return ticketRepository.findByUserIdAndDeedId(userId, deedId);
    }
}
