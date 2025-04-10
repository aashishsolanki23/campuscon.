package com.campuscon.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing society roles for members in the CampusCon application
 */
@Entity
@Table(name = "society_roles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocietyRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "society_id", nullable = false)
    private User society;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "role_name", nullable = false)
    private String roleName;

    @Column(name = "can_post", nullable = false)
    @Builder.Default
    private boolean canPost = false;

    @Column(name = "can_approve_posts", nullable = false)
    @Builder.Default
    private boolean canApprovePosts = false;

    @Column(name = "can_manage_members", nullable = false)
    @Builder.Default
    private boolean canManageMembers = false;

    @Column(name = "can_modify_settings", nullable = false)
    @Builder.Default
    private boolean canModifySettings = false;

    @Column(name = "is_president", nullable = false)
    @Builder.Default
    private boolean isPresident = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
