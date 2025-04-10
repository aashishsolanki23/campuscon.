package com.campuscon.service;

import com.campuscon.model.Brick;
import com.campuscon.model.BrickComment;
import com.campuscon.model.SavedItem;
import com.campuscon.model.User;
import com.campuscon.repository.BrickCommentRepository;
import com.campuscon.repository.BrickRepository;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BrickService {

    private final BrickRepository brickRepository;
    private final BrickCommentRepository brickCommentRepository;
    private final UserRepository userRepository;
    private final BondService bondService;
    private final SavedItemRepository savedItemRepository;
    private final S3Service s3Service;
    private final NotificationService notificationService;
    private final ModerationService moderationService;

    @Value("${aws.s3.bricks-bucket}")
    private String bricksBucket;

    /**
     * Create a new brick with image upload
     */
    @Transactional
    public Brick createBrick(MultipartFile image, String title, String description, Long userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Moderate content before saving
        boolean isContentSafe = moderationService.checkText(title + " " + description);
        boolean isImageSafe = moderationService.checkImage(image);

        if (!isContentSafe || !isImageSafe) {
            throw new IllegalArgumentException("Content violates community guidelines");
        }

        // Upload image to S3
        String imageKey = "bricks/" + UUID.randomUUID().toString();
        String imageUrl = s3Service.uploadFile(image, imageKey, bricksBucket);

        // Create brick
        Brick brick = Brick.builder()
                .title(title)
                .description(description)
                .imageUrl(imageUrl)
                .user(user)
                .isModerated(true)
                .isApproved(true)
                .build();

        return brickRepository.save(brick);
    }

    /**
     * Get brick by ID
     */
    public Brick getBrickById(Long id) {
        return brickRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brick not found"));
    }

    /**
     * Get bricks for Bricks Page - shows bricks from bonded users
     */
    public Page<Brick> getBondedUsersBricks(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        List<User> bondedUsers = bondService.getBondedUsers(user);
        // Add current user to the list to see their own bricks too
        bondedUsers.add(user);
        
        return brickRepository.findByUserInOrderByCreatedAtDesc(bondedUsers, pageable);
    }

    /**
     * Get bricks for user profile
     */
    public Page<Brick> getUserBricks(Long userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return brickRepository.findByUserOrderByCreatedAtDesc(user, pageable);
    }

    /**
     * Get popular bricks for discovery
     */
    public Page<Brick> getPopularBricks(Pageable pageable) {
        return brickRepository.findMostPopularBricks(pageable);
    }

    /**
     * Like a brick
     */
    @Transactional
    public void likeBrick(Long brickId, Long userId) {
        Brick brick = brickRepository.findById(brickId)
                .orElseThrow(() -> new ResourceNotFoundException("Brick not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Set<User> likedByUsers = brick.getLikedByUsers();
        
        if (!likedByUsers.contains(user)) {
            likedByUsers.add(user);
            brick.incrementLikesCount();
            brickRepository.save(brick);
            
            // Send notification to brick owner if it's not the same user
            if (!brick.getUser().getId().equals(userId)) {
                notificationService.sendBrickLikeNotification(brick.getUser(), user, brick);
            }
        }
    }

    /**
     * Unlike a brick
     */
    @Transactional
    public void unlikeBrick(Long brickId, Long userId) {
        Brick brick = brickRepository.findById(brickId)
                .orElseThrow(() -> new ResourceNotFoundException("Brick not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Set<User> likedByUsers = brick.getLikedByUsers();
        
        if (likedByUsers.contains(user)) {
            likedByUsers.remove(user);
            brick.decrementLikesCount();
            brickRepository.save(brick);
        }
    }

    /**
     * Check if user liked a brick
     */
    public boolean isLikedByUser(Long brickId, Long userId) {
        return brickRepository.isLikedByUser(brickId, userId);
    }

    /**
     * Save a brick
     */
    @Transactional
    public void saveBrick(Long brickId, Long userId) {
        Brick brick = brickRepository.findById(brickId)
                .orElseThrow(() -> new ResourceNotFoundException("Brick not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if already saved
        if (savedItemRepository.findByUserAndItemTypeAndItemId(user, SavedItem.ItemType.BRICK, brickId).isPresent()) {
            return; // Already saved
        }

        // Add to user's saved items
        SavedItem savedItem = SavedItem.builder()
                .user(user)
                .itemType(SavedItem.ItemType.BRICK)
                .itemId(brickId)
                .build();
        
        savedItemRepository.save(savedItem);
        
        // Update brick's save count
        Set<User> savedByUsers = brick.getSavedByUsers();
        if (!savedByUsers.contains(user)) {
            savedByUsers.add(user);
            brick.incrementSavesCount();
            brickRepository.save(brick);
        }
    }

    /**
     * Unsave a brick
     */
    @Transactional
    public void unsaveBrick(Long brickId, Long userId) {
        Brick brick = brickRepository.findById(brickId)
                .orElseThrow(() -> new ResourceNotFoundException("Brick not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Remove from saved items
        savedItemRepository.deleteByUserAndItemTypeAndItemId(user, SavedItem.ItemType.BRICK, brickId);
        
        // Update brick's save count
        Set<User> savedByUsers = brick.getSavedByUsers();
        if (savedByUsers.contains(user)) {
            savedByUsers.remove(user);
            brick.decrementSavesCount();
            brickRepository.save(brick);
        }
    }

    /**
     * Check if user saved a brick
     */
    public boolean isSavedByUser(Long brickId, Long userId) {
        return brickRepository.isSavedByUser(brickId, userId);
    }

    /**
     * Add comment to a brick
     */
    @Transactional
    public BrickComment addComment(Long brickId, Long userId, String content, Long parentCommentId) {
        Brick brick = brickRepository.findById(brickId)
                .orElseThrow(() -> new ResourceNotFoundException("Brick not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Moderate content
        boolean isContentSafe = moderationService.checkText(content);
        if (!isContentSafe) {
            throw new IllegalArgumentException("Comment violates community guidelines");
        }

        BrickComment comment = BrickComment.builder()
                .content(content)
                .brick(brick)
                .user(user)
                .isModerated(true)
                .build();

        if (parentCommentId != null) {
            BrickComment parentComment = brickCommentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Parent comment not found"));
            comment.setParentComment(parentComment);
        } else {
            // Only increment brick's comment count for top-level comments
            brick.incrementCommentsCount();
            brickRepository.save(brick);
        }

        BrickComment savedComment = brickCommentRepository.save(comment);
        
        // Send notification to brick owner if it's not the same user
        if (!brick.getUser().getId().equals(userId)) {
            notificationService.sendBrickCommentNotification(brick.getUser(), user, brick);
        }
        
        return savedComment;
    }

    /**
     * Get comments for a brick
     */
    public Page<BrickComment> getBrickComments(Long brickId, Pageable pageable) {
        Brick brick = brickRepository.findById(brickId)
                .orElseThrow(() -> new ResourceNotFoundException("Brick not found"));
        
        return brickCommentRepository.findByBrickAndParentCommentIsNullOrderByCreatedAtDesc(brick, pageable);
    }

    /**
     * Get replies to a comment
     */
    public List<BrickComment> getCommentReplies(Long commentId) {
        return brickCommentRepository.findByParentCommentIdOrderByCreatedAtAsc(commentId);
    }

    /**
     * Delete a brick
     */
    @Transactional
    public void deleteBrick(Long brickId, Long userId) {
        Brick brick = brickRepository.findById(brickId)
                .orElseThrow(() -> new ResourceNotFoundException("Brick not found"));
        
        // Only the owner can delete their brick
        if (!brick.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Not authorized to delete this brick");
        }
        
        // Delete image from S3
        String imageKey = brick.getImageUrl().substring(brick.getImageUrl().lastIndexOf("/") + 1);
        s3Service.deleteFile(imageKey, bricksBucket);
        
        // Delete the brick (cascades to comments)
        brickRepository.delete(brick);
    }

    /**
     * Share a brick
     */
    @Transactional
    public void shareBrick(Long brickId) {
        Brick brick = brickRepository.findById(brickId)
                .orElseThrow(() -> new ResourceNotFoundException("Brick not found"));
        
        brick.incrementSharesCount();
        brickRepository.save(brick);
    }
}
