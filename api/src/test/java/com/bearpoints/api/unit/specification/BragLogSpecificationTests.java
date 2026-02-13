package com.bearpoints.api.unit.specification;

import com.bearpoints.api.criteria.BragLogSearchCriteria;
import com.bearpoints.api.dao.*;
import com.bearpoints.api.entity.*;
import com.bearpoints.api.specification.BragLogSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link BragLogSpecification}.
 * <p>Verifies that specification correctly builds predicates based on search criteria
 * and handles various filter combinations appropriately.
 *
 * @see BragLogSpecification
 * @version 1.1
 * @author Dylan Mercer
 */
@DataJpaTest
@DisplayName("BragLogSpecification Tests")
public class BragLogSpecificationTests {
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private TeacherDAO teacherDAO;

    @Autowired
    private StudentDAO studentDAO;

    @Autowired
    private BehaviorTypeDAO behaviorTypeDAO;

    @Autowired
    private BragLogDAO bragLogDAO;

    private Teacher teacher1;

    private Teacher teacher3;

    private Student student1;

    private Student student4;

    @BeforeEach
    void setUp() {
        teacher1 = createTeacher("john.doe@okcps.org", "John", "Doe", GradeLevel.SECOND);
        Teacher teacher2 = createTeacher("jane.smith@okcps.org", "Jane", "Smith", GradeLevel.THIRD);
        teacher3 = createTeacher("doug.johnson@okcps.org", "Doug", "Johnson", GradeLevel.FOURTH);
        student1 = createStudent("bill.reed@okcps.org", "Bill", "Reed", teacher1);
        Student student2 = createStudent("alice.stephens@okcps.org", "Alice", "Stephens", teacher2);
        Student student3 = createStudent("jenny.long@okcps.org", "Jenny", "Long", teacher3);
        student4 = createStudent("jessi.williams@okcps.org", "Jessi", "Williams", teacher1);
        BehaviorType behavior1 = createBehaviorType("Behaving Brilliantly", 1);
        BehaviorType behavior2 = createBehaviorType("Sensational Bear Time", 3);
        BehaviorType behavior3 = createBehaviorType("Answered Thoughtfully", 2);
        BehaviorType behavior4 = createBehaviorType("Kind To Others", 5);
        createBragLog(student1, Set.of(behavior1, behavior3, behavior4), "first test notes",
                "John Doe", teacher1.getUser()); //8
        createBragLog(student2, Set.of(behavior1, behavior2), "second test notes",
                "Jane Smith", teacher2.getUser()); //4
        createBragLog(student3, Set.of(behavior1, behavior4), "third test notes",
                "John Doe", teacher1.getUser()); //6
        createBragLog(student4, Set.of(behavior3, behavior4), "final test notes",
                "Doug Johnson", teacher3.getUser()); //7
    }

    @Test
    @DisplayName("Should create predicate with student name criteria")
    void shouldCreatePredicateWithStudentNameCriteria() {
        BragLogSearchCriteria criteria = new BragLogSearchCriteria();
        criteria.setStudentName("J");
        Specification<BragLog> spec = BragLogSpecification.withCriteria(criteria);
        Page<BragLog> results = bragLogDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(2, results.getTotalElements());
        assertTrue(results.getContent().stream()
                .anyMatch(bl -> bl.getStudent().getUser().getFirstName().equals("Jenny")));
        assertTrue(results.getContent().stream()
                .anyMatch(bl -> bl.getStudent().getUser().getFirstName().equals("Jessi")));
    }

    @Test
    @DisplayName("Should create predicate with student ID criteria")
    void shouldCreatePredicateWithStudentIDCriteria() {
        BragLogSearchCriteria criteria = new BragLogSearchCriteria();
        criteria.setStudentId(student1.getId());
        Specification<BragLog> spec = BragLogSpecification.withCriteria(criteria);
        Page<BragLog> results = bragLogDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertTrue(results.getContent().stream()
                .anyMatch(bl -> bl.getStudent().getUser().getFirstName().equals("Bill")));
    }

