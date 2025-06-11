package com.campuscon.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entity for storing custom URLs for user profiles
 */
@Entity
@Table(name = "user_custom_urls")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCustomUrl {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(nullable = false)
    private String urlName;
    
    @Column(nullable = false)
    private String url;
}
