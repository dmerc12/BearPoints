package com.bearpoints.api.integration.dao;

import com.bearpoints.api.dao.StudentDAO;
import com.bearpoints.api.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link StudentDAO} data access operations.
 * <p>Verifies:
 * <ul>
 *     <li>Token-based student lookup</li>
 *     <li>Email-based student queries</li>
 *     <li>Teacher-based student filtering</li>
 *     <li>All students retrieval</li>
 *     <li>Synchronization status filtering</li>
 *     <li>Database constraints and uniqueness validation</li>
 * </ul>
 *
 * <p>Test Setup:
 * <ul>
 *     <li>Creates test teacher and student entities</li>
 *     <li>Clears database before each test</li>
 *     <li>Configures consistent test data state</li>
 * </ul>
 *
 * @see DataJpaTest
 * @see TestEntityManager
 * @version 1.1
 * @author Dylan Mercer
 */
@DataJpaTest
public class StudentDAOTests {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StudentDAO studentDAO;

    private Student testStudent;

    private Teacher testTeacher;

    @BeforeEach
    void setup() {
        entityManager.getEntityManager().createQuery("DELETE FROM Student").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM Teacher").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM User").executeUpdate();
        entityManager.flush();
        entityManager.clear();
        User teacherUser = new User();
        teacherUser.setFirstName("Teacher");
        teacherUser.setLastName("User");
        teacherUser.setEmail("teacher.user@okcps.org");
        teacherUser.setRole(Role.TEACHER);
        entityManager.persist(teacherUser);
        testTeacher = new Teacher();
        testTeacher.setUser(teacherUser);
        testTeacher.setGrade(GradeLevel.FIRST);
        entityManager.persist(testTeacher);
        User studentUser = new User();
        studentUser.setFirstName("Student");
        studentUser.setLastName("User");
        studentUser.setEmail("student.user@okcps.org");
        studentUser.setRole(Role.STUDENT);
        entityManager.persist(studentUser);
        testStudent = new Student();
        testStudent.setUser(studentUser);
        testStudent.setToken("TEST_TOKEN");
        testStudent.setTeacher(testTeacher);
        entityManager.persist(testStudent);
        entityManager.flush();
    }

    @Test
    @DisplayName("findByToken returns correct student")
    void shouldFindByToken() {
        Optional<Student> result = studentDAO.findByToken(testStudent.getToken());
        assertTrue(result.isPresent());
        assertEquals(testStudent.getId(), result.get().getId());
    }

    @Test
    @DisplayName("findByToken returns empty for invalid token")
    void shouldNotFindByInvalidToken() {
        Optional<Student> result = studentDAO.findByToken("INVALID_TOKEN");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByUserEmail returns correct student")
    void shouldFindByUserEmail() {
        Optional<Student> result = studentDAO.findByUserEmail(testStudent.getUser().getEmail());
        assertTrue(result.isPresent());
        assertEquals(testStudent.getId(), result.get().getId());
    }

    @Test
    @DisplayName("findByUserEmail returns empty for unknown email")
    void shouldNotFindByInvalidEmail() {
        Optional<Student> result = studentDAO.findByUserEmail("unknown@okcps.org");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByTeacher returns correct students")
    void shouldFindByTeacher() {
        List<Student> result = studentDAO.findByTeacher(testTeacher);
        assertEquals(1, result.size());
        assertEquals(testStudent.getId(), result.getFirst().getId());
    }

    @Test
    @DisplayName("findByTeacher returns empty when teacher has no students")
    void shouldNotFindByTeacherWithNoStudents() {
        User teacherUser = new User();
        teacherUser.setFirstName("Other");
        teacherUser.setLastName("User");
        teacherUser.setEmail("other.user@okcps.org");
        teacherUser.setRole(Role.TEACHER);
        entityManager.persist(teacherUser);
        Teacher otherTeacher = new Teacher();
        otherTeacher.setUser(teacherUser);
        otherTeacher.setGrade(GradeLevel.FIRST);
        entityManager.persist(otherTeacher);
        entityManager.flush();
        List<Student> result = studentDAO.findByTeacher(otherTeacher);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findAll returns all students")
    void shouldReturnAllStudents() {
        List<Student> result = studentDAO.findAll();
        assertEquals(1, result.size());
        assertEquals(testStudent.getId(), result.getFirst().getId());
    }

    @Test
    @DisplayName("findBySyncedToSheets returns unsynced students")
    void shouldReturnUnsyncedStudents() {
        List<Student> result = studentDAO.findBySyncedToSheetsFalse();
        assertEquals(1, result.size());
        assertEquals(testStudent.getId(), result.getFirst().getId());
    }

    @Test
    @DisplayName("Saving student with duplicate token throws exception")
    void shouldPreventDuplicateTokens() {
        User newUser = new User();
        newUser.setFirstName("Student");
        newUser.setLastName("User");
        newUser.setEmail("new.user@okcps.org");
        newUser.setRole(Role.STUDENT);
        entityManager.persist(newUser);
        Student newStudent = new Student();
        newStudent.setUser(newUser);
        newStudent.setTeacher(testTeacher);
        newStudent.setToken("TEST_TOKEN");
        assertThrows(DataIntegrityViolationException.class, () -> {
            studentDAO.save(newStudent);
            entityManager.flush();
        });
    }
}
