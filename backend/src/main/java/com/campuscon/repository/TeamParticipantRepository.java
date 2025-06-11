package com.campuscon.repository;

import com.campuscon.model.Deed;
import com.campuscon.model.TeamParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamParticipantRepository extends JpaRepository<TeamParticipant, Long> {
    
    /**
     * Find all teams in a deed
     */
    List<TeamParticipant> findByDeedOrderByCreatedAtDesc(Deed deed);
    
    /**
     * Find all shortlisted teams in a deed
     */
    List<TeamParticipant> findByDeedAndIsShortlistedTrueOrderByCreatedAtDesc(Deed deed);
    
    /**
     * Find teams that a user is a member of for a specific deed
     */
    @Query("SELECT tp FROM TeamParticipant tp JOIN tp.members m WHERE m.id = :userId AND tp.deed.id = :deedId")
    List<TeamParticipant> findByMemberIdAndDeedId(@Param("userId") Long userId, @Param("deedId") Long deedId);
    
    /**
     * Find a single team by its name in a deed
     */
    Optional<TeamParticipant> findByDeedAndTeamName(Deed deed, String teamName);
    
    /**
     * Check if a user is part of any team in a specific deed
     */
    @Query("SELECT COUNT(tp) > 0 FROM TeamParticipant tp JOIN tp.members m WHERE m.id = :userId AND tp.deed.id = :deedId")
    boolean existsByMemberIdAndDeedId(@Param("userId") Long userId, @Param("deedId") Long deedId);
    
    /**
     * Delete all teams associated with a deed
     */
    void deleteAllByDeed(Deed deed);
}
