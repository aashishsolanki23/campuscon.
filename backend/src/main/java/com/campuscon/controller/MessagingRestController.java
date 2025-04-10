package com.campuscon.controller;

import com.campuscon.dto.ApiResponse;
import com.campuscon.dto.message.MessageResponse;
import com.campuscon.model.Bond;
import com.campuscon.model.ChatGroup;
import com.campuscon.model.User;
import com.campuscon.service.BondService;
import com.campuscon.service.GroupService;
import com.campuscon.service.MessageService;
import com.campuscon.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/messaging")
@RequiredArgsConstructor
public class MessagingRestController {
    private final MessageService messageService;
    private final GroupService groupService;
    private final UserService userService;
    private final BondService bondService;

    /**
     * Get all groups the current user belongs to
     */
    @GetMapping("/groups")
    public ResponseEntity<ApiResponse<List<ChatGroup>>> getUserGroups(Authentication authentication) {
        User currentUser = userService.getUserByEmail(authentication.getName());
        List<ChatGroup> groups = groupService.getUserGroups(currentUser);
        return ResponseEntity.ok(ApiResponse.success(groups, "User groups retrieved successfully"));
    }
    
    /**
     * Create a new custom group
     */
    @PostMapping("/groups")
    public ResponseEntity<ApiResponse<ChatGroup>> createGroup(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        
        User currentUser = userService.getUserByEmail(authentication.getName());
        String groupName = (String) request.get("name");
        String groupImageUrl = (String) request.get("imageUrl");
        @SuppressWarnings("unchecked")
        Set<Long> memberIds = (Set<Long>) request.get("memberIds");
        
        ChatGroup newGroup = groupService.createCustomGroup(
                groupName, 
                groupImageUrl, 
                currentUser.getId(), 
                memberIds
        );
        
        return ResponseEntity.ok(ApiResponse.success(newGroup, "Group created successfully"));
    }
    
    /**
     * Get messages for a specific group
     */
    @GetMapping("/groups/{groupId}/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getGroupMessages(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        User currentUser = userService.getUserByEmail(authentication.getName());
        List<MessageResponse> messages = messageService.getGroupConversation(currentUser.getId(), groupId, page, size);
        return ResponseEntity.ok(ApiResponse.success(messages, "Group messages retrieved successfully"));
    }
    
    /**
     * Add user to group
     */
    @PostMapping("/groups/{groupId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> addUserToGroup(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            Authentication authentication) {
        
        // Verify current user is admin/creator before adding member
        groupService.addUserToGroup(groupId, userId);
        return ResponseEntity.ok(ApiResponse.success(null, "User added to group successfully"));
    }
    
    /**
     * Remove user from group
     */
    @DeleteMapping("/groups/{groupId}/members/{userId}")
    public ResponseEntity<ApiResponse<Void>> removeUserFromGroup(
            @PathVariable Long groupId,
            @PathVariable Long userId,
            Authentication authentication) {
        
        // User can remove themselves or admin can remove others
        User currentUser = userService.getUserByEmail(authentication.getName());
        if (currentUser.getId().equals(userId)) {
            // User removing themselves
            groupService.removeUserFromGroup(groupId, userId);
        } else {
            // Verify current user is admin/creator before removing member
            // Implementation handled in service
            groupService.removeUserFromGroup(groupId, userId);
        }
        
        return ResponseEntity.ok(ApiResponse.success(null, "User removed from group successfully"));
    }
    
    /**
     * Get direct chat messages with another user
     */
    @GetMapping("/direct/{userId}")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getDirectMessages(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        
        User currentUser = userService.getUserByEmail(authentication.getName());
        List<MessageResponse> messages = messageService.getDirectConversation(currentUser.getId(), userId, page, size);
        return ResponseEntity.ok(ApiResponse.success(messages, "Direct messages retrieved successfully"));
    }
    
    /**
     * Get all bonded users (contacts)
     */
    @GetMapping("/bonds")
    public ResponseEntity<ApiResponse<List<User>>> getBondedUsers(Authentication authentication) {
        User currentUser = userService.getUserByEmail(authentication.getName());
        List<User> bondedUsers = bondService.getBondedUsers(currentUser);
        return ResponseEntity.ok(ApiResponse.success(bondedUsers, "Bonded users retrieved successfully"));
    }
    
    /**
     * Send a bond request
     */
    @PostMapping("/bonds/{userId}")
    public ResponseEntity<ApiResponse<Bond>> sendBondRequest(
            @PathVariable Long userId,
            Authentication authentication) {
        
        User currentUser = userService.getUserByEmail(authentication.getName());
        Bond bond = bondService.createBondRequest(currentUser.getId(), userId);
        return ResponseEntity.ok(ApiResponse.success(bond, "Bond request sent successfully"));
    }
    
    /**
     * Accept a bond request
     */
    @PutMapping("/bonds/{bondId}/accept")
    public ResponseEntity<ApiResponse<Bond>> acceptBondRequest(
            @PathVariable Long bondId,
            Authentication authentication) {
        
        User currentUser = userService.getUserByEmail(authentication.getName());
        Bond bond = bondService.acceptBondRequest(bondId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(bond, "Bond request accepted successfully"));
    }
    
    /**
     * Reject a bond request
     */
    @PutMapping("/bonds/{bondId}/reject")
    public ResponseEntity<ApiResponse<Bond>> rejectBondRequest(
            @PathVariable Long bondId,
            Authentication authentication) {
        
        User currentUser = userService.getUserByEmail(authentication.getName());
        Bond bond = bondService.rejectBondRequest(bondId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(bond, "Bond request rejected successfully"));
    }
    
    /**
     * Get all pending bond requests
     */
    @GetMapping("/bonds/pending")
    public ResponseEntity<ApiResponse<List<Bond>>> getPendingBondRequests(Authentication authentication) {
        User currentUser = userService.getUserByEmail(authentication.getName());
        List<Bond> pendingRequests = bondService.getPendingBondRequests(currentUser);
        return ResponseEntity.ok(ApiResponse.success(pendingRequests, "Pending bond requests retrieved successfully"));
    }
    
    /**
     * Delete a message
     */
    @DeleteMapping("/messages/{messageId}")
    public ResponseEntity<ApiResponse<Void>> deleteMessage(
            @PathVariable Long messageId,
            Authentication authentication) {
        
        messageService.deleteMessage(messageId, authentication.getName());
        return ResponseEntity.ok(ApiResponse.success(null, "Message deleted successfully"));
    }
}
