package com.bearpoints.api.config;

import com.bearpoints.api.security.FirebaseAuthFilter;
import jakarta.servlet.Filter;
import org.apache.catalina.filters.RateLimitFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SecurityConfig}.
 * <p>Verifies configuration of Spring Security filter chain components:
 * <ul>
 *     <li>CSRF protection settings</li>
 *     <li>Session management policies</li>
 *     <li>Endpoint authorization rules</li>
 *     <li>Custom filter registration order</li>
 * </ul>
 *
 * <p>Tests validate that the security configuration:
 * <ul>
 *     <li>Disables CSRF protection for stateless API</li>
 *     <li>Configures stateless session management</li>
 *     <li>Sets correct public/private endpoint access rules:
 *          <ul>
 *              <li>Permits public access to POST /api/public/brag-logs</li>
 *              <li>Permits public access to /health and /public/** endpoints</li>
 *              <li>Requires authentication for all other endpoints</li>
 *          </ul>
 *     </li>
 *     <li>Registers custom filters in correct execution order:
 *          <ul>
 *              <li>{@link RateLimitFilter} before {@link FirebaseAuthFilter}</li>
 *              <li>{@link FirebaseAuthFilter} before {@link UsernamePasswordAuthenticationFilter}</li>
 *          </ul>
 *     </li>
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
        when(httpSecurity.csrf(any())).thenReturn(httpSecurity);
        when(httpSecurity.sessionManagement(any())).thenReturn(httpSecurity);
        when(httpSecurity.authorizeHttpRequests(any())).thenReturn(httpSecurity);
        when(httpSecurity.addFilterBefore(any(), any())).thenReturn(httpSecurity);
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
     *              <li>POST /api/public/brag-logs</li>
     *              <li>/health</li>
     *              <li>/public/**</li>
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
        when(authorizedUrl.permitAll()).thenReturn(registry);
        when(authorizedUrl.authenticated()).thenReturn(registry);
        when(registry.requestMatchers(any(HttpMethod.class), any(String.class))).thenReturn(authorizedUrl);
        when(registry.requestMatchers(any(String[].class))).thenReturn(authorizedUrl);
        when(registry.anyRequest()).thenReturn(authorizedUrl);
        authCaptor.getValue().customize(registry);
        verify(registry).requestMatchers(HttpMethod.POST, "/api/public/brag-logs");
        verify(registry).requestMatchers("/health", "/public/**");
        verify(registry).anyRequest();
        verify(authorizedUrl, times(2)).permitAll();
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
        verify(httpSecurity, times(2))
                .addFilterBefore(filterCaptor.capture(), filterClassCaptor.capture());
        List<Filter> filters = filterCaptor.getAllValues();
        List<Class<? extends Filter>> beforeFilters = filterClassCaptor.getAllValues();
        assertEquals(FirebaseAuthFilter.class, filters.getFirst().getClass());
        assertEquals(UsernamePasswordAuthenticationFilter.class, beforeFilters.getFirst());
        assertEquals(RateLimitFilter.class, filters.get(1).getClass());
        assertEquals(FirebaseAuthFilter.class, beforeFilters.get(1));
    }
}
