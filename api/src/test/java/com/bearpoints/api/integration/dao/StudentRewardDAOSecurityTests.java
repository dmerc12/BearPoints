package com.bearpoints.api.integration.dao;

import com.bearpoints.api.config.SecurityConfig;
import com.bearpoints.api.dao.StudentRewardDAO;
import com.bearpoints.api.entity.*;
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
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Security integration tests for {@link StudentRewardDAO}.
 * <p>Verifies Spring Security annotations enforce:
 * <ul>
 *     <li>STUDENT/TEACHER/ADMIN access for reward redemption</li>
 *     <li>ADMIN-only delete privileges</li>
 *     <li>Internal sync method accessibility</li>
 * </ul>
 *
 * <p>Test scenarios:
 * <ul>
 *     <li>Role-based access to save operations</li>
 *     <li>Delete operation restrictions</li>
 *     <li>Internal sync method accessibility</li>
 * </ul>
 *
 * @see WithMockUser
 * @version 1.0
 * @author Dylan Mercer
 */
@DataJpaTest
@Import({SecurityConfig.class, StudentRewardDAOSecurityTests.TestConfig.class})
public class StudentRewardDAOSecurityTests {
    @Autowired
    private StudentRewardDAO studentRewardDAO;

    @Autowired
    private TestEntityManager entityManager;

    private StudentReward testRedemption;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public FirebaseAuthFilter firebaseAuthFilter() {
            return mock(FirebaseAuthFilter.class);
        }
    }

    @BeforeEach
    void setup() {
        User teacherUser = new User();
        teacherUser.setFirstName("teacher");
        teacherUser.setLastName("user");
        teacherUser.setEmail("teacher.user@okcps.org");
        teacherUser.setRole(Role.TEACHER);
        entityManager.persist(teacherUser);
        Teacher teacher = new Teacher();
        teacher.setUser(teacherUser);
        teacher.setGrade(GradeLevel.FIRST);
        entityManager.persist(teacher);
        User studentUser = new User();
        studentUser.setFirstName("student");
        studentUser.setLastName("user");
        studentUser.setEmail("student.user@okcps.org");
        studentUser.setRole(Role.STUDENT);
        entityManager.persist(studentUser);
        Student student = new Student();
        student.setUser(studentUser);
        student.setTeacher(teacher);
        entityManager.persist(student);
        RewardItem reward = new RewardItem();
        reward.setName("reward");
        reward.setPointCost(0);
        reward.setStock(1);
        entityManager.persist(reward);
        testRedemption = new StudentReward();
        testRedemption.setStudent(student);
        testRedemption.setRewardItem(reward);
        entityManager.persist(testRedemption);
        entityManager.flush();
    }

    // ==========
    // SAVE TESTS
    // ==========
    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT can save reward redemption")
    void studentCanSaveRedemption() {
        StudentReward newRedemption = new StudentReward();
        newRedemption.setStudent(testRedemption.getStudent());
        newRedemption.setRewardItem(testRedemption.getRewardItem());
        assertDoesNotThrow(() -> studentRewardDAO.save(newRedemption));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER can save reward redemption")
    void teacherCanSaveRedemption() {
        StudentReward newRedemption = new StudentReward();
        newRedemption.setStudent(testRedemption.getStudent());
        newRedemption.setRewardItem(testRedemption.getRewardItem());
        assertDoesNotThrow(() -> studentRewardDAO.save(newRedemption));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN can save reward redemption")
    void adminCanSaveRedemption() {
        StudentReward newRedemption = new StudentReward();
        newRedemption.setStudent(testRedemption.getStudent());
        newRedemption.setRewardItem(testRedemption.getRewardItem());
        assertDoesNotThrow(() -> studentRewardDAO.save(newRedemption));
    }

    @Test
    @DisplayName("Unauthenticated user cannot save reward redemption")
    void unauthenticatedCannotSaveRedemption() {
        StudentReward newRedemption = new StudentReward();
        newRedemption.setStudent(testRedemption.getStudent());
        newRedemption.setRewardItem(testRedemption.getRewardItem());
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> studentRewardDAO.save(newRedemption));
    }

    // ============
    // DELETE TESTS
    // ============
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN can delete redemption")
    void adminCanDelete() {
        assertDoesNotThrow(() -> studentRewardDAO.delete(testRedemption));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER cannot delete redemption")
    void teacherCannotDelete() {
        assertThrows(AuthorizationDeniedException.class,
                () -> studentRewardDAO.delete(testRedemption));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT cannot delete redemption")
    void studentCannotDelete() {
        assertThrows(AuthorizationDeniedException.class,
                () -> studentRewardDAO.delete(testRedemption));
    }

    // =====================
    // INTERNAL METHOD TESTS
    // =====================
    @Test
    @DisplayName("Internal findBySyncedToSheetsFalse requires no authentication")
    void internalSyncMethodNoAuth() {
        assertDoesNotThrow(() -> {
            List<StudentReward> result = studentRewardDAO.findBySyncedToSheetsFalse();
            assertFalse(result.isEmpty());
        });
    }
}
