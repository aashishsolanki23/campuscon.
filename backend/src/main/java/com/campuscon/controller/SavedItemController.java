package com.campuscon.controller;

import com.campuscon.dto.ApiResponse;
import com.campuscon.dto.deed.DeedResponse;
import com.campuscon.dto.saved.SavedItemResponse;
import com.campuscon.dto.user.UserSummaryResponse;
import com.campuscon.model.Deed;
import com.campuscon.model.SavedItem;
import com.campuscon.model.User;
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

    // Bricks functionality has been removed

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
     * Get saved users
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserSummaryResponse>>> getSavedUsers(
            @PageableDefault(size = 20) Pageable pageable,
            @AuthenticationPrincipal Long userId) {
        
        List<User> savedUsers = savedItemService.getSavedUsers(userId, pageable);
        List<UserSummaryResponse> response = savedUsers.stream()
                .map(user -> mapToUserSummaryResponse(user))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(response, "Saved users retrieved successfully"));
    }

    /**
     * Save a user to favorites
     */
    @PostMapping("/users/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> saveUser(
            @PathVariable("targetUserId") Long targetUserId,
            @AuthenticationPrincipal Long userId) {
        
        savedItemService.saveUser(userId, targetUserId);
        return ResponseEntity.ok(ApiResponse.success(null, "User saved to favorites successfully"));
    }

    /**
     * Remove a user from favorites
     */
    @DeleteMapping("/users/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> unsaveUser(
            @PathVariable("targetUserId") Long targetUserId,
            @AuthenticationPrincipal Long userId) {
        
        savedItemService.unsaveUser(userId, targetUserId);
        return ResponseEntity.ok(ApiResponse.success(null, "User removed from favorites successfully"));
    }

    /**
     * Check if user is saved in favorites
     */
    @GetMapping("/users/{targetUserId}/check")
    public ResponseEntity<ApiResponse<Boolean>> isUserSaved(
            @PathVariable("targetUserId") Long targetUserId,
            @AuthenticationPrincipal Long userId) {
        
        boolean isSaved = savedItemService.isUserSavedByUser(userId, targetUserId);
        return ResponseEntity.ok(ApiResponse.success(isSaved, "User saved status retrieved successfully"));
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

    // Brick mapping functionality has been removed

    /**
     * Helper method to map Deed entity to DeedResponse DTO
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
                // likesCount removed
                .commentsCount((int)deed.getCommentsCount())
                .savesCount((int)deed.getSavesCount())
                .sharesCount((int)deed.getSharesCount())
                // liked status removed
                .saved(deedService.isSavedByUser(deed.getId(), currentUserId))
                // Add registration-related fields to match DeedController
                .registrationEnabled(deed.isRegistrationEnabled())
                .registrationsCount(deed.getRegistrations() != null ? deed.getRegistrations().size() : 0)
                .startDateTime(deed.getStartDateTime())
                .endDateTime(deed.getEndDateTime())
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
                .role("USER") // Default role in unified user model
                .build();
    }
}
