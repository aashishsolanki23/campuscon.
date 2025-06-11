package com.campuscon.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.ClassNotFoundException;
import java.util.Base64;
import java.util.Optional;

/**
 * Utility class for handling cookies for OAuth2 authentication flow
 * Used to store and retrieve state and redirect URIs during OAuth2 authentication
 */
@Component
public class CookieUtils {

    /**
     * Get a cookie by name from the request
     * 
     * @param request HttpServletRequest
     * @param name Name of the cookie
     * @return Optional containing the cookie if found
     */
    public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }
        
        return Optional.empty();
    }

    /**
     * Add a cookie to the response
     * 
     * @param response HttpServletResponse
     * @param name Name of the cookie
     * @param value Value of the cookie
     * @param maxAge Max age of the cookie in seconds
     */
    public void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    /**
     * Delete a cookie
     * 
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param name Name of the cookie
     */
    public void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    cookie.setValue("");
                    cookie.setPath("/");
                    cookie.setMaxAge(0);
                    response.addCookie(cookie);
                }
            }
        }
    }

    /**
     * Serialize an object to a string for cookie storage
     * 
     * @param object Object to serialize
     * @return Base64 encoded string
     */
    public String serialize(Object object) {
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(object));
    }

    /**
     * Deserialize a string to an object
     * 
     * @param cookie Cookie containing serialized object
     * @param cls Class of the object
     * @return Deserialized object
     */
    public <T> T deserialize(Cookie cookie, Class<T> cls) {
        byte[] bytes = Base64.getUrlDecoder().decode(cookie.getValue());
        // Using ByteArrayInputStream and ObjectInputStream to avoid deprecated method
        try (ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return cls.cast(ois.readObject());
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Failed to deserialize cookie value", e);
        }
    }
}
