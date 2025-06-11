package com.campuscon.dto.auth;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String username;
    private String email;
    private java.util.Set<String> userTypes;
    private boolean isVerified;
    private String message;
}
