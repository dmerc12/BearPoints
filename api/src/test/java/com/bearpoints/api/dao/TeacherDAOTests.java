package com.bearpoints.api.dao;

import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.Teacher;
import com.bearpoints.api.entity.User;
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
 * Integration tests for {@link TeacherDAO} data access operations.
 * <p>Verifies:
 * <ul>
 *     <li>Email-based lookups</li>
 *     <li>Grade-based queries</li>
 *     <li>Internal synchronization queries</li>
 *     <li>Database constraints</li>
 * </ul>
 *
 * @see DataJpaTest
 * @see TestEntityManager
 * @version 1.0
 * @author Dylan Mercer
 */
@DataJpaTest
public class TeacherDAOTests {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TeacherDAO teacherDAO;

    private Teacher testTeacher;

    @BeforeEach
    void setup() {
        entityManager.getEntityManager().createQuery("DELETE FROM Teacher").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM User").executeUpdate();
        entityManager.flush();
        entityManager.clear();
        User teacherUser = new User();
        teacherUser.setFirstName("John");
        teacherUser.setLastName("Doe");
        teacherUser.setEmail("john.doe@okcps.org");
        teacherUser.setRole(Role.TEACHER);
        entityManager.persist(teacherUser);
        testTeacher = new Teacher();
        testTeacher.setUser(teacherUser);
        testTeacher.setGrade(GradeLevel.THIRD);
        entityManager.persist(testTeacher);
        entityManager.flush();
    }

    @Test
    @DisplayName("findByUserEmail returns correct teacher")
    void shouldFindTeacherByUserEmail() {
        Optional<Teacher> result = teacherDAO.findByUserEmail(testTeacher.getUser().getEmail());
        assertTrue(result.isPresent());
        assertEquals(testTeacher.getGrade(), result.get().getGrade());
    }

    @Test
    @DisplayName("findByUserEmail returns empty for unknown emails")
    void shouldReturnEmptyForInvalidEmails() {
        Optional<Teacher> result = teacherDAO.findByUserEmail("unknown@okcps.org");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findByGrade returns correct teachers")
    void shouldFindTeacherByGrade() {
        List<Teacher> result = teacherDAO.findByGrade(GradeLevel.THIRD);
        assertEquals(1, result.size());
        assertEquals(testTeacher.getUser().getEmail(), result.getFirst().getUser().getEmail());
    }

    @Test
    @DisplayName("findAll returns all teachers")
    void shouldReturnAllTeachers() {
        List<Teacher> result = teacherDAO.findAll();
        assertEquals(1, result.size());
        assertEquals(testTeacher.getUser().getEmail(), result.getFirst().getUser().getEmail());
    }

    @Test
    @DisplayName("findBySyncedToSheetsFalse returns unsynced teachers")
    void shouldReturnUnsyncedTeachers() {
        List<Teacher> result = teacherDAO.findBySyncedToSheetsFalse();
        assertEquals(1, result.size());
        assertEquals(testTeacher.getUser().getEmail(), result.getFirst().getUser().getEmail());
    }

    @Test
    @DisplayName("Saving teacher with existing user throws DataIntegrityViolation")
    void shouldPreventDuplicateTeacherUsers() {
        Teacher duplicate = new Teacher();
        duplicate.setUser(testTeacher.getUser());
        duplicate.setGrade(GradeLevel.FIRST);
        assertThrows(DataIntegrityViolationException.class, () -> {
            teacherDAO.save(duplicate);
            entityManager.flush();
        });
    }
}
