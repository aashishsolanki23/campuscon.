package com.campuscon.service;

import com.campuscon.model.Brick;
import com.campuscon.model.Deed;
import com.campuscon.model.SavedItem;
import com.campuscon.model.User;
import com.campuscon.repository.BrickRepository;
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
    private final BrickRepository brickRepository;
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
     * Get saved bricks by user
     */
    public Page<Brick> getSavedBricks(Long userId, Pageable pageable) {
        return brickRepository.findSavedBricksByUserId(userId, pageable);
    }

    /**
     * Get saved deeds by user
     */
    public Page<Deed> getSavedDeeds(Long userId, Pageable pageable) {
        return deedRepository.findSavedDeedsByUserId(userId, pageable);
    }

    /**
     * Get saved societies by user
     */
    public List<User> getSavedSocieties(Long userId, Pageable pageable) {
        // Verify user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Get IDs of saved societies
        List<Long> societyIds = savedItemRepository.findItemIdsByUserIdAndItemType(userId, SavedItem.ItemType.SOCIETY);
        
        // Return society users
        return userRepository.findAllById(societyIds);
    }

    /**
     * Save a society
     */
    @Transactional
    public void saveSociety(Long userId, Long societyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        User society = userRepository.findById(societyId)
                .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
        
        // Verify if the user to save is a society
        if (!society.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SOCIETY"))) {
            throw new IllegalArgumentException("Can only save society accounts");
        }
        
        // Check if already saved
        if (savedItemRepository.findByUserAndItemTypeAndItemId(user, SavedItem.ItemType.SOCIETY, societyId).isPresent()) {
            return; // Already saved
        }
        
        // Save the society
        SavedItem savedItem = SavedItem.builder()
                .user(user)
                .itemType(SavedItem.ItemType.SOCIETY)
                .itemId(societyId)
                .build();
        
        savedItemRepository.save(savedItem);
    }

    /**
     * Unsave a society
     */
    @Transactional
    public void unsaveSociety(Long userId, Long societyId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        savedItemRepository.deleteByUserAndItemTypeAndItemId(user, SavedItem.ItemType.SOCIETY, societyId);
    }

    /**
     * Check if a society is saved by user
     */
    public boolean isSocietySavedByUser(Long userId, Long societyId) {
        return savedItemRepository.findByUserAndItemTypeAndItemId(
                userRepository.getReferenceById(userId), 
                SavedItem.ItemType.SOCIETY, 
                societyId
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
        
        // Update the item's save count if it's a brick or deed
        if (savedItem.getItemType() == SavedItem.ItemType.BRICK) {
            brickRepository.findById(savedItem.getItemId()).ifPresent(brick -> {
                // Get a fully loaded user entity instead of a reference proxy
                User user = userRepository.findById(userId).orElseThrow(
                    () -> new ResourceNotFoundException("User not found"));
                
                // Remove user from the savedByUsers collection
                if (brick.getSavedByUsers().remove(user)) {
                    brick.decrementSavesCount();
                    brickRepository.save(brick);
                }
            });
        } else if (savedItem.getItemType() == SavedItem.ItemType.DEED) {
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
