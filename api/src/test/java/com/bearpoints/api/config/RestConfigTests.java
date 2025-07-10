package com.bearpoints.api.config;

import com.bearpoints.api.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link RestConfig}.
 * <p>Verifies configuration of:
 * <ul>
 *     <li>Entity ID exposure in REST responses</li>
 *     <li>Pagination settings</li>
 *     <li>CORS policy configuration</li>
 * </ul>
 *
 * <p>Tests ensure proper configuration of:
 * <ul>
 *     <li>All entity IDs are exposed for REST responses</li>
 *     <li>Pagination is disabled via MAX_VALUE page size</li>
 *     <li>CORS allows requests from localhost:3000 with all methods</li>
 * </ul>
 *
 * @see RestConfig
 * @version 1.0
 * @author Dylan Mercer
 */
public class RestConfigTests {
    @Test
    @DisplayName("Test Rest config sets correct values")
    void configureRepositoryRestConfiguration_SetsCorrectValues() {
        RestConfig restConfig = new RestConfig();
        RepositoryRestConfiguration mockConfig = mock(RepositoryRestConfiguration.class);
        CorsRegistry corsRegistry = new CorsRegistry();
        restConfig.configureRepositoryRestConfiguration(mockConfig, corsRegistry);
        verify(mockConfig).exposeIdsFor(User.class, Teacher.class, Student.class, BehaviorType.class,
                BragLog.class, RewardItem.class, StudentReward.class);
        verify(mockConfig).setDefaultPageSize(Integer.MAX_VALUE);
        verify(mockConfig).setMaxPageSize(Integer.MAX_VALUE);
        CorsConfiguration corsConfig = extractCorsConfiguration(corsRegistry);
        assertNotNull(corsConfig);
        assertNotNull(corsConfig.getAllowedOrigins());
        assertArrayEquals(new String[]{"http://localhost:3000"}, corsConfig.getAllowedOrigins().toArray());
        assertNotNull(corsConfig.getAllowedMethods());
        assertArrayEquals(new String[]{"GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"},
                corsConfig.getAllowedMethods().toArray());
        assertNotNull(corsConfig.getAllowedHeaders());
        assertArrayEquals(new String[]{"*"}, corsConfig.getAllowedHeaders().toArray());
        assertEquals(Boolean.TRUE, corsConfig.getAllowCredentials());
        assertNotNull(corsConfig.getMaxAge());
        assertEquals(3600L, (long) corsConfig.getMaxAge());
    }

    private CorsConfiguration extractCorsConfiguration(CorsRegistry registry) {
        try {
            Method getConfigSource = CorsRegistry.class.getDeclaredMethod("getCorsConfigurations");
            CorsConfigurationSource source = (CorsConfigurationSource) getConfigSource.invoke(registry);
            return source.getCorsConfiguration(new MockHttpServletRequest());
        } catch (Exception ignored) {}
        String[] fieldNames = {"configurations", "corsConfigurations", "registrations"};
        for (String fieldName : fieldNames) {
            try {
                Field field = CorsRegistry.class.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object value = field.get(registry);
                if (value instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, CorsConfiguration> configMap = (Map<String, CorsConfiguration>) value;
                    return configMap.get("/**");
                } else if (value instanceof List) {
                    for (Object item : (List<?>) value) {
                        if (item instanceof CorsRegistration) {
                            Field configField = CorsRegistration.class.getDeclaredField("config");
                            configField.setAccessible(true);
                            return (CorsConfiguration) configField.get(item);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
        throw new IllegalStateException("Could not extract CORS configuration");
    }
}
