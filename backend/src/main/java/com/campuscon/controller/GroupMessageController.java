package com.campuscon.controller;

import com.campuscon.dto.chat.MessageDTO;
import com.campuscon.model.ChatGroup;
import com.campuscon.model.GroupMessage;
import com.campuscon.model.User;
import com.campuscon.repository.ChatGroupRepository;
import com.campuscon.repository.GroupMessageRepository;
import com.campuscon.service.MessageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * WebSocket controller for group messages.
 * Handles real-time messages sent within deed chat groups.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class GroupMessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatGroupRepository chatGroupRepository;
    private final GroupMessageRepository groupMessageRepository;
    private final MessageStorageService messageStorageService;

    /**
     * Handles messages sent to groups.
     *
     * @param groupId Group ID to send the message to
     * @param messageDTO The message payload
     * @param auth Authentication object containing the sender
     */
    @MessageMapping("/group/{groupId}/send")
    public void sendGroupMessage(
            @DestinationVariable Long groupId,
            @Payload MessageDTO messageDTO,
            Authentication auth) {
        
        try {
            User sender = (User) auth.getPrincipal();
            Optional<ChatGroup> groupOpt = chatGroupRepository.findById(groupId);
            
            if (groupOpt.isEmpty()) {
                sendErrorToUser(sender.getId(), "Group not found with ID: " + groupId);
                return;
            }
            
            ChatGroup group = groupOpt.get();
            
            // Check if the user is a member of the group
            if (!group.getMembers().contains(sender)) {
                sendErrorToUser(sender.getId(), "You are not a member of this group");
                return;
            }
            
            // Create and save the group message
            GroupMessage message = new GroupMessage();
            message.setContent(messageDTO.getContent());
            message.setImageUrl(messageDTO.getImageUrl());
            message.setUrl(messageDTO.getUrl());
            message.setUrlMetadata(messageDTO.getUrlMetadata());
            message.setSender(sender);
            message.setGroup(group);
            message.setSentAt(LocalDateTime.now());
            
            // Handle reply to
            if (messageDTO.getReplyToId() != null) {
                Optional<GroupMessage> replyToOpt = groupMessageRepository.findById(messageDTO.getReplyToId());
                replyToOpt.ifPresent(message::setReplyTo);
            }
            
            // If the message contains an image, process and store it
            if (messageDTO.getImageUrl() != null && messageDTO.getImageUrl().startsWith("data:image")) {
                String storedImageUrl = messageStorageService.storeBase64Image(
                        messageDTO.getImageUrl(), "group_" + groupId + "_message");
                message.setImageUrl(storedImageUrl);
                messageDTO.setImageUrl(storedImageUrl);
            }
            
            // Save the message
            GroupMessage savedMessage = groupMessageRepository.save(message);
            
            // Update the DTO with sender information
            messageDTO.setId(savedMessage.getId());
            messageDTO.setSenderId(sender.getId());
            messageDTO.setSenderName(sender.getDisplayName());
            messageDTO.setSenderProfileImage(sender.getProfilePictureUrl());
            messageDTO.setSentAt(savedMessage.getSentAt().toString());
            
            // Send to all subscribers of the group topic
            messagingTemplate.convertAndSend("/topic/group/" + groupId, messageDTO);
            
            log.info("Message sent to group {}: {} by user {}", groupId, 
                    messageDTO.getContent().substring(0, Math.min(20, messageDTO.getContent().length())), 
                    sender.getEmail());
            
        } catch (Exception e) {
            log.error("Error sending group message", e);
            
            // If auth principal is available, send error to user
            if (auth != null && auth.getPrincipal() instanceof User) {
                User sender = (User) auth.getPrincipal();
                sendErrorToUser(sender.getId(), "Failed to send message: " + e.getMessage());
            }
        }
    }
    
    /**
     * Send an error message to a specific user.
     *
     * @param userId ID of user to send error to
     * @param errorMessage Error message content
     */
    private void sendErrorToUser(Long userId, String errorMessage) {
        MessageDTO errorDTO = new MessageDTO();
        errorDTO.setContent(errorMessage);
        errorDTO.setSenderId(0L); // System sender ID
        errorDTO.setSenderName("System");
        errorDTO.setSentAt(LocalDateTime.now().toString());
        
        messagingTemplate.convertAndSendToUser(userId.toString(), 
                "/queue/errors", errorDTO);
        
        log.error("Error sent to user {}: {}", userId, errorMessage);
    }
}
