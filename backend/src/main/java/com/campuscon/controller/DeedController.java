package com.campuscon.controller;

import com.campuscon.dto.ApiResponse;
import com.campuscon.dto.deed.comment.DeedCommentRequest;
import com.campuscon.dto.deed.comment.DeedCommentResponse;
import com.campuscon.dto.deed.DeedResponse;
import com.campuscon.model.Deed;
import com.campuscon.model.DeedComment;
import com.campuscon.service.AuthorizationService;
import com.campuscon.service.DeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;    
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for deed-related operations
 */
@RestController
@RequestMapping("/api/deeds")
@RequiredArgsConstructor
public class DeedController {

    private final DeedService deedService;
    private final AuthorizationService authorizationService;
    
    /**
     * Get all deeds with optional filtering
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<DeedResponse>>> getDeeds(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String search,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal Long userId
    ) {
        Page<Deed> deeds = deedService.getDeeds(category, startDate, endDate, search, pageable);
        Page<DeedResponse> deedResponses = deeds.map(deed -> mapToDeedResponse(deed, userId));
        return ResponseEntity.ok(ApiResponse.success(deedResponses));
    }
    
    /**
     * Get all deeds created by a specific user
     */
    @GetMapping("/creator/{creatorId}")
    public ResponseEntity<ApiResponse<Page<DeedResponse>>> getCreatorDeeds(
            @PathVariable Long creatorId,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal Long userId
    ) {
        Page<Deed> deeds = deedService.getDeedsByCreatorId(creatorId, pageable);
        Page<DeedResponse> deedResponses = deeds.map(deed -> mapToDeedResponse(deed, userId));
        return ResponseEntity.ok(ApiResponse.success(deedResponses));
    }

    /**
     * Get a deed by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeedResponse>> getDeed(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        Deed deed = deedService.getDeedById(id);
        DeedResponse deedResponse = mapToDeedResponse(deed, userId);
        return ResponseEntity.ok(ApiResponse.success(deedResponse));
    }
    
    /**
     * Create a new deed
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DeedResponse>> createDeed(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "bannerImage", required = false) MultipartFile bannerImage,
            @RequestParam("category") String category,
            @RequestParam("venue") String venue,
            @RequestParam(value = "registrationEnabled", defaultValue = "true") boolean registrationEnabled,
            @RequestParam(value = "startDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @RequestParam(value = "endDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime,
            @AuthenticationPrincipal Long userId
    ) throws IOException {
        // Check if user is authorized to create a deed (creator role check would be done in the service)
        
        Deed deed = deedService.createDeed(
                title, description, bannerImage, 
                category, venue, startDateTime, 
                endDateTime, registrationEnabled, userId
        );
        
        DeedResponse deedResponse = mapToDeedResponse(deed, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(deedResponse, "Deed created successfully"));
    }
    
    /**
     * Update an existing deed
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DeedResponse>> updateDeed(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "bannerImage", required = false) MultipartFile bannerImage,
            @RequestParam("category") String category,
            @RequestParam("venue") String venue,
            @RequestParam(value = "registrationEnabled", defaultValue = "true") boolean registrationEnabled,
            @RequestParam(value = "startDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDateTime,
            @RequestParam(value = "endDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDateTime,
            @AuthenticationPrincipal Long userId
    ) throws IOException {
        // Check if the user is authorized to update this deed
        Deed existingDeed = deedService.getDeedById(id);
        authorizationService.checkDeedModificationPermission(userId, existingDeed.getCreator().getId());
        
        Deed updatedDeed = deedService.updateDeed(
                id, title, description, bannerImage, 
                category, venue, startDateTime, 
                endDateTime, registrationEnabled, userId
        );
        
        DeedResponse deedResponse = mapToDeedResponse(updatedDeed, userId);
        return ResponseEntity.ok(ApiResponse.success(deedResponse, "Deed updated successfully"));
    }
    
    /**
     * Delete a deed
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDeed(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId
    ) {
        deedService.deleteDeed(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Deed deleted successfully"));
    }
    
    /**
     * Add a comment to a deed
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<DeedCommentResponse>> addComment(
            @PathVariable Long id,
            @RequestBody DeedCommentRequest commentRequest,
            @AuthenticationPrincipal Long userId
    ) {
        DeedComment comment = deedService.addComment(
                id, userId, commentRequest.getContent(), commentRequest.getParentCommentId()
        );
        
        DeedCommentResponse commentResponse = mapToCommentResponse(comment);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(commentResponse, "Comment added successfully"));
    }
    
    /**
     * Get comments for a deed
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<Page<DeedCommentResponse>>> getComments(
            @PathVariable Long id,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        Page<DeedComment> comments = deedService.getDeedComments(id, pageable);
        Page<DeedCommentResponse> commentResponses = comments.map(this::mapToCommentResponse);
        return ResponseEntity.ok(ApiResponse.success(commentResponses, "Deed comments retrieved"));
    }
    
    /**
     * Get replies to a comment
     */
    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<List<DeedCommentResponse>>> getCommentReplies(
            @PathVariable Long commentId
    ) {
        List<DeedComment> replies = deedService.getCommentReplies(commentId);
        List<DeedCommentResponse> replyResponses = replies.stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(ApiResponse.success(replyResponses, "Comment replies retrieved"));
    }
    
    /**
     * Map a Deed entity to a DeedResponse DTO
     */
    private DeedResponse mapToDeedResponse(Deed deed, Long currentUserId) {
        return DeedResponse.builder()
                .id(deed.getId())
                .title(deed.getTitle())
                .description(deed.getDescription())
                .bannerUrl(deed.getBannerUrl())
                .eventDate(deed.getStartDateTime()) // Using startDateTime as eventDate for backward compatibility
                .venue(deed.getVenue())
                .category(deed.getCategoryDisplayName())
                .creatorId(deed.getCreator().getId())
                .creatorName(deed.getCreator().getUsername())
                .createdAt(deed.getCreatedAt())
                .commentsCount((int)deed.getCommentsCount())
                .savesCount((int)deed.getSavesCount())
                .sharesCount((int)deed.getSharesCount())
                .saved(deedService.isSavedByUser(deed.getId(), currentUserId))
                .registrationEnabled(deed.isRegistrationEnabled())
                .registrationsCount(deed.getRegistrations() != null ? deed.getRegistrations().size() : 0)
                .startDateTime(deed.getStartDateTime())
                .endDateTime(deed.getEndDateTime())
                .build();
    }
    
    /**
     * Map a DeedComment entity to a DeedCommentResponse DTO
     */
    private DeedCommentResponse mapToCommentResponse(DeedComment comment) {
        DeedCommentResponse response = DeedCommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUser() != null ? comment.getUser().getId() : null)
                .userName(comment.getUser() != null ? comment.getUser().getUsername() : "Unknown User")
                .userProfilePicture(comment.getUser() != null ? comment.getUser().getProfilePictureUrl() : null)
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .build();
                
        if (comment.getParentComment() != null) {
            response.setParentCommentId(comment.getParentComment().getId());
        }
        
        return response;
    }
}
