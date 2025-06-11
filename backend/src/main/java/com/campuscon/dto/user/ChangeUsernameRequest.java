package com.campuscon.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for changing a user's username
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeUsernameRequest {
    
    @NotBlank(message = "New username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String newUsername;
}
