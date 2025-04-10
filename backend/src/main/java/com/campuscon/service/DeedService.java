package com.campuscon.service;

import com.campuscon.model.Deed;
import com.campuscon.model.DeedComment;
import com.campuscon.model.SavedItem;
import com.campuscon.model.User;
import com.campuscon.repository.DeedCommentRepository;
import com.campuscon.repository.DeedRepository;
import com.campuscon.repository.SavedItemRepository;
import com.campuscon.repository.UserRepository;
import com.campuscon.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeedService {

    private final DeedRepository deedRepository;
    private final DeedCommentRepository deedCommentRepository;
    private final UserRepository userRepository;
    private final SavedItemRepository savedItemRepository;
    private final S3Service s3Service;
    private final NotificationService notificationService;
    private final ModerationService moderationService;

    @Value("${aws.s3.deeds-bucket}")
    private String deedsBucket;

    /**
     * Create a new deed (society event)
     */
    @Transactional
    public Deed createDeed(MultipartFile banner, String title, String description, 
                           LocalDateTime eventDate, String venue, String category, Long societyId) throws IOException {
        User society = userRepository.findById(societyId)
                .orElseThrow(() -> new ResourceNotFoundException("Society not found"));

        // Verify if user is a society
        if (!society.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SOCIETY"))) {
            throw new IllegalArgumentException("Only societies can create deeds");
        }

        // Moderate content before saving
        boolean isContentSafe = moderationService.checkText(title + " " + description);
        boolean isImageSafe = moderationService.checkImage(banner);

        if (!isContentSafe || !isImageSafe) {
            throw new IllegalArgumentException("Content violates community guidelines");
        }

        // Upload banner to S3
        String bannerKey = "deeds/" + UUID.randomUUID().toString();
        String bannerUrl = s3Service.uploadFile(banner, bannerKey, deedsBucket);

        // Create deed
        Deed deed = Deed.builder()
                .title(title)
                .description(description)
                .bannerUrl(bannerUrl)
                .eventDate(eventDate)
                .venue(venue)
                .category(category)
                .society(society)
                .isModerated(true)
                .isApproved(true)
                .build();

        return deedRepository.save(deed);
    }

    /**
     * Get deed by ID
     */
    public Deed getDeedById(Long id) {
        return deedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
    }

    /**
     * Get deeds by society (for society profile)
     */
    public Page<Deed> getSocietyDeeds(Long societyId, Pageable pageable) {
        User society = userRepository.findById(societyId)
                .orElseThrow(() -> new ResourceNotFoundException("Society not found"));
        
        return deedRepository.findBySocietyOrderByCreatedAtDesc(society, pageable);
    }

    /**
     * Get deeds by college (for home page)
     */
    public Page<Deed> getCollegeDeeds(String collegeName, Pageable pageable) {
        return deedRepository.findByCollegeNameOrderByEventDateDesc(collegeName, pageable);
    }

    /**
     * Get deeds by university (for home page)
     */
    public Page<Deed> getUniversityDeeds(String universityName, Pageable pageable) {
        return deedRepository.findByUniversityNameOrderByEventDateDesc(universityName, pageable);
    }

    /**
     * Get upcoming deeds
     */
    public Page<Deed> getUpcomingDeeds(Pageable pageable) {
        return deedRepository.findUpcomingDeeds(LocalDateTime.now(), pageable);
    }

    /**
     * Get deeds by category
     */
    public Page<Deed> getDeedsByCategory(String category, Pageable pageable) {
        return deedRepository.findByCategoryAndIsApprovedTrueOrderByEventDateDesc(category, pageable);
    }

    /**
     * Get popular deeds (for home page)
     */
    public Page<Deed> getPopularDeeds(Pageable pageable) {
        return deedRepository.findMostPopularDeeds(pageable);
    }

    /**
     * Like a deed
     */
    @Transactional
    public void likeDeed(Long deedId, Long userId) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Set<User> likedByUsers = deed.getLikedByUsers();
        
        if (!likedByUsers.contains(user)) {
            likedByUsers.add(user);
            deed.incrementLikesCount();
            deedRepository.save(deed);
            
            // Send notification to society if it's not the same user
            if (!deed.getSociety().getId().equals(userId)) {
                notificationService.sendDeedLikeNotification(deed.getSociety(), user, deed);
            }
        }
    }

    /**
     * Unlike a deed
     */
    @Transactional
    public void unlikeDeed(Long deedId, Long userId) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Set<User> likedByUsers = deed.getLikedByUsers();
        
        if (likedByUsers.contains(user)) {
            likedByUsers.remove(user);
            deed.decrementLikesCount();
            deedRepository.save(deed);
        }
    }

    /**
     * Check if user liked a deed
     */
    public boolean isLikedByUser(Long deedId, Long userId) {
        return deedRepository.isLikedByUser(deedId, userId);
    }

    /**
     * Save a deed
     */
    @Transactional
    public void saveDeed(Long deedId, Long userId) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if already saved
        if (savedItemRepository.findByUserAndItemTypeAndItemId(user, SavedItem.ItemType.DEED, deedId).isPresent()) {
            return; // Already saved
        }

        // Add to user's saved items
        SavedItem savedItem = SavedItem.builder()
                .user(user)
                .itemType(SavedItem.ItemType.DEED)
                .itemId(deedId)
                .build();
        
        savedItemRepository.save(savedItem);
        
        // Update deed's save count
        Set<User> savedByUsers = deed.getSavedByUsers();
        if (!savedByUsers.contains(user)) {
            savedByUsers.add(user);
            deed.incrementSavesCount();
            deedRepository.save(deed);
        }
    }

    /**
     * Unsave a deed
     */
    @Transactional
    public void unsaveDeed(Long deedId, Long userId) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Remove from saved items
        savedItemRepository.deleteByUserAndItemTypeAndItemId(user, SavedItem.ItemType.DEED, deedId);
        
        // Update deed's save count
        Set<User> savedByUsers = deed.getSavedByUsers();
        if (savedByUsers.contains(user)) {
            savedByUsers.remove(user);
            deed.decrementSavesCount();
            deedRepository.save(deed);
        }
    }

    /**
     * Check if user saved a deed
     */
    public boolean isSavedByUser(Long deedId, Long userId) {
        return deedRepository.isSavedByUser(deedId, userId);
    }

    /**
     * Add comment to a deed
     */
    @Transactional
    public DeedComment addComment(Long deedId, Long userId, String content, Long parentCommentId) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Moderate content
        boolean isContentSafe = moderationService.checkText(content);
        if (!isContentSafe) {
            throw new IllegalArgumentException("Comment violates community guidelines");
        }

        DeedComment comment = DeedComment.builder()
                .content(content)
                .deed(deed)
                .user(user)
                .isModerated(true)
                .build();

        if (parentCommentId != null) {
            DeedComment parentComment = deedCommentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
            comment.setParentComment(parentComment);
        } else {
            // Only increment deed's comment count for top-level comments
            deed.incrementCommentsCount();
            deedRepository.save(deed);
        }

        DeedComment savedComment = deedCommentRepository.save(comment);
        
        // Send notification to society if it's not the same user
        if (!deed.getSociety().getId().equals(userId)) {
            notificationService.sendDeedCommentNotification(deed.getSociety(), user, deed);
        }
        
        return savedComment;
    }

    /**
     * Get comments for a deed
     */
    public Page<DeedComment> getDeedComments(Long deedId, Pageable pageable) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        
        return deedCommentRepository.findByDeedAndParentCommentIsNullOrderByCreatedAtDesc(deed, pageable);
    }

    /**
     * Get replies to a comment
     */
    public List<DeedComment> getCommentReplies(Long commentId) {
        return deedCommentRepository.findByParentCommentIdOrderByCreatedAtAsc(commentId);
    }

    /**
     * Delete a deed
     */
    @Transactional
    public void deleteDeed(Long deedId, Long societyId) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        
        // Only the owner society can delete their deed
        if (!deed.getSociety().getId().equals(societyId)) {
            throw new IllegalArgumentException("Not authorized to delete this deed");
        }
        
        // Delete banner from S3
        String bannerKey = deed.getBannerUrl().substring(deed.getBannerUrl().lastIndexOf("/") + 1);
        s3Service.deleteFile(bannerKey, deedsBucket);
        
        // Delete the deed (cascades to comments)
        deedRepository.delete(deed);
    }

    /**
     * Share a deed
     */
    @Transactional
    public void shareDeed(Long deedId) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        
        deed.incrementSharesCount();
        deedRepository.save(deed);
    }
    
    /**
     * Update a deed
     */
    @Transactional
    public Deed updateDeed(Deed deed) {
        if (deed.getId() == null) {
            throw new IllegalArgumentException("Cannot update a deed without an ID");
        }
        
        // Ensure the deed exists
        deedRepository.findById(deed.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found with id: " + deed.getId()));
        
        return deedRepository.save(deed);
    }
}
