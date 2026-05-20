package com.bearpoints.api.unit.config;

import com.bearpoints.api.config.SecurityConfig;
import com.bearpoints.api.security.FirebaseAuthFilter;
import jakarta.servlet.Filter;
import org.apache.catalina.filters.RateLimitFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SecurityConfig}.
 * <p>Contains two main testing areas:
 * <ul>
 *     <li>Security Filter Chain Configuration (nested class)</li>
 *     <li>CORS Configuration (standalone test)</li>
 * </ul>
 *
 * <p>Nested class verifies configuration of Spring Security filter chain components:
 * <ul>
 *     <li>CSRF protection settings</li>
 *     <li>Session management policies</li>
 *     <li>Endpoint authorization rules</li>
 *     <li>Custom filter registration order</li>
 * </ul>
 *
 * <p>Standalone test verifies CORS configuration values:
 * <ul>
 *     <li>Allowed origins, methods, and headers</li>
 *     <li>Credential support</li>
 *     <li>Exposed headers and max age</li>
 * </ul>
 *
 * <p>Tests validate that the security configuration:
 * <ul>
 *     <li>Disables CSRF protection for stateless API</li>
 *     <li>Configures stateless session management</li>
 *     <li>Sets correct public/private endpoint access rules:
 *          <ul>
 *              <li>Permits public access to POST /api/public/brag-logs</li>
 *              <li>Permits public access to /api/health and /public/** endpoints</li>
 *              <li>Requires authentication for all other endpoints</li>
 *          </ul>
 *     </li>
 *     <li>Registers custom filters in correct execution order:
 *          <ul>
 *              <li>{@link FirebaseAuthFilter} before {@link UsernamePasswordAuthenticationFilter}</li>
 *          </ul>
 *     </li>
 *     <li>Configures propper CORS policies for cross-origin requests</li>
 * </ul>
 *
 * @see SecurityConfig
 * @version 1.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"rawTypes", "unchecked"})
public class SecurityConfigTests {
    @Mock
    private HttpSecurity httpSecurity;

    @Mock
    private SecurityFilterChain securityFilterChain;

    @Mock
    @SuppressWarnings("unused")
    // Injected dependency for SecurityConfig constructor
    private FirebaseAuthFilter firebaseAuthFilter;

    @InjectMocks
    private SecurityConfig securityConfig;

    @Captor
    private ArgumentCaptor<Customizer<CsrfConfigurer<HttpSecurity>>> csrfCustomizerCaptor;

    @Captor
    private ArgumentCaptor<Customizer<SessionManagementConfigurer<HttpSecurity>>> sessionCustomizerCaptor;

    @Captor
    private ArgumentCaptor<Customizer<AuthorizeHttpRequestsConfigurer<HttpSecurity>
            .AuthorizationManagerRequestMatcherRegistry>> authCaptor;

    @Nested
    @DisplayName("Security Filter Chain Configuration Tests")
    class SecurityFilterChainTests {
        /**
         * Configures common mock behavior before each test.
         * <p>Sets up method chain stubs for:
         * <ul>
         *     <li>CSRF configuration</li>
         *     <li>Session management</li>
         *     <li>Authorization rules</li>
         *     <li>Filter registration</li>
         * </ul>
         */
        @BeforeEach
        public void setup() throws Exception {
            doReturn(httpSecurity).when(httpSecurity).csrf(any());
            doReturn(httpSecurity).when(httpSecurity).cors(any());
            doReturn(httpSecurity).when(httpSecurity).sessionManagement(any());
            doReturn(httpSecurity).when(httpSecurity).authorizeHttpRequests(any());
            doReturn(httpSecurity).when(httpSecurity).addFilterBefore(any(), any());
            doReturn(securityFilterChain).when(httpSecurity).build();
        }

        /**
         * Tests that CSRF protection is disabled in the security filter chain.
         * <p>Verifies:
         * <ul>
         *     <li>CSRF customizer is captured during configuration</li>
         *     <li>{@link CsrfConfigurer#disable()} is invoked</li>
         * </ul>
         */
        @Test
        @DisplayName("Security filter chain disables csrf")
        void securityFilterChainDisablesCsrf() throws Exception {
            securityConfig.securityFilterChain(httpSecurity);
            verify(httpSecurity).csrf(csrfCustomizerCaptor.capture());
            CsrfConfigurer<HttpSecurity> csrfConfigurer = mock(CsrfConfigurer.class);
            csrfCustomizerCaptor.getValue().customize(csrfConfigurer);
            verify(csrfConfigurer).disable();
        }

        /**
         * Tests that session management is configured as stateless.
         * <p>Verifies:
         * <ul>
         *     <li>Session management customizer is captured</li>
         *     <li>{@link SessionCreationPolicy#STATELESS} is set</li>
         * </ul>
         */
        @Test
        @DisplayName("Security filter chain sets stateless session")
        void securityFilterChainSetsStatelessSession() throws Exception {
            securityConfig.securityFilterChain(httpSecurity);
            verify(httpSecurity).sessionManagement(sessionCustomizerCaptor.capture());
            SessionManagementConfigurer<HttpSecurity> sessionConfigurer = mock(SessionManagementConfigurer.class);
            sessionCustomizerCaptor.getValue().customize(sessionConfigurer);
            verify(sessionConfigurer).sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        }

        /**
         * Tests the authorization rule configuration for endpoints.
         * <p>Verifies:
         * <ul>
         *     <li>Request matchers are registered for public endpoints:
         *          <ul>
         *              <li>/health</li>
         *          </ul>
         *     </li>
         *     <li>All other requests require authentication</li>
         *     <li>Correct permitAll() and authenticated() calls are made</li>
         * </ul>
         */
        @Test
        @DisplayName("Security filter chain configures authorization rules")
        void securityFilterChainConfiguresAuthorizationRules() throws Exception {
            securityConfig.securityFilterChain(httpSecurity);
            verify(httpSecurity).authorizeHttpRequests(authCaptor.capture());
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry registry =
                    mock(AuthorizeHttpRequestsConfigurer.AuthorizationManagerRequestMatcherRegistry.class);
            AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizedUrl authorizedUrl =
                    mock(AuthorizeHttpRequestsConfigurer.AuthorizedUrl.class);
            lenient().when(authorizedUrl.permitAll()).thenReturn(registry);
            lenient().when(authorizedUrl.authenticated()).thenReturn(registry);
            lenient().when(registry.requestMatchers(any(HttpMethod.class), any(String.class))).thenReturn(authorizedUrl);
            lenient().when(registry.requestMatchers(any(String[].class))).thenReturn(authorizedUrl);
            lenient().when(registry.anyRequest()).thenReturn(authorizedUrl);
            authCaptor.getValue().customize(registry);
            verify(registry).requestMatchers("/actuator/health");
            verify(registry).anyRequest();
            verify(authorizedUrl, times(1)).permitAll();
            verify(authorizedUrl).authenticated();
        }

        /**
         * Tests the registration order of custom security filters.
         * <p>Verifies filter execution order:
         * <ol>
         *     <li>{@link RateLimitFilter} before {@link FirebaseAuthFilter}</li>
         *     <li>{@link FirebaseAuthFilter} before {@link UsernamePasswordAuthenticationFilter}</li>
         * </ol>
         */
        @Test
        @DisplayName("Security filter chain adds filters in correct order")
        void securityFilterChainAddsFiltersInCorrectOrder() throws Exception {
            securityConfig.securityFilterChain(httpSecurity);
            ArgumentCaptor<Filter> filterCaptor = ArgumentCaptor.forClass(Filter.class);
            ArgumentCaptor<Class<? extends Filter>> filterClassCaptor = ArgumentCaptor.forClass(Class.class);
            verify(httpSecurity, times(1))
                    .addFilterBefore(filterCaptor.capture(), filterClassCaptor.capture());
            List<Filter> filters = filterCaptor.getAllValues();
            List<Class<? extends Filter>> beforeFilters = filterClassCaptor.getAllValues();
            assertEquals(1, filters.size());
            assertEquals(1, beforeFilters.size());
            assertEquals(FirebaseAuthFilter.class, filters.getFirst().getClass());
            assertEquals(UsernamePasswordAuthenticationFilter.class, beforeFilters.getFirst());
        }
    }

    /**
     * Tests the CORS configuration source.
     * <p>Verifies:
     * <ul>
     *     <li>Allowed origins are set to localhost:5173</li>
     *     <li>All HTTP methods are allowed</li>
     *     <li>All headers are allowed</li>
     *     <li>Credentials are enabled</li>
     *     <li>Authorization header is exposed</li>
     *     <li>Max age is set to 3600 seconds</li>
     * </ul>
     */
    @Test
    @DisplayName("CORS configuration source sets correct value")
    void corsConfigurationSourceSetsCorrectValue() {
        SecurityConfig securityConfig = new SecurityConfig(null);
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        assertInstanceOf(UrlBasedCorsConfigurationSource.class, source);
        UrlBasedCorsConfigurationSource urlBasedSource = (UrlBasedCorsConfigurationSource) source;
        CorsConfiguration config = urlBasedSource.getCorsConfigurations().get("/**");
        assertNotNull(config);
        assertEquals(List.of("http://localhost:5173", "https://dd8gbzj08h6gp.cloudfront.net", "https://bearpoints.org"), config.getAllowedOrigins());
        assertEquals(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"), config.getAllowedMethods());
        assertEquals(List.of("*"), config.getAllowedHeaders());
        assertEquals(Boolean.TRUE, config.getAllowCredentials());
        assertEquals(List.of("Authorization"), config.getExposedHeaders());
        assertEquals(3600L, config.getMaxAge());
    }
}
