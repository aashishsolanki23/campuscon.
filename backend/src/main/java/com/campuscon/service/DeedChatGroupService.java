package com.campuscon.service;

import com.campuscon.dto.chat.ChatGroupDTO;
import com.campuscon.dto.chat.MessageDTO;
import com.campuscon.enums.DeedGroupType;
import com.campuscon.model.ChatGroup;
import com.campuscon.model.Deed;
import com.campuscon.model.DeedChatGroup;
import com.campuscon.model.GroupMessage;
import com.campuscon.model.TeamParticipant;
import com.campuscon.model.User;
import com.campuscon.repository.ChatGroupRepository;
import com.campuscon.repository.DeedChatGroupRepository;
import com.campuscon.repository.DeedRepository;
import com.campuscon.repository.TeamParticipantRepository;
import com.campuscon.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service for managing the in-group chat system for deeds.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeedChatGroupService {
    private final DeedRepository deedRepository;
    private final ChatGroupRepository chatGroupRepository;
    private final DeedChatGroupRepository deedChatGroupRepository;
    private final TeamParticipantRepository teamParticipantRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Check if a deed exists by ID.
     * 
     * @param deedId ID of the deed to check
     * @return true if deed exists, false otherwise
     */
    public boolean deedExists(Long deedId) {
        return deedRepository.existsById(deedId);
    }
    
    /**
     * Add a user to all the appropriate chat groups for a deed based on their role
     * 
     * @param user User to add to groups
     * @param deedId ID of the deed
     * @param isCreator Whether the user is a creator/admin of the deed
     */
    @Transactional
    public void addUserToDeedGroups(User user, Long deedId, boolean isCreator) {
        // Validate the user exists (using userRepository)
        if (!userRepository.existsById(user.getId())) {
            log.error("Cannot add non-existent user {} to deed groups", user.getId());
            return;
        }
        
        Optional<Deed> deedOpt = deedRepository.findById(deedId);
        if (deedOpt.isEmpty()) {
            log.error("Cannot add user to non-existent deed {}", deedId);
            return;
        }
        
        // Get the appropriate groups based on user role
        List<DeedChatGroup> groups = deedChatGroupRepository.findByDeedOrderByGroupTypeAsc(deedOpt.get());
        
        // Filter based on user role
        if (!isCreator) {
            // For regular participants, only include participant groups and main group
            groups = groups.stream()
                .filter(group -> group.getGroupType() == DeedGroupType.PARTICIPANT_ALL ||
                                group.getGroupType() == DeedGroupType.MAIN)
                .toList();
        }
        // For creators, include all groups (no filtering needed)
        
        // Add the user to each group
        for (DeedChatGroup deedGroup : groups) {
            ChatGroup group = deedGroup.getChatGroup();
            if (!group.getMembers().contains(user)) {
                group.getMembers().add(user);
                chatGroupRepository.save(group);
                log.info("Added user {} to deed group {} ({})", 
                        user.getId(), group.getId(), deedGroup.getGroupType());
            }
        }
    }
    
    /**
     * Get a list of users that are members of any team participating in a deed.
     * 
     * @param deedId ID of the deed
     * @return List of users participating in the deed's teams
     */
    public List<User> getDeedParticipants(Long deedId) {
        // Find the deed first
        Optional<Deed> deedOpt = deedRepository.findById(deedId);
        if (deedOpt.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Get all team participants for this deed
        List<TeamParticipant> teamParticipants = teamParticipantRepository.findByDeedOrderByCreatedAtDesc(deedOpt.get());
        
        // Extract unique users from all teams
        Set<User> uniqueParticipants = new HashSet<>();
        for (TeamParticipant team : teamParticipants) {
            uniqueParticipants.addAll(team.getMembers());
        }
        
        return new ArrayList<>(uniqueParticipants);
    }
    
    /**
     * Create the initial group structure when a deed is created.
     * Creates the main group and the creator's announcement group.
     * 
     * @param deed The newly created deed
     */
    @Transactional
    public void createInitialGroupsForDeed(Deed deed) {
        User creator = deed.getCreator();
        
        // Create main group - named after the deed title
        ChatGroup mainGroup = ChatGroup.builder()
                .name(deed.getTitle() + " Group")
                .description("Main group for " + deed.getTitle())
                .isAutoGenerated(true)
                .creator(creator)
                .build();
        
        // Add creator as a member
        Set<User> members = new HashSet<>();
        members.add(creator);
        mainGroup.setMembers(members);
        chatGroupRepository.save(mainGroup);
        
        // Link to deed as the main group
        DeedChatGroup mainDeedGroup = DeedChatGroup.builder()
                .deed(deed)
                .chatGroup(mainGroup)
                .groupType(DeedGroupType.MAIN)
                .build();
        
        deedChatGroupRepository.save(mainDeedGroup);
        
        // Create the creator's announcement group
        ChatGroup creatorAnnouncementGroup = ChatGroup.builder()
                .name(deed.getTitle() + " Announcements")
                .description("Announcements for all participants in " + deed.getTitle())
                .isAutoGenerated(true)
                .creator(creator)
                .build();
        
        // Add creator as a member initially
        creatorAnnouncementGroup.setMembers(members);
        chatGroupRepository.save(creatorAnnouncementGroup);
        
        // Link to deed as the creator's announcement group
        DeedChatGroup creatorAnnouncementDeedGroup = DeedChatGroup.builder()
                .deed(deed)
                .chatGroup(creatorAnnouncementGroup)
                .groupType(DeedGroupType.CREATOR_ALL)
                .build();
        
        deedChatGroupRepository.save(creatorAnnouncementDeedGroup);
        
        log.info("Created initial chat groups for deed: {}", deed.getTitle());
    }
    
    /**
     * Create a team and its associated chat groups for participants.
     * 
     * @param deed The deed
     * @param teamName The name of the team
     * @param participants List of users in the team
     * @return The created TeamParticipant
     */
    @Transactional
    public TeamParticipant createTeamWithChatGroups(Deed deed, String teamName, List<User> participants) {
        User creator = deed.getCreator();
        
        // Create the team
        TeamParticipant team = TeamParticipant.builder()
                .teamName(teamName)
                .deed(deed)
                .build();
        
        // Add all participants to the team
        team.setMembers(new HashSet<>(participants));
        teamParticipantRepository.save(team);
        
        // Create participant team chat group
        ChatGroup teamChatGroup = ChatGroup.builder()
                .name(teamName + " - Team Chat")
                .description("Team chat for " + teamName + " in " + deed.getTitle())
                .isAutoGenerated(true)
                .creator(creator)
                .build();
        
        // Add team members and deed creator to this group
        Set<User> teamChatMembers = new HashSet<>(participants);
        teamChatMembers.add(creator);
        teamChatGroup.setMembers(teamChatMembers);
        chatGroupRepository.save(teamChatGroup);
        
        // Link to deed as a participant team group
        DeedChatGroup teamDeedGroup = DeedChatGroup.builder()
                .deed(deed)
                .chatGroup(teamChatGroup)
                .groupType(DeedGroupType.PARTICIPANT_TEAM)
                .teamId(team.getId())
                .build();
        
        deedChatGroupRepository.save(teamDeedGroup);
        
        // Create creator's team management group
        ChatGroup creatorTeamGroup = ChatGroup.builder()
                .name(teamName + " - Management")
                .description("Management group for " + teamName + " in " + deed.getTitle())
                .isAutoGenerated(true)
                .creator(creator)
                .build();
        
        // Only creator and admins in this group initially
        Set<User> creatorTeamMembers = new HashSet<>();
        creatorTeamMembers.add(creator);
        creatorTeamGroup.setMembers(creatorTeamMembers);
        chatGroupRepository.save(creatorTeamGroup);
        
        // Link to deed as a creator team group
        DeedChatGroup creatorTeamDeedGroup = DeedChatGroup.builder()
                .deed(deed)
                .chatGroup(creatorTeamGroup)
                .groupType(DeedGroupType.CREATOR_TEAM)
                .teamId(team.getId())
                .build();
        
        deedChatGroupRepository.save(creatorTeamDeedGroup);
        
        // Add participants to the all-participants announcement group if it exists
        Optional<DeedChatGroup> announcementGroupOpt = deedChatGroupRepository
                .findByDeedAndGroupType(deed, DeedGroupType.CREATOR_ALL);
        
        if (announcementGroupOpt.isPresent()) {
            ChatGroup announcementGroup = announcementGroupOpt.get().getChatGroup();
            Set<User> currentMembers = announcementGroup.getMembers();
            currentMembers.addAll(participants);
            announcementGroup.setMembers(currentMembers);
            chatGroupRepository.save(announcementGroup);
        }
        
        log.info("Created team {} with chat groups for deed: {}", teamName, deed.getTitle());
        
        return team;
    }
    
    /**
     * Shortlist a team to the next round.
     * 
     * @param team The team to shortlist
     * @param nextRound The next round number
     * @return The updated team
     */
    @Transactional
    public TeamParticipant shortlistTeam(TeamParticipant team, int nextRound) {
        // Mark team as shortlisted
        team.setShortlisted(true);
        team.setCurrentRound(nextRound);
        team.setShortlistedAt(LocalDateTime.now());
        teamParticipantRepository.save(team);
        
        // Send notification message to the team chat
        Optional<DeedChatGroup> teamChatGroupOpt = deedChatGroupRepository.findByDeedAndGroupTypeAndTeamId(
                team.getDeed(), DeedGroupType.PARTICIPANT_TEAM, team.getId());
        
        if (teamChatGroupOpt.isPresent()) {
            ChatGroup teamChatGroup = teamChatGroupOpt.get().getChatGroup();
            
            // Create system message
            GroupMessage message = new GroupMessage();
            message.setContent("Congratulations " + team.getTeamName() + "! You have been shortlisted to round " 
                    + nextRound + ". All the best for the next round!");
            message.setGroup(teamChatGroup);
            message.setSender(team.getDeed().getCreator());
            message.setSentAt(LocalDateTime.now());
            
            // Send notification via WebSocket
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setGroupId(teamChatGroup.getId());
            messageDTO.setContent(message.getContent());
            messageDTO.setSenderId(message.getSender().getId());
            messageDTO.setSenderName(message.getSender().getDisplayName());
            messageDTO.setSentAt(message.getSentAt().toString());
            
            messagingTemplate.convertAndSend("/topic/group/" + teamChatGroup.getId(), messageDTO);
        }
        
        log.info("Shortlisted team {} to round {} for deed {}", 
                team.getTeamName(), nextRound, team.getDeed().getTitle());
        
        return team;
    }
    
    /**
     * Get all chat groups for a deed that a user has access to.
     * 
     * @param deed The deed
     * @param user The user
     * @return List of chat groups the user can access
     */
    public List<ChatGroupDTO> getUserAccessibleChatGroups(Deed deed, User user) {
        List<ChatGroupDTO> result = new ArrayList<>();
        
        // Check if user is the creator of the deed
        boolean isCreator = user.getId().equals(deed.getCreator().getId());
        
        if (isCreator) {
            // Creator gets access to all groups
            List<DeedChatGroup> allGroups = deedChatGroupRepository.findByDeedOrderByGroupTypeAsc(deed);
            
            for (DeedChatGroup deedGroup : allGroups) {
                ChatGroup group = deedGroup.getChatGroup();
                
                ChatGroupDTO dto = new ChatGroupDTO();
                dto.setId(group.getId());
                dto.setName(group.getName());
                dto.setDescription(group.getDescription());
                dto.setGroupImageUrl(group.getGroupImageUrl());
                dto.setCreatedAt(group.getCreatedAt().toString());
                dto.setGroupType(deedGroup.getGroupType().toString());
                
                if (deedGroup.getTeamId() != null) {
                    Optional<TeamParticipant> teamOpt = teamParticipantRepository.findById(deedGroup.getTeamId());
                    teamOpt.ifPresent(team -> {
                        dto.setTeamId(team.getId());
                        dto.setTeamName(team.getTeamName());
                        dto.setTeamShortlisted(team.isShortlisted());
                        dto.setTeamCurrentRound(team.getCurrentRound());
                    });
                }
                
                result.add(dto);
            }
        } else {
            // Participant only gets access to their team groups and the announcement group
            List<TeamParticipant> userTeams = teamParticipantRepository.findByMemberIdAndDeedId(user.getId(), deed.getId());
            
            for (TeamParticipant team : userTeams) {
                // Get team chat group
                Optional<DeedChatGroup> teamChatGroupOpt = deedChatGroupRepository
                        .findByDeedAndGroupTypeAndTeamId(deed, DeedGroupType.PARTICIPANT_TEAM, team.getId());
                
                if (teamChatGroupOpt.isPresent()) {
                    ChatGroup group = teamChatGroupOpt.get().getChatGroup();
                    
                    ChatGroupDTO dto = new ChatGroupDTO();
                    dto.setId(group.getId());
                    dto.setName(group.getName());
                    dto.setDescription(group.getDescription());
                    dto.setGroupImageUrl(group.getGroupImageUrl());
                    dto.setCreatedAt(group.getCreatedAt().toString());
                    dto.setGroupType(DeedGroupType.PARTICIPANT_TEAM.toString());
                    dto.setTeamId(team.getId());
                    dto.setTeamName(team.getTeamName());
                    dto.setTeamShortlisted(team.isShortlisted());
                    dto.setTeamCurrentRound(team.getCurrentRound());
                    
                    result.add(dto);
                }
                
                // Get announcement group
                Optional<DeedChatGroup> announcementGroupOpt = deedChatGroupRepository
                        .findByDeedAndGroupType(deed, DeedGroupType.CREATOR_ALL);
                
                if (announcementGroupOpt.isPresent() && 
                        announcementGroupOpt.get().getChatGroup().getMembers().contains(user)) {
                    ChatGroup group = announcementGroupOpt.get().getChatGroup();
                    
                    ChatGroupDTO dto = new ChatGroupDTO();
                    dto.setId(group.getId());
                    dto.setName(group.getName());
                    dto.setDescription(group.getDescription());
                    dto.setGroupImageUrl(group.getGroupImageUrl());
                    dto.setCreatedAt(group.getCreatedAt().toString());
                    dto.setGroupType(DeedGroupType.PARTICIPANT_ALL.toString());
                    
                    result.add(dto);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Generate a CSV report of all participants in a deed.
     * 
     * @param deed The deed
     * @return The CSV content as a string
     */
    public String generateParticipantsCSV(Deed deed) {
        // Get all teams
        List<TeamParticipant> teams = teamParticipantRepository.findByDeedOrderByCreatedAtDesc(deed);
        
        StringBuilder csv = new StringBuilder();
        
        // Header row
        csv.append("Team Name,Team ID,Is Shortlisted,Current Round,Member ID,Member Name,Member Email,College\n");
        
        for (TeamParticipant team : teams) {
            for (User member : team.getMembers()) {
                csv.append(String.format("\"%s\",%d,%b,%d,%d,\"%s\",\"%s\",\"%s\"\n",
                        team.getTeamName(),
                        team.getId(),
                        team.isShortlisted(),
                        team.getCurrentRound(),
                        member.getId(),
                        member.getDisplayName(),
                        member.getEmail(),
                        member.getCollegeName() != null ? member.getCollegeName() : ""
                ));
            }
        }
        
        return csv.toString();
    }
    
    /**
     * Generate and return a CSV file for download containing all participants in a deed.
     * 
     * @param deedId The ID of the deed
     * @return ResponseEntity containing the CSV file
     */
    public ResponseEntity<byte[]> downloadParticipantsCSV(Long deedId) {
        Optional<Deed> deedOpt = deedRepository.findById(deedId);
        if (deedOpt.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        
        Deed deed = deedOpt.get();
        String csvContent = generateParticipantsCSV(deed);
        
        // Set up headers for CSV file download
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", 
                deed.getTitle().replaceAll("\\s+", "_") + "_participants.csv");
        
        return new ResponseEntity<>(csvContent.getBytes(), headers, HttpStatus.OK);
    }
    
    /**
     * Auto-enroll a user to deed chat groups when they register for a deed.
     * This should be called whenever a user joins a deed or team.
     * 
     * @param userId The ID of the user to enroll
     * @param deedId The ID of the deed
     * @param teamId The ID of the team they're joining (optional, can be null if not joining a specific team)
     * @return true if enrollment was successful, false otherwise
     */
    @Transactional
    public boolean autoEnrollUserToDeedGroups(Long userId, Long deedId, Long teamId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<Deed> deedOpt = deedRepository.findById(deedId);
        
        if (userOpt.isEmpty() || deedOpt.isEmpty()) {
            log.error("Cannot auto-enroll user. User or deed not found: userId={}, deedId={}", userId, deedId);
            return false;
        }
        
        User user = userOpt.get();
        Deed deed = deedOpt.get();
        
        // Check if user is the creator of the deed
        boolean isCreator = user.getId().equals(deed.getCreator().getId());
        
        // First add to general deed groups based on role
        addUserToDeedGroups(user, deedId, isCreator);
        
        // If joining a specific team, add to team chat groups
        if (teamId != null) {
            Optional<TeamParticipant> teamOpt = teamParticipantRepository.findById(teamId);
            if (teamOpt.isPresent()) {
                TeamParticipant team = teamOpt.get();
                
                // Add user to the team's members if not already there
                if (!team.getMembers().contains(user)) {
                    team.getMembers().add(user);
                    teamParticipantRepository.save(team);
                }
                
                // Add user to team chat group
                Optional<DeedChatGroup> teamChatGroupOpt = deedChatGroupRepository
                        .findByDeedAndGroupTypeAndTeamId(deed, DeedGroupType.PARTICIPANT_TEAM, teamId);
                
                if (teamChatGroupOpt.isPresent()) {
                    ChatGroup teamChatGroup = teamChatGroupOpt.get().getChatGroup();
                    if (!teamChatGroup.getMembers().contains(user)) {
                        teamChatGroup.getMembers().add(user);
                        chatGroupRepository.save(teamChatGroup);
                        log.info("Added user {} to team chat group {} for team {}", 
                            userId, teamChatGroup.getId(), teamId);
                    }
                }
                
                // Send welcome message to the team chat
                sendTeamWelcomeMessage(teamId, userId);
            }
        }
        
        return true;
    }
    
    /**
     * Send a welcome message to the team chat when a new user joins.
     *
     * @param teamId The ID of the team
     * @param userId The ID of the user joining
     */
    private void sendTeamWelcomeMessage(Long teamId, Long userId) {
        Optional<TeamParticipant> teamOpt = teamParticipantRepository.findById(teamId);
        Optional<User> userOpt = userRepository.findById(userId);
        
        if (teamOpt.isEmpty() || userOpt.isEmpty()) {
            return;
        }
        
        TeamParticipant team = teamOpt.get();
        User user = userOpt.get();
        
        Optional<DeedChatGroup> teamChatGroupOpt = deedChatGroupRepository
                .findByDeedAndGroupTypeAndTeamId(team.getDeed(), DeedGroupType.PARTICIPANT_TEAM, teamId);
        
        if (teamChatGroupOpt.isPresent()) {
            ChatGroup teamChatGroup = teamChatGroupOpt.get().getChatGroup();
            
            // Create system welcome message
            GroupMessage message = new GroupMessage();
            message.setContent(user.getDisplayName() + " has joined the team chat. Welcome!");
            message.setGroup(teamChatGroup);
            message.setSender(team.getDeed().getCreator()); // System message appears from creator
            message.setSentAt(LocalDateTime.now());
            
            // Send notification via WebSocket
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setGroupId(teamChatGroup.getId());
            messageDTO.setContent(message.getContent());
            messageDTO.setSenderId(message.getSender().getId());
            messageDTO.setSenderName(message.getSender().getDisplayName());
            messageDTO.setSentAt(message.getSentAt().toString());
            
            messagingTemplate.convertAndSend("/topic/group/" + teamChatGroup.getId(), messageDTO);
        }
    }
    
    /**
     * Check if shortlisting is enabled for a deed based on its configuration.
     * 
     * @param deedId The ID of the deed to check
     * @return true if shortlisting is enabled, false otherwise
     */
    public boolean isShortlistingEnabled(Long deedId) {
        Optional<Deed> deedOpt = deedRepository.findById(deedId);
        
        if (deedOpt.isEmpty()) {
            return false;
        }
        
        Deed deed = deedOpt.get();
        
        // Get the maximum current round from any team in this deed
        // We'll use a direct query approach to avoid dependency on a specific repository method
        List<TeamParticipant> teams = teamParticipantRepository.findByDeedOrderByCreatedAtDesc(deed);
        int maxCurrentRound = 0;
        
        for (TeamParticipant team : teams) {
            if (team.getCurrentRound() > maxCurrentRound) {
                maxCurrentRound = team.getCurrentRound();
            }
        }
        
        int totalRounds = deed.getRounds() != null ? deed.getRounds().size() : 0;
        
        // Shortlisting is enabled if there are more rounds defined than the current max round
        // and if the deed is configured as a team event with rounds
        return totalRounds > maxCurrentRound && deed.isTeamEvent();
    }
}
