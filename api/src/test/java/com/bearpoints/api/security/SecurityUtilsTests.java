package com.bearpoints.api.security;

import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.entity.Teacher;
import com.bearpoints.api.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SecurityUtils}.
 * <p>Verifies the security utility methods for:
 * <ul>
 *     <li>Teacher ownership verification</li>
 *     <li>Edge case handling (null inputs, missing users)</li>
 *     <li>Authentication context validation</li>
 * </ul>
 *
 * <p>Test Scenarios:
 * <ul>
 *     <li>Positive ownership verification</li>
 *     <li>Negative ownership cases</li>
 *     <li>Null and invalid input handling</li>
 *     <li>User not found scenarios</li>
 * </ul>
 *
 * @see SecurityUtils
 * @version 1.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
public class SecurityUtilsTests {
    @Mock
    private UserDAO userDAO;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SecurityUtils securityUtils;

    @Test
    @DisplayName("isOwnTeacher returns true when teacher belongs to authenticated user")
    void isOwnTeacher_WhenOwner_ReturnsTrue() {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("teacher@okcps.org");
        currentUser.setFirstName("John");
        currentUser.setLastName("Doe");
        Teacher teacher = new Teacher();
        teacher.setUser(currentUser);
        when(authentication.getName()).thenReturn("teacher@okcps.org");
        when(userDAO.findByEmail("teacher@okcps.org")).thenReturn(Optional.of(currentUser));
        boolean result = securityUtils.isOwnTeacher(teacher, authentication);
        assertTrue(result);
    }

    @Test
    @DisplayName("isOwnTeacher returns false when teacher does not belong to authenticated user")
    void isOwnTeacher_WhenNotOwner_ReturnsFalse() {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("current@okcps.org");
        currentUser.setFirstName("John");
        currentUser.setLastName("Doe");
        User teacherUser = new User();
        teacherUser.setId(2L);
        teacherUser.setEmail("teacher@okcps.org");
        teacherUser.setFirstName("Jane");
        teacherUser.setLastName("Doe");
        Teacher teacher = new Teacher();
        teacher.setUser(teacherUser);
        when(authentication.getName()).thenReturn("current@okcps.org");
        when(userDAO.findByEmail("current@okcps.org")).thenReturn(Optional.of(currentUser));
        boolean result = securityUtils.isOwnTeacher(teacher, authentication);
        assertFalse(result);
    }

    @Test
    @DisplayName("isOwnTeacher returns false when teacher is null")
    void isOwnTeacher_WhenTeacherNull_ReturnsFalse() {
        boolean result = securityUtils.isOwnTeacher(null, authentication);
        assertFalse(result);
    }

    @Test
    @DisplayName("isOwnTeacher returns false when teacher user is null")
    void isOwnTeacher_WhenTeacherUserNull_ReturnsFalse() {
        Teacher teacher = new Teacher();
        teacher.setUser(null);
        boolean result = securityUtils.isOwnTeacher(teacher, authentication);
        assertFalse(result);
    }

    @Test
    @DisplayName("isOwnTeacher returns false when authentication is null")
    void isOwnTeacher_WhenAuthenticationNull_ReturnsFalse() {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("teacher@okcps.org");
        currentUser.setFirstName("John");
        currentUser.setLastName("Doe");
        Teacher teacher = new Teacher();
        teacher.setUser(currentUser);
        boolean result = securityUtils.isOwnTeacher(teacher, null);
        assertFalse(result);
    }

    @Test
    @DisplayName("isOwnTeacher returns false when authenticated user not found")
    void isOwnTeacher_WhenUserNotFound_ReturnsFalse() {
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setEmail("teacher@okcps.org");
        currentUser.setFirstName("John");
        currentUser.setLastName("Doe");
        Teacher teacher = new Teacher();
        teacher.setUser(currentUser);
        when(authentication.getName()).thenReturn("unknown@okcps.org");
        when(userDAO.findByEmail("unknown@okcps.org")).thenReturn(Optional.empty());
        boolean result = securityUtils.isOwnTeacher(teacher, authentication);
        assertFalse(result);
    }
}
