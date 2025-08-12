package com.campuscon.service;

import com.campuscon.dto.auth.UserRegistrationRequest;
import com.campuscon.model.User;
import com.campuscon.repository.UserRepository;
import com.campuscon.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;



    @Mock
    private OTPService otpService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserRegistrationService userRegistrationService;

    private UserRegistrationRequest userRequest;

    @BeforeEach
    void setUp() {
        userRequest = new UserRegistrationRequest();
        userRequest.setEmail("user@example.com");
        userRequest.setPassword("password123");
        userRequest.setConfirmPassword("password123");
        userRequest.setDisplayName("John User");
        userRequest.setMobileNumber("1234567890");
        userRequest.setCollegeName("Computer Science");
        userRequest.setUniversityName("Tech University");
    }

    @Test
    void testRegisterUser() {
        // Arrange
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(otpService.generateAndSendOTP(anyString(), any())).thenReturn(true);

        // Act
        User result = userRegistrationService.registerUser(userRequest);

        // Assert
        assertNotNull(result);
        assertEquals("user@example.com", result.getEmail());
        assertEquals("Computer Science", result.getCollegeName());
        assertEquals("Tech University", result.getUniversityName());
        assertTrue(result.getUserTypes().contains("USER"));
        assertFalse(result.isEmailVerified());

        verify(otpService).generateAndSendOTP("user@example.com", com.campuscon.model.OTP.OTPType.EMAIL_VERIFICATION);
    }

    @Test
    void testRegisterUserWithoutCollege() {
        // Arrange
        UserRegistrationRequest requestWithoutCollege = new UserRegistrationRequest();
        requestWithoutCollege.setEmail("user2@example.com");
        requestWithoutCollege.setPassword("password123");
        requestWithoutCollege.setConfirmPassword("password123");
        requestWithoutCollege.setDisplayName("Jane User");
        requestWithoutCollege.setMobileNumber("0987654321");
        
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(otpService.generateAndSendOTP(anyString(), any())).thenReturn(true);

        // Act
        User result = userRegistrationService.registerUser(requestWithoutCollege);

        // Assert
        assertNotNull(result);
        assertEquals("user2@example.com", result.getEmail());
        assertNull(result.getCollegeName());
        assertTrue(result.getUserTypes().contains("USER"));
        assertFalse(result.isEmailVerified());

        verify(otpService).generateAndSendOTP("user2@example.com", com.campuscon.model.OTP.OTPType.EMAIL_VERIFICATION);
    }

    @Test
    void testRegisterUserWithExistingEmail() {
        // Arrange
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userRegistrationService.registerUser(userRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterUserWithPasswordMismatch() {
        // Arrange
        userRequest.setConfirmPassword("differentpassword");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userRegistrationService.registerUser(userRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testVerifyUserEmail() {
        // Arrange
        User user = new User();
        user.setEmail("user@example.com");
        user.setEmailVerified(false);
        
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(otpService.verifyOTP("user@example.com", "123456", com.campuscon.model.OTP.OTPType.EMAIL_VERIFICATION))
            .thenReturn(true);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        boolean result = userRegistrationService.verifyUserEmail("user@example.com", "123456");

        // Assert
        assertTrue(result);
        verify(userRepository).save(user);
    }

    @Test
    void testVerifyUserEmailWithInvalidOTP() {
        // Arrange
        User user = new User();
        user.setEmail("user@example.com");
        user.setEmailVerified(false);
        
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(otpService.verifyOTP("user@example.com", "123456", com.campuscon.model.OTP.OTPType.EMAIL_VERIFICATION))
            .thenReturn(false);

        // Act
        boolean result = userRegistrationService.verifyUserEmail("user@example.com", "123456");

        // Assert
        assertFalse(result);
        verify(userRepository, never()).save(any(User.class));
    }
    
    @Test
    void testCreateUsername() {
        // Arrange
        User user = new User();
        user.setEmail("user@example.com");
        user.setUsername(null);
        
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(userRepository.existsByUsername("student123")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User result = userRegistrationService.createUsername("user@example.com", "student123");

        // Assert
        assertNotNull(result);
        assertEquals("student123", result.getUsername());
        verify(userRepository).save(user);
    }
    
    @Test
    void testCreateUsernameAlreadyTaken() {
        // Arrange
        when(userRepository.existsByUsername("student123")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            userRegistrationService.createUsername("user@example.com", "student123");
        });
    }
}
