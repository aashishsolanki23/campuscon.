package com.campuscon.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Unified User model for CampusCon - representing all users regardless of type (student or non-student).
 * Implements both UserDetails (Spring Security) and OAuth2User (for OAuth authentication)
 */
@Data
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User implements UserDetails, OAuth2User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    /**
     * College name for user profile
     */
    @Column(name = "college_name")
    private String collegeName;
    
    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "display_name")
    private String displayName;
    
    // User type defines the role of the user
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_types", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "type")
    private Set<String> userTypes = new HashSet<>();
    
    @Column(name = "is_society", nullable = false, columnDefinition = "boolean default false")
    private boolean isSociety = false;
    
    // Custom URLs storage - for profile links
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<UserCustomUrl> customUrls;
    
    // Contact and notification
    @Column(name = "fcm_token")
    private String fcmToken;
    
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;
    
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;
    

    
    @Column(name = "is_online")
    private boolean isOnline;
    
    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;
    
    @Column(name = "is_email_verified")
    private boolean isEmailVerified;
    
    // OAuth2 related fields
    @Column(name = "provider")
    private String provider; // google, facebook, etc.
    
    @Column(name = "provider_id")
    private String providerId;
    
    /**
     * OpenID Connect specific fields (for Google OAuth)
     */
    @Column(name = "oidc_id_token")
    private String oidcIdToken;
    
    @Column(name = "oidc_sub")
    private String oidcSub;
    
    // Additional fields for OAuth2
    @Transient
    private Map<String, Object> attributes;
    
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return userTypes.stream()
                .map(type -> new SimpleGrantedAuthority("ROLE_" + type))
                .toList();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.isEmailVerified;
    }
    
    public boolean isSociety() {
        return this.isSociety;
    }
    
    // OAuth2User implementation
    @Override
    public Map<String, Object> getAttributes() {
        return attributes == null ? new HashMap<>() : attributes;
    }
    
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
    
    @Override
    public String getName() {
        return this.username;
    }
}
