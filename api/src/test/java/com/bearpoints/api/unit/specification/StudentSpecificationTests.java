package com.bearpoints.api.unit.specification;

import com.bearpoints.api.dao.StudentDAO;
import com.bearpoints.api.dao.TeacherDAO;
import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.dto.StudentSearchCriteria;
import com.bearpoints.api.entity.*;
import com.bearpoints.api.specification.StudentSpecification;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StudentSpecification}.
 * <p>Verifies that specification correctly builds predicates based on search criteria
 * and handles various filter combinations appropriately.
 *
 * @see StudentSpecification
 * @version 1.0
 * @author Dylan Mercer
 */
@DataJpaTest
@DisplayName("StudentSpecification Tests")
public class StudentSpecificationTests {
    @Autowired
    private StudentDAO studentDAO;

    @Autowired
    private TeacherDAO teacherDAO;

    @Autowired
    private UserDAO userDAO;

    private Teacher teacher;

    @BeforeEach
    void setUp() {
        User teacherUser = new User();
        teacherUser.setEmail("teacher@okcps.org");
        teacherUser.setFirstName("Jane");
        teacherUser.setLastName("Smith");
        teacherUser.setRole(Role.TEACHER);
        userDAO.save(teacherUser);
        teacher = new Teacher();
        teacher.setUser(teacherUser);
        teacher.setGrade(GradeLevel.FOURTH);
        teacherDAO.save(teacher);
        Student student1 = createStudent("john.doe@okcps.org", "John", "Doe", 100);
        Student student2 = createStudent("jane.smith@okcps.org", "Jane", "Smith", 150);
        Student student3 = createStudent("bob.johnson@okcps.org", "Bob", "Johnson", 75);
    }

    @Test
    @DisplayName("Should create predicate with email criteria")
    void shouldCreateSpecificationWithEmailCriteria() {
        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setEmail("john");
        Specification<Student> spec = StudentSpecification.withCriteria(criteria);
        Page<Student> results = studentDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(2, results.getTotalElements());
        assertTrue(results.getContent().stream()
                .anyMatch(s -> s.getUser().getEmail().equals("john.doe@okcps.org")));
    }

    @Test
    @DisplayName("Should create specification with first name criteria")
    void shouldCreateSpecificationWithFirstNameCriteria() {
        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setFirstName("Jane");
        Specification<Student> spec = StudentSpecification.withCriteria(criteria);
        Page<Student> results = studentDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("jane.smith@okcps.org", results.getContent().getFirst().getUser().getEmail());
    }

    @Test
    @DisplayName("Should ignore null first name criteria")
    void shouldIgnoreNullFirstNameCriteria() {
        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setFirstName(null);
        criteria.setLastName("Doe");
        Specification<Student> spec = StudentSpecification.withCriteria(criteria);
        Page<Student> results = studentDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("john.doe@okcps.org", results.getContent().getFirst().getUser().getEmail());
    }

    @Test
    @DisplayName("Should ignore null last name criteria")
    void shouldIgnoreNullLastNameCriteria() {
        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setFirstName("John");
        criteria.setLastName(null);
        Specification<Student> spec = StudentSpecification.withCriteria(criteria);
        Page<Student> results = studentDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("john.doe@okcps.org", results.getContent().getFirst().getUser().getEmail());
    }

    @Test
    @DisplayName("Should create specification with last name criteria")
    void shouldCreateSpecificationWithLastNameCriteria() {
        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setLastName("Smith");
        Specification<Student> spec = StudentSpecification.withCriteria(criteria);
        Page<Student> results = studentDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("jane.smith@okcps.org", results.getContent().getFirst().getUser().getEmail());
    }

    @Test
    @DisplayName("Should create specification with teacher ID criteria")
    void shouldCreateSpecificationWithTeacherIdCriteria() {
        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setTeacherId(teacher.getId());
        Specification<Student> spec = StudentSpecification.withCriteria(criteria);
        Page<Student> results = studentDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(3, results.getTotalElements());
    }

    @Test
    @DisplayName("Should create specification with points range criteria")
    void shouldCreateSpecificationWithPointsRangeCriteria() {
        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setMinPoints(80);
        criteria.setMaxPoints(120);
        Specification<Student> spec = StudentSpecification.withCriteria(criteria);
        Page<Student> results = studentDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("john.doe@okcps.org", results.getContent().getFirst().getUser().getEmail());
    }

    @Test
    @DisplayName("Should create specification with multiple criteria")
    void shouldCreateSpecificationWithMultipleCriteria() {
        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setFirstName("John");
        criteria.setMinPoints(50);
        Specification<Student> spec = StudentSpecification.withCriteria(criteria);
        Page<Student> results = studentDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("john.doe@okcps.org", results.getContent().getFirst().getUser().getEmail());
    }

    @Test
    @DisplayName("Should create specification with empty criteria")
    void shouldCreateSpecificationWithEmptyCriteria() {
        StudentSearchCriteria criteria = new StudentSearchCriteria();
        Specification<Student> spec = StudentSpecification.withCriteria(criteria);
        Page<Student> results = studentDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(3, results.getTotalElements());
    }

    @Test
    @DisplayName("Should ignore empty string criteria")
    void shouldHandleIgnoreEmptyStringCriteria() {
        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setEmail("");
        criteria.setFirstName("   ");
        criteria.setLastName(" ");
        Specification<Student> spec = StudentSpecification.withCriteria(criteria);
        Page<Student> results = studentDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(3, results.getTotalElements());
    }

    @Test
    @DisplayName("Should handle case-insensitive search")
    void shouldHandleCaseInsensitiveSearch() {
        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setEmail("JOHN.DOE");
        Specification<Student> spec = StudentSpecification.withCriteria(criteria);
        Page<Student> results = studentDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertEquals("john.doe@okcps.org", results.getContent().getFirst().getUser().getEmail());
    }

    private Student createStudent(String email, String firstName, String lastName, Integer points) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(Role.STUDENT);
        userDAO.save(user);
        Student student = new Student();
        student.setUser(user);
        student.setTeacher(teacher);
        student.setPoints(points);
        student.generateToken();
        return studentDAO.save(student);
    }
}
