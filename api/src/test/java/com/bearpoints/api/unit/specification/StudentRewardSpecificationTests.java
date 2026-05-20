package com.bearpoints.api.unit.specification;

import com.bearpoints.api.criteria.StudentRewardSearchCriteria;
import com.bearpoints.api.dao.*;
import com.bearpoints.api.entity.*;
import com.bearpoints.api.specification.StudentRewardSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link StudentRewardSpecification}.
 * <p>Verifies that specification correctly builds predicates based on search criteria
 * and handles various filter combinations appropriately.
 *
 * @see StudentRewardSpecification
 * @version 1.0
 * @author Dylan Mercer
 */
@DataJpaTest
@DisplayName("StudentRewardSpecification Tests")
public class StudentRewardSpecificationTests {
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private TeacherDAO teacherDAO;

    @Autowired
    private StudentDAO studentDAO;

    @Autowired
    private RewardItemDAO rewardItemDAO;

    @Autowired
    private StudentRewardDAO studentRewardDAO;

    private Student student;

    private RewardItem rewardItem;

    @BeforeEach
    void setUp() {
        student = createStudent();
        rewardItem = createRewardItem("Stickers", 8, 30);
        RewardItem otherRewardItem = createRewardItem("Pizza Party", 70, 90);
        createStudentReward(student, rewardItem);
        createStudentReward(student, otherRewardItem);
    }

    @Test
    @DisplayName("Should create predicate with student name criteria")
    void shouldCreatePredicateWithStudentNameCriteria() {
        StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
        criteria.setStudentName("J");
        Specification<StudentReward> spec = StudentRewardSpecification.withCriteria(criteria);
        Page<StudentReward> results = studentRewardDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(2, results.getTotalElements());
        assertTrue(results.getContent().stream()
                .allMatch(sr -> sr.getStudent().getUser().getFirstName().equals("John")));
    }

    @Test
    @DisplayName("Should create predicate with student ID criteria")
    void shouldCreatePredicateWithStudentIDCriteria() {
        StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
        criteria.setStudentId(student.getId());
        Specification<StudentReward> spec = StudentRewardSpecification.withCriteria(criteria);
        Page<StudentReward> results = studentRewardDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(2, results.getTotalElements());
        assertTrue(results.getContent().stream()
                .allMatch(sr -> sr.getStudent().getUser().getFirstName().equals("John")));
    }

    @Test
    @DisplayName("Should create predicate with item name criteria")
    void shouldCreatePredicateWithItemNameCriteria() {
        StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
        criteria.setItemName("S");
        Specification<StudentReward> spec = StudentRewardSpecification.withCriteria(criteria);
        Page<StudentReward> results = studentRewardDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertTrue(results.getContent().stream()
                .allMatch(sr -> sr.getRewardItem().getName().equals("Stickers")));
    }

    @Test
    @DisplayName("Should create predicate with item ID criteria")
    void shouldCreatePredicateWithItemIDCriteria() {
        StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
        criteria.setItemId(rewardItem.getId());
        Specification<StudentReward> spec = StudentRewardSpecification.withCriteria(criteria);
        Page<StudentReward> results = studentRewardDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertTrue(results.getContent().stream()
                .allMatch(sr -> sr.getRewardItem().getName().equals("Stickers")));
    }

    @Test
    @DisplayName("Should create predicate with min points used criteria")
    void shouldCreatePredicateWithMinPointsUsedCriteria() {
        StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
        criteria.setMinPointsUsed(5);
        Specification<StudentReward> spec = StudentRewardSpecification.withCriteria(criteria);
        Page<StudentReward> results = studentRewardDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(2, results.getTotalElements());
    }

    @Test
    @DisplayName("Should create predicate with max points used criteria")
    void shouldCreatePredicateWithMaxPointsUsedCriteria() {
        StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
        criteria.setMaxPointsUsed(10);
        Specification<StudentReward> spec = StudentRewardSpecification.withCriteria(criteria);
        Page<StudentReward> results = studentRewardDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
    }

