package com.bearpoints.api.unit.service;

import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.criteria.UserSearchCriteria;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.UserDTO;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import com.bearpoints.api.exception.DuplicateResourceException;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.service.impl.UserServiceImpl;
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
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link UserServiceImpl}.
 * <p>Verifies user management functionality including CRUD operations and search.
 *
 * @see UserServiceImpl
 * @version 2.3
 * @author Dylan Mercer
 */
@DisplayName("UserService Tests")
@ExtendWith(MockitoExtension.class)
public class UserServiceTests {
    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private UserServiceImpl userService;

    private final Pageable pageable = PageRequest.of(0, 10);

    @Nested
    @SuppressWarnings("unchecked")
    @DisplayName("When retrieving users")
    class WhenRetrievingUsers {
        @Test
        @DisplayName("Should return only allowed users (ADMIN, STAFF, and PARA")
        void shouldReturnOnlyAllowedUsers() {
            List<User> users = List.of(
                    createUser(1L, "admin1@okcps.org", "Admin1", "One", Role.ADMIN),
                    createUser(5L, "para@okcps.org", "Para1", "One", Role.PARA),
                    createUser(4L, "staff@okcps.org", "Staff1", "One", Role.STAFF),
                    createUser(2L, "teacher@okcps.org", "Teacher", "One", Role.TEACHER),
                    createUser(3L, "student@okcps.org", "Student", "One", Role.STUDENT)
            );
            List<User> filtered = List.of(users.getFirst(), users.get(1), users.get(2));
            Page<User> userPage = new PageImpl<>(filtered, pageable, 3L);
            when(userDAO.findAll(any(Specification.class), eq(pageable))).thenReturn(userPage);
            PagedResponseDTO<UserDTO> result = userService.getAllUsers(pageable);
            assertNotNull(result);
            assertEquals(3, result.getContent().size());
            assertEquals(3L, result.getTotalElements());
            verify(userDAO).findAll(any(Specification.class), eq(pageable));
            verify(userDAO, never()).findAll(eq(pageable));
        }

