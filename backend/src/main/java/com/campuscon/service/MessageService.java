package com.campuscon.service;

import com.campuscon.dto.message.MessageRequest;
import com.campuscon.dto.message.MessageResponse;
import com.campuscon.model.*;
import com.campuscon.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MessageService {
    private final UserRepository userRepository;
    private final ChatGroupRepository chatGroupRepository;
    private final GroupMessageRepository groupMessageRepository;
    private final DirectMessageRepository directMessageRepository;
    private final BondRepository bondRepository;
    private final S3Service s3Service;
    private final NotificationService notificationService;
    
    public MessageService(
            UserRepository userRepository,
            ChatGroupRepository chatGroupRepository,
            GroupMessageRepository groupMessageRepository,
            DirectMessageRepository directMessageRepository,
            BondRepository bondRepository,
            S3Service s3Service,
            @Lazy NotificationService notificationService) {
        this.userRepository = userRepository;
        this.chatGroupRepository = chatGroupRepository;
        this.groupMessageRepository = groupMessageRepository;
        this.directMessageRepository = directMessageRepository;
        this.bondRepository = bondRepository;
        this.s3Service = s3Service;
        this.notificationService = notificationService;
    }
    
    @Value("${aws.s3.messages-bucket}")
    private String messagesBucket;
    
    /**
     * Save a direct message and returns the response DTO
     */
    @Transactional
    public MessageResponse saveDirectMessage(String senderEmail, Long receiverId, MessageRequest request) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
                
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));
        
        // Check if users are bonded (required for direct messaging)
        if (!bondRepository.areUsersBonded(sender, receiver)) {
            throw new RuntimeException("Cannot send message to unbonded user");
        }
        
        DirectMessage message = new DirectMessage();
        message.setContent(request.getContent());
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setRead(false);
        message.setSentAt(LocalDateTime.now());
        
        // Process URL if provided
        if (request.getUrl() != null && !request.getUrl().isEmpty()) {
            message.setUrl(request.getUrl());
            
            // Extract and store metadata from URL
            try {
                String metadata = extractUrlMetadata(request.getUrl());
                message.setUrlMetadata(metadata);
                log.info("URL metadata extracted for: {}", request.getUrl());
            } catch (Exception e) {
                log.warn("Failed to extract metadata from URL: {}", request.getUrl());
                // Continue without metadata rather than failing the message
            }
        }
        
        // Handle image upload if present
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                // Generate a unique key for the image
                String key = "direct-messages/" + sender.getId() + "-" + receiver.getId() + "/" + 
                             UUID.randomUUID().toString() + "-" + request.getImage().getOriginalFilename();
                
                // Upload image to S3
                String imageUrl = s3Service.uploadFile(request.getImage(), key, messagesBucket);
                message.setImageUrl(imageUrl);
                
                log.info("Image uploaded to S3: {}", imageUrl);
            } catch (IOException e) {
                log.error("Failed to upload image: {}", e.getMessage());
                throw new RuntimeException("Failed to upload image", e);
            }
        }
        
        // Set reply reference if provided
        if (request.getReplyToId() != null) {
            DirectMessage replyToMessage = directMessageRepository.findById(request.getReplyToId())
                    .orElseThrow(() -> new RuntimeException("Reply message not found"));
            message.setReplyTo(replyToMessage);
        }
        
        DirectMessage savedMessage = directMessageRepository.save(message);
        
        // Convert to response DTO
        return convertToDirectMessageResponse(savedMessage);
    }
    
    /**
     * Save a group message and returns the response DTO
     */
    @Transactional
    public MessageResponse saveGroupMessage(String senderEmail, Long groupId, MessageRequest request) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
                
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Chat group not found"));
        
        // Check if user is a member of the group
        if (!group.getMembers().contains(sender)) {
            throw new RuntimeException("User is not a member of this group");
        }
        
        // Verify if unbonded users need admin approval
        if (sender != group.getCreator()) {
            // Check if sender is bonded with the group creator
            if (!bondRepository.areUsersBonded(sender, group.getCreator())) {
                throw new RuntimeException("Cannot send message to this group as you are not bonded with the creator");
            }
        }
        
        GroupMessage message = new GroupMessage();
        message.setContent(request.getContent());
        message.setSender(sender);
        message.setGroup(group);
        message.setSentAt(LocalDateTime.now());
        
        // Process URL if provided
        if (request.getUrl() != null && !request.getUrl().isEmpty()) {
            message.setUrl(request.getUrl());
            
            // Extract and store metadata from URL
            try {
                String metadata = extractUrlMetadata(request.getUrl());
                message.setUrlMetadata(metadata);
                log.info("URL metadata extracted for: {}", request.getUrl());
            } catch (Exception e) {
                log.warn("Failed to extract metadata from URL: {}", request.getUrl());
                // Continue without metadata rather than failing the message
            }
        }
        
        // Handle image upload if present
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                // Generate a unique key for the image
                String key = "group-messages/" + group.getId() + "/" + 
                             UUID.randomUUID().toString() + "-" + request.getImage().getOriginalFilename();
                
                // Upload image to S3
                String imageUrl = s3Service.uploadFile(request.getImage(), key, messagesBucket);
                message.setImageUrl(imageUrl);
                
                log.info("Image uploaded to S3: {}", imageUrl);
            } catch (IOException e) {
                log.error("Failed to upload image: {}", e.getMessage());
                throw new RuntimeException("Failed to upload image", e);
            }
        }
        
        // Add sender to readBy list (sender has read their own message)
        message.getReadBy().add(sender);
        
        // Set reply reference if provided
        if (request.getReplyToId() != null) {
            GroupMessage replyToMessage = groupMessageRepository.findById(request.getReplyToId())
                    .orElseThrow(() -> new RuntimeException("Reply message not found"));
            message.setReplyTo(replyToMessage);
        }
        
        GroupMessage savedMessage = groupMessageRepository.save(message);
        
        // Send notification to group members
        notificationService.sendGroupMessageNotification(
            sender.getId(), 
            group, 
            message.getContent()
        );
        
        // Convert to response DTO
        return convertToGroupMessageResponse(savedMessage);
    }
    
    /**
     * Mark a message as read
     */
    @Transactional
    public void markMessageAsRead(Long messageId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Try to find as direct message first
        directMessageRepository.findById(messageId).ifPresent(message -> {
            // Only mark as read if this user is the receiver
            if (message.getReceiver().equals(user)) {
                message.setRead(true);
                directMessageRepository.save(message);
            }
        });
        
        // If not a direct message, try as group message
        groupMessageRepository.findById(messageId).ifPresent(message -> {
            // Only mark as read if user is a member of the group and not the sender
            if (message.getGroup().getMembers().contains(user) && !message.getSender().equals(user)) {
                message.getReadBy().add(user);
                groupMessageRepository.save(message);
            }
        });
    }
    
    /**
     * Add a reaction to a message
     */
    @Transactional
    public MessageResponse addReaction(Long messageId, String userEmail, String reaction) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Try to find as direct message first
        DirectMessage directMessage = directMessageRepository.findById(messageId).orElse(null);
        if (directMessage != null) {
            // Verify user is either sender or receiver
            if (directMessage.getSender().equals(user) || directMessage.getReceiver().equals(user)) {
                directMessage.getReactions().add(reaction);
                DirectMessage savedMessage = directMessageRepository.save(directMessage);
                return convertToDirectMessageResponse(savedMessage);
            }
        }
        
        // If not a direct message, try as group message
        GroupMessage groupMessage = groupMessageRepository.findById(messageId).orElse(null);
        if (groupMessage != null) {
            // Verify user is a group member
            if (groupMessage.getGroup().getMembers().contains(user)) {
                groupMessage.getReactions().add(reaction);
                GroupMessage savedMessage = groupMessageRepository.save(groupMessage);
                return convertToGroupMessageResponse(savedMessage);
            }
        }
        
        return null;
    }
    
    /**
     * Extract metadata from a URL (title, description, image)
     * 
     * @param url The URL to extract metadata from
     * @return JSON string containing metadata
     */
    private String extractUrlMetadata(String url) {
        // This is a placeholder implementation
        // In a real implementation, you would:
        // 1. Fetch the URL content
        // 2. Parse the HTML to extract metadata (title, description, image)
        // 3. Format as JSON
        
        // For now, we'll just return a simple JSON with the URL itself
        return "{\"url\":\"" + url + "\",\"title\":\"Shared Link\",\"description\":\"Click to visit this link\"}";
        
        
        // or integrate with a link preview service
    }
    
    /**
     * Get a paginated list of messages for a direct conversation
     */
    public List<MessageResponse> getDirectConversation(Long userId, Long otherUserId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("Other user not found"));
        
        // Check if users are bonded
        if (!bondRepository.areUsersBonded(user, otherUser)) {
            return Collections.emptyList();
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        Page<DirectMessage> messages = directMessageRepository.findActiveConversation(user, otherUser, pageable);
        
        return messages.getContent().stream()
                .map(this::convertToDirectMessageResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get a paginated list of messages for a group
     */
    public List<MessageResponse> getGroupConversation(Long userId, Long groupId, int page, int size) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        
        // Verify user is a group member
        if (!group.getMembers().contains(user)) {
            throw new RuntimeException("User is not a member of this group");
        }
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("sentAt").descending());
        Page<GroupMessage> messages = groupMessageRepository.findActiveMessagesByGroup(group, pageable);
        
        return messages.getContent().stream()
                .map(this::convertToGroupMessageResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Get recipient ID for a direct message
     */
    public Long getRecipientId(Long messageId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        DirectMessage directMessage = directMessageRepository.findById(messageId).orElse(null);
        if (directMessage != null) {
            if (directMessage.getSender().equals(user)) {
                return directMessage.getReceiver().getId();
            } else if (directMessage.getReceiver().equals(user)) {
                return directMessage.getSender().getId();
            }
        }
        
        return null;
    }
    
    /**
     * Get group ID for a group message
     */
    public Long getGroupId(Long messageId) {
        GroupMessage groupMessage = groupMessageRepository.findById(messageId).orElse(null);
        if (groupMessage != null) {
            return groupMessage.getGroup().getId();
        }
        
        return null;
    }
    
    /**
     * Delete a message (mark as deleted)
     */
    @Transactional
    public void deleteMessage(Long messageId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Try to find as direct message first
        DirectMessage directMessage = directMessageRepository.findById(messageId).orElse(null);
        if (directMessage != null) {
            // Only allow sender to delete
            if (directMessage.getSender().equals(user)) {
                directMessage.setDeleted(true);
                directMessageRepository.save(directMessage);
                return;
            }
        }
        
        // If not a direct message, try as group message
        GroupMessage groupMessage = groupMessageRepository.findById(messageId).orElse(null);
        if (groupMessage != null) {
            // Allow sender or group creator to delete
            if (groupMessage.getSender().equals(user) || 
                groupMessage.getGroup().getCreator().equals(user)) {
                groupMessage.setDeleted(true);
                groupMessageRepository.save(groupMessage);
            }
        }
    }
    
    /**
     * Convert a direct message entity to DTO
     */
    private MessageResponse convertToDirectMessageResponse(DirectMessage message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setContent(message.getContent());
        response.setSentAt(message.getSentAt());
        response.setRead(message.isRead());
        response.setPinned(message.isPinned());
        response.setReactions(message.getReactions());
        
        // Set sender info
        MessageResponse.UserDTO senderDTO = new MessageResponse.UserDTO();
        senderDTO.setId(message.getSender().getId());
        senderDTO.setUsername(message.getSender().getUsername());
        senderDTO.setProfilePictureUrl(message.getSender().getProfilePictureUrl());
        response.setSender(senderDTO);
        
        // Set reply info if exists
        if (message.getReplyTo() != null) {
            response.setReplyToId(message.getReplyTo().getId());
            response.setReplyToContent(message.getReplyTo().getContent());
        }
        
        return response;
    }
    
    /**
     * Convert a group message entity to DTO
     */
    private MessageResponse convertToGroupMessageResponse(GroupMessage message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setContent(message.getContent());
        response.setSentAt(message.getSentAt());
        
        // For group messages, isRead is true if the user has read it
        // This will be handled by the client when receiving the message
        response.setRead(false);
        
        response.setPinned(message.isPinned());
        response.setReactions(message.getReactions());
        
        // Set sender info
        MessageResponse.UserDTO senderDTO = new MessageResponse.UserDTO();
        senderDTO.setId(message.getSender().getId());
        senderDTO.setUsername(message.getSender().getUsername());
        senderDTO.setProfilePictureUrl(message.getSender().getProfilePictureUrl());
        response.setSender(senderDTO);
        
        // Set reply info if exists
        if (message.getReplyTo() != null) {
            response.setReplyToId(message.getReplyTo().getId());
            response.setReplyToContent(message.getReplyTo().getContent());
        }
        
        return response;
    }
}
