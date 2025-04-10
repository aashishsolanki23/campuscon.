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
import com.campuscon.security.CurrentUser;
import com.campuscon.security.UserPrincipal;
import com.campuscon.service.DeedRegistrationService;
import com.campuscon.util.AppConstants;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/deed-registrations")
@RequiredArgsConstructor
public class DeedRegistrationController {
    
    private final DeedRegistrationService deedRegistrationService;

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
        
        return ResponseEntity.ok(ApiResponse.success(
                convertToResponse(registration), "Successfully registered for deed"));
    }

    @GetMapping("/deed/{deedId}")
    @PreAuthorize("hasRole('SOCIETY')")
    public ResponseEntity<ApiResponse<PagedResponse<DeedRegistrationResponse>>> getDeedRegistrations(
            @PathVariable Long deedId,
            @RequestParam(required = false) DeedRegistration.RegistrationStatus status,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = AppConstants.DEFAULT_PAGE_SIZE) int size,
            @CurrentUser UserPrincipal currentUser) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.Direction.DESC, "registeredAt");
        
        Page<DeedRegistration> registrationsPage;
        if (status != null) {
            registrationsPage = deedRegistrationService.getDeedRegistrationsByStatus(deedId, status, pageable);
        } else {
            registrationsPage = deedRegistrationService.getDeedRegistrations(deedId, pageable);
        }
        
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

    @PutMapping("/{registrationId}/status")
    @PreAuthorize("hasRole('SOCIETY')")
    public ResponseEntity<ApiResponse<DeedRegistrationResponse>> updateRegistrationStatus(
            @PathVariable Long registrationId,
            @RequestParam DeedRegistration.RegistrationStatus status,
            @RequestParam(required = false) String rejectionReason,
            @CurrentUser UserPrincipal currentUser) {
        
        DeedRegistration registration = deedRegistrationService.updateRegistrationStatus(
                registrationId, status, rejectionReason, currentUser.getId());
        
        return ResponseEntity.ok(ApiResponse.success(
                convertToResponse(registration), "Registration status updated"));
    }

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
        
        DeedRegistration.RegistrationStatus status = deedRegistrationService.getRegistrationStatus(
                deedId, currentUser.getId());
        
        String statusStr = status != null ? status.name() : "NOT_REGISTERED";
        
        return ResponseEntity.ok(ApiResponse.success(statusStr, "Registration status retrieved"));
    }

    /**
     * Get registration counts for a specific deed
     */
    @GetMapping("/{deedId}/counts")
    public ResponseEntity<ApiResponse<DeedRegistrationCountsResponse>> getRegistrationCounts(
            @PathVariable Long deedId) {
        
        long totalCount = deedRegistrationService.countRegistrations(deedId);
        long pendingCount = deedRegistrationService.countRegistrationsByStatus(
                deedId, DeedRegistration.RegistrationStatus.PENDING);
        long approvedCount = deedRegistrationService.countRegistrationsByStatus(
                deedId, DeedRegistration.RegistrationStatus.APPROVED);
        long rejectedCount = deedRegistrationService.countRegistrationsByStatus(
                deedId, DeedRegistration.RegistrationStatus.REJECTED);
        
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
                .status(registration.getStatus())
                .rejectionReason(registration.getRejectionReason())
                .teamName(registration.getTeamName())
                .teamSize(registration.getTeamSize())
                .additionalInfo(registration.getAdditionalInfo())
                .deedTitle(registration.getDeed().getTitle())
                .username(registration.getUser().getUsername())
                .build();
    }
}
