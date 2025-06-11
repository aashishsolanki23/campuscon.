package com.campuscon.service;

import com.campuscon.enums.DeedCategory;
import com.campuscon.model.Deed;
import com.campuscon.model.DeedComment;
import com.campuscon.model.DeedRegistration;
import com.campuscon.model.SavedItem;
import com.campuscon.model.User;
import com.campuscon.repository.DeedCommentRepository;
import com.campuscon.repository.DeedRepository;
import com.campuscon.repository.SavedItemRepository;
import com.campuscon.repository.UserRepository;
import com.campuscon.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
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
import java.util.stream.Collectors;

@Service
@Slf4j
public class DeedService {

    private final DeedRepository deedRepository;
    private final DeedCommentRepository deedCommentRepository;
    private final UserRepository userRepository;
    private final SavedItemRepository savedItemRepository;
    private final S3Service s3Service;
    private final NotificationService notificationService;
    private final ModerationService moderationService;
    private final DeedRegistrationService deedRegistrationService;

    @Value("${aws.s3.deeds-bucket}")
    private String deedsBucket;
    
    public DeedService(
            DeedRepository deedRepository,
            DeedCommentRepository deedCommentRepository,
            UserRepository userRepository,
            SavedItemRepository savedItemRepository,
            S3Service s3Service,
            NotificationService notificationService,
            ModerationService moderationService,
            @Lazy DeedRegistrationService deedRegistrationService) {
        this.deedRepository = deedRepository;
        this.deedCommentRepository = deedCommentRepository;
        this.userRepository = userRepository;
        this.savedItemRepository = savedItemRepository;
        this.s3Service = s3Service;
        this.notificationService = notificationService;
        this.moderationService = moderationService;
        this.deedRegistrationService = deedRegistrationService;
    }

    /**
     * Create a new deed (event)
     */
    @Transactional
    public Deed createDeed(String title, String description, MultipartFile banner, 
                           String category, String venue, LocalDateTime startDateTime, 
                           LocalDateTime endDateTime, boolean registrationEnabled, Long creatorId) throws IOException {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // All users can create deeds

        // Moderate content before saving
        boolean isContentSafe = moderationService.checkText(title + " " + description);
        boolean isImageSafe = banner != null ? moderationService.checkImage(banner) : true;

        if (!isContentSafe || !isImageSafe) {
            throw new IllegalArgumentException("Content violates community guidelines");
        }

        // Upload banner to S3 if provided
        String bannerUrl = null;
        if (banner != null && !banner.isEmpty()) {
            String bannerKey = "deeds/" + UUID.randomUUID().toString();
            bannerUrl = s3Service.uploadFile(banner, bannerKey, deedsBucket);
        }

        // Create deed
        Deed deed = Deed.builder()
                .title(title)
                .description(description)
                .bannerUrl(bannerUrl)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .venue(venue)
                .category(DeedCategory.fromDisplayName(category))
                .createdBy(creator) // Setting the deed creator
                .registrationEnabled(registrationEnabled)
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
     * Get deeds by user (for user profile)
     */
    public Page<Deed> getUserDeeds(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return deedRepository.findByCreatorOrderByCreatedAtDesc(user, pageable);
    }
    
    /**
     * Get deeds by creator ID
     */
    public Page<Deed> getDeedsByCreatorId(Long creatorId, Pageable pageable) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Creator not found"));
        
        return deedRepository.findByCreatorOrderByCreatedAtDesc(creator, pageable);
    }

