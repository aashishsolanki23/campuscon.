package com.campuscon.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import com.campuscon.dto.ApiResponse;
import com.campuscon.dto.deed.registration.DeedRegistrationRequest;
import com.campuscon.dto.deed.registration.DeedRegistrationResponse;
import com.campuscon.dto.deed.registration.DeedRegistrationCountsResponse;
import com.campuscon.dto.PagedResponse;
import com.campuscon.model.DeedRegistration;
import com.campuscon.model.Ticket;
import com.campuscon.security.CurrentUser;
import com.campuscon.security.UserPrincipal;
import com.campuscon.service.DeedRegistrationService;
import com.campuscon.service.TicketService;
import com.campuscon.util.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/deed-registrations")
@RequiredArgsConstructor
@Slf4j
public class DeedRegistrationController {
    
    private final DeedRegistrationService deedRegistrationService;
    private final TicketService ticketService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<DeedRegistrationResponse>> registerForDeed(
            @Valid @RequestBody DeedRegistrationRequest registrationRequest,
            @CurrentUser UserPrincipal currentUser) {
        
        DeedRegistration registration = deedRegistrationService.registerForDeed(
                registrationRequest.getDeedId(),
                currentUser.getId(),
                registrationRequest.getTeamName(),
                registrationRequest.getTeamSize(),
                registrationRequest.getAdditionalInfo()
        );
        
        // Automatically generate ticket for the registration
        try {
            Ticket ticket = ticketService.generateTicket(
                currentUser.getId(), 
                registrationRequest.getDeedId()
            );
            log.info("Generated ticket {} for user {} registration to deed {}", 
                ticket.getTicketCode(), currentUser.getId(), registrationRequest.getDeedId());
        } catch (Exception e) {
            log.error("Failed to generate ticket for user {} and deed {}", 
                currentUser.getId(), registrationRequest.getDeedId(), e);
            // Continue with registration even if ticket generation fails
        }
        
        return ResponseEntity.ok(ApiResponse.success(
                convertToResponse(registration), "Successfully registered for deed and ticket generated"));
    }

    @GetMapping("/deed/{deedId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PagedResponse<DeedRegistrationResponse>>> getDeedRegistrations(
            @PathVariable Long deedId,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @CurrentUser UserPrincipal currentUser) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "registeredAt");
        
        Page<DeedRegistration> registrationsPage = deedRegistrationService.getDeedRegistrations(deedId, pageable);
        
        List<DeedRegistrationResponse> registrations = registrationsPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        PagedResponse<DeedRegistrationResponse> pagedResponse = new PagedResponse<>(
                registrations,
                registrationsPage.getNumber(),
                registrationsPage.getSize(),
                registrationsPage.getTotalElements(),
                registrationsPage.getTotalPages(),
                registrationsPage.isLast()
        );
        
        return ResponseEntity.ok(ApiResponse.success(pagedResponse, "Deed registrations retrieved successfully"));
    }

    @GetMapping("/user/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<PagedResponse<DeedRegistrationResponse>>> getUserRegistrations(
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @CurrentUser UserPrincipal currentUser) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "registeredAt");
        
        Page<DeedRegistration> registrationsPage = deedRegistrationService.getUserRegistrations(
                currentUser.getId(), pageable);
        
        List<DeedRegistrationResponse> registrations = registrationsPage.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        PagedResponse<DeedRegistrationResponse> pagedResponse = new PagedResponse<>(
                registrations,
                registrationsPage.getNumber(),
                registrationsPage.getSize(),
                registrationsPage.getTotalElements(),
                registrationsPage.getTotalPages(),
                registrationsPage.isLast()
        );
        
        return ResponseEntity.ok(ApiResponse.success(pagedResponse, "User registrations retrieved successfully"));
    }

    // Registration status updates no longer needed with single-click registration

    @DeleteMapping("/{deedId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> cancelRegistration(
            @PathVariable Long deedId,
            @CurrentUser UserPrincipal currentUser) {
        
        deedRegistrationService.cancelRegistration(deedId, currentUser.getId());
        
        return ResponseEntity.ok(ApiResponse.success(null, "Registration cancelled"));
    }

    @GetMapping("/{deedId}/status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> getRegistrationStatus(
            @PathVariable Long deedId,
            @CurrentUser UserPrincipal currentUser) {
        
        boolean isRegistered = deedRegistrationService.isUserRegisteredForDeed(
                deedId, currentUser.getId());
        
        String statusStr = isRegistered ? "REGISTERED" : "NOT_REGISTERED";
        
        return ResponseEntity.ok(ApiResponse.success(statusStr, "Registration status retrieved"));
    }

    /**
     * Get registration counts for a specific deed
     */
    @GetMapping("/{deedId}/counts")
    public ResponseEntity<ApiResponse<DeedRegistrationCountsResponse>> getRegistrationCounts(
            @PathVariable Long deedId) {
        
        long totalCount = deedRegistrationService.countRegistrations(deedId);
        
        // With single-click registration, we no longer track statuses
        // All registrations are automatically approved
        long pendingCount = 0;
        long approvedCount = totalCount;
        long rejectedCount = 0;
        
        DeedRegistrationCountsResponse counts = new DeedRegistrationCountsResponse(
            totalCount, pendingCount, approvedCount, rejectedCount);
        
        return ResponseEntity.ok(ApiResponse.success(counts, "Registration counts retrieved"));
    }

    /**
     * Helper method to convert DeedRegistration entity to DeedRegistrationResponse
     */
    private DeedRegistrationResponse convertToResponse(DeedRegistration registration) {
        return DeedRegistrationResponse.builder()
                .id(registration.getId())
                .deedId(registration.getDeed().getId())
                .userId(registration.getUser().getId())
                .registeredAt(registration.getRegisteredAt())
                .teamName(registration.getTeamName())
                .teamSize(registration.getTeamSize())
                .additionalInfo(registration.getAdditionalInfo())
                .deedTitle(registration.getDeed().getTitle())
                .username(registration.getUser().getUsername())
                .build();
    }
}
