package com.bearpoints.api.security;

import com.bearpoints.api.dao.UserDAO;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Authentication filter for Firebase JWT tokens.
 * <p>This filter:
 * <ul>
 *     <li>Intercepts incoming requests with Authorization headers</li>
 *     <li>Verifies Firebase ID tokens</li>
 *     <li>Loads corresponding user details from the database</li>
 *     <li>Sets Spring Security authentication context</li>
 * </ul>
 * <p>Filter chain continues regardless of authentication success to allow public endpoints.
 * Failed verifications are logged but don't block request flow.
 *
 * @see OncePerRequestFilter
 * @see FirebaseUserDetails
 * @version 1.0
 * @author Dylan Mercer
 */
@Component
public class FirebaseAuthFilter extends OncePerRequestFilter {
    private final FirebaseAuth firebaseAuth;
    private final UserDAO userRepository;

    /**
     * Constructs a new Firebase authentication filter.
     *
     * @param firebaseAuth   Firebase authentication service instance
     * @param userRepository User data access repository
     */
    public FirebaseAuthFilter(
            @NonNull FirebaseAuth firebaseAuth,
            @NonNull UserDAO userRepository) {
        this.firebaseAuth = firebaseAuth;
        this.userRepository = userRepository;
    }

    /**
     *  Processes HTTP requests to authenticate Firebase tokens.
     *  <p>Workflow:
     *  <ol>
     *      <li>Checks for Bearer token in Authorization header</li>
     *      <li>Verifies token using Firebase Admin SDK</li>
     *      <li>Extracts email from decoded token</li>
     *      <li>Loads user details from database</li>
     *      <li>Sets Spring Security authentication context</li>
     *  </ol>
     *  <p>Continues filter chain in all cases, including:
     *  <ul>
     *      <li>Missing Authorization header</li>
     *      <li>Invalid tokens</li>
     *      <li>Unregistered users</li>
     *  </ul>
     *
     * @param request     HTTP servlet request
     * @param response    HTTP servlet response
     * @param filterChain Filter chain to continue processing
     * @throws ServletException If servlet processing fails
     * @throws IOException      If I/O operations fail
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                FirebaseToken decodedToken = firebaseAuth.verifyIdToken(token);
                String email = decodedToken.getEmail();
                if (email != null) {
                    userRepository.findByEmail(email).ifPresent(user -> {
                        FirebaseUserDetails userDetails = new FirebaseUserDetails(user);
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(userDetails,
                                        null, userDetails.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    });
                }
            } catch (FirebaseAuthException e) {
                logger.error("Firebase token verification failed.", e);
            }
        }
        filterChain.doFilter(request, response);
    }
}
