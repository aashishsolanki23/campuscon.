package com.campuscon.controller;

import com.campuscon.dto.ApiResponse;
import com.campuscon.dto.auth.AuthResponse;
import com.campuscon.dto.auth.LoginRequest;
import com.campuscon.dto.auth.RegisterRequest;
import com.campuscon.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        // Validate input based on user type
        validateRegistrationRequest(request);
        
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(ApiResponse.success(response, "User registered successfully"));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "User logged in successfully"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOTP(
            @RequestParam String email,
            @RequestParam String otp) {
        AuthResponse response = authService.verifyOTP(email, otp);
        return ResponseEntity.ok(ApiResponse.success(response, "Email verified successfully"));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOTP(@RequestParam String email) {
        authService.resendOTP(email);
        return ResponseEntity.ok(ApiResponse.success(null, "OTP resent successfully"));
    }
    
    // University matching endpoints have been removed as part of the student verification system cleanup
    
    // College matching endpoints have been removed as part of the student verification system cleanup
    
    /**
     * Validates registration request
     * 
     * @param request The registration request
     * @throws IllegalArgumentException if validation fails
     */
    private void validateRegistrationRequest(RegisterRequest request) {
        // Basic validation is already handled by @Valid annotation
        // Additional custom validations can be added here if needed
    }
    
    // University and college endpoints have been removed
    
    // Institution-related endpoints have been removed as part of the student verification system cleanup
}
