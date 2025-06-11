package com.campuscon.service;

import com.campuscon.model.Deed;
import com.campuscon.model.SavedItem;
import com.campuscon.model.User;
import com.campuscon.repository.DeedRepository;
import com.campuscon.repository.SavedItemRepository;
import com.campuscon.repository.UserRepository;
import com.campuscon.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SavedItemService {

    private final SavedItemRepository savedItemRepository;
    private final UserRepository userRepository;
    private final DeedRepository deedRepository;

    /**
     * Get all saved items by user
     */
    public Page<SavedItem> getSavedItems(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return savedItemRepository.findByUserOrderBySavedAtDesc(user, pageable);
    }

    /**
     * Get saved items by type
     */
    public Page<SavedItem> getSavedItemsByType(Long userId, SavedItem.ItemType itemType, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return savedItemRepository.findByUserAndItemTypeOrderBySavedAtDesc(user, itemType, pageable);
    }

    /**
     * Get saved deeds by user
     */
    public Page<Deed> getSavedDeeds(Long userId, Pageable pageable) {
        return deedRepository.findSavedDeedsByUserId(userId, pageable);
    }

    /**
     * Get saved users by user
     */
    public List<User> getSavedUsers(Long userId, Pageable pageable) {
        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Get IDs of saved users
        List<Long> userIds = savedItemRepository.findItemIdsByUserIdAndItemType(userId, SavedItem.ItemType.USER);
        
        // Return saved users
        return userRepository.findAllById(userIds);
    }

    /**
     * Save a user to favorites
     */
    @Transactional
    public void saveUser(Long userId, Long targetUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User to save not found"));
        
        // Check if already saved
        if (savedItemRepository.findByUserAndItemTypeAndItemId(user, SavedItem.ItemType.USER, targetUserId).isPresent()) {
            return; // Already saved
        }
        
        // Validate targetUser exists and is a valid user to save
        if (targetUser.getId().equals(user.getId())) {
            throw new IllegalArgumentException("Cannot save yourself");
        }
        
        // Save the user
        SavedItem savedItem = SavedItem.builder()
                .user(user)
                .itemType(SavedItem.ItemType.USER)
                .itemId(targetUserId)
                .build();
        
        savedItemRepository.save(savedItem);
    }

    /**
     * Unsave a user from favorites
     */
    @Transactional
    public void unsaveUser(Long userId, Long targetUserId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        savedItemRepository.deleteByUserAndItemTypeAndItemId(user, SavedItem.ItemType.USER, targetUserId);
    }

    /**
     * Check if a user is saved by another user
     */
    public boolean isUserSavedByUser(Long userId, Long targetUserId) {
        return savedItemRepository.findByUserAndItemTypeAndItemId(
                userRepository.getReferenceById(userId), 
                SavedItem.ItemType.USER, 
                targetUserId
        ).isPresent();
    }

    /**
     * Count saved items by type
     */
    public long countSavedItemsByType(Long userId, SavedItem.ItemType itemType) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return savedItemRepository.countByUserAndItemType(user, itemType);
    }

    /**
     * Delete a saved item
     */
    @Transactional
    public void deleteSavedItem(Long savedItemId, Long userId) {
        SavedItem savedItem = savedItemRepository.findById(savedItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Saved item not found"));
        
        // Verify ownership
        if (!savedItem.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Not authorized to delete this saved item");
        }
        
        savedItemRepository.delete(savedItem);
        
        // Update the item's save count if it's a deed
        if (savedItem.getItemType() == SavedItem.ItemType.DEED) {
            deedRepository.findById(savedItem.getItemId()).ifPresent(deed -> {
                // Get a fully loaded user entity instead of a reference proxy
                User user = userRepository.findById(userId).orElseThrow(
                    () -> new ResourceNotFoundException("User not found"));
                
                // Remove user from the savedByUsers collection
                if (deed.getSavedByUsers().remove(user)) {
                    deed.decrementSavesCount();
                    deedRepository.save(deed);
                }
            });
        }
    }
}
