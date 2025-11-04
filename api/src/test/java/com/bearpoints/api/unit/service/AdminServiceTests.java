package com.bearpoints.api.unit.service;

import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.UserDTO;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import com.bearpoints.api.exception.DuplicateResourceException;
import com.bearpoints.api.exception.UserNotFoundException;
import com.bearpoints.api.service.impl.AdminServiceImpl;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AdminServiceImpl}.
 * <p>Verifies admin user management functionality including CRUD operations and search.
 *
 * @see AdminServiceImpl
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("AdminService Tests")
@ExtendWith(MockitoExtension.class)
public class AdminServiceTests {
    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private AdminServiceImpl adminService;

    private final Pageable pageable = PageRequest.of(0, 10);

    @Nested
    @DisplayName("When retrieving admin users")
    class WhenRetrievingAdminUsers {
        @Test
        @DisplayName("Should retrieve all admin users with pagination")
        void shouldRetrieveAllAdminUsersWithPagination() {
            List<User> adminUsers = List.of(
                    createUser(1L, "admin1@okcps.org", "Admin1", "User1", Role.ADMIN),
                    createUser(2L, "admin2@okcps.org", "Admin2", "User2", Role.ADMIN)
            );
            Page<User> adminPage = new PageImpl<>(adminUsers, pageable, 2L);
            when(userDAO.findByRole(eq(Role.ADMIN), any(Pageable.class))).thenReturn(adminPage);
            PagedResponseDTO<UserDTO> result = adminService.getAllAdmins(pageable);
            assertNotNull(result);
            assertEquals(2, result.getContent().size());
            assertEquals(2L, result.getTotalElements());
            verify(userDAO).findByRole(Role.ADMIN, pageable);
        }

