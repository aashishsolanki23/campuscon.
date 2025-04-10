package com.campuscon.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
    
    @NotBlank(message = "University name is required")
    private String universityName;
    
    @NotBlank(message = "College name is required")
    private String collegeName;

    @NotBlank(message = "Roll number is required for students")
    private String rollNumber;
    
    private String batch;
    
    private String course;

    private boolean isSociety;
    
    private String societyName;
    
    private String societyPresidentName;
}
