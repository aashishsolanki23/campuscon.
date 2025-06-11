package com.campuscon.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing user settings in the CampusCon application
 */
@Entity
@Table(name = "user_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Settings {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    // Profile Management Settings
    @Column(name = "profile_visibility")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProfileVisibility profileVisibility = ProfileVisibility.PUBLIC;

    @Column(name = "bond_request_permission")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private BondRequestPermission bondRequestPermission = BondRequestPermission.EVERYONE;

    @Column(name = "comment_permission")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CommentPermission commentPermission = CommentPermission.EVERYONE;

    // Notification Settings
    @Column(name = "push_notifications_enabled")
    @Builder.Default
    private boolean pushNotificationsEnabled = true;

    @Column(name = "bond_request_notifications")
    @Builder.Default
    private boolean bondRequestNotifications = true;

    @Column(name = "message_notifications")
    @Builder.Default
    private boolean messageNotifications = true;

    @Column(name = "interaction_notifications")
    @Builder.Default
    private boolean interactionNotifications = true;

    @Column(name = "event_notifications")
    @Builder.Default
    private boolean eventNotifications = true;

    @Column(name = "email_notifications_enabled")
    @Builder.Default
    private boolean emailNotificationsEnabled = true;

    // App Preferences
    @Column(name = "theme")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ThemePreference themePreference = ThemePreference.LIGHT;

    @Column(name = "language")
    @Builder.Default
    private String language = "English";

    @Column(name = "auto_load_media")
    @Builder.Default
    private boolean autoLoadMedia = true;

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

    // Enums for settings options
    public enum ProfileVisibility {
        PUBLIC, BONDED_ONLY
    }

    public enum BondRequestPermission {
        EVERYONE, ONLY_INSTITUTION_MEMBERS, NOBODY
    }

    public enum CommentPermission {
        EVERYONE, BONDED_ONLY, NOBODY
    }

    public enum ThemePreference {
        LIGHT, DARK
    }
}
