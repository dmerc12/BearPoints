package com.bearpoints.api.dto;

import com.bearpoints.api.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.rest.core.config.Projection;

/**
 * Projection interface for condensed user information.
 * <p>
 * Provides a summary view of User entities with essential fields only.
 * Used when full user details are not required, especially in nested projections.
 *
 * <p>Fields:
 * <ul>
 *     <li>id: - Unique user identifier</li>
 *     <li>email - User's email address</li>
 *     <li>firstName - User's first name</li>
 *     <li>lastName - User's last name</li>
 *     <li>role - User's role converted to string</li>
 * </ul>
 * @version 1.0
 * @author Dylan Mercer
 */
@Projection(name = "userSummary", types = User.class)
public interface UserSummary {
    Long getId();
    String getEmail();
    String getFirstName();
    String getLastName();

    /**
     * Retrieves the role name as a string
     * @return Role name (e.g.p, "STUDENT", "TEACHER")
     */
    @Value("#{target.role.name()}")
    String getRole();
}
