package com.bearpoints.api.security;

import com.bearpoints.api.dao.TeacherDAO;
import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.entity.Student;
import com.bearpoints.api.entity.Teacher;
import com.bearpoints.api.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Security utility methods for authorization checks.
 * <p>Provides helper methods for:
 * <ul>
 *     <li>Ownership verification</li>
 *     <li>Classroom ownership verification</li>
 *     <li>Entity-access authorization</li>
 *     <li>Role-based access control</li>
 * </ul>
 *
 * <p>Key Features:
 * <ul>
 *     <li>Determines if a teacher belongs to authenticated user</li>
 *     <li>Verifies if a student is in teacher's classroom</li>
 *     <li>Handles null and edge cases gracefully</li>
 *     <li>Integrates with Spring Security authentication</li>
 * </ul>
 *
 * @version 1.1
 * @author Dylan Mercer
 */
@Component("securityUtils")
public class SecurityUtils {
    private final UserDAO userDAO;
    private final TeacherDAO teacherDAO;

    public SecurityUtils(UserDAO userDAO, TeacherDAO teacherDAO) {
        this.userDAO = userDAO;
        this.teacherDAO = teacherDAO;
    }

    /**
     * Checks if a teacher belongs to the authenticated user.
     *
     * @param teacher Teacher entity to verify
     * @param authentication Current authentication context
     * @return true if the teacher belongs to the authenticated user, false otherwise
     */
    public boolean isOwnTeacher(Teacher teacher, Authentication authentication) {
        if (teacher == null || teacher.getUser() == null || authentication == null) {
            return false;
        }
        String currentEmail = authentication.getName();
        Optional<User> currentUser = userDAO.findByEmail(currentEmail);
        return currentUser.isPresent() && currentUser.get().getId().equals(teacher.getUser().getId());
    }

    /**
     * Checks if a student belongs to the authenticated teacher's classroom.
     *
     * @param student Student entity to verify
     * @param authentication Current authentication context
     * @return true if student is in teacher's classroom, false otherwise
     */
    public boolean isOwnClassroom(Student student, Authentication authentication) {
        if (student == null || student.getTeacher() == null || authentication == null) {
            return false;
        }
        String currentEmail = authentication.getName();
        Optional<Teacher> currentTeacher = teacherDAO.findByUserEmail(currentEmail);
        return currentTeacher.isPresent() && currentTeacher.get().getId().equals(student.getTeacher().getId());
    }
}
