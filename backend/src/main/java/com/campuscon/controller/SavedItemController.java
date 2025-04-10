package com.campuscon.controller;

import com.campuscon.dto.ApiResponse;
import com.campuscon.dto.brick.BrickResponse;
import com.campuscon.dto.deed.DeedResponse;
import com.campuscon.dto.saved.SavedItemResponse;
import com.campuscon.dto.user.UserSummaryResponse;
import com.campuscon.model.Brick;
import com.campuscon.model.Deed;
import com.campuscon.model.SavedItem;
import com.campuscon.model.User;
import com.campuscon.service.BrickService;
import com.campuscon.service.DeedService;
import com.campuscon.service.SavedItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/saved")
@RequiredArgsConstructor
public class SavedItemController {

    private final SavedItemService savedItemService;
    private final BrickService brickService;
    private final DeedService deedService;

    /**
     * Get all saved items by user (mixed types)
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SavedItemResponse>>> getSavedItems(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal Long userId) {
        
        Page<SavedItem> savedItems = savedItemService.getSavedItems(userId, pageable);
        Page<SavedItemResponse> response = savedItems.map(this::mapToSavedItemResponse);
        return ResponseEntity.ok(ApiResponse.success(response, "Saved items retrieved successfully"));
    }

    /**
     * Get saved items by type
     */
    @GetMapping("/type/{itemType}")
    public ResponseEntity<ApiResponse<Page<SavedItemResponse>>> getSavedItemsByType(
            @PathVariable("itemType") String itemType,
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal Long userId) {
        
        SavedItem.ItemType type = SavedItem.ItemType.valueOf(itemType.toUpperCase());
        Page<SavedItem> savedItems = savedItemService.getSavedItemsByType(userId, type, pageable);
        Page<SavedItemResponse> response = savedItems.map(this::mapToSavedItemResponse);
        return ResponseEntity.ok(ApiResponse.success(response, "Saved items of type " + itemType + " retrieved successfully"));
    }

    /**
     * Get saved bricks
     */
    @GetMapping("/bricks")
    public ResponseEntity<ApiResponse<Page<BrickResponse>>> getSavedBricks(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal Long userId) {
        
        Page<Brick> bricks = savedItemService.getSavedBricks(userId, pageable);
        Page<BrickResponse> response = bricks.map(brick -> mapToBrickResponse(brick, userId));
        return ResponseEntity.ok(ApiResponse.success(response, "Saved bricks retrieved successfully"));
    }

    /**
     * Get saved deeds
     */
    @GetMapping("/deeds")
    public ResponseEntity<ApiResponse<Page<DeedResponse>>> getSavedDeeds(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal Long userId) {
        
        Page<Deed> deeds = savedItemService.getSavedDeeds(userId, pageable);
        Page<DeedResponse> response = deeds.map(deed -> mapToDeedResponse(deed, userId));
        return ResponseEntity.ok(ApiResponse.success(response, "Saved deeds retrieved successfully"));
    }

    /**
     * Get saved societies
     */
    @GetMapping("/societies")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> getSavedSocieties(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal Long userId) {
        
        List<User> societies = savedItemService.getSavedSocieties(userId, pageable);
        List<UserSummaryResponse> response = societies.stream()
                .map(user -> mapToUserSummaryResponse(user))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response, "Saved societies retrieved successfully"));
    }

    /**
     * Save a society
     */
    @PostMapping("/societies/{societyId}")
    public ResponseEntity<ApiResponse<Void>> saveSociety(
            @PathVariable("societyId") Long societyId,
            @AuthenticationPrincipal Long userId) {
        
        savedItemService.saveSociety(userId, societyId);
        return ResponseEntity.ok(ApiResponse.success(null, "Society saved successfully"));
    }

    /**
     * Unsave a society
     */
    @DeleteMapping("/societies/{societyId}")
    public ResponseEntity<ApiResponse<Void>> unsaveSociety(
            @PathVariable("societyId") Long societyId,
            @AuthenticationPrincipal Long userId) {
        
        savedItemService.unsaveSociety(userId, societyId);
        return ResponseEntity.ok(ApiResponse.success(null, "Society unsaved successfully"));
    }

    /**
     * Check if society is saved
     */
    @GetMapping("/societies/{societyId}/check")
    public ResponseEntity<ApiResponse<Boolean>> isSocietySaved(
            @PathVariable("societyId") Long societyId,
            @AuthenticationPrincipal Long userId) {
        
        boolean isSaved = savedItemService.isSocietySavedByUser(userId, societyId);
        return ResponseEntity.ok(ApiResponse.success(isSaved, "Society saved status retrieved successfully"));
    }

    /**
     * Delete a saved item
     */
    @DeleteMapping("/{savedItemId}")
    public ResponseEntity<ApiResponse<Void>> deleteSavedItem(
            @PathVariable("savedItemId") Long savedItemId,
            @AuthenticationPrincipal Long userId) {
        
        savedItemService.deleteSavedItem(savedItemId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "Item unsaved successfully"));
    }

    /**
     * Helper method to map SavedItem entity to SavedItemResponse DTO
     */
    private SavedItemResponse mapToSavedItemResponse(SavedItem savedItem) {
        return SavedItemResponse.builder()
                .id(savedItem.getId())
                .itemType(savedItem.getItemType().name())
                .itemId(savedItem.getItemId())
                .savedAt(savedItem.getSavedAt())
                .build();
    }

    /**
     * Helper method to map Brick entity to BrickResponse DTO
     */
    private BrickResponse mapToBrickResponse(Brick brick, Long currentUserId) {
        return BrickResponse.builder()
                .id(brick.getId())
                .title(brick.getTitle())
                .description(brick.getDescription())
                .imageUrl(brick.getImageUrl())
                .userId(brick.getUser().getId())
                .username(brick.getUser().getUsername())
                .createdAt(brick.getCreatedAt())
                .likesCount((int)brick.getLikesCount())
                .commentsCount((int)brick.getCommentsCount())
                .savesCount((int)brick.getSavesCount())
                .sharesCount((int)brick.getSharesCount())
                .liked(brickService.isLikedByUser(brick.getId(), currentUserId))
                .saved(brickService.isSavedByUser(brick.getId(), currentUserId))
                .build();
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
     * Helper method to map User entity to UserSummaryResponse DTO
     */
    private UserSummaryResponse mapToUserSummaryResponse(User user) {
        return UserSummaryResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getUsername())
                .profilePictureUrl(user.getProfilePictureUrl())
                .societyRole(user.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_SOCIETY")))
                .build();
    }
}
