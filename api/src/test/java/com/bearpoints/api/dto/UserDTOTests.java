package com.bearpoints.api.dto;

import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link UserDTO} functionality.
 * <p>Verifies:
 * <ul>
 *     <li>Correct mapping from User entity to DTO</li>
 *     <li>All fields are properly populated</li>
 * </ul>
 *
 * @see UserDTO
 * @version 1.0
 * @author Dylan Mercer
 */
public class UserDTOTests {
    @Test
    @DisplayName("UserDTO correctly maps all fields from User entity")
    void shouldMapAllFieldsFromUserEntity() {
        User user =  new User();
        user.setId(1L);
        user.setEmail("test.user@example.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.TEACHER);
        UserDTO dto = new UserDTO(user);
        assertEquals(user.getId(), dto.getId());
        assertEquals(user.getEmail(), dto.getEmail());
        assertEquals(user.getFirstName(), dto.getFirstName());
        assertEquals(user.getLastName(), dto.getLastName());
        assertEquals(user.getRole().name(), dto.getRole());
    }
}
