package com.campuscon.service;

import com.campuscon.model.User;
import com.campuscon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    
    /**
     * Get a user by their email address
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
    
    /**
     * Get a user by their ID
     */
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + id));
    }
    
    /**
     * Get a user by their username
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * Update a user's profile information
     */
    @Transactional
    public User updateUserProfile(Long userId, String username, String profilePictureUrl) {
        User user = getUserById(userId);
        
        // Check if new username is available
        if (username != null && !username.equals(user.getUsername())) {
            if (userRepository.existsByUsername(username)) {
                throw new RuntimeException("Username is already taken");
            }
            user.setUsername(username);
        }
        
        if (profilePictureUrl != null) {
            user.setProfilePictureUrl(profilePictureUrl);
        }
        
        return userRepository.save(user);
    }
    
    /**
     * Update a user's FCM token for push notifications
     */
    @Transactional
    public void updateFcmToken(Long userId, String fcmToken) {
        User user = getUserById(userId);
        user.setFcmToken(fcmToken);
        userRepository.save(user);
    }
    
    /**
     * Search for users by username or email
     */
    public List<User> searchUsers(String query) {
        return userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }
    
    /**
     * Check if user exists by email
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Check if user exists by username
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
}
