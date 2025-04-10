package com.campuscon.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "deeds")
public class Deed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "banner_url", nullable = false)
    private String bannerUrl;
    
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;
    
    @Column(name = "venue")
    private String venue;
    
    @Column(name = "category")
    private String category;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "society_id", nullable = false)
    private User society;
    
    @Column(name = "likes_count")
    private long likesCount;
    
    @Column(name = "comments_count")
    private long commentsCount;
    
    @Column(name = "saves_count")
    private long savesCount;
    
    @Column(name = "shares_count")
    private long sharesCount;
    
    @Column(name = "is_moderated")
    private boolean isModerated;
    
    @Column(name = "is_approved")
    private boolean isApproved;
    
    @Column(name = "registration_enabled", nullable = false, columnDefinition = "boolean default false")
    private boolean registrationEnabled;
    
    @Column(name = "eligibility_criteria", columnDefinition = "TEXT")
    private String eligibilityCriteria;
    
    @Column(name = "max_registrations")
    private Integer maxRegistrations;
    
    @Column(name = "require_approval", nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private boolean requireApproval = true;
    
    @Column(name = "require_registration_approval", nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private boolean requireRegistrationApproval = true;
    
    @Column(name = "allow_waitlist", nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private boolean allowWaitlist = true;
    
    @Column(name = "notify_on_registration", nullable = false, columnDefinition = "boolean default true")
    @Builder.Default
    private boolean notifyOnRegistration = true;
    
    @Column(name = "allow_team_registration", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private boolean allowTeamRegistration = false;
    
    @Column(name = "max_team_size")
    private Integer maxTeamSize;
    
    @Column(name = "additional_fields_config", columnDefinition = "TEXT")
    private String additionalFieldsConfig;
    
    @OneToMany(mappedBy = "deed", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeedRegistration> registrations = new ArrayList<>();
    
    @OneToMany(mappedBy = "deed", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeedComment> comments = new ArrayList<>();
    
    @ManyToMany
    @JoinTable(
        name = "deed_likes",
        joinColumns = @JoinColumn(name = "deed_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> likedByUsers = new HashSet<>();
    
    @ManyToMany
    @JoinTable(
        name = "deed_saves",
        joinColumns = @JoinColumn(name = "deed_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> savedByUsers = new HashSet<>();
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public void incrementLikesCount() {
        this.likesCount++;
    }
    
    public void decrementLikesCount() {
        if (this.likesCount > 0) {
            this.likesCount--;
        }
    }
    
    public void incrementSavesCount() {
        this.savesCount++;
    }
    
    public void decrementSavesCount() {
        if (this.savesCount > 0) {
            this.savesCount--;
        }
    }
    
    public void incrementCommentsCount() {
        this.commentsCount++;
    }
    
    public void decrementCommentsCount() {
        if (this.commentsCount > 0) {
            this.commentsCount--;
        }
    }
    
    public void incrementSharesCount() {
        this.sharesCount++;
    }
}
