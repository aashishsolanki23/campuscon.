package com.campuscon.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity class representing a team participating in a deed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "team_participants")
public class TeamParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String teamName;
    
    @ManyToOne
    @JoinColumn(name = "deed_id", nullable = false)
    private Deed deed;
    
    @ManyToMany
    @JoinTable(
        name = "team_members",
        joinColumns = @JoinColumn(name = "team_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> members = new HashSet<>();
    
    @Column(name = "is_shortlisted")
    private boolean isShortlisted;
    
    @Column(name = "current_round")
    private int currentRound;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "shortlisted_at")
    private LocalDateTime shortlistedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        currentRound = 1; // Start at round 1
    }
}
