package com.campuscon.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom authentication entry point for REST API calls
 * Returns 401 Unauthorized response instead of redirecting to login page
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, 
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        // Log the error
        String errorMessage = authException != null ? authException.getMessage() : "Unauthorized access";
        
        // Return 401 error 
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, errorMessage);
    }
}
