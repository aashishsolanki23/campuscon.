package com.campuscon.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class CachingConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Default cache configuration with TTL of 1 hour
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer())
                );

        // Build cache manager with specific configurations for different cache types
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultConfig)
                // User profiles - longer TTL as they change less frequently
                .withCacheConfiguration("users", 
                        defaultConfig.entryTtl(Duration.ofHours(2)))
                // Deeds - moderate TTL as they are frequently accessed
                .withCacheConfiguration("deeds", 
                        defaultConfig.entryTtl(Duration.ofMinutes(30)))
                // Brick cache configuration has been removed
                // Society data - longer TTL as they change less frequently
                .withCacheConfiguration("societies", 
                        defaultConfig.entryTtl(Duration.ofHours(2)))
                // Comments - shorter TTL as they change frequently
                .withCacheConfiguration("comments", 
                        defaultConfig.entryTtl(Duration.ofMinutes(15)))
                .build();
    }
}

