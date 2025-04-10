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

@RestController
@RequestMapping("/api/deeds")
@RequiredArgsConstructor
public class DeedController {

    private final DeedService deedService;
    private final AuthorizationService authorizationService;

    /**
     * Create a new deed (society event) with banner upload
     * Only societies can create deeds
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<DeedResponse>> createDeed(
            @RequestParam("banner") MultipartFile banner,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("eventDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime eventDate,
            @RequestParam("venue") String venue,
            @RequestParam("category") String category,
            @AuthenticationPrincipal Long societyId) throws IOException {
        
        // Check if user is authorized to create a deed (only societies can)
        authorizationService.checkDeedCreationPermission(societyId);
        
        Deed deed = deedService.createDeed(banner, title, description, eventDate, venue, category, societyId);
        DeedResponse response = mapToDeedResponse(deed, societyId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Deed created successfully"));
    }

    /**
     * Get deed by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DeedResponse>> getDeedById(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Long userId) {
        
        Deed deed = deedService.getDeedById(id);
        DeedResponse response = mapToDeedResponse(deed, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Deed retrieved successfully"));
    }

    /**
     * Get deeds by society (for society profile)
     */
    @GetMapping("/society/{societyId}")
    public ResponseEntity<ApiResponse<Page<DeedResponse>>> getSocietyDeeds(
            @PathVariable("societyId") Long profileSocietyId,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal Long userId) {
        
        Page<Deed> deeds = deedService.getSocietyDeeds(profileSocietyId, pageable);
        Page<DeedResponse> response = deeds.map(deed -> mapToDeedResponse(deed, userId));
        return ResponseEntity.ok(ApiResponse.success(response, "Society deeds retrieved successfully"));
    }

    /**
     * Get deeds by college (for home page)
     */
    @GetMapping("/college/{collegeName}")
    public ResponseEntity<ApiResponse<Page<DeedResponse>>> getCollegeDeeds(
            @PathVariable("collegeName") String collegeName,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal Long userId) {
        
        Page<Deed> deeds = deedService.getCollegeDeeds(collegeName, pageable);
        Page<DeedResponse> response = deeds.map(deed -> mapToDeedResponse(deed, userId));
        return ResponseEntity.ok(ApiResponse.success(response, "College deeds retrieved successfully"));
    }

    /**
     * Get deeds by university (for home page)
     */
    @GetMapping("/university/{universityName}")
    public ResponseEntity<ApiResponse<Page<DeedResponse>>> getUniversityDeeds(
            @PathVariable("universityName") String universityName,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal Long userId) {
        
        Page<Deed> deeds = deedService.getUniversityDeeds(universityName, pageable);
        Page<DeedResponse> response = deeds.map(deed -> mapToDeedResponse(deed, userId));
        return ResponseEntity.ok(ApiResponse.success(response, "University deeds retrieved successfully"));
    }

    /**
     * Get upcoming deeds
     */
    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<Page<DeedResponse>>> getUpcomingDeeds(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal Long userId) {
        
        Page<Deed> deeds = deedService.getUpcomingDeeds(pageable);
        Page<DeedResponse> response = deeds.map(deed -> mapToDeedResponse(deed, userId));
        return ResponseEntity.ok(ApiResponse.success(response, "Upcoming deeds retrieved successfully"));
    }

