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
@Table(name = "bricks")
public class Brick {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "image_url", nullable = false)
    private String imageUrl;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "is_moderated")
    private boolean isModerated;
    
    @Column(name = "is_approved")
    private boolean isApproved;
    
    @Column(name = "likes_count")
    private long likesCount;
    
    @Column(name = "comments_count")
    private long commentsCount;
    
    @Column(name = "saves_count")
    private long savesCount;
    
    @Column(name = "shares_count")
    private long sharesCount;
    
    @Builder.Default
    @OneToMany(mappedBy = "brick", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BrickComment> comments = new ArrayList<>();
    
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "brick_likes",
        joinColumns = @JoinColumn(name = "brick_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> likedByUsers = new HashSet<>();
    
    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "brick_saves",
        joinColumns = @JoinColumn(name = "brick_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
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
    
    public void decrementSharesCount() {
        if (this.sharesCount > 0) {
            this.sharesCount--;
        }
    }
}
