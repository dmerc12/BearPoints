package com.bearpoints.api.config;

import com.bearpoints.api.security.FirebaseAuthFilter;
import org.apache.catalina.filters.RateLimitFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configures Spring Security for the application.
 * <p>This security configuration:
 * <ul>
 *     <li>Disables CSRF protection for stateless API</li>
 *     <li>Sets session management to stateless</li>
 *     <li>Configures request authorization:
 *          <ul>
 *              <li>Permits public access to POST /api/public/brag-logs</li>
 *              <li>Permits public access to /health and /public/**</li>
 *              <li>Requires authentication for all other endpoints</li>
 *          </ul>
 *     </li>
 *     <li>Adds custom filters:
 *          <ul>
 *              <li>{@link RateLimitFilter} as first filter for request throttling</li>
 *              <li>{@link FirebaseAuthFilter} for Firebase authentication</li>
 *          </ul>
 *     </li>
 * </ul>
 *
 * @see SecurityFilterChain
 * @see FirebaseAuthFilter
 * @version 1.0
 * @author Dylan Mercer
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    private final FirebaseAuthFilter firebaseAuthFilter;

    public SecurityConfig(FirebaseAuthFilter firebaseAuthFilter) {
        this.firebaseAuthFilter = firebaseAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/public/brag-logs").permitAll()
                        .requestMatchers("/health", "/public/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(firebaseAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new RateLimitFilter(), FirebaseAuthFilter.class);
        return http.build();
    }
}