    /**
     * Get deeds by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<Page<DeedResponse>>> getDeedsByCategory(
            @PathVariable("category") String category,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal Long userId) {
        
        Page<Deed> deeds = deedService.getDeedsByCategory(category, pageable);
        Page<DeedResponse> response = deeds.map(deed -> mapToDeedResponse(deed, userId));
        return ResponseEntity.ok(ApiResponse.success(response, "Category deeds retrieved successfully"));
    }

    /**
     * Get popular deeds (for home page)
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<Page<DeedResponse>>> getPopularDeeds(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal Long userId) {
        
        Page<Deed> deeds = deedService.getPopularDeeds(pageable);
        Page<DeedResponse> response = deeds.map(deed -> mapToDeedResponse(deed, userId));
        return ResponseEntity.ok(ApiResponse.success(response, "Popular deeds retrieved successfully"));
    }

    /**
     * Like a deed
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Void>> likeDeed(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Long userId) {
        
        deedService.likeDeed(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Deed liked successfully"));
    }

    /**
     * Unlike a deed
     */
    @DeleteMapping("/{id}/like")
    public ResponseEntity<ApiResponse<Void>> unlikeDeed(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Long userId) {
        
        deedService.unlikeDeed(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Deed unliked successfully"));
    }

    /**
     * Save a deed
     */
    @PostMapping("/{id}/save")
    public ResponseEntity<ApiResponse<Void>> saveDeed(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Long userId) {
        
        deedService.saveDeed(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Deed saved successfully"));
    }

    /**
     * Unsave a deed
     */
    @DeleteMapping("/{id}/save")
    public ResponseEntity<ApiResponse<Void>> unsaveDeed(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Long userId) {
        
        deedService.unsaveDeed(id, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Deed unsaved successfully"));
    }

    /**
     * Add comment to a deed
     */
    @PostMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<DeedCommentResponse>> addComment(
            @PathVariable("id") Long id,
            @RequestBody DeedCommentRequest request,
            @AuthenticationPrincipal Long userId) {
        
        DeedComment comment = deedService.addComment(id, userId, request.getContent(), request.getParentCommentId());
        DeedCommentResponse response = mapToCommentResponse(comment);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response, "Comment added successfully"));
    }

    /**
     * Get comments for a deed
     */
    @GetMapping("/{id}/comments")
    public ResponseEntity<ApiResponse<Page<DeedCommentResponse>>> getDeedComments(
            @PathVariable("id") Long id,
            @PageableDefault(size = 20) Pageable pageable) {
        
        Page<DeedComment> comments = deedService.getDeedComments(id, pageable);
        Page<DeedCommentResponse> response = comments.map(this::mapToCommentResponse);
        return ResponseEntity.ok(ApiResponse.success(response, "Deed comments retrieved successfully"));
    }

    /**
     * Get replies to a comment
     */
    @GetMapping("/comments/{commentId}/replies")
    public ResponseEntity<ApiResponse<List<DeedCommentResponse>>> getCommentReplies(
            @PathVariable("commentId") Long commentId) {
        
        List<DeedComment> replies = deedService.getCommentReplies(commentId);
        List<DeedCommentResponse> response = replies.stream()
                .map(this::mapToCommentResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response, "Comment replies retrieved successfully"));
    }

    /**
     * Delete a deed
     * Only the society that created the deed or an admin can delete it
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDeed(
            @PathVariable("id") Long id,
            @AuthenticationPrincipal Long societyId) {
        
        Deed deed = deedService.getDeedById(id);
        
        // Check if user is authorized to delete this deed
        authorizationService.checkDeedModificationPermission(societyId, deed.getSociety().getId());
        
        deedService.deleteDeed(id, societyId);
        return ResponseEntity.ok(ApiResponse.success(null, "Deed deleted successfully"));
    }

    /**
     * Share a deed
     */
    @PostMapping("/{id}/share")
    public ResponseEntity<ApiResponse<Void>> shareDeed(
            @PathVariable("id") Long id) {
        
        deedService.shareDeed(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Deed shared successfully"));
    }

    /**
     * Helper method to map Deed entity to DeedResponse DTO
     */
    private DeedResponse mapToDeedResponse(Deed deed, Long currentUserId) {
        return DeedResponse.builder()
                .id(deed.getId())
                .title(deed.getTitle())
                .description(deed.getDescription())
                .bannerUrl(deed.getBannerUrl())
                .eventDate(deed.getEventDate())
                .venue(deed.getVenue())
                .category(deed.getCategory())
                .societyId(deed.getSociety().getId())
                .societyName(deed.getSociety().getUsername())
                .createdAt(deed.getCreatedAt())
                .likesCount((int)deed.getLikesCount())
                .commentsCount((int)deed.getCommentsCount())
                .savesCount((int)deed.getSavesCount())
                .sharesCount((int)deed.getSharesCount())
                .liked(deedService.isLikedByUser(deed.getId(), currentUserId))
                .saved(deedService.isSavedByUser(deed.getId(), currentUserId))
                .build();
    }

    /**
     * Helper method to map DeedComment entity to DeedCommentResponse DTO
     */
    private DeedCommentResponse mapToCommentResponse(DeedComment comment) {
        DeedCommentResponse response = DeedCommentResponse.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .userId(comment.getUser().getId())
                .userName(comment.getUser().getUsername())
                .userProfilePicture(comment.getUser().getProfilePictureUrl())
                .deedId(comment.getDeed().getId())
                .createdAt(comment.getCreatedAt())
                .replyCount(comment.getReplies() != null ? comment.getReplies().size() : 0)
                .updatedAt(comment.getUpdatedAt())
                .build();
        
        if (comment.getParentComment() != null) {
            response.setParentCommentId(comment.getParentComment().getId());
        }
        
        return response;
    }
}
