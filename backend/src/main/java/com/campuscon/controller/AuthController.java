package com.campuscon.controller;

import com.campuscon.dto.ApiResponse;
import com.campuscon.dto.auth.AuthResponse;
import com.campuscon.dto.auth.LoginRequest;
import com.campuscon.dto.auth.UserRegistrationRequest;
import com.campuscon.dto.auth.OTPVerificationRequest;
import com.campuscon.dto.auth.ForgotPasswordRequest;
import com.campuscon.dto.auth.ResetPasswordRequest;
import com.campuscon.dto.auth.UsernameCreationRequest;
import com.campuscon.service.AuthService;
import com.campuscon.service.OTPService;
import com.campuscon.service.UserRegistrationService;
import com.campuscon.model.OTP;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final OTPService otpService;
    private final UserRegistrationService userRegistrationService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody UserRegistrationRequest request) {
        // Validate input based on user type
        validateRegistrationRequest(request);
        
        // Register user without username first
        userRegistrationService.registerUser(request);
        
        // Return response indicating username creation is needed
        return ResponseEntity.ok(ApiResponse.success(null, "User registered successfully. Please create a username."));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "User logged in successfully"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOTP(@Valid @RequestBody OTPVerificationRequest request) {
        // Verify OTP using existing service
        boolean verified = otpService.verifyOTP(request.getEmail(), request.getOtpCode(),
                OTP.OTPType.valueOf(request.getType()));

        if (!verified) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid or expired OTP"));
        }

        // Mark user email as verified
        boolean updated = userRegistrationService.verifyUserEmail(request.getEmail(), request.getOtpCode());
        if (!updated) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to verify email"));
        }

        return ResponseEntity.ok(ApiResponse.success(null, "OTP verified successfully"));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOTP(@RequestParam String email, @RequestParam String type) {
        boolean sent = otpService.generateAndSendOTP(email, OTP.OTPType.valueOf(type));
        
        if (sent) {
            return ResponseEntity.ok(ApiResponse.success(null, "OTP resent successfully"));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to send OTP"));
        }
    }
    
    // University matching endpoints have been removed as part of the student verification system cleanup
    
    // College matching endpoints have been removed as part of the student verification system cleanup
    
    /**
     * Validates registration request
     * 
     * @param request The registration request
     * @throws IllegalArgumentException if validation fails
     */
    private void validateRegistrationRequest(UserRegistrationRequest request) {
        // Basic validation is already handled by @Valid annotation
        // Additional custom validations can be added here if needed
    }
    
    // University and college endpoints have been removed
    
    // Institution-related endpoints have been removed as part of the student verification system cleanup
    
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOTP(@RequestParam String email, @RequestParam String type) {
        boolean sent = otpService.generateAndSendOTP(email, OTP.OTPType.valueOf(type));
        
        if (sent) {
            return ResponseEntity.ok(ApiResponse.success(null, "OTP sent successfully"));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to send OTP"));
        }
    }
    
    @PostMapping("/create-username")
    public ResponseEntity<ApiResponse<AuthResponse>> createUsername(@Valid @RequestBody UsernameCreationRequest request, @RequestParam String email) {
        try {
            userRegistrationService.createUsername(email, request.getUsername());
            return ResponseEntity.ok(ApiResponse.success(null, "Username created successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        boolean sent = otpService.generateAndSendOTP(request.getEmail(), OTP.OTPType.PASSWORD_RESET);
        
        if (sent) {
            return ResponseEntity.ok(ApiResponse.success(null, "Password reset OTP sent successfully"));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("Failed to send password reset OTP"));
        }
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Passwords do not match"));
        }
        
        // Verify OTP
        boolean otpValid = otpService.verifyOTP(request.getEmail(), request.getOtpCode(), OTP.OTPType.PASSWORD_RESET);
        
        if (!otpValid) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid or expired OTP"));
        }
        
        // Update password
        authService.updatePasswordByEmail(request.getEmail(), request.getNewPassword());
        return ResponseEntity.ok(ApiResponse.success(null, "Password reset successfully"));
    }
}
