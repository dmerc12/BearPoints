package com.bearpoints.api.config;

import com.bearpoints.api.entity.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

/**
 * Global REST API configuration for the application.
 * <p>Configures:
 * <ul>
 *     <li>Exposure of entity IDs in REST responses</li>
 *     <li>Pagination settings (disabled by setting max page size)</li>
 *     <li>Cross-Origin Resource Sharing (CORS) policies</li>
 * </ul>
 *
 * <p>Key configurations:
 * <ul>
 *     <li>Exposes database IDs for all JPA entities in REST responses</li>
 *     <li>Disables pagination by setting page size to MAX_VALUE</li>
 *     <li>Allows CORS requests from localhost:3000</li>
 *     <li>Permits all HTTP methods and headers</li>
 *     <li>Enables credentials and sets 1-hour max age for preflight caching</li>
 * </ul>
 *
 * @see RepositoryRestConfigurer
 * @version 1.0
 * @author Dylan Mercer
 */
@Configuration
public class RestConfig implements RepositoryRestConfigurer {
    @Override
    public void configureRepositoryRestConfiguration(
            RepositoryRestConfiguration config,
            CorsRegistry cors) {
        config.exposeIdsFor(
                User.class, Teacher.class, Student.class, BehaviorType.class,
                BragLog.class, RewardItem.class, StudentReward.class
        );
        config.setDefaultPageSize(Integer.MAX_VALUE);
        config.setMaxPageSize(Integer.MAX_VALUE);
        cors.addMapping("/**")
                .allowedOrigins("http://localhost:3000")
                .allowedMethods(
                        HttpMethod.GET.name(),
                        HttpMethod.POST.name(),
                        HttpMethod.PUT.name(),
                        HttpMethod.PATCH.name(),
                        HttpMethod.DELETE.name(),
                        HttpMethod.OPTIONS.name()
                )
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
