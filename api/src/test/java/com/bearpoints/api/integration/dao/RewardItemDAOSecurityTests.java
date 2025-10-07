package com.bearpoints.api.integration.dao;

import com.bearpoints.api.config.SecurityConfig;
import com.bearpoints.api.dao.RewardItemDAO;
import com.bearpoints.api.entity.RewardItem;
import com.bearpoints.api.security.FirebaseAuthFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Security integration tests for {@link RewardItemDAO}.
 * <p>Verifies Spring Security annotations enforce access control rules:
 * <ul>
 *     <li>Authenticated read access for all roles</li>
 *     <li>ADMIN-only write privileges</li>
 *     <li>Internal method accessibility without authentication</li>
 * </ul>
 *
 * @see WithMockUser
 * @see DataJpaTest
 * @version 1.0
 * @author Dylan Mercer
 */
@DataJpaTest
@Import({SecurityConfig.class, RewardItemDAOSecurityTests.TestConfig.class})
public class RewardItemDAOSecurityTests {
    @Autowired
    private RewardItemDAO rewardItemDAO;

    @Autowired
    private TestEntityManager entityManager;

    private RewardItem testItem;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public FirebaseAuthFilter firebaseAuthFilter() {
            return mock(FirebaseAuthFilter.class);
        }
    }

    @BeforeEach
    void setup() {
        entityManager.clear();
        testItem = new RewardItem();
        testItem.setName("Test Reward");
        testItem.setPointCost(50);
        testItem.setStock(10);
        entityManager.persist(testItem);
        entityManager.flush();
    }

    // ==========
    // READ TESTS
    // ==========
    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT can get reward items ordered by name")
    void studentCanGetOrderedRewards() {
        assertDoesNotThrow(() -> {
            List<RewardItem> result = rewardItemDAO.findAllByOrderByNameAsc();
            assertFalse(result.isEmpty());
        });
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER can get reward items ordered by name")
    void teacherCanGetOrderedRewards() {
        assertDoesNotThrow(() -> {
            List<RewardItem> result = rewardItemDAO.findAllByOrderByNameAsc();
            assertFalse(result.isEmpty());
        });
    }

    @Test
    @DisplayName("Unauthenticated user cannot get reward items ordered by name")
    void unauthenticatedCannotGetOrderedRewards() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> rewardItemDAO.findAllByOrderByNameAsc());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT can get all reward items")
    void studentCanGetAllRewards() {
        assertDoesNotThrow(() -> {
            List<RewardItem> result = rewardItemDAO.findAll();
            assertFalse(result.isEmpty());
        });
    }

    // ===========
    // WRITE TESTS
    // ===========
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN can save reward items")
    void adminCanSaveRewardItem() {
        RewardItem newItem = new RewardItem();
        newItem.setName("New Reward");
        newItem.setPointCost(50);
        newItem.setStock(10);
        assertDoesNotThrow(() -> rewardItemDAO.save(newItem));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER cannot save reward items")
    void teacherCannotSaveRewardItem() {
        RewardItem newItem = new RewardItem();
        newItem.setName("New Reward");
        newItem.setPointCost(50);
        newItem.setStock(10);
        assertThrows(AccessDeniedException.class,
                () -> rewardItemDAO.save(newItem));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN can delete reward items")
    void adminCanDeleteRewardItem() {
        assertDoesNotThrow(() -> rewardItemDAO.delete(testItem));
    }

    // =====================
    // INTERNAL METHOD TESTS
    // =====================
    @Test
    @DisplayName("Internal findBySyncedToSheetsFalse requires no authentication")
    void internalFindUnsyncedNoAuth() {
        assertDoesNotThrow(() -> {
            List<RewardItem> result = rewardItemDAO.findBySyncedToSheetsFalse();
            assertFalse(result.isEmpty());
        });
    }
}