    @Test
    @DisplayName("Should create predicate with teacher name criteria")
    void shouldCreatePredicateWithTeacherNameCriteria() {
        BragLogSearchCriteria criteria = new BragLogSearchCriteria();
        criteria.setTeacherName("J");
        Specification<BragLog> spec = BragLogSpecification.withCriteria(criteria);
        Page<BragLog> results = bragLogDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(4, results.getTotalElements());
    }

    @Test
    @DisplayName("Should create predicate with teacher ID criteria")
    void shouldCreatePredicateWithTeacherIDCriteria() {
        BragLogSearchCriteria criteria = new BragLogSearchCriteria();
        criteria.setTeacherId(teacher1.getId());
        Specification<BragLog> spec = BragLogSpecification.withCriteria(criteria);
        Page<BragLog> results = bragLogDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(2, results.getTotalElements());
        assertTrue(results.getContent().stream()
                .allMatch(bl -> bl.getTeacher().getUser().getFirstName().equals("John")));
    }

    @Test
    @DisplayName("Should create predicate with grade criteria")
    void shouldCreatePredicateWithGradeCriteria() {
        BragLogSearchCriteria criteria = new BragLogSearchCriteria();
        criteria.setGrade(GradeLevel.FOURTH);
        Specification<BragLog> spec = BragLogSpecification.withCriteria(criteria);
        Page<BragLog> results = bragLogDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertTrue(results.getContent().stream()
                .anyMatch(bl -> bl.getTeacher().getUser().getFirstName().equals("Doug")));
    }

    @Test
    @DisplayName("Should create predicate with point range criteria")
    void shouldCreatePredicateWithPointRangeCriteria() {
        BragLogSearchCriteria criteria = new BragLogSearchCriteria();
        criteria.setMinPoints(3);
        criteria.setMaxPoints(5);
        Specification<BragLog> spec = BragLogSpecification.withCriteria(criteria);
        Page<BragLog> results = bragLogDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertTrue(results.getContent().stream()
                .anyMatch(bl -> bl.getStudent().getUser().getFirstName().equals("Alice")));
    }

    @Test
    @DisplayName("Should create predicate with date range criteria")
    void shouldCreatePredicateWithDateRangeCriteria() {
        BragLogSearchCriteria criteria = new BragLogSearchCriteria();
        criteria.setStartDate(LocalDateTime.now().minusDays(1));
        criteria.setEndDate(LocalDateTime.now());
        Specification<BragLog> spec = BragLogSpecification.withCriteria(criteria);
        Page<BragLog> results = bragLogDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(4, results.getTotalElements());
    }

    @Test
    @DisplayName("Should create predicate with notes criteria")
    void shouldCreatePredicateWithNotesCriteria() {
        BragLogSearchCriteria criteria = new BragLogSearchCriteria();
        criteria.setNotes("first");
        Specification<BragLog> spec = BragLogSpecification.withCriteria(criteria);
        Page<BragLog> results = bragLogDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertTrue(results.getContent().stream()
                .anyMatch(bl -> bl.getStudent().getUser().getFirstName().equals("Bill")));
    }

    @Test
    @DisplayName("Should create predicate with submitter name criteria")
    void shouldCreatePredicateWithSubmitterNameCriteria() {
        BragLogSearchCriteria criteria = new BragLogSearchCriteria();
        criteria.setSubmitterName("John");
        Specification<BragLog> spec = BragLogSpecification.withCriteria(criteria);
        Page<BragLog> results = bragLogDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(3, results.getTotalElements());
        assertTrue(results.getContent().stream().allMatch(bl -> bl.getSubmitterName().contains("John")));
    }

    @Test
    @DisplayName("Should create predicate with submitter user ID criteria")
    void shouldCreatePredicateWithSubmitterIDCriteria() {
        BragLogSearchCriteria criteria = new BragLogSearchCriteria();
        criteria.setSubmitterUserId(teacher1.getUser().getId());
        Specification<BragLog> spec = BragLogSpecification.withCriteria(criteria);
        Page<BragLog> results = bragLogDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(2, results.getTotalElements());
        assertTrue(results.getContent().stream().allMatch(bl -> bl.getSubmitterUser() != null &&
                bl.getSubmitterUser().getId().equals(teacher1.getUser().getId())));
    }