        @Test
        @DisplayName("Should return empty page when no users exist")
        void shouldReturnEmptyPageWhenNoUsersExist() {
            Page<User> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0L);
            when(userDAO.findAll(any(Specification.class), eq(pageable))).thenReturn(emptyPage);
            PagedResponseDTO<UserDTO> result = userService.getAllUsers(pageable);
            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @SuppressWarnings("unchecked")
    @DisplayName("When searching users with criteria")
    class WhenSearchingUsers {
        @Test
        @DisplayName("Should search users with email criteria")
        void shouldSearchUsersWithEmailCriteria() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setEmail("user1@okcps.org");
            List<User> users = List.of(
                    createUser(1L, "user1@okcps.org", "User1", "User1", Role.ADMIN)
            );
            Page<User> userPage = new PageImpl<>(users, pageable, 1L);
            when(userDAO.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(userPage);
            PagedResponseDTO<UserDTO> result = userService.searchUsers(criteria, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(userDAO).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should search users with first name criteria")
        void shouldSearchUsersWithFirstNameCriteria() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setFirstName("John");
            List<User> users = List.of(
                    createUser(1L, "john@okcps.org", "John", "Doe", Role.ADMIN)
            );
            Page<User> userPage = new PageImpl<>(users, pageable, 1L);
            when(userDAO.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(userPage);
            PagedResponseDTO<UserDTO> result = userService.searchUsers(criteria, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(userDAO).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should search users with last name criteria")
        void shouldSearchUsersWithLastNameCriteria() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setLastName("Doe");
            List<User> users = List.of(
                    createUser(1L, "john@okcps.org", "John", "Doe", Role.ADMIN)
            );
            Page<User> userPage = new PageImpl<>(users, pageable, 1L);
            when(userDAO.findAll(any(Specification.class), any(Pageable.class)))
                    .thenReturn(userPage);
            PagedResponseDTO<UserDTO> result = userService.searchUsers(criteria, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(userDAO).findAll(any(Specification.class), eq(pageable));
        }

        @Test
        @DisplayName("Should return all users with no criteria specified")
        void shouldReturnAllUsersWithNoCriteriaSpecified() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            List<User> users = List.of(
                    createUser(1L, "john@okcps.org", "John", "Doe", Role.ADMIN)
            );
            Page<User> userPage = new PageImpl<>(users, pageable, 1L);
            when(userDAO.findAll(any(Specification.class), eq(pageable)))
                    .thenReturn(userPage);
            PagedResponseDTO<UserDTO> result = userService.searchUsers(criteria, pageable);
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            verify(userDAO).findAll(any(Specification.class), eq(pageable));
            verify(userDAO, never()).findAll(eq(pageable));
        }
    }

    @Nested
    @DisplayName("When retrieving user by ID")
    class WhenRetrievingUserById {
        @Test
        @DisplayName("Should return user when found and role is ADMIN")
        void shouldReturnAdminUserWhenFound() {
            Long userId = 1L;
            User user = createUser(userId, "user@okcps.org", "User", "User", Role.ADMIN);
            when(userDAO.findById(userId)).thenReturn(Optional.of(user));
            UserDTO result = userService.getUserById(userId);
            assertNotNull(result);
            assertEquals(userId, result.getId());
            assertEquals("user@okcps.org", result.getEmail());
            assertEquals("User", result.getFirstName());
            assertEquals("User", result.getLastName());
            assertEquals(Role.ADMIN, result.getRole());
            verify(userDAO).findById(userId);
        }

        @Test
        @DisplayName("Should return user when found and role is STAFF")
        void shouldReturnStaffUserWhenFound() {
            Long userId = 1L;
            User user = createUser(userId, "user@okcps.org", "User", "User", Role.STAFF);
            when(userDAO.findById(userId)).thenReturn(Optional.of(user));
            UserDTO result = userService.getUserById(userId);
            assertNotNull(result);
            assertEquals(userId, result.getId());
            assertEquals("user@okcps.org", result.getEmail());
            assertEquals("User", result.getFirstName());
            assertEquals("User", result.getLastName());
            assertEquals(Role.STAFF, result.getRole());
            verify(userDAO).findById(userId);
        }

        @Test
        @DisplayName("Should return user when found and role is PARA")
        void shouldReturnParaUserWhenFound() {
            Long userId = 1L;
            User user = createUser(userId, "user@okcps.org", "User", "User", Role.PARA);
            when(userDAO.findById(userId)).thenReturn(Optional.of(user));
            UserDTO result = userService.getUserById(userId);
            assertNotNull(result);
            assertEquals(userId, result.getId());
            assertEquals("user@okcps.org", result.getEmail());
            assertEquals("User", result.getFirstName());
            assertEquals("User", result.getLastName());
            assertEquals(Role.PARA, result.getRole());
            verify(userDAO).findById(userId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user not found")
        void shouldThrowResourceNotFoundExceptionWhenUserNotFound() {
            Long userId = 999L;
            when(userDAO.findById(userId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when user has TEACHER role")
        void shouldThrowExceptionWhenUserHasTeacherRole() {
            Long userId = 1L;
            User user = createUser(userId, "teacher@okcps.org", "Teacher", "One", Role.TEACHER);
            when(userDAO.findById(userId)).thenReturn(Optional.of(user));
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.getUserById(userId)
            );
            String message = ex.getMessage();
            assertTrue(message.contains("only handle users with roles:"));
            assertTrue(message.contains("ADMIN") && message.contains("STAFF") && message.contains("PARA"));
            verify(userDAO).findById(userId);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when user has STUDENT role")
        void shouldThrowExceptionWhenUserHasStudentRole() {
            Long userId = 1L;
            User user = createUser(userId, "student@okcps.org", "STUDENT", "One", Role.STUDENT);
            when(userDAO.findById(userId)).thenReturn(Optional.of(user));
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.getUserById(userId)
            );
            String message = ex.getMessage();
            assertTrue(message.contains("only handle users with roles:"));
            assertTrue(message.contains("ADMIN") && message.contains("STAFF") && message.contains("PARA"));
            verify(userDAO).findById(userId);
        }
    }

    @Nested
    @DisplayName("When creating user")
    class WhenCreatingUser {
        @Test
        @DisplayName("Should create new ADMIN user successfully")
        void shouldCreateNewAdminUserSuccessfully() {
            Long userId = 1L;
            String email = "new.admin@okcps.org";
            UserDTO userDTO = new UserDTO(createUser(null, "new.admin@okcps.org", "New", "Admin", Role.ADMIN));
            User savedUser = createUser(userId, email, "New", "User", Role.ADMIN);
            when(userDAO.save(any(User.class))).thenReturn(savedUser);
            UserDTO result = userService.createUser(userDTO);
            assertNotNull(result);
            assertEquals(userId, result.getId());
            assertEquals(email, result.getEmail());
            assertEquals(Role.ADMIN, result.getRole());
            verify(userDAO).findByEmail(anyString());
            verify(userDAO).save(any(User.class));
        }

        @Test
        @DisplayName("Should create new STAFF user successfully")
        void shouldCreateNewStaffUserSuccessfully() {
            Long userId = 1L;
            String email = "new.staff@okcps.org";
            UserDTO userDTO = new UserDTO(createUser(null, "new.staff1@okcps.org", "New", "Staff", Role.STAFF));
            User savedUser = createUser(userId, email, "New", "User", Role.STAFF);
            when(userDAO.save(any(User.class))).thenReturn(savedUser);
            UserDTO result = userService.createUser(userDTO);
            assertNotNull(result);
            assertEquals(userId, result.getId());
            assertEquals(email, result.getEmail());
            assertEquals(Role.STAFF, result.getRole());
            verify(userDAO).findByEmail(anyString());
            verify(userDAO).save(any(User.class));
        }

        @Test
        @DisplayName("Should create new PARA user successfully")
        void shouldCreateNewParaUserSuccessfully() {
            Long userId = 1L;
            String email = "new.para@okcps.org";
            UserDTO userDTO = new UserDTO(createUser(null, email, "New", "Para", Role.PARA));
            User savedUser = createUser(userId, email, "New", "User", Role.PARA);
            when(userDAO.save(any(User.class))).thenReturn(savedUser);
            UserDTO result = userService.createUser(userDTO);
            assertNotNull(result);
            assertEquals(userId, result.getId());
            assertEquals(email, result.getEmail());
            assertEquals(Role.PARA, result.getRole());
            verify(userDAO).findByEmail(anyString());
            verify(userDAO).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when email already exists")
        void shouldThrowDuplicateResourceExceptionWhenEmailAlreadyExists() {
            UserDTO userDTO = new UserDTO(null, "existing@okcps.org", "New", "User", "ADMIN", null, null);
            User existingUser = createUser(1L, "existing@okcps.org", "Existing", "User", Role.TEACHER);
            when(userDAO.findByEmail("existing@okcps.org")).thenReturn(Optional.of(existingUser));
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> userService.createUser(userDTO)
            );
            assertEquals("A user with this email already exists", exception.getMessage());
            verify(userDAO).findByEmail("existing@okcps.org");
            verify(userDAO, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when creating user with TEACHER role")
        void shouldThrowExceptionWhenCreatingTeacher() {
            UserDTO userDTO = new UserDTO(null, "teacher@okcps.org", "Teacher", "One", "TEACHER", null, null);
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.createUser(userDTO)
            );
            String message = ex.getMessage();
            assertTrue(message.contains("only handle users with roles:"));
            assertTrue(message.contains("ADMIN") && message.contains("STAFF") && message.contains("PARA"));
            verify(userDAO, never()).findByEmail(anyString());
            verify(userDAO, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when creating user with STUDENT role")
        void shouldThrowExceptionWhenCreatingStudent() {
            UserDTO userDTO = new UserDTO(null, "student@okcps.org", "Student", "One", "STUDENT", null, null);
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.createUser(userDTO)
            );
            String message = ex.getMessage();
            assertTrue(message.contains("only handle users with roles:"));
            assertTrue(message.contains("ADMIN") && message.contains("STAFF") && message.contains("PARA"));
            verify(userDAO, never()).findByEmail(anyString());
            verify(userDAO, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("When updating user")
    class WhenUpdatingUserUser {
        @Test
        @DisplayName("Should update existing ADMIN user successfully")
        void shouldUpdateExistingAdminUserUserSuccessfully() {
            Long userId = 1L;
            User existingUser = createUser(userId, "old@okcps.org", "Old", "Name", Role.ADMIN);
            UserDTO updateDTO = new UserDTO(createUser(userId, "new@okcps.org", "New", "Last-Name", Role.ADMIN));
            User updatedUser = createUser(userId, "new@okcps.org", "New", "Last-Name", Role.ADMIN);
            when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userDAO.findByEmail("new@okcps.org")).thenReturn(Optional.empty());
            when(userDAO.save(any(User.class))).thenReturn(updatedUser);
            UserDTO result = userService.updateUser(userId, updateDTO);
            assertNotNull(result);
            assertEquals("new@okcps.org", result.getEmail());
            assertEquals("New", result.getFirstName());
            assertEquals("Last-Name",  result.getLastName());
            verify(userDAO).findById(userId);
            verify(userDAO).findByEmail("new@okcps.org");
            verify(userDAO).save(existingUser);
            assertEquals("new@okcps.org", existingUser.getEmail());
            assertEquals("New", existingUser.getFirstName());
            assertEquals("Last-Name", existingUser.getLastName());
        }

        @Test
        @DisplayName("Should update existing STAFF user successfully")
        void shouldUpdateExistingStaffUserUserSuccessfully() {
            Long userId = 1L;
            User existingUser = createUser(userId, "old@okcps.org", "Old", "Name", Role.STAFF);
            UserDTO updateDTO = new UserDTO(createUser(userId, "new@okcps.org", "New", "Last-Name", Role.STAFF));
            User updatedUser = createUser(userId, "new@okcps.org", "New", "Last-Name", Role.STAFF);
            when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userDAO.findByEmail("new@okcps.org")).thenReturn(Optional.empty());
            when(userDAO.save(any(User.class))).thenReturn(updatedUser);
            UserDTO result = userService.updateUser(userId, updateDTO);
            assertNotNull(result);
            assertEquals("new@okcps.org", result.getEmail());
            assertEquals("New", result.getFirstName());
            assertEquals("Last-Name",  result.getLastName());
            verify(userDAO).findById(userId);
            verify(userDAO).findByEmail("new@okcps.org");
            verify(userDAO).save(existingUser);
            assertEquals("new@okcps.org", existingUser.getEmail());
            assertEquals("New", existingUser.getFirstName());
            assertEquals("Last-Name", existingUser.getLastName());
        }

        @Test
        @DisplayName("Should update existing PARA user successfully")
        void shouldUpdateExistingParaUserUserSuccessfully() {
            Long userId = 1L;
            User existingUser = createUser(userId, "old@okcps.org", "Old", "Name", Role.PARA);
            UserDTO updateDTO = new UserDTO(createUser(userId, "new@okcps.org", "New", "Last-Name", Role.PARA));
            User updatedUser = createUser(userId, "new@okcps.org", "New", "Last-Name", Role.PARA);
            when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userDAO.findByEmail("new@okcps.org")).thenReturn(Optional.empty());
            when(userDAO.save(any(User.class))).thenReturn(updatedUser);
            UserDTO result = userService.updateUser(userId, updateDTO);
            assertNotNull(result);
            assertEquals("new@okcps.org", result.getEmail());
            assertEquals("New", result.getFirstName());
            assertEquals("Last-Name",  result.getLastName());
            verify(userDAO).findById(userId);
            verify(userDAO).findByEmail("new@okcps.org");
            verify(userDAO).save(existingUser);
            assertEquals("new@okcps.org", existingUser.getEmail());
            assertEquals("New", existingUser.getFirstName());
            assertEquals("Last-Name", existingUser.getLastName());
        }

        @Test
        @DisplayName("Should update ADMIN user to STAFF role successfully")
        void shouldUpdateAdminToStaffSuccessfully() {
            Long userId = 1L;
            User existingUser = createUser(userId, "admin@okcps.org", "Admin", "User", Role.ADMIN);
            UserDTO updateDTO = new UserDTO(userId, "admin@okcps.org", "Admin", "User", "STAFF", null, null);
            User updatedUser = createUser(userId, "admin@okcps.org", "Admin", "User", Role.STAFF);
            when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userDAO.save(any(User.class))).thenReturn(updatedUser);
            UserDTO result = userService.updateUser(userId, updateDTO);
            assertEquals(Role.STAFF, result.getRole());
            verify(userDAO).findById(userId);
            verify(userDAO, never()).findByEmail(anyString());
            verify(userDAO).save(existingUser);
        }

        @Test
        @DisplayName("Should update ADMIN user to PARA role successfully")
        void shouldUpdateAdminToParaSuccessfully() {
            Long userId = 1L;
            User existingUser = createUser(userId, "admin@okcps.org", "Admin", "User", Role.ADMIN);
            UserDTO updateDTO = new UserDTO(userId, "admin@okcps.org", "Admin", "User", "PARA", null, null);
            User updatedUser = createUser(userId, "admin@okcps.org", "Admin", "User", Role.PARA);
            when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userDAO.save(any(User.class))).thenReturn(updatedUser);
            UserDTO result = userService.updateUser(userId, updateDTO);
            assertEquals(Role.PARA, result.getRole());
            verify(userDAO).findById(userId);
            verify(userDAO, never()).findByEmail(anyString());
            verify(userDAO).save(existingUser);
        }

        @Test
        @DisplayName("Should update STAFF user to ADMIN role successfully")
        void shouldUpdateStaffToAdminSuccessfully() {
            Long userId = 1L;
            User existingUser = createUser(userId, "staff@okcps.org", "Staff", "User", Role.STAFF);
            UserDTO updateDTO = new UserDTO(userId, "staff@okcps.org", "Staff", "User", "ADMIN", null, null);
            User updatedUser = createUser(userId, "staff@okcps.org", "Staff", "User", Role.ADMIN);
            when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userDAO.save(any(User.class))).thenReturn(updatedUser);
            UserDTO result = userService.updateUser(userId, updateDTO);
            assertEquals(Role.ADMIN, result.getRole());
            verify(userDAO).findById(userId);
            verify(userDAO, never()).findByEmail(anyString());
            verify(userDAO).save(existingUser);
        }

        @Test
        @DisplayName("Should update STAFF user to PARA role successfully")
        void shouldUpdateStaffToParaSuccessfully() {
            Long userId = 1L;
            User existingUser = createUser(userId, "staff@okcps.org", "Staff", "User", Role.STAFF);
            UserDTO updateDTO = new UserDTO(userId, "staff@okcps.org", "Staff", "User", "PARA", null, null);
            User updatedUser = createUser(userId, "staff@okcps.org", "Staff", "User", Role.PARA);
            when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userDAO.save(any(User.class))).thenReturn(updatedUser);
            UserDTO result = userService.updateUser(userId, updateDTO);
            assertEquals(Role.PARA, result.getRole());
            verify(userDAO).findById(userId);
            verify(userDAO, never()).findByEmail(anyString());
            verify(userDAO).save(existingUser);
        }

        @Test
        @DisplayName("Should update user without checking email when email unchanged")
        void shouldUpdateUserWithoutCheckingEmailWhenEmailUnchanged() {
            Long userId = 1L;
            User existingUser = createUser(userId, "same@okcps.org", "Old", "Name", Role.ADMIN);
            UserDTO updateDTO = new UserDTO(userId, "same@okcps.org", "New", "Last-Name", "ADMIN", null, null);
            User updatedUser = createUser(userId, "same@okcps.org", "New", "Last-Name", Role.ADMIN);
            when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userDAO.save(any(User.class))).thenReturn(updatedUser);
            UserDTO result = userService.updateUser(userId, updateDTO);
            assertNotNull(result);
            assertEquals("same@okcps.org", result.getEmail());
            assertEquals("New", result.getFirstName());
            assertEquals("Last-Name", result.getLastName());
            verify(userDAO, never()).findByEmail(anyString());
            verify(userDAO).findById(userId);
            verify(userDAO).save(existingUser);
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when updating to existing email")
        void shouldThrowDuplicateResourceExceptionWhenUpdatingToExistingEmail() {
            Long userId = 1L;
            String existingEmail = "existing@okcps.org";
            User existingUser = createUser(userId, "old@okcps.org", "Old", "Name", Role.ADMIN);
            UserDTO updateDTO = new UserDTO(userId, existingEmail, "New", "Last-Name", "ADMIN", null, null);
            User otherUser = createUser(2L, existingEmail, "Other", "User", Role.TEACHER);
            when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userDAO.findByEmail(existingEmail)).thenReturn(Optional.of(otherUser));
            DuplicateResourceException exception = assertThrows(
                    DuplicateResourceException.class,
                    () -> userService.updateUser(userId, updateDTO)
            );
            assertEquals("A user with this email already exists", exception.getMessage());
            verify(userDAO).findById(userId);
            verify(userDAO).findByEmail(existingEmail);
            verify(userDAO, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should allow update when email exists but is the same user")
        void shouldAllowUpdateWhenEmailExistsButIsSameUser() {
            Long userId = 1L;
            String email = "admin@okcps.org";
            User existingUser = createUser(userId, email, "Old", "Name", Role.ADMIN);
            UserDTO updateDTO = new UserDTO(userId, email, "New", "Last-Name", "ADMIN", null, null);
            User updatedUser = createUser(userId, email, "New", "Last-Name", Role.ADMIN);
            when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
            when(userDAO.save(any(User.class))).thenReturn(updatedUser);
            UserDTO result = userService.updateUser(userId, updateDTO);
            assertNotNull(result);
            assertEquals(email, result.getEmail());
            assertEquals("New", result.getFirstName());
            assertEquals("Last-Name", result.getLastName());
            verify(userDAO).findById(userId);
            verify(userDAO, never()).findByEmail(email);
            verify(userDAO).save(existingUser);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user not found")
        void shouldThrowResourceNotFoundExceptionWhenUserNotFound() {
            Long userId = 999L;
            UserDTO updateDTO = new UserDTO(createUser(userId, "new@okcps.org", "New", "Last-Name", Role.ADMIN));
            when(userDAO.findById(userId)).thenReturn(Optional.empty());
            assertThrows(
                    ResourceNotFoundException.class,
                    () -> userService.updateUser(userId, updateDTO)
            );
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when updating a TEACHER user")
        void shouldThrowIllegalArgumentExceptionWhenUpdatingTeacherUser() {
            Long userId = 1L;
            User existingUser = createUser(userId, "teacher@okcps.org", "Teacher", "One", Role.TEACHER);
            UserDTO updateDTO = new UserDTO(userId, "teacher@okcps.org", "Updated", "Name", "TEACHER", 1L, null);
            when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.updateUser(userId, updateDTO)
            );
            String message = ex.getMessage();
            assertTrue(message.contains("only handle users with roles:"));
            assertTrue(message.contains("ADMIN") && message.contains("STAFF") && message.contains("PARA"));
            verify(userDAO).findById(userId);
            verify(userDAO, never()).findByEmail(anyString());
            verify(userDAO, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when updating a STUDENT user")
        void shouldThrowIllegalArgumentExceptionWhenUpdatingStudentUser() {
            Long userId = 1L;
            User existingUser = createUser(userId, "student@okcps.org", "Student", "One", Role.STUDENT);
            UserDTO updateDTO = new UserDTO(userId, "student@okcps.org", "Updated", "Name", "STUDENT", null, 1L);
            when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.updateUser(userId, updateDTO)
            );
            String message = ex.getMessage();
            assertTrue(message.contains("only handle users with roles:"));
            assertTrue(message.contains("ADMIN") && message.contains("STAFF") && message.contains("PARA"));
            verify(userDAO).findById(userId);
            verify(userDAO, never()).findByEmail(anyString());
            verify(userDAO, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when updating user to a TEACHER role")
        void shouldThrowIllegalArgumentExceptionWhenUpdatingToTeacherRole() {
            Long userId = 1L;
            User existingUser = createUser(userId, "admin@okcps.org", "Admin", "User", Role.ADMIN);
            UserDTO updateDTO = new UserDTO(userId, "admin@okcps.org", "Admin", "Name", "TEACHER", null, null);
            when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.updateUser(userId, updateDTO)
            );
            String message = ex.getMessage();
            assertTrue(message.contains("only handle users with roles:"));
            assertTrue(message.contains("ADMIN") && message.contains("STAFF") && message.contains("PARA"));
            verify(userDAO).findById(userId);
            verify(userDAO, never()).findByEmail(anyString());
            verify(userDAO, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when updating user to a STUDENT role")
        void shouldThrowIllegalArgumentExceptionWhenUpdatingToStudentRole() {
            Long userId = 1L;
            User existingUser = createUser(userId, "admin@okcps.org", "Admin", "User", Role.ADMIN);
            UserDTO updateDTO = new UserDTO(userId, "admin@okcps.org", "Admin", "Name", "STUDENT", null, null);
            when(userDAO.findById(userId)).thenReturn(Optional.of(existingUser));
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.updateUser(userId, updateDTO)
            );
            String message = ex.getMessage();
            assertTrue(message.contains("only handle users with roles:"));
            assertTrue(message.contains("ADMIN") && message.contains("STAFF") && message.contains("PARA"));
            verify(userDAO).findById(userId);
            verify(userDAO, never()).findByEmail(anyString());
            verify(userDAO, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("When deleting user")
    class WhenDeletingUser {
        @Test
        @DisplayName("Should delete ADMIN user successfully")
        void shouldDeleteAdminUserSuccessfully() {
            Long userId = 1L;
            User user = createUser(userId, "user@okcps.org", "User", "User", Role.ADMIN);
            when(userDAO.findById(userId)).thenReturn(Optional.of(user));
            userService.deleteUser(userId);
            verify(userDAO).findById(userId);
            verify(userDAO).delete(user);
        }

        @Test
        @DisplayName("Should delete STAFF user successfully")
        void shouldDeleteStaffUserSuccessfully() {
            Long userId = 1L;
            User user = createUser(userId, "user@okcps.org", "User", "User", Role.STAFF);
            when(userDAO.findById(userId)).thenReturn(Optional.of(user));
            userService.deleteUser(userId);
            verify(userDAO).findById(userId);
            verify(userDAO).delete(user);
        }

        @Test
        @DisplayName("Should delete PARA user successfully")
        void shouldDeleteParaUserSuccessfully() {
            Long userId = 1L;
            User user = createUser(userId, "user@okcps.org", "User", "User", Role.PARA);
            when(userDAO.findById(userId)).thenReturn(Optional.of(user));
            userService.deleteUser(userId);
            verify(userDAO).findById(userId);
            verify(userDAO).delete(user);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user not found")
        void shouldThrowResourceNotFoundExceptionWhenAdminUserNotFound() {
            Long userId = 999L;
            when(userDAO.findById(userId)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(userId));
            verify(userDAO).findById(userId);
            verify(userDAO, never()).delete(any(User.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when deleting TEACHER user")
        void shouldThrowExceptionWhenDeletingTeacherUser() {
            Long userId = 1L;
            User user = createUser(userId, "teacher@okcps.org", "Teacher", "User", Role.TEACHER);
            when(userDAO.findById(userId)).thenReturn(Optional.of(user));
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.deleteUser(userId)
            );
            String message = ex.getMessage();
            assertTrue(message.contains("only handle users with roles:"));
            assertTrue(message.contains("ADMIN") && message.contains("STAFF") && message.contains("PARA"));
            verify(userDAO).findById(userId);
            verify(userDAO, never()).delete(user);
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when deleting STUDENT user")
        void shouldThrowExceptionWhenDeletingStudentUser() {
            Long userId = 1L;
            User user = createUser(userId, "student@okcps.org", "Student", "User", Role.STUDENT);
            when(userDAO.findById(userId)).thenReturn(Optional.of(user));
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> userService.deleteUser(userId)
            );
            String message = ex.getMessage();
            assertTrue(message.contains("only handle users with roles:"));
            assertTrue(message.contains("ADMIN") && message.contains("STAFF") && message.contains("PARA"));
            verify(userDAO).findById(userId);
            verify(userDAO, never()).delete(user);
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
