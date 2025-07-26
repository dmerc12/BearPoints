package com.bearpoints.api.config;

import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TestDataInitializer}
 * <p>Verifies initialization behavior for test teacher accounts in non-production environments:
 * <ul>
 *     <li>Conditional execution based on environment variables</li>
 *     <li>Proper handling of edge cases and error conditions</li>
 *     <li>Correct entity creation and property assignment</li>
 *     <li>Security context management during initialization</li>
 * </ul>
 *
 * <p>Tests validate:
 * <ul>
 *     <li>Initialization is skipped when TEST_TEACHER_EMAIL is unset or blank</li>
 *     <li>No action is taken when test teacher already exists</li>
 *     <li>Teacher is created with correct properties when conditions are met</li>
 *     <li>Security context is cleared after successful creation and exceptions</li>
 *     <li>All database interactions follow expected patterns</li>
 *     <li>Runtime exceptions are properly handled and propagated</li>
 * </ul>
 *
 * @see TestDataInitializer
 * @version 1.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
public class TestDataInitializerTests {
    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private TestDataInitializer testDataInitializer;

    private final String testEmail = "test.teacher@example.com";

    /**
     * Helper method to simulate environment variable setting
     * @param value Value to set for TEST_TEACHER_EMAIL
     */
    private void setEnvVariable(String value) throws Exception {
        Field field = TestDataInitializer.class.getDeclaredField("testTeacherEmail");
        field.setAccessible(true);
        field.set(testDataInitializer, value);
    }

    /**
     * Clears security context and resets environment state before each test
     */
    @BeforeEach
    void setUp() throws Exception {
        SecurityContextHolder.clearContext();
        setEnvVariable(null);
    }

    /**
     * Verifies initialization is skipped when TEST_TEACHER_EMAIL is unset
     * <p>Asserts:
     * <ul>
     *     <li>No database interactions occur</li>
     *     <li>Security context remains cleared</li>
     * </ul>
     */
    @Test
    @DisplayName("Skipped when TEST_TEACHER_EMAIL is not set")
    void skippedWhenTestTeacherEmailIsNotSet() {
        testDataInitializer.run();
        verify(userDAO, never()).findByEmail(any());
        verify(userDAO, never()).save(any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Verifies initialization is skipped when TEST_TEACHER_EMAIL is blank
     * <p>Asserts:
     * <ul>
     *     <li>No database interactions occur</li>
     *     <li>Security context remains cleared</li>
     * </ul>
     */
    @Test
    @DisplayName("Skipped when TEST_TEACHER_EMAIL is blank")
    void skippedWhenTestTeacherEmailIsBlank() throws Exception {
        setEnvVariable("");
        testDataInitializer.run();
        verify(userDAO, never()).findByEmail(any());
        verify(userDAO, never()).save(any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Verifies no action is taken when test teacher already exists
     * <p>Asserts:
     * <ul>
     *     <li>Teacher lookup occurs but no save operation is performed</li>
     *     <li>Security context remains cleared</li>
     * </ul>
     */
    @Test
    @DisplayName("Skipped when teacher already exists")
    void skippedWhenTeacherAlreadyExists() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.of(new User()));
        testDataInitializer.run();
        verify(userDAO, never()).save(any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Verifies teacher creation when all conditions are met
     * <p>Asserts:
     * <ul>
     *     <li>User save operation is executed exactly once</li>
     *     <li>Security context is cleared after execution</li>
     * </ul>
     */
    @Test
    @DisplayName("Creates teacher when conditions are met")
    void createTeacherWhenConditionsAreMet() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        testDataInitializer.run();
        verify(userDAO).save(any(User.class));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Verifies security context is cleared during exception handling
     * <p>Asserts:
     * <ul>
     *     <li>Exceptions during save operations are propagated</li>
     *     <li>Security context is cleared despite operation failure</li>
     * </ul>
     */
    @Test
    @DisplayName("Clears security context on exception")
    void clearSecurityContextOnException() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        doThrow(new RuntimeException("Test exception")).when(userDAO).save(any());
        assertThrows(RuntimeException.class, testDataInitializer::run);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Verifies created teacher has all required properties
     * <p>Asserts created user contains:
     * <ul>
     *     <li>Correct email address from environment variable</li>
     *     <li>First name "Test" and last name "Teacher"</li>
     *     <li>TEACHER role assignment</li>
     *     <li>Associated teacher entity with SECOND grade level</li>
     * </ul>
     */
    @Test
    @DisplayName("Creates teacher with correct properties")
    void createTeacherWithCorrectProperties() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        testDataInitializer.run();
        verify(userDAO).save(argThat(user ->
                testEmail.equals(user.getEmail()) &&
                        "Test".equals(user.getFirstName()) &&
                        "Teacher".equals(user.getLastName()) &&
                        user.getRole() == Role.TEACHER &&
                        user.getTeacher() != null &&
                        user.getTeacher().getGrade() == GradeLevel.SECOND
        ));
    }
}
