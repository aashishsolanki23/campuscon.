package com.campuscon.service;

import com.campuscon.enums.UserRole;
import com.campuscon.model.User;
import com.campuscon.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.InternalAuthenticationServiceException;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        try {
            // Handle OIDC authentication specifically for Google
            if ("google".equals(userRequest.getClientRegistration().getRegistrationId()) && 
                oAuth2User instanceof OidcUser) {
                return processOidcUser(userRequest, (OidcUser) oAuth2User);
            }
            
            // Handle standard OAuth2 authentication for other providers
            return processOAuth2User(userRequest, oAuth2User);
        } catch (Exception ex) {
            log.error("Error in OAuth2 authentication", ex);
            throw new InternalAuthenticationServiceException(ex.getMessage(), ex);
        }
    }

    /**
     * Handle OpenID Connect authentication (Google)
     */
    private OAuth2User processOidcUser(OAuth2UserRequest userRequest, OidcUser oidcUser) {
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = oidcUser.getSubject();
        String email = oidcUser.getEmail();
        String name = oidcUser.getFullName();
        OidcIdToken idToken = oidcUser.getIdToken();
        String picture = oidcUser.getPicture();
        
        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OIDC provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Update existing user with OIDC details
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setOidcIdToken(idToken.getTokenValue());
            user.setOidcSub(providerId);
            
            // Update profile picture if available
            if (picture != null && !picture.isEmpty()) {
                user.setProfilePictureUrl(picture);
            }
            
            updateUser(user, name);
        } else {
            user = registerNewOidcUser(provider, providerId, email, name, idToken, picture);
        }

        user.setAttributes(oidcUser.getAttributes());
        return user;
    }
    
    /**
     * Process standard OAuth2 authentication
     */
    private OAuth2User processOAuth2User(OAuth2UserRequest userRequest, OAuth2User oAuth2User) {
        String provider = userRequest.getClientRegistration().getRegistrationId();
        String providerId = getAttributeValue(oAuth2User, "sub", "id");
        String email = getAttributeValue(oAuth2User, "email");
        String name = getAttributeValue(oAuth2User, "name");
        String picture = getAttributeValue(oAuth2User, "picture");
        
        if (email == null || email.isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            // Update existing user
            user.setProvider(provider);
            user.setProviderId(providerId);
            
            // Update profile picture if available
            if (picture != null && !picture.isEmpty()) {
                user.setProfilePictureUrl(picture);
            }
            
            updateUser(user, name);
        } else {
            user = registerNewUser(provider, providerId, email, name, picture);
        }

        user.setAttributes(oAuth2User.getAttributes());
        return user;
    }

    private String getAttributeValue(OAuth2User oAuth2User, String... keys) {
        Map<String, Object> attributes = oAuth2User.getAttributes();
        for (String key : keys) {
            if (attributes.containsKey(key)) {
                Object value = attributes.get(key);
                if (value != null) {
                    return value.toString();
                }
            }
        }
        return null;
    }

    /**
     * Register a new user from OpenID Connect authentication
     */
    private User registerNewOidcUser(String provider, String providerId, String email, String name, 
                                   OidcIdToken idToken, String picture) {
        User user = new User();
        user.setProvider(provider);
        user.setProviderId(providerId);
        user.setEmail(email);
        user.setOidcIdToken(idToken.getTokenValue());
        user.setOidcSub(providerId);
        
        // Generate a username based on email
        user.setUsername(generateUsernameFromEmail(email));
        user.setDisplayName(name);
        user.setPassword(""); // OAuth2 users don't need a password
        Set<String> userTypes = new HashSet<>();
        userTypes.add(UserRole.USER.name());
        user.setUserTypes(userTypes); // Default role
        user.setEmailVerified(true); // OAuth2 providers verify emails
        
        // Set college name from email domain without verification
        String emailDomain = email.substring(email.indexOf('@') + 1);
        user.setCollegeName(emailDomain);
        
        if (picture != null && !picture.isEmpty()) {
            user.setProfilePictureUrl(picture);
        }
        
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    /**
     * Register a new user from standard OAuth2 authentication
     */
    private User registerNewUser(String provider, String providerId, String email, String name, 
                                String picture) {
        User user = new User();
        user.setProvider(provider);
        user.setProviderId(providerId);
        user.setEmail(email);
        
        // Generate a username based on email
        user.setUsername(generateUsernameFromEmail(email));
        user.setDisplayName(name);
        user.setPassword(""); // OAuth2 users don't need a password
        Set<String> userTypes = new HashSet<>();
        userTypes.add(UserRole.USER.name());
        user.setUserTypes(userTypes); // Default role
        user.setEmailVerified(true); // OAuth2 providers verify emails
        
        // Set college name from email domain without verification
        String emailDomain = email.substring(email.indexOf('@') + 1);
        user.setCollegeName(emailDomain);
        
        if (picture != null && !picture.isEmpty()) {
            user.setProfilePictureUrl(picture);
        }
        
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    /**
     * Generate a valid username from email address
     */
    private String generateUsernameFromEmail(String email) {
        String baseUsername = email.substring(0, email.indexOf('@'));
        
        // Check if username already exists
        if (!userRepository.existsByUsername(baseUsername)) {
            return baseUsername;
        }
        
        // Add a number suffix until we find a unique username
        int suffix = 1;
        String newUsername;
        do {
            newUsername = baseUsername + suffix;
            suffix++;
        } while (userRepository.existsByUsername(newUsername));
        
        return newUsername;
    }

    private void updateUser(User user, String name) {
        if (name != null && !name.isEmpty() && (user.getDisplayName() == null || user.getDisplayName().isEmpty())) {
            user.setDisplayName(name);
        }
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
