package com.bearpoints.api.security;

import com.bearpoints.api.dao.UserDAO;
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
 *     <li>Entity-access authorization</li>
 *     <li>Role-based access control</li>
 * </ul>
 *
 * @version 1.0
 * @author Dylan Mercer
 */
@Component("securityUtils")
public class SecurityUtils {
    private final UserDAO userDAO;

    public SecurityUtils(UserDAO userDAO) {
        this.userDAO = userDAO;
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
}
