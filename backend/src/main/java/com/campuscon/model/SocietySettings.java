package com.campuscon.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Entity representing society-specific settings in the CampusCon application
 */
@Entity
@Table(name = "society_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocietySettings {

    @Id
    @Column(name = "society_id")
    private Long societyId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "society_id")
    private User society;

    // Society Management Settings
    @Column(name = "auto_approve_bonding")
    @Builder.Default
    private boolean autoApproveBonding = false;

    @Column(name = "allow_member_posting")
    @Builder.Default
    private boolean allowMemberPosting = true;

    @Column(name = "require_post_approval")
    @Builder.Default
    private boolean requirePostApproval = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relationships
    @OneToMany(mappedBy = "society", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SocietyRole> societyRoles;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
