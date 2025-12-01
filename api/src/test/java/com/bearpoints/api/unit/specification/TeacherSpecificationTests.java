package com.bearpoints.api.unit.specification;

import com.bearpoints.api.dao.TeacherDAO;
import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.dto.TeacherSearchCriteria;
import com.bearpoints.api.entity.*;
import com.bearpoints.api.specification.TeacherSpecification;
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
 * Unit tests for {@link TeacherSpecification}.
 * <p>Verifies that specification correctly builds predicates based on search criteria
 * and handles various filter combinations appropriately.
 *
 * @see TeacherSpecification
 * @version 1.0
 * @author Dylan Mercer
 */
@DataJpaTest
@DisplayName("TeacherSpecification Tests")
public class TeacherSpecificationTests {
    @Autowired
    private TeacherDAO teacherDAO;

    @Autowired
    private UserDAO userDAO;

    @BeforeEach
    void setup() {
        createTeacher("j.doe@okcps.org", "John", "Doe", GradeLevel.FIRST);
        createTeacher("j.smith@okcps.org", "Jane", "Smith", GradeLevel.SECOND);
        createTeacher("d.johnson@okcps.org", "Doug", "Johnson", GradeLevel.THIRD);
    }

    @Test
    @DisplayName("Should create predicate with email criteria")
    void shouldCreateSpecificationWithEmailCriteria() {
        TeacherSearchCriteria criteria = new TeacherSearchCriteria();
        criteria.setEmail("j.");
        Specification<Teacher> spec = TeacherSpecification.withCriteria(criteria);
        Page<Teacher> results = teacherDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(2, results.getTotalElements());
        assertTrue(results.getContent().stream()
                .anyMatch(s -> s.getUser().getEmail().equals("j.doe@okcps.org")));
    }

    @Test
    @DisplayName("Should create specification with first name criteria")
    void shouldCreateSpecificationWithFirstNameCriteria() {
        TeacherSearchCriteria criteria = new TeacherSearchCriteria();
        criteria.setFirstName("Jane");
        Specification<Teacher> spec = TeacherSpecification.withCriteria(criteria);
        Page<Teacher> results = teacherDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("j.smith@okcps.org", results.getContent().getFirst().getUser().getEmail());
    }

    @Test
    @DisplayName("Should ignore null first name criteria")
    void shouldIgnoreNullFirstNameCriteria() {
        TeacherSearchCriteria criteria = new TeacherSearchCriteria();
        criteria.setFirstName(null);
        criteria.setLastName("Doe");
        Specification<Teacher> spec = TeacherSpecification.withCriteria(criteria);
        Page<Teacher> results = teacherDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("j.doe@okcps.org", results.getContent().getFirst().getUser().getEmail());
    }

    @Test
    @DisplayName("Should ignore null last name criteria")
    void shouldIgnoreNullLastNameCriteria() {
        TeacherSearchCriteria criteria = new TeacherSearchCriteria();
        criteria.setFirstName("John");
        criteria.setLastName(null);
        Specification<Teacher> spec = TeacherSpecification.withCriteria(criteria);
        Page<Teacher> results = teacherDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("j.doe@okcps.org", results.getContent().getFirst().getUser().getEmail());
    }

    @Test
    @DisplayName("Should create specification with last name criteria")
    void shouldCreateSpecificationWithLastNameCriteria() {
        TeacherSearchCriteria criteria = new TeacherSearchCriteria();
        criteria.setLastName("Smith");
        Specification<Teacher> spec = TeacherSpecification.withCriteria(criteria);
        Page<Teacher> results = teacherDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("j.smith@okcps.org", results.getContent().getFirst().getUser().getEmail());
    }

    @Test
    @DisplayName("Should create specification with grade criteria")
    void shouldCreateSpecificationWithGradeCriteria() {
        TeacherSearchCriteria criteria = new TeacherSearchCriteria();
        criteria.setGrade(GradeLevel.SECOND);
        Specification<Teacher> spec = TeacherSpecification.withCriteria(criteria);
        Page<Teacher> results = teacherDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("j.smith@okcps.org", results.getContent().getFirst().getUser().getEmail());
    }

    @Test
    @DisplayName("Should create specification with multiple criteria")
    void shouldCreateSpecificationWithMultipleCriteria() {
        TeacherSearchCriteria criteria = new TeacherSearchCriteria();
        criteria.setFirstName("John");
        criteria.setLastName("Doe");
        Specification<Teacher> spec = TeacherSpecification.withCriteria(criteria);
        Page<Teacher> results = teacherDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("j.doe@okcps.org", results.getContent().getFirst().getUser().getEmail());
    }

    @Test
    @DisplayName("Should create specification with empty criteria")
    void shouldCreateSpecificationWithEmptyCriteria() {
        TeacherSearchCriteria criteria = new TeacherSearchCriteria();
        Specification<Teacher> spec = TeacherSpecification.withCriteria(criteria);
        Page<Teacher> results = teacherDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(3, results.getTotalElements());
    }

    @Test
    @DisplayName("Should ignore empty string criteria")
    void shouldHandleIgnoreEmptyStringCriteria() {
        TeacherSearchCriteria criteria = new TeacherSearchCriteria();
        criteria.setEmail("");
        criteria.setFirstName("   ");
        criteria.setLastName(" ");
        Specification<Teacher> spec = TeacherSpecification.withCriteria(criteria);
        Page<Teacher> results = teacherDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(3, results.getTotalElements());
    }

    @Test
    @DisplayName("Should handle case-insensitive search")
    void shouldHandleCaseInsensitiveSearch() {
        TeacherSearchCriteria criteria = new TeacherSearchCriteria();
        criteria.setEmail("J.DOE");
        Specification<Teacher> spec = TeacherSpecification.withCriteria(criteria);
        Page<Teacher> results = teacherDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("j.doe@okcps.org", results.getContent().getFirst().getUser().getEmail());
    }

    private void createTeacher(String email, String firstName, String lastName, GradeLevel grade) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(Role.TEACHER);
        userDAO.save(user);
        Teacher teacher = new Teacher();
        teacher.setUser(user);
        teacher.setGrade(grade);
        teacherDAO.save(teacher);
    }
}
