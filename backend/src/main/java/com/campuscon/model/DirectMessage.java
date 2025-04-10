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
@Table(name = "direct_messages")
public class DirectMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    @Column(name = "is_read")
    private boolean isRead;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Column(name = "is_pinned")
    private boolean isPinned;

    @ManyToOne
    @JoinColumn(name = "reply_to_id")
    private DirectMessage replyTo;

    @ElementCollection
    @CollectionTable(name = "direct_message_reactions", 
                    joinColumns = @JoinColumn(name = "message_id"))
    @Column(name = "reaction")
    private Set<String> reactions = new HashSet<>();
    
    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }
}