    @Test
    @DisplayName("Should create predicate with point range criteria")
    void shouldCreatePredicateWithPointRangeCriteria() {
        StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
        criteria.setMinPointsUsed(5);
        criteria.setMaxPointsUsed(10);
        Specification<StudentReward> spec = StudentRewardSpecification.withCriteria(criteria);
        Page<StudentReward> results = studentRewardDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
    }

    @Test
    @DisplayName("Should create predicate with date range criteria")
    void shouldCreatePredicateWithDateRangeCriteria() {
        StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
        criteria.setStartDate(LocalDateTime.now().minusDays(1));
        criteria.setEndDate(LocalDateTime.now());
        Specification<StudentReward> spec = StudentRewardSpecification.withCriteria(criteria);
        Page<StudentReward> results = studentRewardDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(2, results.getTotalElements());
    }

    @Test
    @DisplayName("Should create predicate with all criteria")
    void shouldCreatePredicateWithAllCriteria() {
        StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
        criteria.setStudentName("J");
        criteria.setStudentId(student.getId());
        criteria.setItemName("S");
        criteria.setItemId(rewardItem.getId());
        criteria.setMinPointsUsed(5);
        criteria.setMaxPointsUsed(10);
        criteria.setStartDate(LocalDateTime.now().minusDays(1));
        criteria.setEndDate(LocalDateTime.now());
        Specification<StudentReward> spec = StudentRewardSpecification.withCriteria(criteria);
        Page<StudentReward> results = studentRewardDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertTrue(results.getContent().stream()
                .allMatch(sr -> sr.getStudent().getUser().getFirstName().equals("John")));
        assertTrue(results.getContent().stream()
                .allMatch(sr -> sr.getRewardItem().getName().equals("Stickers")));
    }

    @Test
    @DisplayName("Should create predicate with empty criteria")
    void shouldCreatePredicateWithEmptyCriteria() {
        StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
        Specification<StudentReward> spec = StudentRewardSpecification.withCriteria(criteria);
        Page<StudentReward> results = studentRewardDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(2, results.getTotalElements());
    }

    @Test
    @DisplayName("Should handle case-insensitive search")
    void shouldHandleCaseInsensitiveSearch() {
        StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
        criteria.setStudentName("JOHN");
        criteria.setItemName("STICKERS");
        Specification<StudentReward> spec = StudentRewardSpecification.withCriteria(criteria);
        Page<StudentReward> results = studentRewardDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertTrue(results.getContent().stream()
                .allMatch(sr -> sr.getStudent().getUser().getFirstName().equals("John")));
        assertTrue(results.getContent().stream()
                .allMatch(sr -> sr.getRewardItem().getName().equals("Stickers")));
    }

    @Test
    @DisplayName("Should handle empty name string")
    void shouldHandleEmptyNameString() {
        StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
        criteria.setStudentName("   ");
        criteria.setItemName("   ");
        Specification<StudentReward> spec = StudentRewardSpecification.withCriteria(criteria);
        Page<StudentReward> results = studentRewardDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(2, results.getTotalElements());
    }

    private User createUser(String email, String firstName, String lastName, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        return userDAO.save(user);
    }

    private Student createStudent() {
        User teacherUser = createUser("j.smith@okcps.org", "Jane", "Smith", Role.TEACHER);
        Teacher teacher = new Teacher();
        teacher.setUser(teacherUser);
        teacher.setGrade(GradeLevel.FIRST);
        teacher = teacherDAO.save(teacher);
        User studentUser = createUser("j.doe@okcps.org", "John", "Doe", Role.STUDENT);
        Student student = new Student();
        student.setUser(studentUser);
        student.setTeacher(teacher);
        student.generateToken();
        return studentDAO.save(student);
    }

    private RewardItem createRewardItem(String name, Integer pointCost, Integer stock) {
        RewardItem rewardItem = new RewardItem();
        rewardItem.setName(name);
        rewardItem.setPointCost(pointCost);
        rewardItem.setStock(stock);
        return rewardItemDAO.save(rewardItem);
    }

    private void createStudentReward(Student student, RewardItem rewardItem) {
        StudentReward studentReward = new StudentReward();
        studentReward.setStudent(student);
        studentReward.setRewardItem(rewardItem);
        studentRewardDAO.save(studentReward);
    }
}
