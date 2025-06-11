package com.campuscon.security;

import com.campuscon.model.User;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

// UserDetails already extends Serializable
import java.util.Collection;
import java.util.Objects;

/**
 * Custom UserDetails implementation for authentication
 */
@AllArgsConstructor
@Builder
@Getter
public class UserPrincipal implements UserDetails {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String username;
    private String email;
    @JsonIgnore
    private String password;
    private Collection<? extends GrantedAuthority> authorities;
    
    // Additional fields for CampusCon
    private Set<String> userTypes;
    private String profilePictureUrl;
    private String collegeName;
    private Boolean isEmailVerified;
    private String rollNumber;
    private String batchYear;
    private String courseCode;
    private String organizationRole;
    private String displayName;

    /**
     * Create a UserPrincipal from User entity
     */
    public static UserPrincipal create(User user) {
        // Get authorities directly from the user
        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        return UserPrincipal.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .password(user.getPassword())
                .authorities(authorities)
                .userTypes(user.getUserTypes())
                .profilePictureUrl(user.getProfilePictureUrl())
                .collegeName(user.getCollegeName())
                .isEmailVerified(user.isEmailVerified())
                .displayName(user.getDisplayName())
                .build();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEmailVerified != null ? isEmailVerified : true;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
