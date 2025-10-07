package com.bearpoints.api.unit.dto;

import com.bearpoints.api.dto.UserProjection;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link UserProjection} projection.
 * <p>Verifies:
 * <ul>
 *     <li>Correct mapping of all user fields</li>
 *     <li>Proper role conversion</li>
 *     <li>Graceful handling of null values in optional fields</li>
 * </ul>
 * @see UserProjection
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("User Projection Tests")
public class UserProjectionTests {
    /**
     * Tests complete user data mapping.
     * <p>Verifies:
     * <ul>
     *     <li>All fields are correctly projected</li>
     *     <li>Role conversion returns enum name as string</li>
     *     <li>Data integrity is maintained</li>
     * </ul>
     */
    @Test
    @DisplayName("Should correctly map all user fields")
    void shouldReturnCorrectUserProjection() {
        User user = new User();
        user.setId(1L);
        user.setEmail("john.doe@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.ADMIN);
        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        UserProjection projection = factory.createProjection(UserProjection.class, user);
        assertEquals(user.getId(), projection.getId());
        assertEquals(user.getEmail(), projection.getEmail());
        assertEquals(user.getFirstName(), projection.getFirstName());
        assertEquals(user.getLastName(), projection.getLastName());
        assertEquals(user.getRole().name(), projection.getRole());
    }

    /**
     * Tests user with partial data.
     * <p>Verifies:
     * <ul>
     *     <li>Required id and role fields are always present</li>
     *     <li>Optional fields return null when not set</li>
     *     <li>No exceptions are thrown with missing data</li>
     * </ul>
     */
    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNullValuesGracefully() {
        User user = new User();
        user.setId(2L);
        user.setRole(Role.STUDENT);
        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        UserProjection projection = factory.createProjection(UserProjection.class, user);
        assertEquals(user.getId(), projection.getId());
        assertNull(projection.getEmail());
        assertNull(projection.getFirstName());
        assertNull(projection.getLastName());
        assertEquals(user.getRole().name(), projection.getRole());
    }
}