    @Test
    @DisplayName("Should create predicate with all criteria")
    void shouldCreatePredicateWithAllCriteria() {
        BragLogSearchCriteria criteria = new BragLogSearchCriteria();
        criteria.setStudentName("J");
        criteria.setStudentId(student4.getId());
        criteria.setTeacherName("J");
        criteria.setTeacherId(teacher1.getId());
        criteria.setGrade(GradeLevel.SECOND);
        criteria.setMinPoints(5);
        criteria.setMaxPoints(7);
        criteria.setStartDate(LocalDateTime.now().minusDays(1));
        criteria.setEndDate(LocalDateTime.now());
        criteria.setNotes("final");
        criteria.setSubmitterName("Doug");
        criteria.setSubmitterUserId(teacher3.getUser().getId());
        Specification<BragLog> spec = BragLogSpecification.withCriteria(criteria);
        Page<BragLog> results = bragLogDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertTrue(results.getContent().stream()
                .anyMatch(bl -> bl.getStudent().getUser().getFirstName().equals("Jessi")));
    }

    @Test
    @DisplayName("Should create specification with empty criteria")
    void shouldCreateSpecificationWithEmptyCriteria() {
        BragLogSearchCriteria criteria = new BragLogSearchCriteria();
        Specification<BragLog> spec = BragLogSpecification.withCriteria(criteria);
        Page<BragLog> results = bragLogDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(4, results.getTotalElements());
    }

    @Test
    @DisplayName("Should ignore empty string criteria")
    void shouldIgnoreEmptyStringCriteria() {
        BragLogSearchCriteria criteria = new BragLogSearchCriteria();
        criteria.setStudentName(" ");
        criteria.setTeacherName(" ");
        criteria.setNotes(" ");
        criteria.setSubmitterName(" ");
        Specification<BragLog> spec = BragLogSpecification.withCriteria(criteria);
        Page<BragLog> results = bragLogDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(4, results.getTotalElements());
    }

    @Test
    @DisplayName("Should handle case-insensitive search")
    void shouldHandleCaseInsensitiveSearch() {
        BragLogSearchCriteria criteria = new BragLogSearchCriteria();
        criteria.setStudentName("JESSI");
        criteria.setTeacherName("JOHN");
        criteria.setNotes("FINAL");
        criteria.setSubmitterName("DOUG");
        Specification<BragLog> spec = BragLogSpecification.withCriteria(criteria);
        Page<BragLog> results = bragLogDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertTrue(results.getContent().stream()
                .anyMatch(bl -> bl.getStudent().getUser().getFirstName().equals("Jessi")));
    }

    private User createUser(String email, String firstName, String lastName, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        return userDAO.save(user);
    }

    private Teacher createTeacher(String email, String firstName, String lastName, GradeLevel grade) {
        User user = createUser(email, firstName, lastName, Role.TEACHER);
        Teacher teacher = new Teacher();
        teacher.setUser(user);
        teacher.setGrade(grade);
        return teacherDAO.save(teacher);
    }

    private Student createStudent(String email, String firstName, String lastName, Teacher teacher) {
        User user = createUser(email, firstName, lastName, Role.STUDENT);
        Student student = new Student();
        student.setUser(user);
        student.setTeacher(teacher);
        return studentDAO.save(student);
    }

    private BehaviorType createBehaviorType(String name, Integer pointValue) {
        BehaviorType behaviorType = new BehaviorType();
        behaviorType.setName(name);
        behaviorType.setPointValue(pointValue);
        behaviorType.setActive(true);
        return behaviorTypeDAO.save(behaviorType);
    }

    private void createBragLog(Student student, Set<BehaviorType> behaviors, String notes,
                               String submitterName, User submitterUser) {
        BragLog bragLog = new BragLog();
        bragLog.setStudent(student);
        bragLog.setTeacher(student.getTeacher());
        bragLog.setBehaviors(behaviors);
        bragLog.setNotes(notes);
        bragLog.setSubmitterName(submitterName);
        bragLog.setSubmitterUser(submitterUser);
        bragLogDAO.save(bragLog);
    }
}
