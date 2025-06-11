package com.campuscon.model;

import com.campuscon.enums.DeedCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Entity representing an event (Deed) in the CampusCon platform.
 * Deeds are created by users and can be viewed and registered for by other users.
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(toBuilder = true)
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
    
    @Column(name = "thumbnail_url")
    private String thumbnailUrl;
    
    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;
    
    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;
    
    @Column(name = "venue")
    private String venue;
    
    @Column(name = "address", columnDefinition = "TEXT")
    private String address;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    @Builder.Default
    private DeedCategory category = DeedCategory.EVENT;
    
    @Column(name = "url")
    private String url;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "created_by")
    private User createdBy;
    
    @Column(name = "comments_count")
    @Builder.Default
    private long commentsCount = 0;
    
    @Column(name = "saves_count")
    @Builder.Default
    private long savesCount = 0;
    
    @Column(name = "shares_count")
    @Builder.Default
    private long sharesCount = 0;
    
    @Column(name = "is_moderated")
    @Builder.Default
    private boolean isModerated = false;
    
    @Column(name = "is_deleted")
    @Builder.Default
    private boolean isDeleted = false;
    
    @Column(name = "is_featured")
    @Builder.Default
    private boolean isFeatured = false;
    
    @Column(name = "is_approved")
    @Builder.Default
    private boolean isApproved = true;
    
    @OneToMany(mappedBy = "deed", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeedComment> comments = new ArrayList<>();
    
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
    
    // Registration settings
    @Column(name = "registration_required")
    @Builder.Default
    private Boolean registrationRequired = false;
    
    @Column(name = "registration_due_date")
    private LocalDateTime registrationDueDate;
    
    @Column(name = "is_registration_open")
    @Builder.Default
    private Boolean isRegistrationOpen = true;
    
    @Column(name = "registration_enabled")
    @Builder.Default
    private boolean registrationEnabled = true;
    
    @Column(name = "is_open_for_all")
    @Builder.Default
    private boolean isOpenForAll = true;
    
    @Column(name = "is_registration_only")
    @Builder.Default
    private boolean isRegistrationOnly = false;
    
    @Column(name = "is_team_event")
    @Builder.Default
    private boolean isTeamEvent = false;
    
    @Column(name = "min_team_size")
    private Integer minTeamSize;
    
    @Column(name = "max_team_size")
    private Integer maxTeamSize;
    
    @Column(name = "certificates_provided")
    @Builder.Default
    private boolean certificatesProvided = false;
    
    @Column(name = "eligibility_criteria", columnDefinition = "TEXT")
    private String eligibilityCriteria;
    
    @Column(name = "require_approval")
    @Builder.Default
    private boolean requireApproval = false;
    
    @Column(name = "max_registrations")
    private Integer maxRegistrations;
    
    @Column(name = "registration_fee")
    private Double registrationFee;
    
    @Column(name = "notify_on_registration")
    @Builder.Default
    private boolean notifyOnRegistration = true;
    
    @Column(name = "allow_waitlist")
    @Builder.Default
    private boolean allowWaitlist = false;
    
    @Column(name = "additional_fields_config", columnDefinition = "TEXT")
    private String additionalFieldsConfig;
    
    @Column(name = "first_prize", columnDefinition = "TEXT")
    private String firstPrize;
    
    @Column(name = "second_prize", columnDefinition = "TEXT")
    private String secondPrize;
    
    @Column(name = "third_prize", columnDefinition = "TEXT")
    private String thirdPrize;
    
    @OneToMany(mappedBy = "deed", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<DeedRound> rounds = new ArrayList<>();
    
    @OneToMany(mappedBy = "deed", cascade = CascadeType.ALL)
    @Builder.Default
    private List<DeedRegistration> registrations = new ArrayList<>();
    
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
    
    /**
     * Convenience method to check if this deed is of a specific category
     * @param categoryToCheck the DeedCategory to check against
     * @return true if this deed is of the specified category
     */
    public boolean isOfCategory(DeedCategory categoryToCheck) {
        return this.category == categoryToCheck;
    }
    
    /**
     * Get a description of the deed category suitable for display
     * @return a human-readable description of the deed category
     */
    public String getCategoryDisplayName() {
        return this.category.getDisplayName();
    }
    
    /**
     * Alias for getCreatedBy() to maintain compatibility with existing code
     * @return the creator of this deed
     */
    public User getCreator() {
        return this.createdBy;
    }
    
    /**
     * Alias for setCreatedBy() to maintain compatibility with existing code
     * @param creator the creator to set
     */
    public void setCreator(User creator) {
        this.createdBy = creator;
    }
    
    /**
     * Boolean getter for registrationEnabled to maintain compatibility
     */
    public boolean isRegistrationEnabled() {
        return this.registrationEnabled;
    }
    
    /**
     * Boolean getter for requireApproval to maintain compatibility
     */
    public boolean isRequireApproval() {
        return this.requireApproval;
    }
    
    /**
     * Boolean getter for openForAll to maintain compatibility
     */
    public boolean isOpenForAll() {
        return this.isOpenForAll;
    }
    
    /**
     * Boolean getter for registrationOnly to maintain compatibility
     */
    public boolean isRegistrationOnly() {
        return this.isRegistrationOnly;
    }
    
    /**
     * Boolean getter for teamEvent to maintain compatibility
     */
    public boolean isTeamEvent() {
        return this.isTeamEvent;
    }
    
    /**
     * Boolean getter for certificatesProvided to maintain compatibility
     */
    public boolean isCertificatesProvided() {
        return this.certificatesProvided;
    }
    
    /**
     * Boolean getter for notifyOnRegistration to maintain compatibility
     */
    public boolean isNotifyOnRegistration() {
        return this.notifyOnRegistration;
    }
    
    /**
     * Boolean getter for allowWaitlist to maintain compatibility
     */
    public boolean isAllowWaitlist() {
        return this.allowWaitlist;
    }
    
    /**
     * Setter for notifyOnRegistration field
     * @param notifyOnRegistration the value to set
     */
    public void setNotifyOnRegistration(boolean notifyOnRegistration) {
        this.notifyOnRegistration = notifyOnRegistration;
    }
    
    /**
     * Setter for allowWaitlist field
     * @param allowWaitlist the value to set
     */
    public void setAllowWaitlist(boolean allowWaitlist) {
        this.allowWaitlist = allowWaitlist;
    }
}
