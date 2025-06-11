package com.campuscon.service;

import com.campuscon.model.ChatGroup;
import com.campuscon.model.Deed;
import com.campuscon.model.User;
import com.campuscon.repository.DeedRepository;
import com.campuscon.repository.UserRepository;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class NotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final DeedRepository deedRepository;
    private final FirebaseMessaging firebaseMessaging;

    private final ExecutorService executorService;
    
    public NotificationService(
            SimpMessagingTemplate messagingTemplate,
            UserRepository userRepository,
            DeedRepository deedRepository,
            FirebaseMessaging firebaseMessaging) {
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.deedRepository = deedRepository;
        this.firebaseMessaging = firebaseMessaging;

        this.executorService = Executors.newCachedThreadPool();
    }
    
    @Value("${app.notifications.enabled:true}")
    private boolean notificationsEnabled;
    
    /**
     * Send a notification about a new message to a user
     */
    public void sendNewMessageNotification(Long senderId, Long receiverId, String messageContent, boolean isGroupMessage, Long groupId) {
        if (!notificationsEnabled) return;
        
        userRepository.findById(senderId).ifPresent(sender -> {
            userRepository.findById(receiverId).ifPresent(receiver -> {
                // WebSocket notification
                Map<String, Object> notification = new HashMap<>();
                notification.put("type", "NEW_MESSAGE");
                notification.put("senderId", senderId);
                notification.put("senderName", sender.getUsername());
                notification.put("messagePreview", truncateMessage(messageContent));
                notification.put("isGroupMessage", isGroupMessage);
                if (isGroupMessage) {
                    notification.put("groupId", groupId);
                }
                
                // Send via WebSocket
                messagingTemplate.convertAndSendToUser(
                        receiver.getId().toString(),
                        "/queue/notifications",
                        notification
                );
                
                // Send via FCM if device token is available
                sendFcmNotification(receiver, 
                        sender.getUsername(), 
                        truncateMessage(messageContent),
                        isGroupMessage ? "GROUP_MESSAGE" : "DIRECT_MESSAGE"
                );
            });
        });
    }
    
    /**
     * Send a notification about a new bond request
     */
    public void sendBondRequestNotification(Long requesterId, Long receiverId) {
        if (!notificationsEnabled) return;
        
        userRepository.findById(requesterId).ifPresent(requester -> {
            userRepository.findById(receiverId).ifPresent(receiver -> {
                // WebSocket notification
                Map<String, Object> notification = new HashMap<>();
                notification.put("type", "BOND_REQUEST");
                notification.put("requesterId", requesterId);
                notification.put("requesterName", requester.getUsername());
                
                // Send via WebSocket
                messagingTemplate.convertAndSendToUser(
                        receiver.getId().toString(),
                        "/queue/notifications",
                        notification
                );
                
                // Send via FCM if device token is available
                sendFcmNotification(receiver, 
                        requester.getUsername(), 
                        requester.getUsername() + " sent you a bond request",
                        "BOND_REQUEST"
                );
            });
        });
    }
    
    /**
     * Send a notification about an accepted bond
     */
    public void sendBondAcceptedNotification(Long requesterId, Long accepterId) {
        if (!notificationsEnabled) return;
        
        userRepository.findById(requesterId).ifPresent(requester -> {
            userRepository.findById(accepterId).ifPresent(accepter -> {
                // WebSocket notification
                Map<String, Object> notification = new HashMap<>();
                notification.put("type", "BOND_ACCEPTED");
                notification.put("accepterId", accepterId);
                notification.put("accepterName", accepter.getUsername());
                
                // Send via WebSocket
                messagingTemplate.convertAndSendToUser(
                        requester.getId().toString(),
                        "/queue/notifications",
                        notification
                );
                
                // Send via FCM if device token is available
                sendFcmNotification(requester, 
                        accepter.getUsername(), 
                        accepter.getUsername() + " accepted your bond request",
                        "BOND_ACCEPTED"
                );
            });
        });
    }
    
    /**
     * Send a notification to all group members about a new group message
     */
    public void sendGroupMessageNotification(Long senderId, ChatGroup group, String messageContent) {
        if (!notificationsEnabled) return;
        
        userRepository.findById(senderId).ifPresent(sender -> {
            group.getMembers().forEach(member -> {
                // Skip notification to sender
                if (!member.getId().equals(senderId)) {
                    // WebSocket notification
                    Map<String, Object> notification = new HashMap<>();
                    notification.put("type", "GROUP_MESSAGE");
                    notification.put("senderId", senderId);
                    notification.put("senderName", sender.getUsername());
                    notification.put("groupId", group.getId());
                    notification.put("groupName", group.getName());
                    notification.put("messagePreview", truncateMessage(messageContent));
                    
                    // Send via WebSocket
                    messagingTemplate.convertAndSendToUser(
                            member.getId().toString(),
                            "/queue/notifications",
                            notification
                    );
                    
                    // Send via FCM if device token is available
                    sendFcmNotification(member, 
                            group.getName(), 
                            sender.getUsername() + ": " + truncateMessage(messageContent),
                            "GROUP_MESSAGE"
                    );
                }
            });
        });
    }
    
    /**
     * Send FCM push notification to a user's devices
     */
    private void sendFcmNotification(User user, String title, String body, String notificationType) {
        if (user.getFcmToken() == null || user.getFcmToken().isEmpty()) {
            return;
        }
        
        // Run in separate thread to avoid blocking
        executorService.submit(() -> {
            try {
                Notification notification = Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build();
                
                Map<String, String> data = new HashMap<>();
                data.put("type", notificationType);
                
                Message message = Message.builder()
                        .setToken(user.getFcmToken())
                        .setNotification(notification)
                        .putAllData(data)
                        .build();
                
                firebaseMessaging.send(message);
            } catch (Exception e) {
                log.error("Failed to send FCM notification", e);
            }
        });
    }
    
    /**
     * Truncate message to a preview length
     */
    private String truncateMessage(String message) {
        if (message == null) return "";
        return message.length() > 50 ? message.substring(0, 47) + "..." : message;
    }
    
    /**
     * Send a notification to an individual participant when they are shortlisted for the next round
     * 
     * @param participantId The participant user ID
     * @param deedId The deed ID
     * @param roundNumber The next round number the participant has been shortlisted for
     */
    public void sendParticipantShortlistNotification(Long participantId, Long deedId, int roundNumber) {
        if (!notificationsEnabled) return;
        
        userRepository.findById(participantId).ifPresent(participant -> {
            deedRepository.findById(deedId).ifPresent(deed -> {
                executorService.submit(() -> {
                    try {
                        String title = deed.getTitle();
                        String message = "Congratulations! You have been shortlisted for round " + roundNumber + ". All the best!";
                        
                        // WebSocket notification
                        Map<String, Object> notification = new HashMap<>();
                        notification.put("type", "PARTICIPANT_SHORTLISTED");
                        notification.put("deedId", deedId);
                        notification.put("deedTitle", title);
                        notification.put("roundNumber", roundNumber);
                        notification.put("message", message);
                        
                        // Send via WebSocket
                        messagingTemplate.convertAndSendToUser(
                                participant.getId().toString(),
                                "/queue/notifications",
                                notification
                        );
                        
                        // Send via FCM if device token is available
                        sendFcmNotification(participant, 
                                "Shortlisted for " + title, 
                                message,
                                "PARTICIPANT_SHORTLISTED"
                        );
                        
                        log.info("Sent shortlist notification to participant {} for deed {}", participantId, deedId);
                    } catch (Exception e) {
                        log.error("Error sending participant shortlist notification", e);
                    }
                });
            });
        });
    }
    
    /**
     * Send a notification to all team members when they are shortlisted for the next round
     * 
     * @param deedId The deed ID
     * @param teamId The team ID
     * @param roundNumber The next round number
     */
    public void sendTeamShortlistedNotification(Long deedId, Long teamId, Integer roundNumber) {
        if (!notificationsEnabled) return;
        
        deedRepository.findById(deedId).ifPresent(deed -> {
            // Get all users from the team
            executorService.submit(() -> {
                try {
                    // Find all members of the team
                    List<User> teamMembers = userRepository.findByTeamId(teamId);
                    String title = deed.getTitle();
                    String message = "Congratulations! You have been shortlisted for the next round. All the best!";
                    
                    // Chat group notification removed (inGroup system deleted)
                    
                    for (User member : teamMembers) {
                        // WebSocket notification
                        Map<String, Object> notification = new HashMap<>();
                        notification.put("type", "TEAM_SHORTLISTED");
                        notification.put("deedId", deedId);
                        notification.put("deedTitle", title);
                        notification.put("teamId", teamId);
                        notification.put("roundNumber", roundNumber);
                        notification.put("message", message);
                        
                        // Send via WebSocket
                        messagingTemplate.convertAndSendToUser(
                                member.getId().toString(),
                                "/queue/notifications",
                                notification
                        );
                        
                        // Send via FCM if device token is available
                        sendFcmNotification(member, 
                                "Team Shortlisted for " + title, 
                                message,
                                "TEAM_SHORTLISTED"
                        );
                    }
                    
                    log.info("Sent shortlist notifications to team {} for deed {}", teamId, deedId);
                } catch (Exception e) {
                    log.error("Error sending shortlist notifications", e);
                }
            });
        });
    }
    
    // Like notification methods have been removed
    
    /**
     * Send a notification when someone comments on a deed
     */
    public void sendDeedCommentNotification(User creator, User commenter, Deed deed) {
        if (!notificationsEnabled) return;
        
        executorService.submit(() -> {
            // WebSocket notification
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "DEED_COMMENT");
            notification.put("commenterId", commenter.getId());
            notification.put("commenterName", commenter.getUsername());
            notification.put("deedId", deed.getId());
            notification.put("deedTitle", deed.getTitle());
            
            // Send via WebSocket
            messagingTemplate.convertAndSendToUser(
                    creator.getId().toString(),
                    "/queue/notifications",
                    notification
            );
            
            // Send via FCM if device token is available
            sendFcmNotification(creator, 
                    commenter.getUsername() + " commented on your deed", 
                    deed.getTitle(),
                    "DEED_COMMENT"
            );
        });
    }
    
    /**
     * Send a notification when someone registers for a deed
     */
    public void sendDeedRegistrationNotification(User creator, User registrant, Deed deed) {
        if (!notificationsEnabled) return;
        
        executorService.submit(() -> {
            // WebSocket notification
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "DEED_REGISTRATION");
            notification.put("registrantId", registrant.getId());
            notification.put("registrantName", registrant.getUsername());
            notification.put("deedId", deed.getId());
            notification.put("deedTitle", deed.getTitle());
            
            // Send via WebSocket
            messagingTemplate.convertAndSendToUser(
                    creator.getId().toString(),
                    "/queue/notifications",
                    notification
            );
            
            // Send via FCM if device token is available
            sendFcmNotification(creator, 
                    registrant.getUsername() + " registered for your deed", 
                    deed.getTitle(),
                    "DEED_REGISTRATION"
            );
        });
    }
    
    /**
     * Send a notification when deed registration is approved
     */
    public void sendDeedRegistrationApprovedNotification(User user, Deed deed) {
        if (!notificationsEnabled) return;
        
        executorService.submit(() -> {
            // WebSocket notification
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "DEED_REGISTRATION_APPROVED");
            notification.put("deedId", deed.getId());
            notification.put("deedTitle", deed.getTitle());
            notification.put("creatorId", deed.getCreator().getId());
            notification.put("creatorName", deed.getCreator().getUsername());
            
            // Send via WebSocket
            messagingTemplate.convertAndSendToUser(
                    user.getId().toString(),
                    "/queue/notifications",
                    notification
            );
            
            // Send via FCM if device token is available
            sendFcmNotification(user, 
                    "Registration Approved", 
                    "Your registration for " + deed.getTitle() + " has been approved!",
                    "DEED_REGISTRATION_APPROVED"
            );
        });
    }
    
    /**
     * Send a notification when deed registration is rejected
     */
    public void sendDeedRegistrationRejectedNotification(User user, Deed deed, String reason) {
        if (!notificationsEnabled) return;
        
        executorService.submit(() -> {
            // WebSocket notification
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "DEED_REGISTRATION_REJECTED");
            notification.put("deedId", deed.getId());
            notification.put("deedTitle", deed.getTitle());
            notification.put("creatorId", deed.getCreator().getId());
            notification.put("creatorName", deed.getCreator().getUsername());
            notification.put("reason", reason);
            
            // Send via WebSocket
            messagingTemplate.convertAndSendToUser(
                    user.getId().toString(),
                    "/queue/notifications",
                    notification
            );
            
            // Send via FCM if device token is available
            sendFcmNotification(user, 
                    "Registration Rejected", 
                    "Your registration for " + deed.getTitle() + " was not approved. Reason: " + truncateMessage(reason),
                    "DEED_REGISTRATION_REJECTED"
            );
        });
    }
    
    /**
     * Send a notification when registration is cancelled
     */
    public void sendDeedRegistrationCancelledNotification(User creator, User registrant, Deed deed) {
        if (!notificationsEnabled) return;
        
        executorService.submit(() -> {
            // WebSocket notification
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "DEED_REGISTRATION_CANCELLED");
            notification.put("registrantId", registrant.getId());
            notification.put("registrantName", registrant.getUsername());
            notification.put("deedId", deed.getId());
            notification.put("deedTitle", deed.getTitle());
            
            // Send via WebSocket
            messagingTemplate.convertAndSendToUser(
                    creator.getId().toString(),
                    "/queue/notifications",
                    notification
            );
            
            // Send via FCM if device token is available
            sendFcmNotification(creator, 
                    registrant.getUsername() + " cancelled registration", 
                    "For deed: " + deed.getTitle(),
                    "DEED_REGISTRATION_CANCELLED"
            );
        });
    }
    
    /**
     * Send a notification when a user's registration is approved from the waitlist
     * Used by DeedSettingsService when processing the waitlist
     */
    public void sendDeedRegistrationApprovalNotification(Long userId, Long deedId, String deedTitle, String message) {
        if (!notificationsEnabled) return;
        
        executorService.submit(() -> {
            userRepository.findById(userId).ifPresent(user -> {
                Deed deed = deedRepository.findById(deedId).orElse(null);
                if (deed != null) {
                    // WebSocket notification
                    Map<String, Object> notification = new HashMap<>();
                    notification.put("type", "DEED_REGISTRATION_APPROVED");
                    notification.put("deedId", deedId);
                    notification.put("deedTitle", deedTitle);
                    notification.put("creatorId", deed.getCreator().getId());
                    notification.put("creatorName", deed.getCreator().getUsername());
                    
                    // Send via WebSocket
                    messagingTemplate.convertAndSendToUser(
                            user.getId().toString(),
                            "/queue/notifications",
                            notification
                    );
                    
                    // Send via FCM if device token is available
                    sendFcmNotification(user, 
                            "Registration Approved", 
                            message,
                            "DEED_REGISTRATION_APPROVED"
                    );
                }
            });
        });
    }
    
    // Brick notification methods have been removed
}