        @Test
        @DisplayName("Should return empty page when no admin users exist")
        void shouldReturnEmptyPageWhenNoAdminUsersExist() {
            Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0L);
            when(userDAO.findByRole(eq(Role.ADMIN), any(Pageable.class))).thenReturn(emptyPage);
            PagedResponseDTO<UserDTO> result = adminService.getAllAdmins(pageable);
            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("When searching admin users")
    class WhenSearchingAdminUsers {
        @Test
        @DisplayName("Should search admin users by email")
        void shouldSearchAdminUsersByEmail() {
            String email = "admin";
            List<User> adminUsers = List.of(
                    createUser(1L, "admin1@okcps.org", "Admin1", "User1", Role.ADMIN)
            );
            Page<User> adminPage = new PageImpl<>(adminUsers, pageable, 1L);
            when(userDAO.findByRoleAndEmailContainingIgnoreCase(eq(Role.ADMIN), eq(email), any(Pageable.class)))
                    .thenReturn(adminPage);
            PagedResponseDTO<UserDTO> result = adminService.searchAdminsByEmail(email, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals("admin1@okcps.org", result.getContent().getFirst().getEmail());
            verify(userDAO).findByRoleAndEmailContainingIgnoreCase(Role.ADMIN, email, pageable);
        }

        @Test
        @DisplayName("Should search admin users by first name")
        void shouldSearchAdminUsersByFirstName() {
            String firstName = "John";
            List<User> adminUsers = List.of(
                    createUser(1L, "john@okcps.org", "John", "Doe", Role.ADMIN)
            );
            Page<User> adminPage = new PageImpl<>(adminUsers, pageable, 1L);
            when(userDAO.findByRoleAndFirstNameContainingIgnoreCase(eq(Role.ADMIN), eq(firstName), any(Pageable.class)))
                    .thenReturn(adminPage);
            PagedResponseDTO<UserDTO> result = adminService.searchAdminsByFirstName(firstName, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals("John", result.getContent().getFirst().getFirstName());
            verify(userDAO).findByRoleAndFirstNameContainingIgnoreCase(Role.ADMIN, firstName, pageable);
        }

        @Test
        @DisplayName("Should search admin users by last name")
        void shouldSearchAdminUsersByLastName() {
            String lastName = "Doe";
            List<User> adminUsers = List.of(
                    createUser(1L, "john@okcps.org", "John", "Doe", Role.ADMIN)
            );
            Page<User> adminPage = new PageImpl<>(adminUsers, pageable, 1L);
            when(userDAO.findByRoleAndLastNameContainingIgnoreCase(eq(Role.ADMIN), eq(lastName), any(Pageable.class)))
                    .thenReturn(adminPage);
            PagedResponseDTO<UserDTO> result = adminService.searchAdminsByLastName(lastName, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals("Doe", result.getContent().getFirst().getLastName());
            verify(userDAO).findByRoleAndLastNameContainingIgnoreCase(Role.ADMIN, lastName, pageable);
        }
    }

    @Nested
    @DisplayName("When retrieving admin user by ID")
    class WhenRetrievingAdminUserById {
        @Test
        @DisplayName("Should return admin user when found")
        void shouldReturnAdminUserWhenFound() {
            Long adminId = 1L;
            User adminUser = createUser(adminId, "admin@okcps.org", "Admin", "User", Role.ADMIN);
            when(userDAO.findById(adminId)).thenReturn(Optional.of(adminUser));
            UserDTO result = adminService.getAdminById(adminId);
            assertNotNull(result);
            assertEquals(adminId, result.getId());
            assertEquals("admin@okcps.org", result.getEmail());
            assertEquals("Admin", result.getFirstName());
            assertEquals("User", result.getLastName());
            assertEquals(Role.ADMIN.name(), result.getRole());
            verify(userDAO).findById(adminId);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when admin user not found")
        void shouldThrowUserNotFoundExceptionWhenAdminUserNotFound() {
            Long adminId = 999L;
            when(userDAO.findById(adminId)).thenReturn(Optional.empty());
            assertThrows(UserNotFoundException.class, () -> adminService.getAdminById(adminId));
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user exists but is not admin")
        void shouldThrowUserNotFoundExceptionWhenUserExistsButIsNotAdmin() {
            Long userId = 1L;
            User studentUser = createUser(userId, "student@okcps.org", "Student", "User", Role.STUDENT);
            when(userDAO.findById(userId)).thenReturn(Optional.of(studentUser));
            assertThrows(UserNotFoundException.class, () -> adminService.getAdminById(userId));
        }
    }

    @Nested
    @DisplayName("When creating admin user")
    class WhenCreatingAdminUser {
        @Test
        @DisplayName("Should create new admin user successfully")
        void shouldCreateNewAdminUserSuccessfully() {
            UserDTO userDTO = new UserDTO(createUser(null, "new.admin@okcps.org", "New", "Admin", Role.ADMIN));
            User savedUser = createUser(1L, "new.admin@okcps.org", "New", "Admin", Role.ADMIN);
            when(userDAO.save(any(User.class))).thenReturn(savedUser);
            UserDTO result = adminService.createAdmin(userDTO);
            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("new.admin@okcps.org", result.getEmail());
            assertEquals("ADMIN", result.getRole());
            verify(userDAO).findByEmail(anyString());
            verify(userDAO).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when email already exists")
        void shouldThrowDuplicateResourceExceptionWhenEmailAlreadyExists() {
            UserDTO userDTO = new UserDTO(null, "existing@okcps.org", "New", "Admin", "ADMIN");
            User existingUser = createUser(1L, "existing@okcps.org", "Existing", "User", Role.TEACHER);
            when(userDAO.findByEmail("existing@okcps.org")).thenReturn(Optional.of(existingUser));
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> adminService.createAdmin(userDTO)
            );
            assertEquals("A user with this email already exists", exception.getMessage());
            verify(userDAO).findByEmail("existing@okcps.org");
            verify(userDAO, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should create admin with same role regardless of input role")
        void shouldCreateAdminWithAdminRoleRegardlessOfInputRole() {
            UserDTO userDTO = new UserDTO(null, "new@okcps.org", "New", "Admin", "STUDENT");
            User savedUser = createUser(1L, "new@okcps.org", "New", "Admin", Role.ADMIN);
            when(userDAO.findByEmail("new@okcps.org")).thenReturn(Optional.empty());
            when(userDAO.save(any(User.class))).thenReturn(savedUser);
            UserDTO result = adminService.createAdmin(userDTO);
            assertEquals("ADMIN", result.getRole());
            verify(userDAO).save(argThat(user -> user.getRole() == Role.ADMIN));
        }
    }

    @Nested
    @DisplayName("When updating admin user")
    class WhenUpdatingAdminUser {
        @Test
        @DisplayName("Should update existing admin user successfully")
        void shouldUpdateExistingAdminUserSuccessfully() {
            Long adminId = 1L;
            User existingAdmin = createUser(adminId, "old@okcps.org", "Old", "Name", Role.ADMIN);
            UserDTO updateDTO = new UserDTO(createUser(adminId, "new@okcps.org", "New", "Last-Name", Role.ADMIN));
            User updatedAdmin = createUser(adminId, "new@okcps.org", "New", "Last-Name", Role.ADMIN);
            when(userDAO.findById(adminId)).thenReturn(Optional.of(existingAdmin));
            when(userDAO.findByEmail("new@okcps.org")).thenReturn(Optional.empty());
            when(userDAO.save(any(User.class))).thenReturn(updatedAdmin);
            UserDTO result = adminService.updateAdmin(adminId, updateDTO);
            assertNotNull(result);
            assertEquals("new@okcps.org", result.getEmail());
            assertEquals("New", result.getFirstName());
            assertEquals("Last-Name",  result.getLastName());
            verify(userDAO).findById(adminId);
            verify(userDAO).findByEmail("new@okcps.org");
            verify(userDAO).save(existingAdmin);
            assertEquals("new@okcps.org", existingAdmin.getEmail());
            assertEquals("New", existingAdmin.getFirstName());
            assertEquals("Last-Name", existingAdmin.getLastName());
        }

        @Test
        @DisplayName("Should update admin user without checking email when email unchanged")
        void shouldUpdateAdminUserWithoutCheckingEmailWhenEmailUnchanged() {
            Long adminId = 1L;
            User existingAdmin = createUser(adminId, "same@okcps.org", "Old", "Name", Role.ADMIN);
            UserDTO updateDTO = new UserDTO(adminId, "same@okcps.org", "New", "Last-Name", "ADMIN");
            User updatedAdmin = createUser(adminId, "same@okcps.org", "New", "Last-Name", Role.ADMIN);
            when(userDAO.findById(adminId)).thenReturn(Optional.of(existingAdmin));
            when(userDAO.save(any(User.class))).thenReturn(updatedAdmin);
            UserDTO result = adminService.updateAdmin(adminId, updateDTO);
            assertNotNull(result);
            assertEquals("same@okcps.org", result.getEmail());
            assertEquals("New", result.getFirstName());
            assertEquals("Last-Name", result.getLastName());
            verify(userDAO, never()).findByEmail(anyString());
            verify(userDAO).findById(adminId);
            verify(userDAO).save(existingAdmin);
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when updating to existing email")
        void shouldThrowDuplicateResourceExceptionWhenUpdatingToExistingEmail() {
            Long adminId = 1L;
            String existingEmail = "existing@okcps.org";
            User existingAdmin = createUser(adminId, "old@okcps.org", "Old", "Name", Role.ADMIN);
            UserDTO updateDTO = new UserDTO(adminId, existingEmail, "New", "Last-Name", "ADMIN");
            User otherUser = createUser(2L, existingEmail, "Other", "User", Role.TEACHER);
            when(userDAO.findById(adminId)).thenReturn(Optional.of(existingAdmin));
            when(userDAO.findByEmail(existingEmail)).thenReturn(Optional.of(otherUser));
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> adminService.updateAdmin(adminId, updateDTO)
            );
            assertEquals("A user with this email already exists", exception.getMessage());
            verify(userDAO).findById(adminId);
            verify(userDAO).findByEmail(existingEmail);
            verify(userDAO, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should allow update when email exists but is the same user")
        void shouldAllowUpdateWhenEmailExistsButIsSameUser() {
            Long adminId = 1L;
            String adminEmail = "admin@okcps.org";
            User existingAdmin = createUser(adminId, adminEmail, "Old", "Name", Role.ADMIN);
            UserDTO updateDTO = new UserDTO(adminId, adminEmail, "New", "Last-Name", "ADMIN");
            User updatedAdmin = createUser(adminId, adminEmail, "New", "Last-Name", Role.ADMIN);
            when(userDAO.findById(adminId)).thenReturn(Optional.of(existingAdmin));
            when(userDAO.save(any(User.class))).thenReturn(updatedAdmin);
            UserDTO result = adminService.updateAdmin(adminId, updateDTO);
            assertNotNull(result);
            assertEquals(adminEmail, result.getEmail());
            assertEquals("New", result.getFirstName());
            assertEquals("Last-Name", result.getLastName());
            verify(userDAO).findById(adminId);
            verify(userDAO, never()).findByEmail(adminEmail);
            verify(userDAO).save(existingAdmin);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when admin user not found")
        void shouldThrowUserNotFoundExceptionWhenAdminUserNotFound() {
            Long adminId = 999L;
            UserDTO updateDTO = new UserDTO(createUser(adminId, "new@okcps.org", "New", "Last-Name", Role.ADMIN));
            when(userDAO.findById(adminId)).thenReturn(Optional.empty());
            assertThrows(
                    UserNotFoundException.class,
                    () -> adminService.updateAdmin(adminId, updateDTO)
            );
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user exists but is student")
        void shouldThrowUserNotFoundExceptionWhenUserExistsButIsStudent() {
            Long userId = 1L;
            User studentUser = createUser(userId, "student@okcps.org", "Student", "User", Role.STUDENT);
            UserDTO updateDTO = new UserDTO(createUser(userId, "new@okcps.org", "New", "Name", Role.ADMIN));
            when(userDAO.findById(userId)).thenReturn(Optional.of(studentUser));
            assertThrows(
                    UserNotFoundException.class,
                    () -> adminService.updateAdmin(userId, updateDTO)
            );
            verify(userDAO).findById(userId);
            verify(userDAO, never()).findByEmail(anyString());
            verify(userDAO, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user exists but is teacher")
        void shouldThrowUserNotFoundExceptionWhenUserExistsButIsTeacher() {
            Long userId = 1L;
            User teacherUser = createUser(userId, "teacher@okcps.org", "Teacher", "User", Role.TEACHER);
            UserDTO updateDTO = new UserDTO(createUser(userId, "new@okcps.org", "New", "Name", Role.ADMIN));
            when(userDAO.findById(userId)).thenReturn(Optional.of(teacherUser));
            assertThrows(
                    UserNotFoundException.class,
                    () -> adminService.updateAdmin(userId, updateDTO)
            );
            verify(userDAO).findById(userId);
            verify(userDAO, never()).findByEmail(anyString());
            verify(userDAO, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("When deleting admin user")
    class WhenDeletingAdminUser {
        @Test
        @DisplayName("Should delete admin user successfully")
        void shouldDeleteAdminUserSuccessfully() {
            Long adminId = 1L;
            User adminUser = createUser(adminId, "admin@okcps.org", "Admin", "User", Role.ADMIN);
            when(userDAO.findById(adminId)).thenReturn(Optional.of(adminUser));
            adminService.deleteAdmin(adminId);
            verify(userDAO).findById(adminId);
            verify(userDAO).delete(adminUser);
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when admin user not found")
        void shouldThrowUserNotFoundExceptionWhenAdminUserNotFound() {
            Long adminId = 999L;
            when(userDAO.findById(adminId)).thenReturn(Optional.empty());
            assertThrows(UserNotFoundException.class, () -> adminService.deleteAdmin(adminId));
        }

        @Test
        @DisplayName("Should throw UserNotFoundException when user exists but is not admin")
        void shouldThrowUserNotFoundExceptionWhenUserExistsButIsNotAdmin() {
            Long userId = 1L;
            User teacherUser = createUser(userId, "teacher@okcps.org", "Teacher", "User", Role.TEACHER);
            when(userDAO.findById(userId)).thenReturn(Optional.of(teacherUser));
            assertThrows(UserNotFoundException.class, () -> adminService.deleteAdmin(userId));
            verify(userDAO).findById(userId);
            verify(userDAO, never()).delete(any(User.class));
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
