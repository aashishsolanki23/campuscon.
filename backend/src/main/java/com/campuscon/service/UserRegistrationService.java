package com.campuscon.service;

import com.campuscon.dto.auth.UserRegistrationRequest;
import com.campuscon.model.User;
import com.campuscon.repository.UserRepository;
import com.campuscon.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationService {
    
    private final UserRepository userRepository;
    private final OTPService otpService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    
    /**
     * Register a new user with the complete flow
     * @param request the registration request
     * @return the created user
     */
    @Transactional
    public User registerUser(UserRegistrationRequest request) {
        try {
            // Validate request
            validateRegistrationRequest(request);
            
            // Check if user already exists
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("User with this email already exists");
            }
            
            // Create user entity
            User user = new User();
            // Username will be set later after registration
            user.setEmail(request.getEmail());
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setDisplayName(request.getDisplayName());
            user.setMobileNumber(request.getMobileNumber());
            user.setEmailVerified(false);
            
            // Set user types - all users are just USER type
            Set<String> userTypes = new HashSet<>();
            userTypes.add("USER");
            user.setUserTypes(userTypes);
            
            // Handle OAuth2 information if present
            if (request.getProvider() != null) {
                user.setProvider(request.getProvider());
                user.setProviderId(request.getProviderId());
                user.setOidcIdToken(request.getOidcIdToken());
                user.setOidcSub(request.getOidcSub());
            }
            
            // Handle college and university - optional for all users
            if (request.getCollegeName() != null) {
                user.setCollegeName(request.getCollegeName());
                if (request.getUniversityName() != null) {
                    user.setUniversityName(request.getUniversityName());
                }
            }
            
            // Save user
            user = userRepository.save(user);
            
            // Send OTP for email verification - all users get verified
            otpService.generateAndSendOTP(user.getEmail(), com.campuscon.model.OTP.OTPType.EMAIL_VERIFICATION);
            
            log.info("User registered successfully: {}", user.getEmail());
            return user;
            
        } catch (Exception e) {
            log.error("Error registering user: {}", request.getEmail(), e);
            throw e;
        }
    }
    
    /**
     * Verify user email with OTP
     * @param email the user email
     * @param otpCode the OTP code
     * @return true if verification successful
     */
    @Transactional
    public boolean verifyUserEmail(String email, String otpCode) {
        try {
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            boolean verified = otpService.verifyOTP(email, otpCode, com.campuscon.model.OTP.OTPType.EMAIL_VERIFICATION);
            
            if (verified) {
                user.setEmailVerified(true);
                userRepository.save(user);
                log.info("User email verified: {}", email);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            log.error("Error verifying user email: {}", email, e);
            throw e;
        }
    }
    
    /**
     * Complete user profile after email verification
     * @param userId the user ID
     * @return the updated user
     */
    @Transactional
    public User completeUserProfile(Long userId) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            if (!user.isEmailVerified()) {
                throw new IllegalStateException("User email not verified");
            }
            
            // Additional profile completion logic can be added here
            // For example, setting default preferences, creating user groups, etc.
            
            user = userRepository.save(user);
            log.info("User profile completed: {}", user.getUsername());
            return user;
            
        } catch (Exception e) {
            log.error("Error completing user profile: {}", userId, e);
            throw e;
        }
    }
    
    /**
     * Create username for user after registration
     * @param email the user email
     * @param username the username to set
     * @return the updated user
     */
    @Transactional
    public User createUsername(String email, String username) {
        try {
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            // Check if username is already taken
            if (userRepository.existsByUsername(username)) {
                throw new IllegalArgumentException("Username already taken");
            }
            
            user.setUsername(username);
            user = userRepository.save(user);
            
            log.info("Username created for user: {}", username);
            return user;
            
        } catch (Exception e) {
            log.error("Error creating username for user: {}", email, e);
            throw e;
        }
    }
    
    /**
     * Validate registration request
     * @param request the registration request
     * @throws IllegalArgumentException if validation fails
     */
    private void validateRegistrationRequest(UserRegistrationRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Registration request cannot be null");
        }
        
        // College name is optional for all users
        
        // Validate password confirmation
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Password and confirm password do not match");
        }
        
        // Additional validations can be added here
    }
}