    /**
     * Get deeds by college (for home page)
     */
    public Page<Deed> getCollegeDeeds(String collegeName, Pageable pageable) {
        return deedRepository.findByCollegeNameOrderByStartDateTimeDesc(collegeName, pageable);
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
    public Page<Deed> getDeedsByCategory(String categoryDisplayName, Pageable pageable) {
        DeedCategory category = DeedCategory.fromDisplayName(categoryDisplayName);
        return deedRepository.findByCategoryAndIsApprovedTrueOrderByStartDateTimeDesc(category, pageable);
    }
    
    /**
     * Get deeds with filtering options
     */
    public Page<Deed> getDeeds(String category, LocalDateTime startDate, LocalDateTime endDate, String search, Pageable pageable) {
        if (category != null && !category.isEmpty()) {
            return getDeedsByCategory(category, pageable);
        }
        
        if (search != null && !search.isEmpty()) {
            return deedRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCaseOrderByStartDateTimeDesc(
                search, search, pageable);
        }
        
        if (startDate != null && endDate != null) {
            return deedRepository.findByStartDateTimeBetweenOrderByStartDateTimeDesc(startDate, endDate, pageable);
        }
        
        if (startDate != null) {
            return deedRepository.findByStartDateTimeAfterOrderByStartDateTimeDesc(startDate, pageable);
        }
        
        return deedRepository.findByIsApprovedTrueOrderByStartDateTimeDesc(pageable);
    }

    /**
     * Get popular deeds (for home page)
     */
    public Page<Deed> getPopularDeeds(Pageable pageable) {
        return deedRepository.findMostPopularDeeds(pageable);
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
        
        // Send notification to creator if it's not the same user
        if (!deed.getCreatedBy().getId().equals(userId)) {
            notificationService.sendDeedCommentNotification(deed.getCreatedBy(), user, deed);
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
    public void deleteDeed(Long deedId, Long userId) {
        Deed deed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found"));
        
        // Verify ownership (only the user who created the deed can delete it)
        if (!deed.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("Not authorized to delete this deed");
        }
        
        // Get the list of all registered users before deleting registrations
        List<User> registeredUsers = deedRegistrationService.getDeedRegistrations(deedId, Pageable.unpaged())
                .stream()
                .map(DeedRegistration::getUser)
                .distinct()
                .collect(Collectors.toList());
        
        // Clean up all registrations for this deed manually
        // This ensures that user group structures are properly cleaned up
        for (User user : registeredUsers) {
            try {
                deedRegistrationService.cancelRegistration(deedId, user.getId());
                log.info("Cancelled registration for user ID: {} for deed ID: {}", user.getId(), deedId);
            } catch (Exception e) {
                log.error("Error cancelling registration for user ID: {} for deed ID: {}", user.getId(), deedId, e);
            }
        }
        
        // Group structure deletion removed (inGroup system deleted)
        
        // Delete any saved references to this deed
        savedItemRepository.deleteByItemTypeAndItemId(SavedItem.ItemType.DEED, deedId);
        
        // Delete banner from S3
        if (deed.getBannerUrl() != null && !deed.getBannerUrl().isEmpty()) {
            try {
                String bannerKey = deed.getBannerUrl().substring(deed.getBannerUrl().lastIndexOf("/") + 1);
                s3Service.deleteFile(bannerKey, deedsBucket);
            } catch (Exception e) {
                log.error("Error deleting banner for deed ID: {}", deedId, e);
            }
        }
        
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
    public Deed updateDeed(Long deedId, String title, String description, MultipartFile banner, 
                         String categoryDisplayName, String venue, LocalDateTime startDateTime, 
                         LocalDateTime endDateTime, boolean registrationEnabled, Long userId) throws IOException {
        // Ensure the deed exists
        Deed existingDeed = deedRepository.findById(deedId)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found with id: " + deedId));
        
        // Moderate content before saving
        boolean isContentSafe = moderationService.checkText(title + " " + description);
        boolean isImageSafe = banner != null ? moderationService.checkImage(banner) : true;

        if (!isContentSafe || !isImageSafe) {
            throw new IllegalArgumentException("Content violates community guidelines");
        }
        
        // Update basic properties
        existingDeed.setTitle(title);
        existingDeed.setDescription(description);
        existingDeed.setCategory(DeedCategory.fromDisplayName(categoryDisplayName));
        existingDeed.setVenue(venue);
        existingDeed.setStartDateTime(startDateTime);
        existingDeed.setEndDateTime(endDateTime);
        existingDeed.setRegistrationEnabled(registrationEnabled);
        
        // Upload banner to S3 if provided
        if (banner != null && !banner.isEmpty()) {
            // If there's an existing banner, delete it first
            if (existingDeed.getBannerUrl() != null && !existingDeed.getBannerUrl().isEmpty()) {
                try {
                    String existingBannerKey = existingDeed.getBannerUrl().substring(existingDeed.getBannerUrl().lastIndexOf("/") + 1);
                    s3Service.deleteFile(existingBannerKey, deedsBucket);
                } catch (Exception e) {
                    log.error("Error deleting old banner for deed ID: {}", deedId, e);
                }
            }
            
            // Upload new banner
            String bannerKey = "deeds/" + deedId + "/" + UUID.randomUUID().toString();
            String bannerUrl = s3Service.uploadFile(banner, bannerKey, deedsBucket);
            existingDeed.setBannerUrl(bannerUrl);
        }
        
        return deedRepository.save(existingDeed);
    }
}
