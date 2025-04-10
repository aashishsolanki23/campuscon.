package com.campuscon.dto.deed.registration;

import com.campuscon.model.DeedRegistration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for deed registration responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeedRegistrationResponse {
    
    private Long id;
    private Long deedId;
    private String deedTitle;
    private Long userId;
    private String username;
    private LocalDateTime registeredAt;
    private DeedRegistration.RegistrationStatus status;
    private String teamName;
    private Integer teamSize;
    private String additionalInfo;
    private String rejectionReason;
}
