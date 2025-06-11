package com.campuscon.controller;

import com.campuscon.dto.ApiResponse;
import com.campuscon.dto.deed.DeedFormRequest;
import com.campuscon.dto.deed.DeedResponse;
import com.campuscon.model.User;
import com.campuscon.service.DeedFormService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for managing Deeds (events) in the CampusCon platform.
 * Provides endpoints for creating, retrieving, updating, and deleting Deeds.
 */
@RestController
@RequestMapping("/api/v1/deeds")
@RequiredArgsConstructor
@Slf4j
public class DeedFormController {

    private final DeedFormService deedFormService;
    
    /**
     * Create a new Deed
     * 
     * @param request The deed form request
     * @param user The authenticated user (society)
     * @return ResponseEntity with the created deed
     */
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<DeedResponse>> createDeed(
            @Valid @RequestBody DeedFormRequest request,
            @AuthenticationPrincipal User user) {
        
        log.info("Creating new deed: {}", request.getTitle());
        DeedResponse createdDeed = deedFormService.createDeed(request, user.getId());
        
        ApiResponse<DeedResponse> response = ApiResponse.success(createdDeed, "Deed created successfully");
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * Retrieve a deed by ID
     * 
     * @param id The ID of the deed to retrieve
     * @return ResponseEntity with the deed
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeedResponse>> getDeedById(@PathVariable Long id) {
        log.info("Fetching deed with ID: {}", id);
        DeedResponse deed = deedFormService.getDeedById(id);
        
        ApiResponse<DeedResponse> response = ApiResponse.success(deed);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Update an existing deed
     * 
     * @param id The ID of the deed to update
     * @param request The updated deed form request
     * @param user The authenticated user (society)
     * @return ResponseEntity with the updated deed
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<DeedResponse>> updateDeed(
            @PathVariable Long id,
            @Valid @RequestBody DeedFormRequest request,
            @AuthenticationPrincipal User user) {
        
        log.info("Updating deed with ID: {}", id);
        DeedResponse updatedDeed = deedFormService.updateDeed(id, request, user.getId());
        
        ApiResponse<DeedResponse> response = ApiResponse.success(updatedDeed, "Deed updated successfully");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a deed
     * 
     * @param id The ID of the deed to delete
     * @param user The authenticated user (society)
     * @return ResponseEntity with success message
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Void>> deleteDeed(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        
        log.info("Deleting deed with ID: {}", id);
        deedFormService.deleteDeed(id, user.getId());
        
        ApiResponse<Void> response = ApiResponse.success(null, "Deed deleted successfully");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all deeds with optional filtering
     * 
     * @param category Optional category filter
     * @param societyId Optional society ID filter
     * @param startDate Optional start date filter
     * @param endDate Optional end date filter
     * @param isTeamEvent Optional team event filter
     * @param isOpenForAll Optional open for all filter
     * @param searchTerm Optional search term for title and description
     * @param page Page number for pagination
     * @param size Page size for pagination
     * @param sort Sort field and direction for pagination
     * @return ResponseEntity with paginated deeds
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAllDeeds(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Long societyId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Boolean isTeamEvent,
            @RequestParam(required = false) Boolean isOpenForAll,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startDateTime,asc") String sort) {
        
        log.info("Fetching deeds with filters - category: {}, societyId: {}", category, societyId);
        
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") 
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        
        Page<DeedResponse> deedPage = deedFormService.getAllDeeds(
                category, societyId, startDate, endDate, isTeamEvent, isOpenForAll, searchTerm, pageable);
        
        Map<String, Object> pageData = new HashMap<>();
        pageData.put("content", deedPage.getContent());
        pageData.put("pageable", deedPage.getPageable());
        pageData.put("totalPages", deedPage.getTotalPages());
        pageData.put("totalElements", deedPage.getTotalElements());
        pageData.put("last", deedPage.isLast());
        pageData.put("first", deedPage.isFirst());
        pageData.put("size", deedPage.getSize());
        pageData.put("number", deedPage.getNumber());
        pageData.put("numberOfElements", deedPage.getNumberOfElements());
        pageData.put("empty", deedPage.isEmpty());
        
        ApiResponse<Map<String, Object>> response = ApiResponse.success(pageData);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Approve a deed (admin only)
     * 
     * @param id The ID of the deed to approve
     * @param user The authenticated user (admin)
     * @return ResponseEntity with the approved deed
     */
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DeedResponse>> approveDeed(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        
        log.info("Approving deed with ID: {}", id);
        DeedResponse approvedDeed = deedFormService.approveDeed(id, user.getId());
        
        ApiResponse<DeedResponse> response = ApiResponse.success(approvedDeed, "Deed approved successfully");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get deed categories
     * 
     * @return ResponseEntity with categories
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<String[]>> getDeedCategories() {
        // Get categories from the DeedCategory enum
        String[] categories = com.campuscon.enums.DeedCategory.getAllCategoryDisplayNames();
        
        ApiResponse<String[]> response = ApiResponse.success(categories);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get deeds specific to the authenticated user's college.
     * 
    //  * @param user The authenticated user
    //  * @param page Page number for pagination
    //  * @param size Page size for pagination
    //  * @param sort Sort field and direction for pagination
    //  * @return ResponseEntity with paginated college-specific deeds
    //  */
    // @GetMapping("/college")
    // @PreAuthorize("isAuthenticated()")
    // public ResponseEntity<ApiResponse<Page<DeedResponse>>> getCollegeDeeds(
    //         @AuthenticationPrincipal User user,
    //         @RequestParam(defaultValue = "0") int page,
    //         @RequestParam(defaultValue = "20") int size,
    //         @RequestParam(defaultValue = "startDateTime,desc") String sort) {
        
    //     // Parse sort parameters
    //     String[] sortParams = sort.split(",");
    //     String sortField = sortParams[0];
    //     Sort.Direction sortDirection = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("desc") ?
    //             Sort.Direction.DESC : Sort.Direction.ASC;
        
    //     // Create pageable request
    //     Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        
    //     // Get deeds from service
    //     Page<DeedResponse> deeds = deedFormService.getCollegeDeeds(user, pageable);
        
    //     // Create response
    //     ApiResponse<Page<DeedResponse>> response = ApiResponse.success(deeds);
        
    //     return ResponseEntity.ok(response);
    // }
}
