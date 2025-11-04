package com.bearpoints.api.unit.security;

import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import com.bearpoints.api.security.FirebaseUserDetails;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link FirebaseUserDetails} functionality.
 * <p>Tests include:
 * <ul>
 *     <li>Overridden inherited functionality</li>
 * </ul>
 *
 * @see FirebaseUserDetails
 *
 * @version 1.0
 * @author Dylan Mercer
 */
public class FirebaseUserDetailsTests {
    private User user;
    private FirebaseUserDetails userDetails;

    @BeforeEach
    public void setup() {
        user = new User();
        user.setEmail("valid.user@okcps.org");
        user.setFirstName("ValidFirstName");
        user.setLastName("ValidLastName");
        user.setRole(Role.ADMIN);
        userDetails = new FirebaseUserDetails(user);
    }

    /** Test get authorities method */
    @Test
    @DisplayName("Get spring security user's role")
    public void getSpringSecurityUserRole() {
        Collection<? extends GrantedAuthority> role = userDetails.getAuthorities();
        Assertions.assertThatCollection(role).isEqualTo(
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole())));
    }

    /** Test get password method */
    @Test
    @DisplayName("Oauth used in place of passwords")
    public void getOauthUsedInPlace() {
        assertNull(userDetails.getPassword());
    }

    /** Test get username method */
    @Test
    @DisplayName("Get spring security user's email")
    public void getSpringSecurityUserEmail() {
        assertEquals(userDetails.getUsername(), user.getEmail());
    }

    /** Test is account non expired method */
    @Test
    @DisplayName("Spring security user's account is never expired")
    public void getSpringSecurityUserAccountIsNeverExpired() {
        userDetails.isAccountNonExpired();
    }

    /** Test is account non-locked method */
    @Test
    @DisplayName("Spring security user's account is never locked")
    public void getSpringSecurityUserAccountIsNeverLocked() {
        userDetails.isAccountNonLocked();
    }

    /** Test is credentials non expired method */
    @Test
    @DisplayName("Spring security user's credentials are never expired")
    public void getSpringSecurityUserCredentialsAreNeverExpired() {
        userDetails.isCredentialsNonExpired();
    }

    /** Test is enabled method */
    @Test
    @DisplayName("Spring security user's account is always enabled")
    public void getSpringSecurityUserAccountIsAlwaysEnabled() {
        userDetails.isEnabled();
    }
}
