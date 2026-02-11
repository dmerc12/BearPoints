package com.bearpoints.api.unit.controller;

import com.bearpoints.api.controller.UserController;
import com.bearpoints.api.criteria.UserSearchCriteria;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.UserDTO;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import com.bearpoints.api.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link UserController}.
 * <p>Verifies functionality of user management API endpoints:
 * <ul>
 *     <li>Pagination and sorting parameter handling</li>
 *     <li>Response entity construction and HTTP status codes</li>
 *     <li>Service method invocation with correct parameters</li>
 *     <li>Search and filtering endpoint functionality</li>
 * </ul>
 *
 * @see UserController
 * @version 2.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserController Unit Tests")
public class UserControllerTests {
    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User createUser(Long id, String email, String firstName, String lastName) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(Role.ADMIN);
        return user;
    }

    @Nested
    @DisplayName("GET /api/users - When retrieving all users")
    class WhenRetrievingAllUsers {
        @Test
        @DisplayName("Should return paginated users with default parameters")
        void shouldReturnPaginatedUsersWithDefaultParameters() {
            List<UserDTO> users = List.of(
                    new UserDTO(createUser(1L, "user1@okcps.org", "User1", "User1")),
                    new UserDTO(createUser(2L, "user2@okcps.org", "User2", "User2"))
            );
            Page<UserDTO> userPage = new PageImpl<>(users,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "lastName")),
                    2L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(userPage);
            when(userService.getAllUsers(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = userController
                    .getAllUsers(PageRequest.of(0, 20,
                            Sort.by(Sort.Direction.ASC, "lastName")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().getContent().size());
            verify(userService).getAllUsers(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle custom pagination and sorting parameters")
        void shouldHandleCustomPaginationAndSortingParameters() {
            List<UserDTO> users = List.of(
                    new UserDTO(createUser(1L, "user1@okcps.org", "User1", "User1"))
            );
            Page<UserDTO> userPage = new PageImpl<>(users,
                    PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "email")),
                    15L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(userPage);
            when(userService.getAllUsers(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = userController
                    .getAllUsers(PageRequest.of(1, 10,
                            Sort.by(Sort.Direction.DESC, "email")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(userService).getAllUsers(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle multiple sort parameters")
        void shouldHandleMultipleSortParameters() {
            List<UserDTO> users = List.of(
                    new UserDTO(createUser(1L, "user1@okcps.org", "User1", "User1"))
            );
            Sort multiSort = Sort.by(
                    Sort.Order.asc("lastName"),
                    Sort.Order.desc("firstName"),
                    Sort.Order.asc("email"),
                    Sort.Order.asc("role")
            );
            Page<UserDTO> userPage = new PageImpl<>(users,
                    PageRequest.of(0, 20, multiSort),
                    1L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(userPage);
            when(userService.getAllUsers(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = userController
                    .getAllUsers(PageRequest.of(0, 20, multiSort));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(userService).getAllUsers(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/users/search - When searching users")
    class WhenSearchingUsers {
        @Test
        @DisplayName("Should search users by email")
        void shouldSearchUsersByEmail() {
            String email = "user";
            List<UserDTO> users = List.of(
                    new UserDTO(createUser(1L, email + "1@okcps.org", "User1", "User1"))
            );
            Page<UserDTO> userPage = new PageImpl<>(users,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "email")),
                    1L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(userPage);
            when(userService.searchUsers(any(UserSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = userController.searchUsers(email,
                    null, null, null,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "email")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(userService).searchUsers(any(UserSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search users by first name")
        void shouldSearchUsersByFirstName() {
            String firstName = "John";
            List<UserDTO> users = List.of(
                    new UserDTO(createUser(1L, "john@okcps.org", firstName, "Doe"))
            );
            Page<UserDTO> userPage = new PageImpl<>(users,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "firstName")),
                    1L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(userPage);
            when(userService.searchUsers(any(UserSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = userController.searchUsers(null,
                    firstName, null, null,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "firstName")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(userService).searchUsers(any(UserSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search users by last name")
        void shouldSearchUsersByLastName() {
            String lastName = "Doe";
            List<UserDTO> users = List.of(
                    new UserDTO(createUser(1L, "john@okcps.org", "John", lastName))
            );
            Page<UserDTO> userPage = new PageImpl<>(users,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "lastName")),
                    1L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(userPage);
            when(userService.searchUsers(any(UserSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = userController.searchUsers(null,
                    null, lastName, null,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "lastName")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(userService).searchUsers(any(UserSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search users by role")
        void shouldSearchUsersByRole() {
            String role = "ADMIN";
            List<UserDTO> users = List.of(
                    new UserDTO(createUser(1L, "john@okcps.org", "John", "Doe"))
            );
            Page<UserDTO> userPage = new PageImpl<>(users,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "role")),
                    1L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(userPage);
            when(userService.searchUsers(any(UserSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = userController.searchUsers(null,
                    null, null, role,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "role")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(userService).searchUsers(any(UserSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search users with combined criteria")
        void shouldSearchUsersWithCombinedCriteria() {
            String email = "j";
            String firstName = "John";
            String lastName = "Doe";
            String role = "ADMIN";
            List<UserDTO> users = List.of(
                    new UserDTO(createUser(1L, email + "ohn@okcps.org", firstName, lastName))
            );
            Page<UserDTO> userPage = new PageImpl<>(users,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "firstName")),
                    1L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(userPage);
            when(userService.searchUsers(any(UserSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = userController.searchUsers(email,
                    firstName, lastName, role,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "lastName")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(userService).searchUsers(any(UserSearchCriteria.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/users/{id} - When retrieving user by ID")
    class WhenRetrievingUserById {
        @Test
        @DisplayName("Should return user when found")
        void shouldReturnUserWhenFound() {
            Long userId = 1L;
            UserDTO user = new UserDTO(createUser(userId, "user@okcps.org", "User", "User"));
            when(userService.getUserById(userId)).thenReturn(user);
            ResponseEntity<UserDTO> response = userController.getUserById(userId);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(userId, response.getBody().getId());
            assertEquals("user@okcps.org", response.getBody().getEmail());
            verify(userService).getUserById(userId);
        }
    }

    @Nested
    @DisplayName("POST /api/users - When creating user")
    class WhenCreatingUser {
        @Test
        @DisplayName("Should create new user and return 201 status")
        void shouldCreateNewUserAndReturn201Status() {
            UserDTO userDTO = new UserDTO(createUser(null, "new.user@okcps.org", "New", "User"));
            UserDTO createdUser = new UserDTO(createUser(1L, "new.user@okcps.org", "New", "User"));
            when(userService.createUser(userDTO)).thenReturn(createdUser);
            ResponseEntity<UserDTO> response = userController.createUser(userDTO);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1L, response.getBody().getId());
            assertEquals("new.user@okcps.org", response.getBody().getEmail());
            verify(userService).createUser(userDTO);
        }
    }

    @Nested
    @DisplayName("PUT /api/users/{id} - When updating user")
    class WhenUpdatingUser {
        @Test
        @DisplayName("Should update existing user and return 200 status")
        void shouldUpdateExistingUserAndReturn200Status() {
            Long userId = 1L;
            UserDTO userDTO = new UserDTO(createUser(userId, "updated@okcps.org", "Updated", "User"));
            UserDTO updatedUser = new UserDTO(createUser(userId, "updated@okcps.org", "Updated", "User"));
            when(userService.updateUser(userId, userDTO)).thenReturn(updatedUser);
            ResponseEntity<UserDTO> response = userController.updateUser(userId, userDTO);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("updated@okcps.org", response.getBody().getEmail());
            assertEquals("Updated", response.getBody().getFirstName());
            verify(userService).updateUser(userId, userDTO);
        }
    }

    @Nested
    @DisplayName("DELETE /api/users/{id} - When deleting user")
    class WhenDeletingUser {
        @Test
        @DisplayName("Should delete user and return 204 status")
        void shouldDeleteUserAndReturn204Status() {
            Long userId = 1L;
            ResponseEntity<UserDTO> response = userController.deleteUser(userId);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
            verify(userService).deleteUser(userId);
        }
    }
}
