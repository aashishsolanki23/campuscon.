package com.campuscon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Map;

/**
 * Service for content moderation using AKOOL AI
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ModerationService {

    private final RestTemplate restTemplate;
    
    @Value("${akool.api.key}")
    private String apiKey;
    
    @Value("${akool.api.url}")
    private String apiUrl;
    
    @Value("${moderation.text.enabled:true}")
    private boolean textModerationEnabled;
    
    @Value("${moderation.image.enabled:true}")
    private boolean imageModerationEnabled;

    /**
     * Check if text content is safe
     * 
     * @param content The text content to check
     * @return true if content is safe, false otherwise
     */
    public boolean checkText(String content) {
        if (!textModerationEnabled) {
            return true; // Moderation disabled, allow all content
        }
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-KEY", apiKey);
            
            Map<String, Object> requestBody = Map.of(
                "text", content,
                "categories", new String[] {"hate", "harassment", "self-harm", "sexual", "violence"}
            );
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                apiUrl + "/text-moderation", 
                request, 
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );
            
            Map<String, Object> responseBody = response.getBody();
            
            if (response.getStatusCode().is2xxSuccessful() && responseBody != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> results = responseBody != null ? 
                    (Map<String, Object>) responseBody.get("results") : 
                    Map.of();
                
                // Determine if content is safe based on flagged categories and confidence scores
                // Typically, we would set thresholds for each category
                boolean isFlagged = (boolean) results.getOrDefault("flagged", false);
                
                log.info("Text moderation result: {}", isFlagged ? "Flagged" : "Safe");
                return !isFlagged;
            }
            
            log.warn("Failed to moderate text content: {}", response.getStatusCode());
            return true; // Allow on failure to avoid blocking legitimate content
            
        } catch (Exception e) {
            log.error("Error during text moderation", e);
            return true; // Allow on failure to avoid blocking legitimate content
        }
    }

    /**
     * Check if image content is safe
     * 
     * @param image The image file to check
     * @return true if image is safe, false otherwise
     */
    public boolean checkImage(MultipartFile image) {
        if (!imageModerationEnabled) {
            return true; // Moderation disabled, allow all images
        }
        
        try {
            // Convert image to Base64
            String base64Image = Base64.getEncoder().encodeToString(image.getBytes());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-API-KEY", apiKey);
            
            Map<String, Object> requestBody = Map.of(
                "image", base64Image,
                "categories", new String[] {"explicit", "suggestive", "violence", "graphic"}
            );
            
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
            
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = restTemplate.postForEntity(
                apiUrl + "/image-moderation", 
                request, 
                (Class<Map<String, Object>>) (Class<?>) Map.class
            );
            
            Map<String, Object> responseBody = response.getBody();
            
            if (response.getStatusCode().is2xxSuccessful() && responseBody != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> results = responseBody != null ? 
                    (Map<String, Object>) responseBody.get("results") : 
                    Map.of();
                
                // Determine if content is safe based on flagged categories and confidence scores
                boolean isFlagged = (boolean) results.getOrDefault("flagged", false);
                
                log.info("Image moderation result: {}", isFlagged ? "Flagged" : "Safe");
                return !isFlagged;
            }
            
            log.warn("Failed to moderate image content: {}", response.getStatusCode());
            return true; // Allow on failure to avoid blocking legitimate content
            
        } catch (Exception e) {
            log.error("Error during image moderation", e);
            return true; // Allow on failure to avoid blocking legitimate content
        }
    }
}
