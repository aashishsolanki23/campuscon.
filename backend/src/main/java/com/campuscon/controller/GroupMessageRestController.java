package com.campuscon.controller;

import com.campuscon.dto.ApiResponse;
import com.campuscon.dto.chat.ChatGroupDTO;
import com.campuscon.dto.chat.MessageDTO;
import com.campuscon.model.ChatGroup;
import com.campuscon.model.GroupMessage;
import com.campuscon.model.User;
import com.campuscon.repository.ChatGroupRepository;
import com.campuscon.repository.GroupMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST controller for group messages and chat groups.
 * Provides endpoints to fetch messages and group information.
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class GroupMessageRestController {

    private final ChatGroupRepository chatGroupRepository;
    private final GroupMessageRepository groupMessageRepository;

    /**
     * Get messages for a specific chat group.
     *
     * @param groupId ID of the chat group
     * @param page Page number (0-based)
     * @param size Page size
     * @param user Authenticated user
     * @return ResponseEntity with messages
     */
    @GetMapping("/groups/{groupId}/messages")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MessageDTO>>> getGroupMessages(
            @PathVariable Long groupId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @AuthenticationPrincipal User user) {

        // Check if the group exists
        Optional<ChatGroup> groupOpt = chatGroupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Chat group not found with ID: " + groupId));
        }

        ChatGroup group = groupOpt.get();

        // Check if user is a member of this group
        if (!group.getMembers().contains(user)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("You are not a member of this chat group"));
        }

        // Fetch messages (newest first, then paginate)
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        Page<GroupMessage> messages = groupMessageRepository.findByGroupAndIsDeletedFalse(group, pageable);

        // Convert to DTOs
        List<MessageDTO> messageDTOs = messages.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(messageDTOs));
    }

    /**
     * Get groups where the current user is a member.
     *
     * @param user Authenticated user
     * @return ResponseEntity with list of groups
     */
    @GetMapping("/groups")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ChatGroupDTO>>> getUserGroups(
            @AuthenticationPrincipal User user) {

        // Find all groups where user is a member
        List<ChatGroup> userGroups = chatGroupRepository.findByMembersContaining(user);

        // Convert to DTOs
        List<ChatGroupDTO> groupDTOs = userGroups.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(groupDTOs));
    }

    /**
     * Get details for a specific group.
     *
     * @param groupId ID of the chat group
     * @param user Authenticated user
     * @return ResponseEntity with group details
     */
    @GetMapping("/groups/{groupId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<ChatGroupDTO>> getGroupDetails(
            @PathVariable Long groupId,
            @AuthenticationPrincipal User user) {

        // Check if the group exists
        Optional<ChatGroup> groupOpt = chatGroupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Chat group not found with ID: " + groupId));
        }

        ChatGroup group = groupOpt.get();

        // Check if user is a member of this group
        if (!group.getMembers().contains(user)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("You are not a member of this chat group"));
        }

        // Convert to DTO
        ChatGroupDTO groupDTO = convertToDTO(group);

        return ResponseEntity.ok(ApiResponse.success(groupDTO));
    }

    /**
     * Convert a GroupMessage to MessageDTO.
     *
     * @param message GroupMessage entity
     * @return MessageDTO
     */
    private MessageDTO convertToDTO(GroupMessage message) {
        MessageDTO dto = new MessageDTO();
        dto.setId(message.getId());
        dto.setGroupId(message.getGroup().getId());
        dto.setContent(message.getContent());
        dto.setImageUrl(message.getImageUrl());
        dto.setUrl(message.getUrl());
        dto.setUrlMetadata(message.getUrlMetadata());
        dto.setSenderId(message.getSender().getId());
        dto.setSenderName(message.getSender().getDisplayName());
        dto.setSenderProfileImage(message.getSender().getProfilePictureUrl());
        dto.setSentAt(message.getSentAt().toString());
        dto.setDeleted(message.isDeleted());
        dto.setPinned(message.isPinned());

        // Handle reply to message
        if (message.getReplyTo() != null) {
            dto.setReplyToId(message.getReplyTo().getId());
            dto.setReplyToContent(message.getReplyTo().getContent());
            dto.setReplyToSenderId(message.getReplyTo().getSender().getId());
            dto.setReplyToSenderName(message.getReplyTo().getSender().getDisplayName());
        }

        return dto;
    }

    /**
     * Convert a ChatGroup to ChatGroupDTO.
     *
     * @param group ChatGroup entity
     * @return ChatGroupDTO
     */
    private ChatGroupDTO convertToDTO(ChatGroup group) {
        ChatGroupDTO dto = new ChatGroupDTO();
        dto.setId(group.getId());
        dto.setName(group.getName());
        dto.setDescription(group.getDescription());
        dto.setGroupImageUrl(group.getGroupImageUrl());
        dto.setCreatedAt(group.getCreatedAt().toString());
        dto.setMemberCount(group.getMembers().size());
        
        // Add other fields as needed from DeedChatGroup/TeamParticipant
        // These would come from a joined query in a real implementation
        
        return dto;
    }
}
