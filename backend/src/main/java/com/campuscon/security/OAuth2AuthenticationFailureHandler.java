package com.campuscon.security;

import com.campuscon.util.CookieUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * Handler for OAuth2 authentication failures
 * Redirects to the frontend with appropriate error parameters
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final CookieUtils cookieUtils;
    
    @Value("${oauth2.redirect-uri}")
    private String redirectUri;
    
    @Value("${oauth2.cookie.redirect-param-name:redirect_uri}")
    private String redirectParamName;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, 
                                        AuthenticationException exception) throws IOException, ServletException {
        
        String targetUrl = cookieUtils.getCookie(request, redirectParamName)
                .map(Cookie::getValue)
                .orElse(redirectUri);

        targetUrl = UriComponentsBuilder.fromUriString(targetUrl)
                .queryParam("error", exception.getLocalizedMessage())
                .build().toUriString();

        // Clear cookies after use
        cookieUtils.deleteCookie(request, response, redirectParamName);

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
