package com.bearpoints.api.unit.specification;

import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.dto.AdminSearchCriteria;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import com.bearpoints.api.specification.AdminSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AdminSpecification}.
 * <p>Verifies that specification correctly builds predicates based on search criteria
 * and handles various filter combinations appropriately.
 *
 * @see AdminSpecification
 * @version 1.0
 * @author Dylan Mercer
 */
@DataJpaTest
@DisplayName("AdminSpecification Tests")
public class AdminSpecificationTests {
    @Autowired
    private UserDAO userDAO;

    @BeforeEach
    void setup() {
        createAdmin("j.doe@okcps.org", "John", "Doe");
        createAdmin("j.smith@okcps.org", "Jane", "Smith");
        createAdmin("b.johnson@okcps.org", "Bill", "Johnson");
    }

    private void createAdmin(String email, String firstName, String lastName) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRole(Role.ADMIN);
        userDAO.save(user);
    }

    @Test
    @DisplayName("Should create predicate with email criteria")
    void shouldCreateSpecificationWithEmailCriteria() {
        AdminSearchCriteria criteria = new AdminSearchCriteria();
        criteria.setEmail("j.d");
        Specification<User> spec = AdminSpecification.withCriteria(criteria);
        Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertTrue(results.getContent().stream()
                .anyMatch(s -> s.getEmail().equals("j.doe@okcps.org")));
    }

    @Test
    @DisplayName("Should create specification with first name criteria")
    void shouldCreateSpecificationWithFirstNameCriteria() {
        AdminSearchCriteria criteria = new AdminSearchCriteria();
        criteria.setFirstName("Jane");
        Specification<User> spec = AdminSpecification.withCriteria(criteria);
        Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("j.smith@okcps.org", results.getContent().getFirst().getEmail());
    }

    @Test
    @DisplayName("Should ignore null first name criteria")
    void shouldIgnoreNullFirstNameCriteria() {
        AdminSearchCriteria criteria = new AdminSearchCriteria();
        criteria.setFirstName(null);
        criteria.setLastName("Doe");
        Specification<User> spec = AdminSpecification.withCriteria(criteria);
        Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("j.doe@okcps.org", results.getContent().getFirst().getEmail());
    }

    @Test
    @DisplayName("Should ignore null last name criteria")
    void shouldIgnoreNullLastNameCriteria() {
        AdminSearchCriteria criteria = new AdminSearchCriteria();
        criteria.setFirstName("John");
        criteria.setLastName(null);
        Specification<User> spec = AdminSpecification.withCriteria(criteria);
        Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("j.doe@okcps.org", results.getContent().getFirst().getEmail());
    }

    @Test
    @DisplayName("Should create specification with last name criteria")
    void shouldCreateSpecificationWithLastNameCriteria() {
        AdminSearchCriteria criteria = new AdminSearchCriteria();
        criteria.setLastName("Smith");
        Specification<User> spec = AdminSpecification.withCriteria(criteria);
        Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("j.smith@okcps.org", results.getContent().getFirst().getEmail());
    }

    @Test
    @DisplayName("Should create specification with multiple criteria")
    void shouldCreateSpecificationWithMultipleCriteria() {
        AdminSearchCriteria criteria = new AdminSearchCriteria();
        criteria.setFirstName("John");
        criteria.setEmail("J");
        Specification<User> spec = AdminSpecification.withCriteria(criteria);
        Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("j.doe@okcps.org", results.getContent().getFirst().getEmail());
    }

    @Test
    @DisplayName("Should create specification with empty criteria")
    void shouldCreateSpecificationWithEmptyCriteria() {
        AdminSearchCriteria criteria = new AdminSearchCriteria();
        Specification<User> spec = AdminSpecification.withCriteria(criteria);
        Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(3, results.getTotalElements());
    }

    @Test
    @DisplayName("Should ignore empty string criteria")
    void shouldHandleIgnoreEmptyStringCriteria() {
        AdminSearchCriteria criteria = new AdminSearchCriteria();
        criteria.setEmail("");
        criteria.setFirstName("   ");
        criteria.setLastName(" ");
        Specification<User> spec = AdminSpecification.withCriteria(criteria);
        Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(3, results.getTotalElements());
    }

    @Test
    @DisplayName("Should handle case-insensitive search")
    void shouldHandleCaseInsensitiveSearch() {
        AdminSearchCriteria criteria = new AdminSearchCriteria();
        criteria.setEmail("J.DOE");
        Specification<User> spec = AdminSpecification.withCriteria(criteria);
        Page<User> results = userDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("j.doe@okcps.org", results.getContent().getFirst().getEmail());
    }
}
