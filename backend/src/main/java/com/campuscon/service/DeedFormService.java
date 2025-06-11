package com.campuscon.service;

import com.campuscon.dto.deed.DeedFormRequest;
import com.campuscon.dto.deed.DeedResponse;
import com.campuscon.enums.DeedCategory;
import com.campuscon.dto.deed.DeedRoundDto;
import com.campuscon.exception.ResourceNotFoundException;
import com.campuscon.exception.UnauthorizedException;
import com.campuscon.model.Deed;
import com.campuscon.model.DeedRound;
import com.campuscon.model.User;
// User now has userTypes list instead of UserRole enum
import com.campuscon.repository.DeedRepository;
import com.campuscon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing Deed (event) operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeedFormService {

    private final DeedRepository deedRepository;
    private final UserRepository userRepository;
    private final DeedChatGroupService deedChatGroupService;
    
    /**
     * Create a new Deed from the provided request
     * 
     * @param request The deed form request
     * @param userId The ID of the user creating the deed (must be a society)
     * @return The created deed response
     */
    @Transactional
    public DeedResponse createDeed(DeedFormRequest request, Long userId) {
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Validate that the user is an organizer
        if (!creator.isSociety()) {
            throw new UnauthorizedException("Only organizers can create deeds");
        }
        
        // Create a new deed entity from the request
        Deed deed = mapRequestToDeed(request, null);
        deed.setCreator(creator);
        
        // Save the deed
        Deed savedDeed = deedRepository.save(deed);
        
        // Initialize chat groups for this deed
        deedChatGroupService.createInitialGroupsForDeed(savedDeed);
        
        return DeedResponse.fromEntity(savedDeed);
    }
    
    /**
     * Update an existing Deed
     * 
     * @param id The ID of the deed to update
     * @param request The updated deed form request
     * @param userId The ID of the user updating the deed (must be the creator)
     * @return The updated deed response
     */
    @Transactional
    public DeedResponse updateDeed(Long id, DeedFormRequest request, Long userId) {
        Deed existingDeed = deedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found with ID: " + id));
        
        // Check if the user is the creator
        if (!existingDeed.getCreator().getId().equals(userId)) {
            throw new UnauthorizedException("You can only update deeds you have created");
        }
        
        // Update the deed entity
        Deed updatedDeed = mapRequestToDeed(request, existingDeed);
        
        // Save the updated deed
        Deed savedDeed = deedRepository.save(updatedDeed);
        
        return DeedResponse.fromEntity(savedDeed);
    }
    
    /**
     * Get a deed by ID
     * 
     * @param id The ID of the deed to retrieve
     * @return The deed response
     */
    public DeedResponse getDeedById(Long id) {
        Deed deed = deedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found with ID: " + id));
        
        return DeedResponse.fromEntity(deed);
    }
    
    /**
     * Delete a deed by ID
     * 
     * @param id The ID of the deed to delete
     * @param userId The ID of the user deleting the deed (must be the creator)
     */
    @Transactional
    public void deleteDeed(Long id, Long userId) {
        Deed deed = deedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found with ID: " + id));
        
        // Check if the user is the creator
        if (!deed.getCreator().getId().equals(userId)) {
            throw new UnauthorizedException("You can only delete deeds you have created");
        }
        
        deedRepository.delete(deed);
    }
    
    /**
     * Get all deeds with optional filtering
     * 
     * @param category Optional category filter
     * @param societyId Optional society ID filter
     * @param startDate Optional start date filter (ISO 8601)
     * @param endDate Optional end date filter (ISO 8601)
     * @param isTeamEvent Optional team event filter
     * @param isOpenForAll Optional open for all filter
     * @param searchTerm Optional search term for title and description
     * @param pageable Pagination information
     * @return Page of deed responses
     */
    public Page<DeedResponse> getAllDeeds(
            String category,
            Long societyId,
            String startDate,
            String endDate,
            Boolean isTeamEvent,
            Boolean isOpenForAll,
            String searchTerm,
            Pageable pageable) {
        
        // Implementation would include custom repository methods for filtering
        // This is a simplified version
        Page<Deed> deedPage = deedRepository.findAll(pageable);
        
        return deedPage.map(DeedResponse::fromEntity);
    }
    
    /**
     * Get deeds specific to a user's college
     * 
     * @param user The authenticated user
     * @param pageable Pagination information
     * @return Page of college-specific deed responses
     */
    public Page<DeedResponse> getCollegeDeeds(User user, Pageable pageable) {
        // Get the user's college name
        String collegeName = user.getCollegeName();
        
        if (collegeName == null || collegeName.isEmpty()) {
            // If user has no college, return an empty page
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
        
        // Find deeds for this college
        // This would typically involve a repository method like:
        // Page<Deed> deedPage = deedRepository.findByCollegeName(collegeName, pageable);
        
        // For now, use a simplified version that returns all deeds
        // In a real implementation, you would filter by college
        Page<Deed> deedPage = deedRepository.findAll(pageable);
        
        return deedPage.map(DeedResponse::fromEntity);
    }
    
    /**
     * Approve a deed (admin only)
     * 
     * @param id The ID of the deed to approve
     * @param userId The ID of the user approving the deed (must be an admin)
     * @return The approved deed response
     */
    @Transactional
    public DeedResponse approveDeed(Long id, Long userId) {
        User admin = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        
        // Validate that the user is an admin
        if (!admin.getUserTypes().contains("ADMIN")) {
            throw new UnauthorizedException("Only admins can approve deeds");
        }
        
        Deed deed = deedRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deed not found with ID: " + id));
        
        deed.setApproved(true);
        Deed savedDeed = deedRepository.save(deed);
        
        return DeedResponse.fromEntity(savedDeed);
    }
    
    /**
     * Map a DeedFormRequest to a Deed entity
     * 
     * @param request The deed form request
     * @param existingDeed Optional existing deed for updates
     * @return The mapped deed entity
     */
    private Deed mapRequestToDeed(DeedFormRequest request, Deed existingDeed) {
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
        
        Deed deed = existingDeed != null ? existingDeed : new Deed();
        
        // Convert String category to DeedCategory enum
        DeedCategory category = DeedCategory.fromDisplayName(request.getCategory());
        deed.setCategory(category);
        deed.setTitle(request.getTitle());
        deed.setDescription(request.getDescription());
        deed.setVenue(request.getVenue());
        deed.setUrl(request.getUrl());
        deed.setOpenForAll(request.isOpenForAll());
        deed.setRegistrationOnly(request.isRegistrationOnly());
        deed.setStartDateTime(LocalDateTime.parse(request.getStartDateTime(), formatter));
        deed.setEndDateTime(LocalDateTime.parse(request.getEndDateTime(), formatter));
        deed.setTeamEvent(request.isTeamEvent());
        deed.setMinTeamSize(request.getMinTeamSize());
        deed.setMaxTeamSize(request.getMaxTeamSize());
        deed.setMaxRegistrations(request.getMaxRegistrations());
        deed.setThumbnailUrl(request.getThumbnailUrl());
        deed.setFirstPrize(request.getFirstPrize());
        deed.setSecondPrize(request.getSecondPrize());
        deed.setThirdPrize(request.getThirdPrize());
        deed.setCertificatesProvided(request.isCertificatesProvided());
        
        // Handle registration setting based on isRegistrationOnly
        deed.setRegistrationEnabled(request.isRegistrationOnly());
        
        // Handle rounds
        if (existingDeed != null) {
            // Clear existing rounds to avoid duplicates
            existingDeed.getRounds().clear();
        }
        
        List<DeedRound> rounds = new ArrayList<>();
        for (DeedRoundDto roundDto : request.getRounds()) {
            DeedRound round = new DeedRound();
            round.setRoundName(roundDto.getRoundName());
            round.setRoundUrl(roundDto.getRoundUrl());
            
            if (roundDto.getRoundDateTime() != null && !roundDto.getRoundDateTime().isEmpty()) {
                round.setRoundDateTime(LocalDateTime.parse(roundDto.getRoundDateTime(), formatter));
            }
            
            round.setRoundVenue(roundDto.getRoundVenue());
            round.setRoundDescription(roundDto.getRoundDescription());
            round.setDeed(deed);
            
            rounds.add(round);
        }
        
        deed.setRounds(rounds);
        
        return deed;
    }
    
    // /**
    //  * Get deeds specific to a user's college.
    //  * 
    //  * @param user The authenticated user
    //  * @param pageable Pagination information
    //  * @return Page of deed responses for the user's college
    //  */
    // public Page<DeedResponse> getCollegeDeeds(User user, Pageable pageable) {
    //     // Check if the user has a college name set
    //     if (user.getCollegeName() == null || user.getCollegeName().isEmpty()) {
    //         throw new RuntimeException("You don't have a college associated with your profile");
    //     }
        
    //     // Get all deeds associated with the user's college
    //     String collegeName = user.getCollegeName();
        
    //     // Use the repository method to get deeds by college name
    //     Page<Deed> deeds = deedRepository.findByCollegeNameOrderByStartDateTimeDesc(collegeName, pageable);
        
    //     // Map deeds to DeedResponse objects using the existing fromEntity method
    //     return deeds.map(DeedResponse::fromEntity);
    // }
}
