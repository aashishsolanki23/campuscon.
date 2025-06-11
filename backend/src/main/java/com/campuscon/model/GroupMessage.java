package com.campuscon.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@Entity
@Table(name = "group_messages")
public class GroupMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "url")
    private String url;
    
    @Column(name = "url_metadata", columnDefinition = "TEXT")
    private String urlMetadata;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = false)
    private ChatGroup group;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Column(name = "is_pinned")
    private boolean isPinned;

    @ManyToOne
    @JoinColumn(name = "reply_to_id")
    private GroupMessage replyTo;

    @ElementCollection
    @CollectionTable(name = "group_message_reactions", 
                    joinColumns = @JoinColumn(name = "message_id"))
    @Column(name = "reaction")
    private Set<String> reactions = new HashSet<>();
    
    @ManyToMany
    @JoinTable(
        name = "group_message_read_status",
        joinColumns = @JoinColumn(name = "message_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> readBy = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }
}
