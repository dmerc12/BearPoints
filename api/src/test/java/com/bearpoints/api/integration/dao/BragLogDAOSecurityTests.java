package com.bearpoints.api.integration.dao;

import com.bearpoints.api.config.SecurityConfig;
import com.bearpoints.api.dao.BragLogDAO;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Security integration tests for {@link BragLogDAO}
 * <p>Verifies Spring Security annotations enforce access control rules:
 * <ul>
 *     <li>Authenticated read access for all roles</li>
 *     <li>ADMIN-only delete privileges</li>
 *     <li>Internal method accessibility without authentication</li>
 * </ul>
 *
 * <p>Test scenarios cover:
 * <ul>
 *     <li>Role-based access to student specific brag logs</li>
 *     <li>Global brag log retrieval permissions</li>
 *     <li>Delete operation authorization constraints</li>
 *     <li>Internal synchronization method accessibility</li>
 * </ul>
 *
 * <p>Test Configuration:
 * <ul>
 *     <li>Initializes complete entity graph (User -> Teacher -> Student -> BragLog -> BehaviorType)</li>
 *     <li>Uses mock authentication filter to bypass Firebase</li>
 *     <li>Cleans database before each test</li>
 * </ul>
 *
 * @see WithMockUser
 * @see DataJpaTest
 * @version 1.0
 * @author Dylan Mercer
 */
@DataJpaTest
@Import({SecurityConfig.class, BragLogDAOSecurityTests.TestConfig.class})
public class BragLogDAOSecurityTests {
    @Autowired
    private BragLogDAO bragLogDAO;

    @Autowired
    private TestEntityManager entityManager;

    private BragLog testBragLog;
    private Student testStudent;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public FirebaseAuthFilter firebaseAuthFilter() {
            return mock(FirebaseAuthFilter.class);
        }
    }

    @BeforeEach
    void setup() {
        entityManager.getEntityManager().createQuery("DELETE FROM BragLog").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM BehaviorType").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Student").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Teacher").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM User").executeUpdate();
        entityManager.flush();
        entityManager.clear();
        User teacherUser = new User();
        teacherUser.setFirstName("Teacher");
        teacherUser.setLastName("User");
        teacherUser.setEmail("teacher.user@okcps.org");
        teacherUser.setRole(Role.TEACHER);
        entityManager.persist(teacherUser);
        Teacher testTeacher = new Teacher();
        testTeacher.setUser(teacherUser);
        testTeacher.setGrade(GradeLevel.PRE_K);
        entityManager.persist(testTeacher);
        User studentUser = new User();
        studentUser.setFirstName("Student");
        studentUser.setLastName("User");
        studentUser.setEmail("student.user@okcps.org");
        studentUser.setRole(Role.STUDENT);
        entityManager.persist(studentUser);
        testStudent = new Student();
        testStudent.setUser(studentUser);
        testStudent.generateToken();
        testStudent.setTeacher(testTeacher);
        entityManager.persist(testStudent);
        BehaviorType testBehaviorType = new BehaviorType();
        testBehaviorType.setName("Test Behavior");
        testBehaviorType.setPointValue(3);
        testBehaviorType.setActive(true);
        entityManager.persist(testBehaviorType);
        testBragLog = new BragLog();
        testBragLog.setStudent(testStudent);
        testBragLog.setTeacher(testTeacher);
        testBragLog.setPointsGenerated(3);
        testBragLog.setTimestamp(LocalDateTime.now());
        testBragLog.setBehaviors(Set.of(testBehaviorType));
        entityManager.persist(testBragLog);
        entityManager.flush();
    }

    // ===============
    // FIND BY STUDENT
    // ===============
    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT can find brag logs by student")
    void studentCanFindByStudent() {
        assertDoesNotThrow(() -> {
            List<BragLog> result = bragLogDAO.findByStudent(testStudent);
            assertFalse(result.isEmpty());
        });
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER can find brag logs by student")
    void teacherCanFindByStudent() {
        assertDoesNotThrow(() -> {
            List<BragLog> result = bragLogDAO.findByStudent(testStudent);
            assertFalse(result.isEmpty());
        });
    }

    @Test
    @DisplayName("Unauthenticated user cannot find brag logs by student")
    void unauthenticatedCanNotFindByStudent() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> bragLogDAO.findByStudent(testStudent));
    }

    // ========
    // FIND ALL
    // ========
    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT can find all brag logs")
    void studentCanFindAll() {
        assertDoesNotThrow(() -> {
            List<BragLog> result = bragLogDAO.findAll();
            assertFalse(result.isEmpty());
        });
    }

    @Test
    @DisplayName("Unauthenticated user cannot find all brag logs")
    void unauthenticatedCannotFindAll() {
        assertThrows(AuthenticationCredentialsNotFoundException.class,
                () -> bragLogDAO.findAll());
    }

    // ============
    // DELETE TESTS
    // ============
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN can delete brag log")
    void adminCanDeleteBragLog() {
        assertDoesNotThrow(() -> bragLogDAO.delete(testBragLog));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER cannot delete brag log")
    void teacherCannotDeleteBragLog() {
        assertThrows(AccessDeniedException.class,
                () -> bragLogDAO.delete(testBragLog));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT cannot delete brag log")
    void studentCannotDeleteBragLog() {
        assertThrows(AccessDeniedException.class,
                () -> bragLogDAO.delete(testBragLog));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN can delete all brag logs")
    void adminCanDeleteAll() {
        assertDoesNotThrow(() -> bragLogDAO.deleteAll());
    }

    // =====================
    // INTERNAL METHOD TESTS
    // =====================
    @Test
    @DisplayName("Internal findBySyncedToSheetsFalse requires no authentication")
    void internalFindUnsyncedNoAuth() {
        assertDoesNotThrow(() -> {
            List<BragLog> result = bragLogDAO.findBySyncedToSheetsFalse();
            assertFalse(result.isEmpty());
        });
    }

    @Test
    @DisplayName("Internal findByTimestampAfter requires no authentication")
    void internalFindTimestampAfterNoAuth() {
        assertDoesNotThrow(() -> {
            List<BragLog> result = bragLogDAO.findByTimestampAfter(LocalDateTime.now().minusDays(1));
            assertFalse(result.isEmpty());
        });
    }
}
