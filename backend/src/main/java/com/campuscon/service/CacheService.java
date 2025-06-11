package com.campuscon.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * Service for interacting with Redis cache
 * Provides methods for storing, retrieving, and removing cached data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * Store a value in the cache with a specified key
     * 
     * @param key The cache key
     * @param value The value to store
     */
    public void put(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
        log.debug("Stored value in cache with key: {}", key);
    }
    
    /**
     * Store a value in the cache with a specified key and expiration time
     * 
     * @param key The cache key
     * @param value The value to store
     * @param expiry The expiration time in seconds
     */
    public void put(String key, Object value, long expiry) {
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(expiry));
        log.debug("Stored value in cache with key: {} and expiry: {} seconds", key, expiry);
    }
    
    /**
     * Retrieve a value from the cache
     * 
     * @param key The cache key
     * @return The cached value, or null if not found
     */
    public Object get(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            log.debug("Retrieved value from cache with key: {}", key);
        } else {
            log.debug("Cache miss for key: {}", key);
        }
        return value;
    }
    
    /**
     * Remove a value from the cache
     * 
     * @param key The cache key to remove
     */
    public void delete(String key) {
        redisTemplate.delete(key);
        log.debug("Removed value from cache with key: {}", key);
    }
    
    /**
     * Check if a key exists in the cache
     * 
     * @param key The cache key to check
     * @return true if the key exists, false otherwise
     */
    public boolean hasKey(String key) {
        Boolean exists = redisTemplate.hasKey(key);
        return exists != null && exists;
    }
}
