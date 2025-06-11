package com.campuscon.model;

import com.campuscon.enums.DeedGroupType;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity class representing the relationship between deeds and chat groups.
 * Manages the hierarchical structure of chat groups for each deed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "deed_chat_groups")
public class DeedChatGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "deed_id", nullable = false)
    private Deed deed;

    @ManyToOne
    @JoinColumn(name = "chat_group_id", nullable = false)
    private ChatGroup chatGroup;
    
    /**
     * Defines the type of group in the hierarchy
     * MAIN - The main group for the deed, named after the deed title
     * PARTICIPANT_TEAM - Group for specific team/admin interactions
     * PARTICIPANT_ALL - Group for all participants and admins
     * CREATOR_TEAM - Group for creator to manage specific teams
     * CREATOR_ALL - Group for creator to make announcements to all participants
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "group_type", nullable = false)
    private DeedGroupType groupType;
    
    /**
     * For PARTICIPANT_TEAM and CREATOR_TEAM types, this references the team ID
     */
    @Column(name = "team_id")
    private Long teamId;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
