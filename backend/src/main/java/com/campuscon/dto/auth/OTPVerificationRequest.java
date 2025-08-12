package com.campuscon.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OTPVerificationRequest {
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;
    
    @NotBlank(message = "OTP code is required")
    private String otpCode;
    
    private String type = "EMAIL_VERIFICATION"; // Default type
}
