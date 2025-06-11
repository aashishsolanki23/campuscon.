package com.campuscon.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for generating text embeddings
 * This service can use various embedding providers (local or remote)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private final RestTemplate restTemplate;
    
    @Value("${embedding.api.url:#{null}}")
    private String embeddingApiUrl;
    
    @Value("${embedding.api.key:#{null}}")
    private String embeddingApiKey;
    
    @Value("${embedding.model:text-embedding-ada-002}")
    private String embeddingModel;
    
    @Value("${embedding.dimension:1536}")
    private int embeddingDimension;
    
    /**
     * Generate an embedding for the given text
     * @param text The text to generate an embedding for
     * @return The embedding as a float array
     */
    public float[] generateEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            // Return a zero vector for empty text
            return new float[embeddingDimension];
        }
        
        if (embeddingApiUrl != null && !embeddingApiUrl.isEmpty()) {
            try {
                return callExternalEmbeddingApi(text);
            } catch (Exception e) {
                log.error("Error calling external embedding API", e);
                return generateLocalEmbedding(text);
            }
        } else {
            return generateLocalEmbedding(text);
        }
    }
    
    /**
     * Call an external API to generate embeddings
     * @param text The text to generate an embedding for
     * @return The embedding as a float array
     */
    private float[] callExternalEmbeddingApi(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (embeddingApiKey != null && !embeddingApiKey.isEmpty()) {
            headers.set("Authorization", "Bearer " + embeddingApiKey);
        }
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("input", text);
        requestBody.put("model", embeddingModel);
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> response = restTemplate.postForObject(embeddingApiUrl, request, Map.class);
        
        if (response != null && response.containsKey("data")) {
            // Use explicit casting with proper type checking to avoid unchecked conversion warnings
            Object dataObj = response.get("data");
            if (dataObj instanceof List<?>) {
                List<?> dataList = (List<?>) dataObj;
                if (!dataList.isEmpty() && dataList.get(0) instanceof Map) {
                    Map<?, ?> dataMap = (Map<?, ?>) dataList.get(0);
                    Object embeddingObj = dataMap.get("embedding");
                    
                    if (embeddingObj instanceof List<?>) {
                        List<?> embeddingList = (List<?>) embeddingObj;
                        
                        // Convert to float array, verifying each element is a Number
                        float[] result = new float[embeddingList.size()];
                        for (int i = 0; i < embeddingList.size(); i++) {
                            Object item = embeddingList.get(i);
                            if (item instanceof Number) {
                                result[i] = ((Number) item).floatValue();
                            } else {
                                throw new RuntimeException("Embedding contains non-numeric values");
                            }
                        }
                        
                        return result;
                    }
                }
            }
        }
        
        throw new RuntimeException("Failed to generate embedding from external API");
    }
    
    /**
     * Generate a simple local embedding for the text
     * This is a fallback method that doesn't provide true semantic embeddings
     * but can be used for development or when no external API is available
     * 
     * @param text The text to generate an embedding for
     * @return The embedding as a float array
     */
    private float[] generateLocalEmbedding(String text) {
        // This is a very simple hash-based embedding - not semantically meaningful
        // In a production environment, you'd use a proper embedding model
        
        float[] embedding = new float[embeddingDimension];
        
        // Normalize the text
        String normalizedText = text.toLowerCase().trim();
        
        // Use the hash code of the text to seed a random number generator
        java.util.Random random = new java.util.Random(normalizedText.hashCode());
        
        // Generate pseudo-random but deterministic values
        for (int i = 0; i < embeddingDimension; i++) {
            embedding[i] = random.nextFloat() * 2 - 1; // Range [-1, 1]
        }
        
        // Normalize the vector to unit length
        double sum = 0;
        for (float v : embedding) {
            sum += v * v;
        }
        
        float norm = (float) Math.sqrt(sum);
        for (int i = 0; i < embeddingDimension; i++) {
            embedding[i] /= norm;
        }
        
        return embedding;
    }
}
