package com.campuscon.controller;

import com.campuscon.dto.ApiResponse;
import com.campuscon.dto.auth.AuthResponse;
import com.campuscon.dto.auth.LoginRequest;
import com.campuscon.dto.auth.RegisterRequest;
import com.campuscon.model.College;
import com.campuscon.model.University;
import com.campuscon.service.AuthService;
import com.campuscon.service.CollegeService;
import com.campuscon.service.UniversityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UniversityService universityService;
    private final CollegeService collegeService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
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
        return ResponseEntity.ok(ApiResponse.success(response, "OTP verified successfully"));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<Void>> resendOTP(@RequestParam String email) {
        // Implementation will be added for resending OTP
        return ResponseEntity.ok(ApiResponse.success(null, "OTP resent successfully"));
    }

    @PostMapping("/verify-society")
    public ResponseEntity<ApiResponse<AuthResponse>> verifySociety(
            @RequestParam String presidentEmail,
            @RequestParam String societyId) {
        AuthResponse response = authService.verifySociety(presidentEmail, societyId);
        return ResponseEntity.ok(ApiResponse.success(response, "Society verified successfully"));
    }
    
    @GetMapping("/universities")
    public ResponseEntity<ApiResponse<List<University>>> getAllUniversities() {
        List<University> universities = universityService.getAllUniversities();
        return ResponseEntity.ok(ApiResponse.success(universities, "Universities retrieved successfully"));
    }
    
    @GetMapping("/colleges")
    public ResponseEntity<ApiResponse<List<College>>> getCollegesByUniversity(@RequestParam Long universityId) {
        List<College> colleges = collegeService.getCollegesByUniversityId(universityId);
        return ResponseEntity.ok(ApiResponse.success(colleges, "Colleges retrieved successfully"));
    }
    
    @GetMapping("/validate-email-domain")
    public ResponseEntity<ApiResponse<Boolean>> validateEmailDomain(
            @RequestParam String email,
            @RequestParam Long collegeId) {
        Boolean isValid = collegeService.validateEmailDomain(email, collegeId);
        return ResponseEntity.ok(ApiResponse.success(isValid, isValid ? "Email domain is valid" : "Email domain is invalid"));
    }
}
