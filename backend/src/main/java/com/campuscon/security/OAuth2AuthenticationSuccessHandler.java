package com.campuscon.security;

import com.campuscon.model.User;
import com.campuscon.service.JwtService;
import com.campuscon.util.CookieUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

import java.io.IOException;

@Component
/**
 * Handles successful OAuth2 authentication by generating a JWT token
 * and redirecting the user to the frontend with the token.
 */
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final CookieUtils cookieUtils;
    
    @Value("${oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String defaultRedirectUri;
    
    @Value("${oauth2.cookie.redirect-param-name:redirect_uri}")
    private String redirectParamName;
    
    @Value("${oauth2.authorized-redirect-uris:http://localhost:3000/oauth2/redirect}")
    private String[] authorizedRedirectUris;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                        Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 Authentication success for user: {}", authentication.getName());
        
        if (response.isCommitted()) {
            log.warn("Response has already been committed. Unable to redirect.");
            return;
        }
        
        String targetUrl = determineTargetUrl(request, response, authentication);
        
        if (authentication.getPrincipal() instanceof User user) {
            // Generate JWT
            String jwt = jwtService.generateToken(user);
            
            // Add token to response header
            response.addHeader("Authorization", "Bearer " + jwt);
            
            // Add token to the redirect URL
            targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                    .queryParam("token", jwt)
                    .queryParam("user_id", user.getId())
                    .build().toUriString();
            
            // Clear authentication cookies
            cookieUtils.deleteCookie(request, response, redirectParamName);
            
            // Redirect to frontend with token
            getRedirectStrategy().sendRedirect(request, response, targetUrl);
        } else {
            // This should not happen with our setup, but handle gracefully
            log.error("Unexpected principal type: {}", authentication.getPrincipal().getClass());
            getRedirectStrategy().sendRedirect(request, response, "/oauth2/error");
        }
    }
    
    /**
     * Determine the target URL for redirection after successful authentication
     * 
     * First tries to use saved redirect URI from cookies, then falls back to default
     * Also validates that the URI is authorized for redirection
     */
    protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        Optional<String> redirectUri = cookieUtils.getCookie(request, redirectParamName)
                .map(Cookie::getValue);
        
        String targetUrl = redirectUri.orElse(defaultRedirectUri);
        
        // Validate that the redirect URI is authorized
        if (!isAuthorizedRedirectUri(targetUrl)) {
            log.warn("Unauthorized redirect URI: {}, will redirect to default redirect URI", targetUrl);
            return defaultRedirectUri;
        }
        
        return targetUrl;
    }
    
    /**
     * Check if the provided redirect URI is in the list of authorized URIs
     */
    private boolean isAuthorizedRedirectUri(String uri) {
        if (uri == null) {
            return false;
        }
        
        java.net.URI clientRedirectUri = java.net.URI.create(uri);
        
        for (String authorizedUri : authorizedRedirectUris) {
            java.net.URI authorizedURI = java.net.URI.create(authorizedUri);
            
            // Only validate the host and port, not the path as that can be dynamic
            if (authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                    && authorizedURI.getPort() == clientRedirectUri.getPort()) {
                return true;
            }
        }
        
        return false;
    }
}
