package com.bearpoints.api.integration;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for all integration tests using Testcontainers with PostgreSQL.
 * This abstract class provides a standardized test environment configuration
 * for all integration tests, eliminating repetitive setup code and ensuring
 * consistency across test suites. It establishes a PostgreSQL container
 * with optimal configuration for integration testing scenarios.
 *
 * <p>Key features provided by this base class:
 * <ul>
 *     <li>PostgreSQL Testcontainer instance for realistic database testing</li>
 *     <li>Automatic configuration of Spring Boot test context with "test" profile</li>
 *     <li>Dynamic property injection for database connection parameters</li>
 *     <li>Optimized Hikari connection pool settings for test performance</li>
 * </ul>
 *
 * <p>Usage: Extend this class in integration test classes to inherit the
 * complete test environment setup. Test classes should focus only on their
 * specific test logic without boilerplate configuration.
 *
 * <p>Note: The @DirtiesContext annotation ensures test isolation by resetting
 * the Spring application context after each test class execution.
 *
 * @see SpringBootTest
 * @see Testcontainers
 * @version 1.0
 * @author Dylan Mercer
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public abstract class BaseIntegrationTest {
    static {
        System.setProperty("TESTCONTAINERS_RYUK_DISABLED", "true");
        System.setProperty("TESTCONTAINERS_REUSE_ENABLE", "true");
        System.setProperty("TESTCONTAINERS_CHECKS_DISABLE", "true");
    }

    @Container
    @SuppressWarnings("resource")
    static final PostgreSQLContainer<?> POSTGRES_CONTAINER = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test")
            .withUsername("postgres")
            .withPassword("postgres")
            .withReuse(true)
            .withStartupTimeout(java.time.Duration.ofSeconds(60))
            .withConnectTimeoutSeconds(30);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);
        registry.add("spring.datasource.hikari.maximum-pool-size", () -> "5");
        registry.add("spring.datasource.hikari.minimum-idle", () -> "2");
        registry.add("spring.datasource.hikari.connection-timeout", () -> "10000");
        registry.add("spring.datasource.hikari.idle-timeout", () -> "30000");
        registry.add("spring.datasource.hikari.max-lifetime", () -> "180000");
    }
}
