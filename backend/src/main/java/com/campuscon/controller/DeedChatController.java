package com.campuscon.controller;

import com.campuscon.dto.ApiResponse;
import com.campuscon.dto.chat.ChatGroupDTO;
import com.campuscon.model.Deed;
import com.campuscon.model.TeamParticipant;
import com.campuscon.model.User;
import com.campuscon.repository.DeedRepository;
import com.campuscon.repository.TeamParticipantRepository;
import com.campuscon.repository.UserRepository;
import com.campuscon.service.DeedChatGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the in-group chat system for deeds.
 */
@RestController
@RequestMapping("/api/deeds/chat")
@RequiredArgsConstructor
@Slf4j
public class DeedChatController {
    private final DeedRepository deedRepository;
    private final TeamParticipantRepository teamParticipantRepository;
    private final UserRepository userRepository;
    private final DeedChatGroupService deedChatGroupService;
    
    /**
     * Get all chat groups for a deed that the authenticated user has access to.
     * 
     * @param deedId The deed ID
     * @param user The authenticated user
     * @return ResponseEntity with list of chat groups
     */
    @GetMapping("/groups/{deedId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<ChatGroupDTO>>> getChatGroups(
            @PathVariable Long deedId,
            @AuthenticationPrincipal User user) {
        
        Optional<Deed> deedOpt = deedRepository.findById(deedId);
        
        if (deedOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Deed not found with ID: " + deedId));
        }
        
        Deed deed = deedOpt.get();
        List<ChatGroupDTO> chatGroups = deedChatGroupService.getUserAccessibleChatGroups(deed, user);
        
        return ResponseEntity.ok(ApiResponse.success(chatGroups));
    }
    
    /**
     * Create a new team for a deed with the specified participants.
     * 
     * @param deedId The deed ID
     * @param teamName The name of the team
     * @param participantIds The IDs of the users to add to the team
     * @param user The authenticated user (must be deed creator)
     * @return ResponseEntity with the created team
     */
    @PostMapping("/teams/{deedId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createTeam(
            @PathVariable Long deedId,
            @RequestParam String teamName,
            @RequestParam List<Long> participantIds,
            @AuthenticationPrincipal User user) {
        
        Optional<Deed> deedOpt = deedRepository.findById(deedId);
        
        if (deedOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Deed not found with ID: " + deedId));
        }
        
        Deed deed = deedOpt.get();
        
        // Check if user is the creator of the deed
        if (!user.getId().equals(deed.getCreator().getId())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Only the creator of the deed can create teams"));
        }
        
        // Check if team name is already taken for this deed
        Optional<TeamParticipant> existingTeam = teamParticipantRepository.findByDeedAndTeamName(deed, teamName);
        if (existingTeam.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("A team with this name already exists for this deed"));
        }
        
        // Get all participants
        List<User> participants = userRepository.findAllById(participantIds);
        
        if (participants.isEmpty() || participants.size() != participantIds.size()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("One or more participants not found"));
        }
        
        // Create team and chat groups
        TeamParticipant team = deedChatGroupService.createTeamWithChatGroups(deed, teamName, participants);
        
        return ResponseEntity.ok(ApiResponse.success("Team created successfully with ID: " + team.getId()));
    }
    
    /**
     * Shortlist a team to the next round.
     * 
     * @param teamId The team ID
     * @param nextRound The next round number
     * @param user The authenticated user (must be deed creator)
     * @return ResponseEntity with success message
     */
    @PostMapping("/teams/{teamId}/shortlist")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> shortlistTeam(
            @PathVariable Long teamId,
            @RequestParam int nextRound,
            @AuthenticationPrincipal User user) {
        
        Optional<TeamParticipant> teamOpt = teamParticipantRepository.findById(teamId);
        
        if (teamOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Team not found with ID: " + teamId));
        }
        
        TeamParticipant team = teamOpt.get();
        Deed deed = team.getDeed();
        
        // Check if user is the creator of the deed
        if (!user.getId().equals(deed.getCreator().getId())) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Only the creator of the deed can shortlist teams"));
        }
        
        // Check if next round is valid (greater than current round)
        if (nextRound <= team.getCurrentRound()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Next round must be greater than current round: " + team.getCurrentRound()));
        }
        
        // Shortlist team
        team = deedChatGroupService.shortlistTeam(team, nextRound);
        
        return ResponseEntity.ok(ApiResponse.success("Team shortlisted successfully to round " + nextRound));
    }
    
    /**
     * Export all participants for a deed as a CSV file.
     * 
     * @param deedId The deed ID
     * @param user The authenticated user (must be deed creator)
     * @return ResponseEntity with CSV file
     */
    @GetMapping("/export/{deedId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Resource> exportParticipants(
            @PathVariable Long deedId,
            @AuthenticationPrincipal User user) {
        
        Optional<Deed> deedOpt = deedRepository.findById(deedId);
        
        if (deedOpt.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Deed deed = deedOpt.get();
        
        // Check if user is the creator of the deed
        if (!user.getId().equals(deed.getCreator().getId())) {
            return ResponseEntity.badRequest().build();
        }
        
        // Generate CSV
        String csv = deedChatGroupService.generateParticipantsCSV(deed);
        
        // Create filename with deed title and timestamp
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm"));
        String filename = deed.getTitle().replaceAll("[^a-zA-Z0-9]", "_") + "_participants_" + timestamp + ".csv";
        
        // Create resource from CSV string
        ByteArrayResource resource = new ByteArrayResource(csv.getBytes(StandardCharsets.UTF_8));
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename);
        
        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .contentLength(resource.contentLength())
                .body(resource);
    }
}
