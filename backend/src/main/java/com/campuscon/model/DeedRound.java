package com.campuscon.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity representing a round in a Deed (event).
 * Each Deed can have multiple rounds with different details and schedules.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "deed_rounds")
public class DeedRound {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "round_name", nullable = false)
    private String roundName;
    
    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;
    
    @Column(name = "round_url")
    private String roundUrl;
    
    @Column(name = "round_date_time")
    private LocalDateTime roundDateTime;
    
    @Column(name = "round_venue")
    private String roundVenue;
    
    @Column(name = "round_description", columnDefinition = "TEXT")
    private String roundDescription;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deed_id", nullable = false)
    private Deed deed;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
