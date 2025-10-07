package com.bearpoints.api.integration.dao;

import com.bearpoints.api.config.SecurityConfig;
import com.bearpoints.api.dao.TeacherDAO;
import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.Teacher;
import com.bearpoints.api.entity.User;
import com.bearpoints.api.security.FirebaseAuthFilter;
import com.bearpoints.api.security.SecurityUtils;
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
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Security integration tests for {@link TeacherDAO}.
 * <p>Verifies Spring Security annotations enforce:
 * <ul>
 *     <li>ADMIN role requirement for delete operations</li>
 *     <li>TEACHER role can update their own profile</li>
 *     <li>ADMIN role can perform all operations</li>
 *     <li>All authenticated users can access read operations</li>
 * </ul>
 *
 * <p>Test Configuration:
 * <ul>
 *     <li>Uses mock SecurityUtils for ownership verification</li>
 *     <li>Initializes test data for each test case</li>
 *     <li>Tests role-based access control scenarios</li>
 * </ul>
 *
 * @see WithMockUser
 * @version 1.0
 * @author Dylan Mercer
 */
@DataJpaTest
@Import({SecurityConfig.class, TeacherDAOSecurityTests.TestConfig.class})
public class TeacherDAOSecurityTests {
    @Autowired
    private TeacherDAO teacherDAO;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SecurityUtils securityUtils;

    private Teacher testTeacher;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public FirebaseAuthFilter firebaseAuthFilter() {
            return mock(FirebaseAuthFilter.class);
        }

        @Bean
        public SecurityUtils securityUtils() {
            return mock(SecurityUtils.class);
        }
    }

    @BeforeEach
    void setup() {
        entityManager.getEntityManager().createQuery("DELETE FROM Teacher").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM User").executeUpdate();
        entityManager.flush();
        User teacherUser = new User();
        teacherUser.setFirstName("Test");
        teacherUser.setLastName("Teacher");
        teacherUser.setEmail("test.teacher@okcps.org");
        teacherUser.setRole(Role.TEACHER);
        entityManager.persist(teacherUser);
        testTeacher = new Teacher();
        testTeacher.setUser(teacherUser);
        testTeacher.setGrade(GradeLevel.FOURTH);
        entityManager.persist(testTeacher);
        entityManager.flush();
        when(securityUtils.isOwnTeacher(any(), any())).thenReturn(false);
    }

    // ==========
    // READ TESTS
    // ==========
    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT can find teacher by email")
    void studentCanFindByEmail() {
        assertDoesNotThrow(() -> {
            Optional<Teacher> result = teacherDAO.findByUserEmail(testTeacher.getUser().getEmail());
            assertTrue(result.isPresent());
        });
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER can find teacher by email")
    void teacherCanFindByEmail() {
        assertDoesNotThrow(() -> {
            Optional<Teacher> result = teacherDAO.findByUserEmail(testTeacher.getUser().getEmail());
            assertTrue(result.isPresent());
        });
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN can find teacher by email")
    void adminCanFindByEmail() {
        assertDoesNotThrow(() -> {
            Optional<Teacher> result = teacherDAO.findByUserEmail(testTeacher.getUser().getEmail());
            assertTrue(result.isPresent());
        });
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT can find teacher by grade")
    void studentCanFindByGrade() {
        assertDoesNotThrow(() -> {
            List<Teacher> result = teacherDAO.findByGrade(testTeacher.getGrade());
            assertFalse(result.isEmpty());
        });
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT can access all teachers")
    void studentCanFindAllTeachers() {
        assertDoesNotThrow(() -> {
            List<Teacher> result = teacherDAO.findAll();
            assertFalse(result.isEmpty());
        });
    }

    // ==========
    // SAVE TESTS
    // ==========
    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER can update own profile")
    void teacherCanUpdateOwnProfile() {
        when(securityUtils.isOwnTeacher(eq(testTeacher), any())).thenReturn(true);
        testTeacher.setGrade(GradeLevel.FIRST);
        assertDoesNotThrow(() -> teacherDAO.save(testTeacher));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER cannot update other teacher's profile")
    void teacherCannotUpdateOtherProfile() {
        User otherUser = new User();
        otherUser.setFirstName("Other");
        otherUser.setLastName("User");
        otherUser.setEmail("other.teacher@okcps.org");
        otherUser.setRole(Role.TEACHER);
        entityManager.persist(otherUser);
        Teacher otherTeacher = new Teacher();
        otherTeacher.setUser(otherUser);
        otherTeacher.setGrade(GradeLevel.SECOND);
        entityManager.persist(otherTeacher);
        entityManager.flush();
        otherTeacher.setGrade(GradeLevel.K);
        assertThrows(AccessDeniedException.class, () -> teacherDAO.save(otherTeacher));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER cannot create new teacher")
    void teacherCannotCreateTeacher() {
        Teacher newTeacher = new Teacher();
        User user = new User();
        user.setFirstName("New");
        user.setLastName("Teacher");
        user.setEmail("new.teacher@okcps.org");
        user.setRole(Role.TEACHER);
        entityManager.persist(user);
        newTeacher.setUser(user);
        newTeacher.setGrade(GradeLevel.SECOND);
        assertThrows(AccessDeniedException.class, () -> teacherDAO.save(newTeacher));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN can create teacher")
    void adminCanCreateTeacher() {
        Teacher newTeacher = new Teacher();
        User user = new User();
        user.setFirstName("Admin");
        user.setLastName("Create");
        user.setEmail("admin.create@okcps.org");
        user.setRole(Role.TEACHER);
        entityManager.persist(user);
        newTeacher.setUser(user);
        newTeacher.setGrade(GradeLevel.K);
        assertDoesNotThrow(() -> teacherDAO.save(newTeacher));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT cannot save teacher")
    void studentCannotSaveTeacher() {
        Teacher newTeacher = new Teacher();
        assertThrows(AccessDeniedException.class, () -> teacherDAO.save(newTeacher));
    }

    // ============
    // DELETE TESTS
    // ============
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN can delete teacher")
    void adminCanDeleteTeacher() {
        assertDoesNotThrow(() -> teacherDAO.delete(testTeacher));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER cannot delete teacher")
    void teacherCannotDeleteTeacher() {
        assertThrows(AccessDeniedException.class, () -> teacherDAO.delete(testTeacher));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT cannot delete teacher")
    void studentCannotDeleteTeacher() {
        assertThrows(AccessDeniedException.class, () -> teacherDAO.delete(testTeacher));
    }
}
