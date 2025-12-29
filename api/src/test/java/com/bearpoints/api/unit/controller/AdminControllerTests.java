package com.bearpoints.api.unit.controller;

import com.bearpoints.api.controller.AdminController;
import com.bearpoints.api.criteria.AdminSearchCriteria;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.UserDTO;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import com.bearpoints.api.service.AdminService;
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
 * Unit tests for {@link AdminController}.
 * <p>Verifies functionality of admin management API endpoints:
 * <ul>
 *     <li>Pagination and sorting parameter handling</li>
 *     <li>Response entity construction and HTTP status codes</li>
 *     <li>Service method invocation with correct parameters</li>
 *     <li>Search and filtering endpoint functionality</li>
 * </ul>
 *
 * @see AdminController
 * @version 1.1
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdminController Unit Tests")
public class AdminControllerTests {
    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

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
    @DisplayName("GET /api/admins - When retrieving all admin users")
    class WhenRetrievingAllAdminUsers {
        @Test
        @DisplayName("Should return paginated admin users with default parameters")
        void shouldReturnPaginatedAdminUsersWithDefaultParameters() {
            List<UserDTO> adminUsers = List.of(
                    new UserDTO(createUser(1L, "admin1@okcps.org", "Admin1", "Admin2")),
                    new UserDTO(createUser(2L, "admin2@okcps.org", "Admin2", "Admin2"))
            );
            Page<UserDTO> adminPage = new PageImpl<>(adminUsers,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "lastName")),
                    2L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(adminPage);
            when(adminService.getAllAdmins(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = adminController
                    .getAllAdmins(PageRequest.of(0, 20,
                            Sort.by(Sort.Direction.ASC, "lastName")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(2, response.getBody().getContent().size());
            verify(adminService).getAllAdmins(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle custom pagination and sorting parameters")
        void shouldHandleCustomPaginationAndSortingParameters() {
            List<UserDTO> adminUsers = List.of(
                    new UserDTO(createUser(1L, "admin1@okcps.org", "Admin1", "User1"))
            );
            Page<UserDTO> adminPage = new PageImpl<>(adminUsers,
                    PageRequest.of(1, 10, Sort.by(Sort.Direction.DESC, "email")),
                    15L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(adminPage);
            when(adminService.getAllAdmins(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = adminController
                    .getAllAdmins(PageRequest.of(1, 10,
                            Sort.by(Sort.Direction.DESC, "email")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(adminService).getAllAdmins(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle multiple sort parameters")
        void shouldHandleMultipleSortParameters() {
            List<UserDTO> adminUsers = List.of(
                    new UserDTO(createUser(1L, "admin1@okcps.org", "Admin1", "User1"))
            );
            Sort multiSort = Sort.by(
                    Sort.Order.asc("lastName"),
                    Sort.Order.desc("firstName"),
                    Sort.Order.asc("email")
            );
            Page<UserDTO> adminPage = new PageImpl<>(adminUsers,
                    PageRequest.of(0, 20, multiSort),
                    1L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(adminPage);
            when(adminService.getAllAdmins(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = adminController
                    .getAllAdmins(PageRequest.of(0, 20, multiSort));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(adminService).getAllAdmins(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/admins/search - When searching admin users")
    class WhenSearchingAdminUsers {
        @Test
        @DisplayName("Should search admin users by email")
        void shouldSearchAdminUsersByEmail() {
            String email = "admin";
            List<UserDTO> adminUsers = List.of(
                    new UserDTO(createUser(1L, email + "1@okcps.org", "Admin1", "User1"))
            );
            Page<UserDTO> adminPage = new PageImpl<>(adminUsers,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "email")),
                    1L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(adminPage);
            when(adminService.searchAdmins(any(AdminSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = adminController.searchAdmins(email,
                    null, null,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "email")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(adminService).searchAdmins(any(AdminSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search admin users by first name")
        void shouldSearchAdminUsersByFirstName() {
            String firstName = "John";
            List<UserDTO> adminUsers = List.of(
                    new UserDTO(createUser(1L, "john@okcps.org", firstName, "Doe"))
            );
            Page<UserDTO> adminPage = new PageImpl<>(adminUsers,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "firstName")),
                    1L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(adminPage);
            when(adminService.searchAdmins(any(AdminSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = adminController.searchAdmins(null,
                    firstName, null,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "firstName")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(adminService).searchAdmins(any(AdminSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search admin users by last name")
        void shouldSearchAdminUsersByLastName() {
            String lastName = "Doe";
            List<UserDTO> adminUsers = List.of(
                    new UserDTO(createUser(1L, "john@okcps.org", "John", lastName))
            );
            Page<UserDTO> adminPage = new PageImpl<>(adminUsers,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "lastName")),
                    1L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(adminPage);
            when(adminService.searchAdmins(any(AdminSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = adminController.searchAdmins(null,
                    null, lastName,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "lastName")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(adminService).searchAdmins(any(AdminSearchCriteria.class), any(Pageable.class));
        }

        @Test
        @DisplayName("Should search admin users with combined criteria")
        void shouldSearchAdminUsersWithCombinedCriteria() {
            String email = "j";
            String firstName = "John";
            String lastName = "Doe";
            List<UserDTO> adminUsers = List.of(
                    new UserDTO(createUser(1L, email + "ohn@okcps.org", firstName, lastName))
            );
            Page<UserDTO> adminPage = new PageImpl<>(adminUsers,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "firstName")),
                    1L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(adminPage);
            when(adminService.searchAdmins(any(AdminSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = adminController.searchAdmins(email,
                    firstName, lastName,
                    PageRequest.of(0, 20, Sort.by(Sort.Direction.ASC, "lastName")));
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(adminService).searchAdmins(any(AdminSearchCriteria.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/admins/{id} - When retrieving admin user by ID")
    class WhenRetrievingAdminUserById {
        @Test
        @DisplayName("Should return admin user when found")
        void shouldReturnAdminUserWhenFound() {
            Long adminId = 1L;
            UserDTO adminUser = new UserDTO(createUser(adminId, "admin@okcps.org", "Admin", "User"));
            when(adminService.getAdminById(adminId)).thenReturn(adminUser);
            ResponseEntity<UserDTO> response = adminController.getAdminById(adminId);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(adminId, response.getBody().getId());
            assertEquals("admin@okcps.org", response.getBody().getEmail());
            verify(adminService).getAdminById(adminId);
        }
    }

    @Nested
    @DisplayName("POST /api/admins - When creating admin user")
    class WhenCreatingAdminUser {
        @Test
        @DisplayName("Should create new admin user and return 201 status")
        void shouldCreateNewAdminUserAndReturn201Status() {
            UserDTO userDTO = new UserDTO(createUser(null, "new.admin@okcps.org", "New", "Admin"));
            UserDTO createdAdmin = new UserDTO(createUser(1L, "new.admin@okcps.org", "New", "Admin"));
            when(adminService.createAdmin(userDTO)).thenReturn(createdAdmin);
            ResponseEntity<UserDTO> response = adminController.createAdmin(userDTO);
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1L, response.getBody().getId());
            assertEquals("new.admin@okcps.org", response.getBody().getEmail());
            verify(adminService).createAdmin(userDTO);
        }
    }

    @Nested
    @DisplayName("PUT /api/admins/{id} - When updating admin user")
    class WhenUpdatingAdminUser {
        @Test
        @DisplayName("Should update existing admin user and return 200 status")
        void shouldUpdateExistingAdminUserAndReturn200Status() {
            Long adminId = 1L;
            UserDTO userDTO = new UserDTO(createUser(adminId, "updated@okcps.org", "Updated", "Admin"));
            UserDTO updatedAdmin = new UserDTO(createUser(adminId, "updated@okcps.org", "Updated", "Admin"));
            when(adminService.updateAdmin(adminId, userDTO)).thenReturn(updatedAdmin);
            ResponseEntity<UserDTO> response = adminController.updateAdmin(adminId, userDTO);
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals("updated@okcps.org", response.getBody().getEmail());
            assertEquals("Updated", response.getBody().getFirstName());
            verify(adminService).updateAdmin(adminId, userDTO);
        }
    }

    @Nested
    @DisplayName("DELETE /api/admins/{id} - When deleting admin user")
    class WhenDeletingAdminUser {
        @Test
        @DisplayName("Should delete admin user and return 204 status")
        void shouldDeleteAdminUserAndReturn204Status() {
            Long adminId = 1L;
            ResponseEntity<UserDTO> response = adminController.deleteAdmin(adminId);
            assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
            assertNull(response.getBody());
            verify(adminService).deleteAdmin(adminId);
        }
    }
}
