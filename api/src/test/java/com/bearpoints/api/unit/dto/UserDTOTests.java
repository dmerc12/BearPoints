package com.bearpoints.api.unit.dto;

import com.bearpoints.api.dto.UserDTO;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link UserDTO} functionality.
 * <p>Verifies:
 * <ul>
 *     <li>Correct mapping from User entity to DTO</li>
 *     <li>All fields are properly populated</li>
 *     <li>Edge cases and different role mapping</li>
 * </ul>
 *
 * @see UserDTO
 * @version 1.1
 * @author Dylan Mercer
 */
@DisplayName("UserDTO Tests")
public class UserDTOTests {
    @Nested
    @DisplayName("When mapping from User entity")
    class WhenMappingFromUserEntity {
        @Test
        @DisplayName("Should correctly maps all fields from User entity")
        void shouldMapAllFieldsFromUserEntity() {
            User user = createUser(1L, "test.user@example.com", "Test", "User", Role.TEACHER);
            UserDTO dto = new UserDTO(user);
            assertEquals(user.getId(), dto.getId());
            assertEquals(user.getEmail(), dto.getEmail());
            assertEquals(user.getFirstName(), dto.getFirstName());
            assertEquals(user.getLastName(), dto.getLastName());
            assertEquals(user.getRole().name(), dto.getRole());
        }

        @Test
        @DisplayName("Should correctly map STUDENT role")
        void shouldCorrectlyMapStudentRole() {
            User user = createUser(2L, "student@okcps.org", "John", "Doe", Role.STUDENT);
            UserDTO dto = new UserDTO(user);
            assertEquals(user.getId(), dto.getId());
            assertEquals(user.getFirstName(), dto.getFirstName());
            assertEquals(user.getLastName(), dto.getLastName());
            assertEquals(user.getEmail(),  dto.getEmail());
            assertEquals(user.getRole().name(), dto.getRole());
        }

        @Test
        @DisplayName("Should correctly map TEACHER role")
        void shouldCorrectlyMapTeacherRole() {
            User user = createUser(3L, "teacher@okcps.org", "John", "Doe", Role.TEACHER);
            UserDTO dto = new UserDTO(user);
            assertEquals(user.getId(), dto.getId());
            assertEquals(user.getFirstName(), dto.getFirstName());
            assertEquals(user.getLastName(), dto.getLastName());
            assertEquals(user.getEmail(),  dto.getEmail());
            assertEquals(user.getRole().name(), dto.getRole());
        }

        @Test
        @DisplayName("Should correctly map ADMIN role")
        void shouldCorrectlyMapAdminRole() {
            User user = createUser(4L, "admin@okcps.org", "John", "Doe", Role.ADMIN);
            UserDTO dto = new UserDTO(user);
            assertEquals(user.getId(), dto.getId());
            assertEquals(user.getFirstName(), dto.getFirstName());
            assertEquals(user.getLastName(), dto.getLastName());
            assertEquals(user.getEmail(),  dto.getEmail());
            assertEquals(user.getRole().name(), dto.getRole());
        }

        @Test
        @DisplayName("Should handle user with null ID")
        void shouldHandleUserWithNullId() {
            User user = createUser(null, "user@okcps.org", "First", "Last", Role.STUDENT);
            UserDTO dto = new UserDTO(user);
            assertNull(dto.getId());
            assertEquals(user.getEmail(), dto.getEmail());
            assertEquals(user.getFirstName(), dto.getFirstName());
            assertEquals(user.getLastName(), dto.getLastName());
            assertEquals(user.getRole().name(), dto.getRole());
        }
    }

    @Nested
    @DisplayName("When validating field consistency")
    class WhenValidatingFieldConsistency {
        @Test
        @DisplayName("Should maintain consistent field values across multiple instances")
        void shouldMaintainConsistentFieldValuesAcrossMultipleInstances() {
            User user = createUser(5L, "consistent@okcps.org", "Consistent", "User", Role.TEACHER);
            UserDTO dto1 = new UserDTO(user);
            UserDTO dto2 = new UserDTO(user);
            assertEquals(dto1.getId(), dto2.getId());
            assertEquals(dto1.getFirstName(), dto2.getFirstName());
            assertEquals(dto1.getLastName(), dto2.getLastName());
            assertEquals(dto1.getEmail(), dto2.getEmail());
            assertEquals(dto1.getRole(), dto2.getRole());
        }

        @Test
        @DisplayName("Should reflect changes in underlying user entity")
        void shouldReflectChangesInUnderlyingUserEntity() {
            String originalEmail = "original@okps.org";
            String originalName = "Original";
            Role originalRole = Role.STUDENT;
            User user = createUser(6L, originalEmail, originalName, originalName, originalRole);
            UserDTO originalDTO = new UserDTO(user);
            String updatedName = "Updated";
            String updatedEmail = "updated@okcps.org";
            Role updatedRole = Role.TEACHER;
            user.setFirstName(updatedName);
            user.setLastName(updatedName);
            user.setEmail(updatedEmail);
            user.setRole(updatedRole);
            UserDTO updatedDTO = new UserDTO(user);
            assertEquals(originalDTO.getId(), updatedDTO.getId());
            assertEquals(updatedName, updatedDTO.getFirstName());
            assertEquals(updatedName, updatedDTO.getLastName());
            assertEquals(updatedEmail, updatedDTO.getEmail());
            assertEquals(updatedRole.name(), updatedDTO.getRole());
            assertEquals(originalName, originalDTO.getFirstName());
            assertEquals(originalName, originalDTO.getLastName());
            assertEquals(originalEmail, originalDTO.getEmail());
            assertEquals(originalRole.name(), originalDTO.getRole());
        }
    }

    private User createUser(Long id, String email, String firstName, String lastName, Role role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        return user;
    }
}
