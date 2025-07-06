package com.bearpoints.api.security;

import com.bearpoints.api.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Represents logged-in user.
 * <p>Extends spring security user details
 *
 * @author Dylan Mercer
 * @version 1.0
 */
@Getter
public class FirebaseUserDetails implements UserDetails {
    private final User user;

    /**
     * Instantiates a new Firebase user details.
     *
     * @param user the user
     */
    public FirebaseUserDetails(User user) {
        this.user = user;
    }

    /** Get user's role */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_"  + user.getRole().name())
        );
    }

    /** OAuth used in place of passwords */
    @Override
    public String getPassword() {
        return null;
    }

    /** Email used in place of username */
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    /** Accounts do not expire */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** Accounts do not lock */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** Credentials do not expire */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** User is always enabled */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
