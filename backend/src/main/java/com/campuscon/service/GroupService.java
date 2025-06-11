package com.campuscon.service;

import com.campuscon.model.ChatGroup;
import com.campuscon.model.User;
import com.campuscon.repository.ChatGroupRepository;
import com.campuscon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
// RedisTemplate import removed as it's no longer needed
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
// Optional import removed as no longer needed

@Service
@RequiredArgsConstructor
public class GroupService {
    private final ChatGroupRepository chatGroupRepository;
    private final UserRepository userRepository;
    // RedisTemplate field removed as it's no longer needed

    /**
     * Auto-generated batch groups functionality has been removed
     */
    // getOrCreateBatchGroup method has been removed
    
    /**
     * Auto-generated batch groups functionality has been removed
     */
    // addUserToBatchGroup method has been removed
    
    /**
     * Creates a custom group with the given users
     */
    public ChatGroup createCustomGroup(String groupName, String groupImageUrl, Long creatorId, Set<Long> memberIds) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creator user not found"));
        
        // Check if a group with this name already exists for this creator
        if (chatGroupRepository.existsByNameAndCreator(groupName, creator)) {
            throw new RuntimeException("You already have a group with this name");
        }
        
        ChatGroup newGroup = new ChatGroup();
        newGroup.setName(groupName);
        newGroup.setGroupImageUrl(groupImageUrl);
        newGroup.setCreator(creator);
        // Auto-generated groups feature has been removed
        
        // Add creator to members
        newGroup.getMembers().add(creator);
        
        // Add other members
        if (memberIds != null && !memberIds.isEmpty()) {
            List<User> members = userRepository.findAllById(memberIds);
            newGroup.getMembers().addAll(members);
        }
        
        return chatGroupRepository.save(newGroup);
    }
    
    /**
     * Gets all groups that a user is a member of
     */
    public List<ChatGroup> getUserGroups(User user) {
        return chatGroupRepository.findUserGroups(user);
    }
    
    /**
     * Add a user to a group
     */
    public void addUserToGroup(Long groupId, Long userId) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Auto-generated groups check has been removed
        
        if (!group.getMembers().contains(user)) {
            group.getMembers().add(user);
            chatGroupRepository.save(group);
        }
    }
    
    /**
     * Remove a user from a group
     */
    public void removeUserFromGroup(Long groupId, Long userId) {
        ChatGroup group = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Auto-generated groups check has been removed
        
        if (group.getCreator().equals(user)) {
            throw new RuntimeException("Group creator cannot leave the group");
        }
        
        if (group.getMembers().contains(user)) {
            group.getMembers().remove(user);
            chatGroupRepository.save(group);
        }
    }
}
