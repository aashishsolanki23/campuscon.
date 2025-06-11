package com.campuscon.service;

import com.campuscon.model.User;
import com.campuscon.model.UserCustomUrl;
import com.campuscon.repository.UserCustomUrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Service for managing user custom URLs
 */
@Service
@RequiredArgsConstructor
public class UserCustomUrlService {
    
    private final UserCustomUrlRepository userCustomUrlRepository;
    
    /**
     * Save custom URLs for a user from a map of URL name to URL
     * 
     * @param user The user to save URLs for
     * @param customUrls Map of URL name to URL value
     */
    @Transactional
    public void saveUserCustomUrls(User user, Map<String, String> customUrls) {
        if (customUrls == null || customUrls.isEmpty()) {
            return;
        }
        
        // Remove existing custom URLs for this user
        userCustomUrlRepository.deleteByUserId(user.getId());
        
        // Add new custom URLs
        customUrls.forEach((name, url) -> {
            if (url != null && !url.trim().isEmpty()) {
                UserCustomUrl customUrl = new UserCustomUrl();
                customUrl.setUser(user);
                customUrl.setUrlName(name);
                customUrl.setUrl(url);
                userCustomUrlRepository.save(customUrl);
            }
        });
    }
    
    /**
     * Get all custom URLs for a user
     * 
     * @param userId The ID of the user
     * @return List of custom URLs
     */
    public List<UserCustomUrl> getCustomUrlsByUserId(Long userId) {
        return userCustomUrlRepository.findByUserId(userId);
    }
}
