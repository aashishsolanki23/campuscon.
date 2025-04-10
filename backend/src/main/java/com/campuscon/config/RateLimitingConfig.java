package com.campuscon.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Configuration for API rate limiting
 * Protects sensitive endpoints from abuse in production
 */
@Configuration
@Profile("production")
public class RateLimitingConfig {

    // Cache to hold rate limiters for different API keys or IPs
    private final Map<String, TokenBucket> bucketCache = new ConcurrentHashMap<>();
    
    // Rate limit configurations
    private static final int STANDARD_CAPACITY = 100;
    private static final int AUTH_CAPACITY = 10;
    private static final int SENSITIVE_CAPACITY = 5;
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);
    
    /**
     * Get a rate limiter for a specific API client identified by key
     * @param clientKey IP address or API key
     * @param type The type of limiter to use
     * @return The appropriate rate limiter
     */
    public TokenBucket resolveBucket(String clientKey, RateLimitType type) {
        String cacheKey = clientKey + ":" + type.name();
        return bucketCache.computeIfAbsent(cacheKey, k -> {
            switch (type) {
                case AUTH:
                    return new TokenBucket(AUTH_CAPACITY, REFILL_PERIOD);
                case SENSITIVE:
                    return new TokenBucket(SENSITIVE_CAPACITY, REFILL_PERIOD);
                case STANDARD:
                default:
                    return new TokenBucket(STANDARD_CAPACITY, REFILL_PERIOD);
            }
        });
    }
    
    /**
     * Simple token bucket implementation for rate limiting
     */
    public static class TokenBucket {
        private final int capacity;
        private final Duration refillPeriod;
        private final AtomicInteger tokens;
        private Instant lastRefillTime;
        
        public TokenBucket(int capacity, Duration refillPeriod) {
            this.capacity = capacity;
            this.refillPeriod = refillPeriod;
            this.tokens = new AtomicInteger(capacity);
            this.lastRefillTime = Instant.now();
        }
        
        /**
         * Try to consume a token from the bucket
         * @return Consumption result with remaining tokens
         */
        public synchronized ConsumptionResult tryConsume() {
            refillIfNeeded();
            
            int currentTokens = tokens.get();
            if (currentTokens > 0) {
                tokens.decrementAndGet();
                return new ConsumptionResult(true, currentTokens - 1, 0);
            } else {
                long secondsToWait = getSecondsToNextRefill();
                return new ConsumptionResult(false, 0, secondsToWait);
            }
        }
        
        private void refillIfNeeded() {
            Instant now = Instant.now();
            Duration elapsed = Duration.between(lastRefillTime, now);
            
            if (elapsed.compareTo(refillPeriod) >= 0) {
                tokens.set(capacity);
                lastRefillTime = now;
            }
        }
        
        private long getSecondsToNextRefill() {
            Instant now = Instant.now();
            Duration elapsed = Duration.between(lastRefillTime, now);
            Duration remaining = refillPeriod.minus(elapsed);
            return Math.max(0, remaining.getSeconds());
        }
    }
    
    /**
     * Result of a token consumption attempt
     */
    public static class ConsumptionResult {
        private final boolean consumed;
        private final int remainingTokens;
        private final long secondsToWait;
        
        public ConsumptionResult(boolean consumed, int remainingTokens, long secondsToWait) {
            this.consumed = consumed;
            this.remainingTokens = remainingTokens;
            this.secondsToWait = secondsToWait;
        }
        
        public boolean isConsumed() {
            return consumed;
        }
        
        public int getRemainingTokens() {
            return remainingTokens;
        }
        
        public long getSecondsToWait() {
            return secondsToWait;
        }
    }
    
    public enum RateLimitType {
        STANDARD,
        AUTH,
        SENSITIVE
    }
}
