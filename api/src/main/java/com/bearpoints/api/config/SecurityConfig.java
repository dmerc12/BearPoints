package com.bearpoints.api.config;

import com.bearpoints.api.security.FirebaseAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configures Spring Security for the application.
 * <p>This security configuration:
 * <ul>
 *     <li>Disables CSRF protection for stateless API</li>
 *     <li>Sets session management to stateless</li>
 *     <li>Configures Cross-Origin Resource Sharing (CORS) policies</li>
 *     <li>Defines endpoint authorization rules</li>
 *     <li>Adds custom authentication filter:
 *          <ul>
 *              <li>{@link FirebaseAuthFilter} for Firebase authentication</li>
 *          </ul>
 *     </li>
 * </ul>
 *
 * <p>CORS configuration:
 * <ul>
 *     <li>Allows requests from localhost:5173</li>
 *     <li>Permits all HTTP methods and headers</li>
 *     <li>Enables credentials support</li>
 *     <li>Exposes Authorization header</li>
 *     <li>Sets 1-hour max age for preflight caching</li>
 * </ul>
 *
 * @see SecurityFilterChain
 * @see FirebaseAuthFilter
 * @version 1.0
 * @author Dylan Mercer
 */
@Configuration
@Profile("!test")
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {
    private final FirebaseAuthFilter firebaseAuthFilter;

    public SecurityConfig(FirebaseAuthFilter firebaseAuthFilter) {
        this.firebaseAuthFilter = firebaseAuthFilter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.addExposedHeader("Authorization");
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/api/public/brag-logs").permitAll()
                        .requestMatchers("/api/health", "/public/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(firebaseAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
