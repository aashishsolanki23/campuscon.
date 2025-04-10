package com.campuscon.config;

import com.campuscon.model.User;
import com.campuscon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    
    // Map to track active users by their username
    private final ConcurrentHashMap<String, Boolean> activeUsers = new ConcurrentHashMap<>();

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();
        
        if (principal instanceof UsernamePasswordAuthenticationToken) {
            String username = principal.getName();
            log.info("User connected: {}", username);
            
            // Mark user as active
            activeUsers.put(username, true);
            
            // Update user's online status in database
            updateUserOnlineStatus(username, true);
            
            // Broadcast online status to all users
            broadcastUserStatus(username, true);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        Principal principal = headerAccessor.getUser();
        
        if (principal instanceof UsernamePasswordAuthenticationToken) {
            String username = principal.getName();
            log.info("User disconnected: {}", username);
            
            // Mark user as inactive
            activeUsers.remove(username);
            
            // Update user's online status in database
            updateUserOnlineStatus(username, false);
            
            // Broadcast offline status to all users
            broadcastUserStatus(username, false);
        }
    }
    
    /**
     * Update user's online status in the database
     */
    private void updateUserOnlineStatus(String username, boolean isOnline) {
        Optional<User> userOpt = userRepository.findByEmail(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setOnline(isOnline);
            if (isOnline) {
                user.setLastSeenAt(null); // Clear last seen when user is online
            } else {
                user.setLastSeenAt(java.time.LocalDateTime.now());
            }
            userRepository.save(user);
        }
    }
    
    /**
     * Broadcast user status change to all connected clients
     */
    private void broadcastUserStatus(String username, boolean isOnline) {
        Optional<User> userOpt = userRepository.findByEmail(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            Map<String, Object> statusUpdate = new HashMap<>();
            statusUpdate.put("type", "USER_STATUS_CHANGE");
            statusUpdate.put("userId", user.getId());
            statusUpdate.put("username", user.getUsername());
            statusUpdate.put("isOnline", isOnline);
            if (!isOnline) {
                statusUpdate.put("lastSeen", user.getLastSeenAt());
            }
            
            // Broadcast to all users through a public topic
            messagingTemplate.convertAndSend("/topic/user-status", statusUpdate);
        }
    }
    
    /**
     * Check if a user is currently online
     */
    public boolean isUserOnline(String username) {
        return activeUsers.containsKey(username);
    }
}
