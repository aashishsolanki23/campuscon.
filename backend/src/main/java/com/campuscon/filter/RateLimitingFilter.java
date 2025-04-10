package com.campuscon.filter;

import com.campuscon.config.RateLimitingConfig;
import com.campuscon.config.RateLimitingConfig.ConsumptionResult;
import com.campuscon.config.RateLimitingConfig.TokenBucket;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Filter that implements rate limiting for API endpoints
 * Only active in production environment
 */
@Component
@Order(1)
@Profile("production")
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingConfig rateLimitingConfig;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Get client IP address or identifier
        String clientKey = getClientIP(request);
        String requestURI = request.getRequestURI();
        
        // Determine which rate limiter to use based on the endpoint
        RateLimitingConfig.RateLimitType type = RateLimitingConfig.RateLimitType.STANDARD;
        
        // Apply stricter limits to authentication and sensitive endpoints
        if (requestURI.contains("/api/auth")) {
            type = RateLimitingConfig.RateLimitType.AUTH;
        } else if (requestURI.contains("/api/security") || 
                   requestURI.contains("/api/settings") || 
                   requestURI.contains("/api/user/delete")) {
            type = RateLimitingConfig.RateLimitType.SENSITIVE;
        }
        
        // Get the appropriate bucket for this client and endpoint type
        TokenBucket bucket = rateLimitingConfig.resolveBucket(clientKey, type);
        
        // Try to consume a token from the bucket
        ConsumptionResult result = bucket.tryConsume();
        
        // If successful, proceed with the request
        if (result.isConsumed()) {
            // Add rate limit headers
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(result.getRemainingTokens()));
            response.addHeader("X-Rate-Limit-Reset", String.valueOf(result.getSecondsToWait()));
            
            filterChain.doFilter(request, response);
        } else {
            // If rate limit exceeded, return 429 Too Many Requests
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            
            // Add rate limit headers
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(0));
            response.addHeader("X-Rate-Limit-Reset", String.valueOf(result.getSecondsToWait()));
            
            // Build and send the error response
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", HttpStatus.TOO_MANY_REQUESTS.value());
            errorResponse.put("error", "Too Many Requests");
            errorResponse.put("message", "Rate limit exceeded. Please try again later.");
            errorResponse.put("retryAfterSeconds", result.getSecondsToWait());
            
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            
            // Log the rate limiting event
            log.warn("Rate limit exceeded for client: {}, URI: {}, Type: {}", clientKey, requestURI, type);
        }
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Get the first IP in the list which is the client's IP
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
