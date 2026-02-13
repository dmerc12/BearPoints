package com.bearpoints.api.unit.specification;

import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.criteria.UserSearchCriteria;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import com.bearpoints.api.specification.UserSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UserSpecification}.
 * <p>Verifies that specification correctly builds predicates based on search criteria
 * and handles various filter combinations appropriately.
 *
 * @see UserSpecification
 * @version 2.1
 * @author Dylan Mercer
 */
@DataJpaTest
@DisplayName("UserSpecification Tests")
public class UserSpecificationTests {
    @Autowired
    private UserDAO userDAO;

    @BeforeEach
    void setup() {
        createUser("j.doe@okcps.org", "John", "Doe", Role.ADMIN);
        createUser("j.smith@okcps.org", "Jane", "Smith", Role.ADMIN);
        createUser("b.johnson@okcps.org", "Bill", "Johnson", Role.TEACHER);
        createUser("s.williams@okcps.org", "Sarah", "Williams", Role.TEACHER);
        createUser("m.brown@okcps.org", "Mike", "Brown", Role.STUDENT);
        createUser("staff@okcps.org", "Alex", "Staff", Role.STAFF);
    }

    private void createUser(String email, String firstName, String lastName, Role role) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRole(role);
        userDAO.save(user);
    }

    @Nested
    @DisplayName("When using withCriteria")
    class WhenUsingWithCriteria {
        @Test
        @DisplayName("Should filter by ADMIN role when role criteria is provided")
        void shouldFilterByAdminRole() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setRole(Role.ADMIN);
            Specification<User> spec = UserSpecification.withCriteria(criteria);
            Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
            assertEquals(2, results.getTotalElements());
            assertTrue(results.getContent().stream().allMatch(user -> user.getRole() == Role.ADMIN));
        }

        @Test
        @DisplayName("Should filter by STAFF role when role criteria is provided")
        void shouldFilterByStaffRole() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setRole(Role.STAFF);
            Specification<User> spec = UserSpecification.withCriteria(criteria);
            Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
            assertEquals(1, results.getTotalElements());
            assertTrue(results.getContent().stream().allMatch(user -> user.getRole() == Role.STAFF));
        }

        @Test
        @DisplayName("Should filter by TEACHER role when role criteria is provided")
        void shouldFilterByTeacherRole() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setRole(Role.TEACHER);
            Specification<User> spec = UserSpecification.withCriteria(criteria);
            Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
            assertEquals(2, results.getTotalElements());
            assertTrue(results.getContent().stream().allMatch(user -> user.getRole() == Role.TEACHER));
        }

        @Test
        @DisplayName("Should filter by STUDENT role when role criteria is provided")
        void shouldFilterByStudentRole() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setRole(Role.STUDENT);
            Specification<User> spec = UserSpecification.withCriteria(criteria);
            Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
            assertEquals(1, results.getTotalElements());
            assertTrue(results.getContent().stream().allMatch(user -> user.getRole() == Role.STUDENT));
        }

        @Test
        @DisplayName("Should not filter by role when role criteria is null")
        void shouldNotFilterByRoleWhenRoleCriteriaIsNull() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            Specification<User> spec = UserSpecification.withCriteria(criteria);
            Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
            assertEquals(6, results.getTotalElements());
        }

        @Test
        @DisplayName("Should create predicate with email criteria")
        void shouldCreateSpecificationWithEmailCriteria() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setEmail("j.d");
            Specification<User> spec = UserSpecification.withCriteria(criteria);
            Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
            assertEquals(1, results.getTotalElements());
            assertTrue(results.getContent().stream()
                    .anyMatch(s -> s.getEmail().equals("j.doe@okcps.org")));
        }

        @Test
        @DisplayName("Should combine role and email criteria")
        void shouldCombineRoleAndEmailCriteria() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setRole(Role.ADMIN);
            criteria.setEmail("j");
            Specification<User> spec = UserSpecification.withCriteria(criteria);
            Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
            assertEquals(2, results.getTotalElements());
            assertTrue(results.getContent().stream().allMatch(user -> user.getRole() == Role.ADMIN &&
                    user.getEmail().contains("j")));
        }

        @Test
        @DisplayName("Should create specification with first name criteria")
        void shouldCreateSpecificationWithFirstNameCriteria() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setFirstName("Jane");
            Specification<User> spec = UserSpecification.withCriteria(criteria);
            Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
            assertEquals(1, results.getTotalElements());
            assertEquals("j.smith@okcps.org", results.getContent().getFirst().getEmail());
        }

        @Test
        @DisplayName("Should ignore null first name criteria")
        void shouldIgnoreNullFirstNameCriteria() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setFirstName(null);
            criteria.setLastName("Doe");
            Specification<User> spec = UserSpecification.withCriteria(criteria);
            Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
            assertEquals(1, results.getTotalElements());
            assertEquals("j.doe@okcps.org", results.getContent().getFirst().getEmail());
        }

        @Test
        @DisplayName("Should create specification with last name criteria")
        void shouldCreateSpecificationWithLastNameCriteria() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setLastName("Smith");
            Specification<User> spec = UserSpecification.withCriteria(criteria);
            Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
            assertEquals(1, results.getTotalElements());
            assertEquals("j.smith@okcps.org", results.getContent().getFirst().getEmail());
        }

        @Test
        @DisplayName("Should ignore null last name criteria")
        void shouldIgnoreNullLastNameCriteria() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setFirstName("John");
            criteria.setLastName(null);
            Specification<User> spec = UserSpecification.withCriteria(criteria);
            Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
            assertEquals(1, results.getTotalElements());
            assertEquals("j.doe@okcps.org", results.getContent().getFirst().getEmail());
        }

        @Test
        @DisplayName("Should create specification with multiple criteria")
        void shouldCreateSpecificationWithMultipleCriteria() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setFirstName("John");
            criteria.setEmail("J");
            Specification<User> spec = UserSpecification.withCriteria(criteria);
            Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
            assertEquals(1, results.getTotalElements());
            assertEquals("j.doe@okcps.org", results.getContent().getFirst().getEmail());
        }

        @Test
        @DisplayName("Should create specification with empty criteria")
        void shouldCreateSpecificationWithEmptyCriteria() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            Specification<User> spec = UserSpecification.withCriteria(criteria);
            Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
            assertEquals(6, results.getTotalElements());
        }

        @Test
        @DisplayName("Should ignore empty string criteria")
        void shouldHandleIgnoreEmptyStringCriteria() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setEmail("");
            criteria.setFirstName("   ");
            criteria.setLastName(" ");
            Specification<User> spec = UserSpecification.withCriteria(criteria);
            Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
            assertEquals(6, results.getTotalElements());
        }

        @Test
        @DisplayName("Should handle case-insensitive search")
        void shouldHandleCaseInsensitiveSearch() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setEmail("J.DOE");
            Specification<User> spec = UserSpecification.withCriteria(criteria);
            Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
            assertEquals(1, results.getTotalElements());
            assertEquals("j.doe@okcps.org", results.getContent().getFirst().getEmail());
        }
    }

    @Nested
    @DisplayName("When using byExactName")
    class WhenUsingByExactName {
        @Test
        @DisplayName("Should find user by exact first and last name (case-insensitive)")
        void shouldFindUserByExactFirstAndLastName() {
            Specification<User> spec = UserSpecification.byExactName("John", "Doe");
            List<User> results = userDAO.findAll(spec);
            assertEquals(1, results.size());
            assertEquals("j.doe@okcps.org", results.getFirst().getEmail());
        }

        @Test
        @DisplayName("Should return empty list when no exact match found")
        void shouldReturnEmptyListWhenNoExactMatchFound() {
            Specification<User> spec = UserSpecification.byExactName("Nonexistent", "User");
            List<User> results = userDAO.findAll(spec);
            assertTrue(results.isEmpty());
        }

        @Test
        @DisplayName("Should find user with lowercase search")
        void shouldFindUserWithLowerCaseSearch() {
            Specification<User> spec = UserSpecification.byExactName("john", "doe");
            List<User> results = userDAO.findAll(spec);
            assertEquals(1, results.size());
            assertEquals("j.doe@okcps.org", results.getFirst().getEmail());
        }

        @Test
        @DisplayName("Should find user with uppercase search")
        void shouldBeCaseSensitiveForExactNameMatch() {
            Specification<User> spec = UserSpecification.byExactName("JOHN", "DOE");
            List<User> results = userDAO.findAll(spec);
            assertEquals(1, results.size());
            assertEquals("j.doe@okcps.org", results.getFirst().getEmail());
        }

        @Test
        @DisplayName("Should find user with mixed case search")
        void shouldFindUserWithMixedCaseSearch() {
            Specification<User> spec = UserSpecification.byExactName("JoHn", "dOe");
            List<User> results = userDAO.findAll(spec);
            assertEquals(1, results.size());
            assertEquals("j.doe@okcps.org", results.getFirst().getEmail());
        }
    }

    @Nested
    @DisplayName("When using isValidSearchString")
    class WhenUsingValidSearchString {
        @Test
        @DisplayName("Should return true for non-null, non-empty string")
        void shouldReturnTrueForNonNullNonEmptyString() {
            assertTrue(UserSpecification.isValidSearchString("test"));
            assertTrue(UserSpecification.isValidSearchString(" a "));
        }

        @Test
        @DisplayName("Should return false for null string")
        void shouldReturnFalseForNullString() {
            assertFalse(UserSpecification.isValidSearchString(null));
        }

        @Test
        @DisplayName("Should return false for empty string")
        void shouldReturnFalseForEmptyString() {
            assertFalse(UserSpecification.isValidSearchString(""));
        }

        @Test
        @DisplayName("Should return false for whitespace-only string")
        void shouldReturnFalseForWhitespaceOnlyString() {
            assertFalse(UserSpecification.isValidSearchString(" "));
            assertFalse(UserSpecification.isValidSearchString("\t\n"));
        }
    }
}
