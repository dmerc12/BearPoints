package com.bearpoints.api.unit.controller;

import com.bearpoints.api.controller.AdminController;
import com.bearpoints.api.dto.AdminSearchCriteria;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
 * @version 1.0
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
    @DisplayName("When retrieving all admin users")
    class WhenRetrievingAllAdminUsers {
        @Test
        @DisplayName("Should return paginated admin users with default parameters")
        void shouldReturnPaginatedAdminUsersWithDefaultParameters() {
            List<UserDTO> adminUsers = List.of(
                    new UserDTO(createUser(1L, "admin1@okcps.org", "Admin1", "Admin2")),
                    new UserDTO(createUser(2L, "admin2@okcps.org", "Admin2", "Admin2"))
            );
            Page<UserDTO> adminPage = new PageImpl<>(adminUsers, PageRequest.of(0, 20), 2L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(adminPage);
            when(adminService.getAllAdmins(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = adminController.getAllAdmins(0, 20, "lastName,asc");
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
            Page<UserDTO> adminPage = new PageImpl<>(adminUsers, PageRequest.of(1, 10), 15L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(adminPage);
            when(adminService.getAllAdmins(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = adminController.getAllAdmins(1, 10, "email,desc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(adminService).getAllAdmins(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle sort parameter with DESC in uppercase")
        void shouldHandleSortParameterWithDescInUppercase() {
            List<UserDTO> adminUsers = List.of(
                    new UserDTO(createUser(1L, "admin1@okcps.org", "Admin1", "User1"))
            );
            Page<UserDTO> adminPage = new PageImpl<>(adminUsers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(adminPage);
            when(adminService.getAllAdmins(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = adminController.getAllAdmins(0, 20, "firstName,DESC");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(adminService).getAllAdmins(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle sort parameter with mixed case direction")
        void shouldHandleSortParameterWithMixedCaseDirection() {
            List<UserDTO> adminUsers = List.of(
                    new UserDTO(createUser(1L, "admin1@okcps.org", "Admin1", "User1"))
            );
            Page<UserDTO> adminPage = new PageImpl<>(adminUsers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(adminPage);
            when(adminService.getAllAdmins(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = adminController.getAllAdmins(0, 20, "firstName,DeSc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(adminService).getAllAdmins(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle sort parameter with single field (no direction)")
        void shouldHandleSortParameterWithSingleField() {
            List<UserDTO> adminUsers = List.of(
                    new UserDTO(createUser(1L, "admin1@okcps.org", "Admin1", "User1"))
            );
            Page<UserDTO> adminPage = new PageImpl<>(adminUsers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(adminPage);
            when(adminService.getAllAdmins(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = adminController.getAllAdmins(0, 20, "email");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(adminService).getAllAdmins(any(Pageable.class));
        }

        @Test
        @DisplayName("Should handle sort parameter with invalid direction")
        void shouldHandleSortParameterWithInvalidDirection() {
            List<UserDTO> adminUsers = List.of(
                    new UserDTO(createUser(1L, "admin1@okcps.org", "Admin1", "User1"))
            );
            Page<UserDTO> adminPage = new PageImpl<>(adminUsers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(adminPage);
            when(adminService.getAllAdmins(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = adminController.getAllAdmins(0, 20, "lastName,invalid");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            verify(adminService).getAllAdmins(any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("When searching admin users")
    class WhenSearchingAdminUsers {
        @Test
        @DisplayName("Should search admin users by email")
        void shouldSearchAdminUsersByEmail() {
            String email = "admin";
            List<UserDTO> adminUsers = List.of(
                    new UserDTO(createUser(1L, email + "1@okcps.org", "Admin1", "User1"))
            );
            Page<UserDTO> adminPage = new PageImpl<>(adminUsers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(adminPage);
            when(adminService.searchAdmins(any(AdminSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = adminController.searchAdmins(email,
                    null, null, 0, 20, "email");
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
            Page<UserDTO> adminPage = new PageImpl<>(adminUsers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(adminPage);
            when(adminService.searchAdmins(any(AdminSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = adminController.searchAdmins(null,
                    firstName, null, 0, 20, "firstName,asc");
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
            Page<UserDTO> adminPage = new PageImpl<>(adminUsers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(adminPage);
            when(adminService.searchAdmins(any(AdminSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = adminController.searchAdmins(null,
                    null, lastName, 0, 20, "lastName,desc");
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
            Page<UserDTO> adminPage = new PageImpl<>(adminUsers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(adminPage);
            when(adminService.searchAdmins(any(AdminSearchCriteria.class), any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = adminController.searchAdmins(email,
                    firstName, lastName, 0, 20, "lastName,desc,firstName,desc,email,desc");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1, response.getBody().getContent().size());
            verify(adminService).searchAdmins(any(AdminSearchCriteria.class), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("When retrieving admin user by ID")
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
    @DisplayName("When creating admin user")
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
    @DisplayName("When updating admin user")
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
    @DisplayName("When deleting admin user")
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

    @Nested
    @DisplayName("When testing sort parameter splitting")
    class WhenTestingSortParameterSplitting {
        @Test
        @DisplayName("Should handle sort parameter with multiple commas")
        void shouldHandleSortParameterWithMultipleCommas() {
            List<UserDTO> adminUsers = List.of(
                    new UserDTO(createUser(1L, "admin1@okcps.org", "Admin1", "User1"))
            );
            Page<UserDTO> adminPage = new PageImpl<>(adminUsers, PageRequest.of(0, 20), 1L);
            PagedResponseDTO<UserDTO> expectedResponse = PagedResponseDTO.of(adminPage);
            when(adminService.getAllAdmins(any(Pageable.class))).thenReturn(expectedResponse);
            ResponseEntity<PagedResponseDTO<UserDTO>> response = adminController.getAllAdmins(0, 20, "field1,field2,field3");
            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(adminService).getAllAdmins(any(Pageable.class));
        }
    }
}
