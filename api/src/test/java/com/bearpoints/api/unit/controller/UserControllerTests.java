package com.bearpoints.api.unit.controller;

import com.bearpoints.api.controller.UserController;
import com.bearpoints.api.dto.UserDTO;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import com.bearpoints.api.security.FirebaseUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserController}.
 * <p>Verifies functionality of user-related API endpoints:
 * <ul>
 *     <li>Authentication principal handling</li>
 *     <li>Response entity construction</li>
 *     <li>User data mapping to DTO</li>
 *     <li>Validation error handling</li>
 * </ul>
 *
 * <p>Tests validate that:
 * <ul>
 *     <li>Authenticated users receive their details in the expected format</li>
 *     <li>Proper HTTP status codes are returned for all scenarios</li>
 *     <li>Data mapping between entities and DTOs is correct</li>
 *     <li>Validation errors are handled automatically by Spring</li>
 * </ul>
 *
 * @see UserController
 * @version 1.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
public class UserControllerTests {
    private UserController userController;

    private User createValidUser() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test.user@okcps.org");
        user.setRole(Role.ADMIN);
        return user;
    }

    @BeforeEach
    public void setup() {
        userController = new UserController();
    }

    /**
     * Tests that authenticated users receive their details correctly.
     * <p>Verifies:
     * <ul>
     *     <li>HTTP 200 OK status</li>
     *     <li>Response body contains UserDTO</li>
     *     <li>All user fields are correctly mapped</li>
     * </ul>
     */
    @Test
    @DisplayName("GET /me returns authenticated user details")
    void getCurrentUser_ReturnsAuthenticatedUserDetails() {
        User user = createValidUser();
        FirebaseUserDetails userDetails = new FirebaseUserDetails(user);
        ResponseEntity<UserDTO> response = userController.getCurrentUser(userDetails);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        UserDTO userDTO = response.getBody();
        assertEquals(user.getId(), userDTO.getId());
        assertEquals(user.getFirstName(), userDTO.getFirstName());
        assertEquals(user.getLastName(), userDTO.getLastName());
        assertEquals(user.getEmail(), userDTO.getEmail());
        assertEquals(user.getRole(), userDTO.getRole());
    }

    /**
     * Tests handling of invalid user details.
     * <p>Verifies:
     * <ul>
     *     <li>Controller throws IllegalStateException</li>
     *     <li>Exception message indicates invalid user details</li>
     * </ul>
     */
    @Test
    @DisplayName("Invalid user details throws illegal state exception")
    void getCurrentUser_InvalidUserDetails_ThrowsException() {
        FirebaseUserDetails mockDetails = mock(FirebaseUserDetails.class);
        when(mockDetails.getUser()).thenReturn(null);
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> userController.getCurrentUser(mockDetails));
        assertEquals("Invalid user details", exception.getMessage());
    }
}
