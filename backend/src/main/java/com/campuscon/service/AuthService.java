package com.campuscon.service;

import com.campuscon.dto.auth.AuthResponse;
import com.campuscon.dto.auth.LoginRequest;
import com.campuscon.dto.auth.RegisterRequest;
import com.campuscon.model.User;
import com.campuscon.repository.UserRepository;
import com.campuscon.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;
    private final UserCustomUrlService userCustomUrlService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // For Google OAuth registration, generate a random username if not provided
        if (request.isGoogleAuth() && (request.getUsername() == null || request.getUsername().isEmpty())) {
            request.setUsername(generateRandomUsername(request.getDisplayName()));
        }
        
        // Validate if username or email already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Password confirmation validation - only for traditional authentication
        if (!request.isGoogleAuth()) {
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                throw new RuntimeException("Password is required for traditional registration");
            }
            
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                throw new RuntimeException("Passwords do not match");
            }
        }

        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        
        // Set password for traditional auth, set a secure random password for OAuth
        if (request.isGoogleAuth()) {
            // For OAuth users, generate a secure random password they won't use
            // This is needed because our UserDetails implementation requires a password
            String secureRandomPassword = passwordEncoder.encode(java.util.UUID.randomUUID().toString());
            user.setPassword(secureRandomPassword);
            
            // Set provider information
            user.setProvider("google");
            user.setEmailVerified(true); // Google OAuth emails are pre-verified
        } else {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setProvider("local");
        }
        
        // Set display name (required field)
        user.setDisplayName(request.getDisplayName());
        
        // Set default user role
        Set<String> userTypes = new HashSet<>();
        userTypes.add("USER");
        user.setUserTypes(userTypes);
        
        // Store mobile number if provided
        if (request.getMobileNumber() != null && !request.getMobileNumber().isEmpty()) {
            user.setMobileNumber(request.getMobileNumber());
        }
        
        // Set profile information if provided
        if (request.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(request.getProfilePictureUrl());
        }
        
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }

        // Process user registration with common functionality for all users
        processUserRegistration(user, request);
        
        // Save the user to the database
        user = userRepository.save(user);
        
        // Save custom URLs if provided
        if (request.getCustomUrls() != null && !request.getCustomUrls().isEmpty()) {
            userCustomUrlService.saveUserCustomUrls(user, request.getCustomUrls());
        }

        // Generate response with appropriate message
        String message;
        if (request.isGoogleAuth()) {
            message = "Registration with Google successful. You can now log in.";
        } else {
            message = "Registration successful. You can now log in.";
        }
                
        return AuthResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .userTypes(user.getUserTypes())
                .isVerified(user.isEmailVerified())
                .message(message)
                .build();
    }
    
    /**
     * Generate a random username based on the user's display name
     * 
     * @param displayName The user's display name
     * @return A random username
     */
    private String generateRandomUsername(String displayName) {
        // Create base from display name - lowercase, remove spaces and special chars
        String base = displayName.toLowerCase()
                .replaceAll("\\s+", "")
                .replaceAll("[^a-z0-9]", "");
        
        // Truncate if too long
        if (base.length() > 10) {
            base = base.substring(0, 10);
        }
        
        // Add random numbers until we find a username that doesn't exist
        String username;
        Random random = new Random();
        int attempts = 0;
        
        do {
            int randomNum = random.nextInt(10000);
            username = base + randomNum;
            attempts++;
            
            // Safety check to avoid infinite loop
            if (attempts > 50) {
                username = "user" + System.currentTimeMillis();
                break;
            }
        } while (userRepository.existsByUsername(username));
        
        return username;
    }
    
    /**
     * Process user registration with common functionality for all users.
     * This method handles university/college information based on user type.
     */
    private void processUserRegistration(User user, RegisterRequest request) {
        // For Google authentication, we already set the user as verified
        if (request.isGoogleAuth()) {
            user.setEmailVerified(true);
        } else {
            // For regular users, no email verification needed
            user.setEmailVerified(true);
            
            // Set college name if provided
            if (request.getCollegeName() != null && !request.getCollegeName().isEmpty()) {
                user.setCollegeName(request.getCollegeName());
            } else {
                // For email-based signup, try to use the email domain as college
                String emailDomain = request.getEmail().substring(request.getEmail().indexOf('@') + 1);
                if (emailDomain != null && !emailDomain.isEmpty()) {
                    user.setCollegeName(emailDomain);
                }
            }
        }
    }
    
    // Student email validation and fuzzy matching functions removed

    public AuthResponse login(LoginRequest request) {
        // Authenticate credentials
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );
        
        // Get user details
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Generate JWT token
        String jwt = jwtUtil.generateToken(user);
        
        return AuthResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .userTypes(user.getUserTypes()) // Include userTypes in response
                .isVerified(user.isEmailVerified()) // Include verification status
                .token(jwt)
                .message("Login successful")
                .build();
    }

    /**
     * Verify an OTP sent to user's email and mark the email as verified if correct.
     * 
     * @param email The email to verify
     * @param otp The OTP code entered by user
     * @return AuthResponse with authentication token if successful
     */
    @Transactional
    public AuthResponse verifyOTP(String email, String otp) {
        // Get the stored OTP from Redis
        String storedOTP = (String) redisTemplate.opsForValue().get("OTP:" + email);
        
        if (storedOTP == null) {
            // OTP expired or doesn't exist
            throw new RuntimeException("OTP has expired or was not generated");
        }
        
        if (!storedOTP.equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }
        
        // OTP is correct, mark email as verified and clean up Redis
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
                
        // Mark email as verified
        user.setEmailVerified(true);
            
        // Delete the OTP from Redis
        redisTemplate.delete("OTP:" + email);
            
        // Save the updated user
        userRepository.save(user);
        
        // Generate JWT token
        String jwt = jwtUtil.generateToken(user);
            
        return AuthResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .userTypes(user.getUserTypes())
                .token(jwt)
                .isVerified(true)
                .message("Email verification successful")
                .build();
    }
    
    /**
     * Resends the OTP for email verification
     * @param email The email to resend OTP to
     */
    public void resendOTP(String email) {
        // Check if user exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if already verified
        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified");
        }
        
        // Generate and send new OTP
        String otp = generateOTP();
        redisTemplate.opsForValue().set(
            "OTP:" + user.getEmail(),
            otp,
            10, // OTP valid for 10 minutes
            TimeUnit.MINUTES
        );

        // Send OTP via email
        emailService.sendOTPEmail(user.getEmail(), otp);
    }

    // Society verification method has been removed as part of the user system simplification

    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000); // 6-digit OTP
        return String.valueOf(otp);
    }

    // The extractRollNumberInfo method has been removed as academic fields are no longer used

    /**
     * Update a user's password by email (used for password reset with OTP).
     */
    @Transactional
    public void updatePasswordByEmail(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
