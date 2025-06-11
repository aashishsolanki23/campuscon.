package com.campuscon.controller;

import com.campuscon.dto.ApiResponse;
import com.campuscon.dto.ticket.TicketResponse;
import com.campuscon.model.Ticket;
import com.campuscon.security.CurrentUser;
import com.campuscon.security.UserPrincipal;
import com.campuscon.service.TicketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", maxAge = 3600)
public class TicketController {

    private final TicketService ticketService;

    @GetMapping("/my-tickets")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getUserTickets(
            @CurrentUser UserPrincipal currentUser) {
        
        List<Ticket> tickets = ticketService.getUserTickets(currentUser.getId());
        List<TicketResponse> ticketResponses = tickets.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(
                ticketResponses, "User tickets retrieved successfully"));
    }

    @GetMapping("/code/{ticketCode}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketByCode(
            @PathVariable String ticketCode) {
        
        Optional<Ticket> ticket = ticketService.getTicketByCode(ticketCode);
        
        if (ticket.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(
                    convertToResponse(ticket.get()), "Ticket found"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/deed/{deedId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getDeedTickets(
            @PathVariable Long deedId) {
        
        List<Ticket> tickets = ticketService.getDeedTickets(deedId);
        List<TicketResponse> ticketResponses = tickets.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(
                ticketResponses, "Deed tickets retrieved successfully"));
    }

    @GetMapping("/user/{userId}/deed/{deedId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<TicketResponse>> getUserDeedTicket(
            @PathVariable Long userId,
            @PathVariable Long deedId,
            @CurrentUser UserPrincipal currentUser) {
        
        // Users can only access their own tickets unless they have admin role
        if (!userId.equals(currentUser.getId()) && !currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        Optional<Ticket> ticket = ticketService.getUserDeedTicket(userId, deedId);
        
        if (ticket.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(
                    convertToResponse(ticket.get()), "User deed ticket found"));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/generate/{deedId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<TicketResponse>> generateTicket(
            @PathVariable Long deedId,
            @CurrentUser UserPrincipal currentUser) {
        
        try {
            Ticket ticket = ticketService.generateTicket(currentUser.getId(), deedId);
            return ResponseEntity.ok(ApiResponse.success(
                    convertToResponse(ticket), "Ticket generated successfully"));
        } catch (Exception e) {
            log.error("Error generating ticket for user {} and deed {}", 
                    currentUser.getId(), deedId, e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Failed to generate ticket: " + e.getMessage()));
        }
    }

    private TicketResponse convertToResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .userId(ticket.getUserId())
                .deedId(ticket.getDeedId())
                .ticketCode(ticket.getTicketCode())
                .qrCodePath(ticket.getQrCodePath())
                .createdAt(ticket.getCreatedAt())
                .deedTitle(ticket.getDeed() != null ? ticket.getDeed().getTitle() : null)
                .userName(ticket.getUser() != null ? ticket.getUser().getName() : null)
                .build();
    }
}
