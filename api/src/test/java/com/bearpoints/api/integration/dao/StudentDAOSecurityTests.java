package com.bearpoints.api.integration.dao;

import com.bearpoints.api.config.SecurityConfig;
import com.bearpoints.api.dao.StudentDAO;
import com.bearpoints.api.entity.*;
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
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Security integration tests for {@link StudentDAO}.
 * <p>Verifies Spring Security annotations enforce:
 * <ul>
 *     <li>Public access to token-based lookups</li>
 *     <li>TEACHER/ADMIN access to email-based lookups</li>
 *     <li>Authenticated access to teacher-based lookups</li>
 *     <li>Authenticated access to all students retrieval</li>
 *     <li>Classroom ownership requirements for write operations</li>
 *     <li>ADMIN-only delete privileges</li>
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
 * @version 1.1
 * @author Dylan Mercer
 */
@DataJpaTest
@Import({SecurityConfig.class, StudentDAOSecurityTests.TestConfig.class})
public class StudentDAOSecurityTests {
    @Autowired
    private StudentDAO studentDAO;

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SecurityUtils securityUtils;

    private Teacher testTeacher;
    private Student testStudent;

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
        entityManager.getEntityManager().createQuery("DELETE FROM Student");
        entityManager.getEntityManager().createQuery("DELETE FROM Teacher");
        entityManager.getEntityManager().createQuery("DELETE FROM User");
        entityManager.flush();
        User teacherUser = new User();
        teacherUser.setFirstName("Test");
        teacherUser.setLastName("Teacher");
        teacherUser.setEmail("test.teacher@okcps.org");
        teacherUser.setRole(Role.TEACHER);
        entityManager.persist(teacherUser);
        testTeacher = new Teacher();
        testTeacher.setUser(teacherUser);
        testTeacher.setGrade(GradeLevel.FIRST);
        entityManager.persist(testTeacher);
        User studentUser = new User();
        studentUser.setFirstName("Test");
        studentUser.setLastName("Student");
        studentUser.setEmail("test.student@okcps.org");
        studentUser.setRole(Role.STUDENT);
        entityManager.persist(studentUser);
        testStudent = new Student();
        testStudent.setUser(studentUser);
        testStudent.setToken("TEST_TOKEN");
        testStudent.setTeacher(testTeacher);
        entityManager.persist(testStudent);
        entityManager.flush();
        when(securityUtils.isOwnClassroom(any(), any())).thenReturn(false);
    }

    // ===============
    // READ OPERATIONS
    // ===============
    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT can find student by token")
    void studentCanFindByToken() {
        assertDoesNotThrow(() -> {
            Optional<Student> result = studentDAO.findByToken(testStudent.getToken());
            assertTrue(result.isPresent());
            assertEquals(result.get().getId(), testStudent.getId());
        });
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER can find student by email")
    void teacherCanFindByEmail() {
        Optional<Student> result = studentDAO.findByUserEmail(testStudent.getUser().getEmail());
        assertTrue(result.isPresent());
        assertEquals(result.get().getId(), testStudent.getId());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT can find students by teacher")
    void studentCanFindByTeacher() {
        assertDoesNotThrow(() -> {
            List<Student> result = studentDAO.findByTeacher(testTeacher);
            assertEquals(1, result.size());
        });
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT can access all students")
    void studentCanFindAllStudents() {
        assertDoesNotThrow(() -> {
            List<Student> result = studentDAO.findAll();
            assertFalse(result.isEmpty());
        });
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER can access all students")
    void teacherCanFindAllStudents() {
        assertDoesNotThrow(() -> {
            List<Student> result = studentDAO.findAll();
            assertFalse(result.isEmpty());
        });
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN can access all students")
    void adminCanFindAllStudents() {
        assertDoesNotThrow(() -> {
            List<Student> result = studentDAO.findAll();
            assertFalse(result.isEmpty());
        });
    }

    @Test
    @DisplayName("Unauthenticated user cannot access students by teacher")
    void unauthenticatedCannotFindByTeacher() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> studentDAO.findByTeacher(testTeacher));
    }

    @Test
    @DisplayName("Unauthenticated user cannot access all students")
    void unauthenticatedCannotFindAllStudents() {
        assertThrows(AuthenticationCredentialsNotFoundException.class, () -> studentDAO.findAll());
    }

    // ===============
    // SAVE OPERATIONS
    // ===============
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN can create student")
    void adminCanCreateStudent() {
        User studentUser = new User();
        studentUser.setFirstName("New");
        studentUser.setLastName("Student");
        studentUser.setEmail("new.student@okcps.org");
        studentUser.setRole(Role.STUDENT);
        entityManager.persist(studentUser);
        Student newStudent = new Student();
        newStudent.setUser(studentUser);
        newStudent.setToken("NEW_TOKEN");
        newStudent.setTeacher(testTeacher);
        assertDoesNotThrow(() -> studentDAO.save(newStudent));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER can create student in own classroom")
    void teacherCanCreateStudentInOwnClass() {
        User studentUser = new User();
        studentUser.setFirstName("New");
        studentUser.setLastName("Student");
        studentUser.setEmail("new.student@okcps.org");
        studentUser.setRole(Role.STUDENT);
        entityManager.persist(studentUser);
        Student newStudent = new Student();
        newStudent.setUser(studentUser);
        newStudent.setToken("NEW_TOKEN");
        newStudent.setTeacher(testTeacher);
        when(securityUtils.isOwnClassroom(eq(newStudent), any())).thenReturn(true);
        assertDoesNotThrow(() -> studentDAO.save(newStudent));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER cannot create student in other classroom")
    void teacherCannotCreateStudentInOtherClass() {
        User teacherUser = new User();
        teacherUser.setFirstName("Other");
        teacherUser.setLastName("Teacher");
        teacherUser.setEmail("other.teacher@okcps.org");
        teacherUser.setRole(Role.TEACHER);
        entityManager.persist(teacherUser);
        Teacher otherTeacher = new Teacher();
        otherTeacher.setUser(teacherUser);
        otherTeacher.setGrade(GradeLevel.FIRST);
        entityManager.persist(otherTeacher);
        entityManager.flush();
        User studentUser = new User();
        studentUser.setFirstName("New");
        studentUser.setLastName("Student");
        studentUser.setEmail("new.student@okcps.org");
        studentUser.setRole(Role.STUDENT);
        entityManager.persist(studentUser);
        Student newStudent = new Student();
        newStudent.setUser(studentUser);
        newStudent.setToken("NEW_TOKEN");
        newStudent.setTeacher(otherTeacher);
        assertThrows(AccessDeniedException.class, () -> studentDAO.save(newStudent));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT cannot create student")
    void studentCannotCreateStudent() {
        User studentUser = new User();
        studentUser.setFirstName("New");
        studentUser.setLastName("Student");
        studentUser.setEmail("new.student@okcps.org");
        studentUser.setRole(Role.STUDENT);
        entityManager.persist(studentUser);
        Student newStudent = new Student();
        newStudent.setUser(studentUser);
        newStudent.setToken("NEW_TOKEN");
        newStudent.setTeacher(testTeacher);
        assertThrows(AccessDeniedException.class, () -> studentDAO.save(newStudent));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN can update any student")
    void adminCanUpdateAnyStudent() {
        testStudent.setPoints(200);
        assertDoesNotThrow(() -> studentDAO.save(testStudent));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER can update student in own classroom")
    void teacherCanUpdateStudentInOwnClass() {
        when(securityUtils.isOwnClassroom(eq(testStudent), any())).thenReturn(true);
        testStudent.setPoints(100);
        assertDoesNotThrow(() -> studentDAO.save(testStudent));
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    @DisplayName("STUDENT cannot update student")
    void studentCannotUpdateStudent() {
        testStudent.setPoints(100);
        assertThrows(AccessDeniedException.class, () -> studentDAO.save(testStudent));
    }

    // =================
    // DELETE OPERATIONS
    // =================
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN can delete student")
    void adminCanDeleteStudent() {
        assertDoesNotThrow(() -> studentDAO.delete(testStudent));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    @DisplayName("TEACHER cannot delete student")
    void teacherCannotDeleteStudent() {
        assertThrows(AccessDeniedException.class, () -> studentDAO.delete(testStudent));
    }
}
