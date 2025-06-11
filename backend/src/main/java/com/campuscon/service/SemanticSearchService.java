package com.campuscon.service;

import com.campuscon.model.Deed;
import com.campuscon.repository.DeedRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Set;

import javax.annotation.PostConstruct;

/**
 * Service for semantic search using Redis as a vector database
 * This service provides functionality to:
 * 1. Generate embeddings for deed titles and descriptions
 * 2. Store these embeddings in Redis
 * 3. Search for deeds by semantic similarity
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SemanticSearchService {

    private final DeedRepository deedRepository;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmbeddingService embeddingService;
    
    private static final String DEED_EMBEDDING_KEY_PREFIX = "deed:embedding:";
    private static final String DEED_TITLE_EMBEDDING_KEY_PREFIX = "deed:title:embedding:";
    private static final String DEED_DESCRIPTION_EMBEDDING_KEY_PREFIX = "deed:description:embedding:";
    
    /**
     * Initialize Redis with vector similarity search capabilities
     */
    @PostConstruct
    public void init() {
        log.info("Initializing SemanticSearchService...");
        // This would be where you would initialize the Redis module for vector search if needed
    }
    
    /**
     * Generate and store embeddings for a deed
     * @param deed The deed to generate embeddings for
     */
    public void processAndStoreDeedEmbeddings(Deed deed) {
        // Generate embeddings for title and description
        float[] titleEmbedding = embeddingService.generateEmbedding(deed.getTitle());
        float[] descriptionEmbedding = embeddingService.generateEmbedding(deed.getDescription());
        
        // Store embeddings in Redis
        storeTitleEmbedding(deed.getId(), titleEmbedding);
        storeDescriptionEmbedding(deed.getId(), descriptionEmbedding);
        
        // Also store a combined embedding for faster search
        float[] combinedEmbedding = combineEmbeddings(titleEmbedding, descriptionEmbedding);
        storeCombinedEmbedding(deed.getId(), combinedEmbedding);
        
        log.info("Stored embeddings for deed: {}", deed.getId());
    }
    
    /**
     * Store a title embedding in Redis
     * @param deedId The deed ID
     * @param embedding The embedding vector
     */
    private void storeTitleEmbedding(Long deedId, float[] embedding) {
        String key = DEED_TITLE_EMBEDDING_KEY_PREFIX + deedId;
        storeEmbedding(key, embedding);
    }
    
    /**
     * Store a description embedding in Redis
     * @param deedId The deed ID
     * @param embedding The embedding vector
     */
    private void storeDescriptionEmbedding(Long deedId, float[] embedding) {
        String key = DEED_DESCRIPTION_EMBEDDING_KEY_PREFIX + deedId;
        storeEmbedding(key, embedding);
    }
    
    /**
     * Store a combined embedding in Redis
     * @param deedId The deed ID
     * @param embedding The embedding vector
     */
    private void storeCombinedEmbedding(Long deedId, float[] embedding) {
        String key = DEED_EMBEDDING_KEY_PREFIX + deedId;
        storeEmbedding(key, embedding);
    }
    
    /**
     * Store an embedding in Redis
     * @param key The Redis key
     * @param embedding The embedding vector
     */
    private void storeEmbedding(String key, float[] embedding) {
        String embeddingString = convertEmbeddingToString(embedding);
        redisTemplate.opsForValue().set(key, embeddingString);
    }
    
    /**
     * Convert an embedding vector to a string for Redis storage
     * @param embedding The embedding vector
     * @return String representation of the embedding
     */
    private String convertEmbeddingToString(float[] embedding) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < embedding.length; i++) {
            sb.append(embedding[i]);
            if (i < embedding.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
    
    /**
     * Convert a string back to an embedding vector
     * @param embeddingString String representation of the embedding
     * @return The embedding vector
     */
    private float[] convertStringToEmbedding(String embeddingString) {
        String[] parts = embeddingString.split(",");
        float[] embedding = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            embedding[i] = Float.parseFloat(parts[i]);
        }
        return embedding;
    }
    
    /**
     * Combine title and description embeddings
     * @param titleEmbedding Title embedding
     * @param descriptionEmbedding Description embedding
     * @return Combined embedding
     */
    private float[] combineEmbeddings(float[] titleEmbedding, float[] descriptionEmbedding) {
        // Simple weighted average: 60% title, 40% description
        float titleWeight = 0.6f;
        float descriptionWeight = 0.4f;
        
        float[] combined = new float[titleEmbedding.length];
        for (int i = 0; i < combined.length; i++) {
            combined[i] = titleEmbedding[i] * titleWeight + descriptionEmbedding[i] * descriptionWeight;
        }
        
        return combined;
    }
    
    /**
     * Calculate cosine similarity between two embeddings
     * @param embedding1 First embedding
     * @param embedding2 Second embedding
     * @return Cosine similarity score
     */
    private double cosineSimilarity(float[] embedding1, float[] embedding2) {
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < embedding1.length; i++) {
            dotProduct += embedding1[i] * embedding2[i];
            norm1 += embedding1[i] * embedding1[i];
            norm2 += embedding2[i] * embedding2[i];
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    /**
     * Search for deeds by semantic similarity
     * @param query The search query
     * @param limit Maximum number of results to return
     * @param minScore Minimum similarity score (0-1)
     * @return List of deed IDs ordered by relevance
     */
    public List<Long> searchDeeds(String query, int limit, double minScore) {
        // Generate embedding for the query
        float[] queryEmbedding = embeddingService.generateEmbedding(query);
        
        // Search all deed embeddings
        Map<Long, Double> scores = new HashMap<>();
        
        // Get all keys matching the deed embedding pattern
        Set<String> keys = redisTemplate.keys(DEED_EMBEDDING_KEY_PREFIX + "*");
        
        if (keys == null || keys.isEmpty()) {
            log.warn("No deed embeddings found in Redis");
            return new ArrayList<>();
        }
        
        for (String key : keys) {
            // Extract deed ID from key
            Long deedId = Long.parseLong(key.substring(DEED_EMBEDDING_KEY_PREFIX.length()));
            
            // Get the embedding
            String embeddingStr = redisTemplate.opsForValue().get(key);
            if (embeddingStr == null) {
                continue;
            }
            
            float[] deedEmbedding = convertStringToEmbedding(embeddingStr);
            
            // Calculate similarity
            double similarity = cosineSimilarity(queryEmbedding, deedEmbedding);
            
            // Add to scores if above threshold
            if (similarity >= minScore) {
                scores.put(deedId, similarity);
            }
        }
        
        // Sort by similarity score (descending) and take top results
        return scores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    
    /**
     * Search for deeds by semantic similarity with category filtering
     * @param query The search query
     * @param categories List of categories to filter by
     * @param limit Maximum number of results to return
     * @param minScore Minimum similarity score (0-1)
     * @return List of deed IDs ordered by relevance
     */
    public List<Long> searchDeedsByCategory(String query, List<String> categories, int limit, double minScore) {
        // First get semantically similar deeds
        List<Long> similarDeedIds = searchDeeds(query, Integer.MAX_VALUE, minScore);
        
        if (categories == null || categories.isEmpty()) {
            return similarDeedIds.stream().limit(limit).collect(Collectors.toList());
        }
        
        // Then filter by category
        List<Deed> filteredDeeds = deedRepository.findAllById(similarDeedIds).stream()
                .filter(deed -> categories.contains(deed.getCategoryDisplayName()))
                .limit(limit)
                .collect(Collectors.toList());
        
        // Return just the IDs
        return filteredDeeds.stream()
                .map(Deed::getId)
                .collect(Collectors.toList());
    }
    
    /**
     * Search for deeds by semantic similarity and location
     * @param query The search query
     * @param state State to filter by (can be null)
     * @param limit Maximum number of results to return
     * @param minScore Minimum similarity score (0-1)
     * @return List of deed IDs ordered by relevance
     */
    public List<Long> searchDeedsByLocation(String query, String state, int limit, double minScore) {
        // First get semantically similar deeds
        List<Long> similarDeedIds = searchDeeds(query, Integer.MAX_VALUE, minScore);
        
        if (state == null || state.isEmpty()) {
            return similarDeedIds.stream().limit(limit).collect(Collectors.toList());
        }
        
        // Then filter by location (checking if the address contains the state name)
        List<Deed> filteredDeeds = deedRepository.findAllById(similarDeedIds).stream()
                .filter(deed -> deed.getAddress() != null && 
                       deed.getAddress().toLowerCase().contains(state.toLowerCase()))
                .limit(limit)
                .collect(Collectors.toList());
        
        // Return just the IDs
        return filteredDeeds.stream()
                .map(Deed::getId)
                .collect(Collectors.toList());
    }
}
