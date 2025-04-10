package com.campuscon.controller;

import com.campuscon.dto.message.MessageRequest;
import com.campuscon.dto.message.MessageResponse;
import com.campuscon.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MessageController {
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    /**
     * Handle direct messages between users
     * Path: /app/chat/{receiverId}
     */
    @MessageMapping("/chat/{receiverId}")
    public void sendDirectMessage(
            @DestinationVariable Long receiverId,
            @Payload MessageRequest messageRequest,
            Authentication authentication) {
        
        String senderEmail = authentication.getName();
        MessageResponse messageResponse = messageService.saveDirectMessage(senderEmail, receiverId, messageRequest);
        
        // Send to the specific user's queue
        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/messages",
                messageResponse
        );
        
        // Also send a confirmation back to sender
        messagingTemplate.convertAndSendToUser(
                authentication.getName(),
                "/queue/messages",
                messageResponse
        );
    }

    /**
     * Handle group messages
     * Path: /app/groups/{groupId}
     */
    @MessageMapping("/groups/{groupId}")
    public void sendGroupMessage(
            @DestinationVariable Long groupId,
            @Payload MessageRequest messageRequest,
            Authentication authentication) {
        
        String senderEmail = authentication.getName();
        MessageResponse messageResponse = messageService.saveGroupMessage(senderEmail, groupId, messageRequest);
        
        // Broadcast to the group topic
        messagingTemplate.convertAndSend(
                "/topic/groups/" + groupId,
                messageResponse
        );
    }
    
    /**
     * Mark a message as read
     * Path: /app/messages/{messageId}/read
     */
    @MessageMapping("/messages/{messageId}/read")
    public void markMessageAsRead(
            @DestinationVariable Long messageId,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        messageService.markMessageAsRead(messageId, userEmail);
    }
    
    /**
     * Add a reaction to a message
     * Path: /app/messages/{messageId}/react
     */
    @MessageMapping("/messages/{messageId}/react")
    public void addReaction(
            @DestinationVariable Long messageId,
            @Payload String reaction,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        MessageResponse messageResponse = messageService.addReaction(messageId, userEmail, reaction);
        
        // For direct messages, send to both participants
        if (messageResponse != null) {
            Long recipientId = messageService.getRecipientId(messageId, userEmail);
            if (recipientId != null) {
                messagingTemplate.convertAndSendToUser(
                        recipientId.toString(),
                        "/queue/messages",
                        messageResponse
                );
                
                messagingTemplate.convertAndSendToUser(
                        authentication.getName(),
                        "/queue/messages",
                        messageResponse
                );
            } else {
                // For group messages, broadcast to the group
                Long groupId = messageService.getGroupId(messageId);
                if (groupId != null) {
                    messagingTemplate.convertAndSend(
                            "/topic/groups/" + groupId,
                            messageResponse
                    );
                }
            }
        }
    }
}
