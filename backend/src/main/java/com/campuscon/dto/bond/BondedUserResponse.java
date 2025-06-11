package com.campuscon.dto.bond;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning information about bonded users
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BondedUserResponse {
    private Long userId;
    private String username;
    private String displayName;
    private String profilePictureUrl;
    private String collegeName;
}
