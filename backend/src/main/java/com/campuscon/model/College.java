package com.campuscon.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "colleges")
public class College {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "university_id", nullable = false)
    private University university;
    
    @Column
    private String emailSubdomain;

    // If the college has a specific format for roll numbers
    @Column
    private String rollNumberFormat;
    
    public String getFullEmailDomain() {
        if (emailSubdomain != null && !emailSubdomain.isEmpty()) {
            return emailSubdomain + "." + university.getEmailDomain();
        }
        return university.getEmailDomain();
    }
}
