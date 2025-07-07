package com.bearpoints.api.security;

import com.bearpoints.api.dao.UserRepository;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link FirebaseAuthFilter} functionality.
 * <p>Tests include:
 * <ul>
 *     <li>Authentication header absence</li>
 *     <li>Non-Bearer authorization header</li>
 *     <li>Valid token with registered user</li>
 *     <li>Valid token with null email</li>
 *     <li>Invalid token verification</li>
 *     <li>Valid token with unregistered user</li>
 * </ul>
 * <p>Verifies:
 * <ul>
 *     <li>Proper authentication context setting</li>
 *     <li>Filter chain continuation in all cases</li>
 *     <li>Error handling for invalid tokens</li>
 *     <li>Repository interaction patterns</li>
 * </ul>
 *
 * @see FirebaseAuthFilter
 * @version 1.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
public class FirebaseAuthFilterTests {
    @Mock
    private FirebaseAuth firebaseAuth;

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private FirebaseToken decodedToken;

    @InjectMocks
    private FirebaseAuthFilter firebaseAuthFilter;

    /** Clears security context after each test */
    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /** Test no authentication header */
    @Test
    @DisplayName("Auth header null - skips authentication")
    public void authHeaderNull() throws ServletException, IOException {
        when(request.getHeader("Authorization")).thenReturn(null);
        firebaseAuthFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    /** Test non-bearer authorization header */
    @Test
    @DisplayName("Non-Bearer authorization header - skips authentication")
    public void noBearerAuthorizationHeader() throws ServletException, IOException, FirebaseAuthException {
        when(request.getHeader("Authorization")).thenReturn("Basic base64credentials");
        firebaseAuthFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verify(firebaseAuth, never()).verifyIdToken(anyString());
        verify(userRepository, never()).findByEmail(anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    /** Test valid token with registered user */
    @Test
    @DisplayName("Valid token with registered email - sets authentication")
    public void authEmailNotNull() throws ServletException, IOException, FirebaseAuthException {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid_token");
        when(firebaseAuth.verifyIdToken("valid_token")).thenReturn(decodedToken);
        when(decodedToken.getEmail()).thenReturn("user@okcps.org");
        User user = new User();
        user.setRole(Role.ADMIN);
        when(userRepository.findByEmail("user@okcps.org")).thenReturn(Optional.of(user));
        firebaseAuthFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
    }

    /** Test valid token with null email */
    @Test
    @DisplayName("Valid token with null email - skips authentication")
    public void authEmailNull() throws ServletException, IOException, FirebaseAuthException {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid_token");
        when(firebaseAuth.verifyIdToken("valid_token")).thenReturn(decodedToken);
        firebaseAuthFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verify(userRepository, never()).findByEmail(anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    /** Test invalid token verification */
    @Test
    @DisplayName("Invalid token - logs error and continues")
    public void firebaseTokenVerificationFail() throws ServletException, IOException, FirebaseAuthException {
        when(request.getHeader("Authorization")).thenReturn("Bearer invalid_token");
        when(firebaseAuth.verifyIdToken("invalid_token")).thenThrow(mock(FirebaseAuthException.class));
        firebaseAuthFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verify(userRepository, never()).findByEmail(anyString());
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    /** Test valid token with unregistered user */
    @Test
    @DisplayName("Valid token with unregistered email - skips authentication")
    public void validTokenUnregisteredUser() throws ServletException, IOException, FirebaseAuthException {
        when(request.getHeader("Authorization")).thenReturn("Bearer valid_token");
        when(firebaseAuth.verifyIdToken("valid_token")).thenReturn(decodedToken);
        when(decodedToken.getEmail()).thenReturn("unknown@okcps.org");
        when(userRepository.findByEmail("unknown@okcps.org")).thenReturn(Optional.empty());
        firebaseAuthFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
