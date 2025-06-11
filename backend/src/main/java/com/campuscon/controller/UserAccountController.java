package com.campuscon.controller;

import com.campuscon.dto.ApiResponse;
import com.campuscon.dto.user.ChangeUsernameRequest;
import com.campuscon.dto.user.UserSummaryResponse;
import com.campuscon.model.User;
import com.campuscon.security.CurrentUser;
import com.campuscon.security.UserPrincipal;
import com.campuscon.service.UserAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * Controller for managing user accounts
 */
@RestController
@RequestMapping("/api/v1/account")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Account Management", description = "APIs for managing user accounts, including username changes and account deletion")
public class UserAccountController {

    private final UserAccountService userAccountService;
    
    /**
     * Change the username of the current user
     */
    @Operation(
        summary = "Change username",
        description = "Updates the username of the currently authenticated user"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Username updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid username format"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/username")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> changeUsername(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody ChangeUsernameRequest request) {
        
        log.info("Changing username for user ID: {}", currentUser.getId());
        User user = userAccountService.changeUsername(currentUser.getId(), request);
        
        UserSummaryResponse response = UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getUsername()) // Using username as name
                .role(user.getUserTypes() != null && !user.getUserTypes().isEmpty() ? 
                      String.join(",", user.getUserTypes()) : "USER")
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response, "Username updated successfully"));
    }
    
    /**
     * Update a user's organization role or display name
     */
    @Operation(
        summary = "Update organization info",
        description = "Updates the organization role or display name of the current user"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User info updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid format"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @PutMapping("/organization-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserSummaryResponse>> updateOrganizationInfo(
            @CurrentUser UserPrincipal currentUser,
            @Valid @RequestBody ChangeUsernameRequest request) {
        
        log.info("Updating organization info for user ID: {}", currentUser.getId());
        User user = userAccountService.updateUserOrganizationInfo(currentUser.getId(), request);
        
        UserSummaryResponse response = UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getUsername())
                .role(user.getUserTypes() != null && !user.getUserTypes().isEmpty() ? 
                      String.join(",", user.getUserTypes()) : "USER")
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response, "User info updated successfully"));
    }
    
    /**
     * Delete the current user's account
     */
    @Operation(
        summary = "Delete account",
        description = "Permanently deletes the current user's account and all associated data"
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Account deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @DeleteMapping("/")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<String>> deleteAccount(@CurrentUser UserPrincipal currentUser) {
        log.info("Deleting account for user ID: {}", currentUser.getId());
        
        boolean deleted = userAccountService.deleteUserAccount(currentUser.getId());
        if (deleted) {
            return ResponseEntity.ok(ApiResponse.success("Account deleted successfully"));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to delete account"));
        }
    }
}
