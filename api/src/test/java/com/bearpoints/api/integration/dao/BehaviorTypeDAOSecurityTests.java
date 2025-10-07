package com.bearpoints.api.integration.dao;

import com.bearpoints.api.config.SecurityConfig;
import com.bearpoints.api.dao.BehaviorTypeDAO;
import com.bearpoints.api.entity.BehaviorType;
import com.bearpoints.api.security.FirebaseAuthFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Security integration tests for {@link BehaviorTypeDAO}.
 * <p>Verifies Spring Security annotations enforce:
 * <ul>
 *     <li>ADMIN role requirement for write operations</li>
 *     <li>Public access for read-only methods</li>
 * </ul>
 *
 * <p>Test scenarios:
 * <ul>
 *     <li>Admin role can modify records</li>
 *     <li>Non-admin roles blocked from write operations</li>
 *     <li>Unauthenticated users can read public data</li>
 * </ul>
 *
 * @see WithMockUser
 * @version 1.0
 * @author Dylan Mercer
 */
@DataJpaTest
@Import({SecurityConfig.class, BehaviorTypeDAOSecurityTests.TestConfig.class})
public class BehaviorTypeDAOSecurityTests {
    @Autowired
    private BehaviorTypeDAO behaviorTypeDAO;

    @Autowired
    private TestEntityManager entityManager;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public FirebaseAuthFilter firebaseAuthFilter() {
            return mock(FirebaseAuthFilter.class);
        }
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN can save behavior types")
    void adminShouldSaveBehaviorTypes() {
        BehaviorType newBehavior = new BehaviorType();
        newBehavior.setName("Helpful");
        newBehavior.setPointValue(3);
        newBehavior.setActive(true);
        assertDoesNotThrow(() -> behaviorTypeDAO.save(newBehavior));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER cannot save behavior types")
    void nonAdminShouldNotSaveBehaviorTypes() {
        BehaviorType newBehavior = new BehaviorType();
        newBehavior.setName("Helpful");
        newBehavior.setPointValue(3);
        newBehavior.setActive(true);
        assertThrows(AccessDeniedException.class, () -> behaviorTypeDAO.save(newBehavior));
    }

    @Test
    @DisplayName("Unauthenticated user can read active types")
    void unauthenticatedUserShouldReadActiveTypes() {
        BehaviorType behavior = new BehaviorType();
        behavior.setName("Public");
        behavior.setPointValue(2);
        behavior.setActive(true);
        entityManager.persist(behavior);
        entityManager.flush();
        assertDoesNotThrow(() -> {
            List<BehaviorType> result = behaviorTypeDAO.findByActiveTrue();
            assertFalse(result.isEmpty());
        });
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT cannot delete behavior types")
    void studentShouldNotDeleteBehaviorTypes() {
        BehaviorType behavior = new BehaviorType();
        behavior.setName("Temp");
        behavior.setPointValue(1);
        behavior.setActive(true);
        BehaviorType savedBehavior = entityManager.persist(behavior);
        entityManager.flush();
        assertThrows(AccessDeniedException.class, () -> behaviorTypeDAO.delete(savedBehavior));
    }
}
