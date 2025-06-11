package com.campuscon.repository;

import com.campuscon.model.SavedItem;
import com.campuscon.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SavedItemRepository extends JpaRepository<SavedItem, Long> {
    
    // Find saved items by user and type
    Page<SavedItem> findByUserAndItemTypeOrderBySavedAtDesc(User user, SavedItem.ItemType itemType, Pageable pageable);
    
    // Find all saved items by user
    Page<SavedItem> findByUserOrderBySavedAtDesc(User user, Pageable pageable);
    
    // Check if an item is saved by user
    Optional<SavedItem> findByUserAndItemTypeAndItemId(User user, SavedItem.ItemType itemType, Long itemId);
    
    // Count saved items by user and type
    long countByUserAndItemType(User user, SavedItem.ItemType itemType);
    
    // Delete saved item by user, type and itemId
    void deleteByUserAndItemTypeAndItemId(User user, SavedItem.ItemType itemType, Long itemId);
    
    // Delete all saved items by type and itemId (regardless of user)
    void deleteByItemTypeAndItemId(SavedItem.ItemType itemType, Long itemId);
    
    // Get item IDs by type for a user
    @Query("SELECT si.itemId FROM SavedItem si WHERE si.user.id = :userId AND si.itemType = :itemType")
    List<Long> findItemIdsByUserIdAndItemType(@Param("userId") Long userId, @Param("itemType") SavedItem.ItemType itemType);
}
