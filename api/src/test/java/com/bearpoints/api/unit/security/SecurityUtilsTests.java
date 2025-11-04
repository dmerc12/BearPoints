package com.bearpoints.api.unit.security;

import com.bearpoints.api.security.SecurityUtils;
import com.bearpoints.api.dao.TeacherDAO;
import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.entity.*;
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
 *     <li>Classroom ownership verification</li>
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
 * @version 1.1
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
public class SecurityUtilsTests {
    @Mock
    private UserDAO userDAO;

    @Mock
    TeacherDAO teacherDAO;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private SecurityUtils securityUtils;

    // =======================
    // isOwnTeacher Test Cases
    // =======================
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

    // =========================
    // isOwnClassroom Test Cases
    // =========================
    @Test
    @DisplayName("isOwnClassroom returns true when student in teacher's classroom")
    void isOwnClassroom_WhenOwner_ReturnsTrue() {
        User teacherUser = new User();
        teacherUser.setId(1L);
        teacherUser.setEmail("teacher@okcps.org");
        teacherUser.setRole(Role.TEACHER);
        Teacher teacher = new Teacher();
        teacher.setId(10L);
        teacher.setUser(teacherUser);
        Student student = new Student();
        student.setTeacher(teacher);
        when(authentication.getName()).thenReturn("teacher@okcps.org");
        when(teacherDAO.findByUserEmail("teacher@okcps.org")).thenReturn(Optional.of(teacher));
        assertTrue(securityUtils.isOwnClassroom(student, authentication));
    }

    @Test
    @DisplayName("isOwnClassroom returns false when student in another classroom")
    void isOwnClassroom_WhenDifferentClass_ReturnsFalse() {
        User currentTeacherUser = new User();
        currentTeacherUser.setId(1L);
        currentTeacherUser.setEmail("current@okcps.org");
        Teacher currentTeacher = new Teacher();
        currentTeacher.setId(10L);
        currentTeacher.setUser(currentTeacherUser);
        User otherTeacherUser = new User();
        otherTeacherUser.setId(2L);
        otherTeacherUser.setEmail("other@okcps.org");
        Teacher otherTeacher = new Teacher();
        otherTeacher.setId(20L);
        otherTeacher.setUser(otherTeacherUser);
        Student student = new Student();
        student.setTeacher(otherTeacher);
        when(authentication.getName()).thenReturn("current@okcps.org");
        when(teacherDAO.findByUserEmail("current@okcps.org")).thenReturn(Optional.of(currentTeacher));
        assertFalse(securityUtils.isOwnClassroom(student, authentication));
    }

    @Test
    @DisplayName("isOwnClassroom returns false when student is null")
    void isOwnClassroom_WhenStudentNull_ReturnsFalse() {
        assertFalse(securityUtils.isOwnClassroom(null, authentication));
    }

    @Test
    @DisplayName("isOwnClassroom returns false when student teacher is null")
    void isOwnClassroom_WhenStudentTeacherNull_ReturnsFalse() {
        Student student = new Student();
        student.setTeacher(null);
        assertFalse(securityUtils.isOwnClassroom(student, authentication));
    }

    @Test
    @DisplayName("isOwnClassroom returns false when authentication is null")
    void isOwnClassroom_WhenAuthenticationNull_ReturnsFalse() {
        Teacher teacher = new Teacher();
        teacher.setId(10L);
        Student student = new Student();
        student.setTeacher(teacher);
        assertFalse(securityUtils.isOwnClassroom(student, null));
    }

    @Test
    @DisplayName("isOwnClassroom returns false when teacher not found")
    void isOwnClassroom_WhenTeacherNotFound_ReturnsFalse() {
        Teacher teacher = new Teacher();
        teacher.setId(10L);
        Student student = new Student();
        student.setTeacher(teacher);
        when(authentication.getName()).thenReturn("unknown@okcps.org");
        when(teacherDAO.findByUserEmail("unknown@okcps.org")).thenReturn(Optional.empty());
        assertFalse(securityUtils.isOwnClassroom(student, authentication));
    }
}
