package com.bearpoints.api.unit.config;

import com.bearpoints.api.config.RestConfig;
import com.bearpoints.api.entity.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link RestConfig}.
 * <p>Verifies configuration of:
 * <ul>
 *     <li>Entity ID exposure in REST responses</li>
 *     <li>Pagination settings</li>
 * </ul>
 *
 * <p>Tests ensure proper configuration of:
 * <ul>
 *     <li>All entity IDs are exposed for REST responses</li>
 *     <li>Pagination is disabled via MAX_VALUE page size</li>
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
    }
}
