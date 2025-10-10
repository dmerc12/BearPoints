package com.bearpoints.api.integration.dao;

import com.bearpoints.api.dao.BragLogDAO;
import com.bearpoints.api.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional integration tests for {@link BragLogDAO}.
 * <p>Verifies data access operations:
 * <ul>
 *     <li>Student-specific log retrieval</li>
 *     <li>Global log retrieval</li>
 *     <li>Synchronization status filtering</li>
 *     <li>Temporal filtering</li>
 *     <li>Entity persistence</li>
 * </ul>
 *
 * <p>Test Setup:
 * <ul>
 *     <li>Creates complete entity graph with timestamped brag log</li>
 *     <li>Clears database state before each test</li>
 *     <li>Uses Hibernate's automatic timestamp generation</li>
 * </ul>
 *
 * @see DataJpaTest
 * @see TestEntityManager
 * @version 1.0
 * @author Dylan Mercer
 */
@DataJpaTest
public class BragLogDAOTests {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BragLogDAO bragLogDAO;

    private Student testStudent;
    private Teacher testTeacher;
    private BehaviorType testBehaviorType;
    private BragLog testBragLog;

    @BeforeEach
    void setup() {
        entityManager.getEntityManager().createQuery("DELETE FROM BragLog").executeUpdate();
        entityManager.getEntityManager().createQuery("DELETE FROM BehaviorType").executeUpdate();
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
        testTeacher.setGrade(GradeLevel.PRE_K);
        entityManager.persist(testTeacher);
        User studentUser = new User();
        studentUser.setFirstName("Student");
        studentUser.setLastName("User");
        studentUser.setEmail("student.user@okcps.org");
        studentUser.setRole(Role.STUDENT);
        entityManager.persist(studentUser);
        testStudent = new Student();
        testStudent.setUser(studentUser);
        testStudent.generateToken();
        testStudent.setTeacher(testTeacher);
        entityManager.persist(testStudent);
        testBehaviorType = new BehaviorType();
        testBehaviorType.setName("Test Behavior");
        testBehaviorType.setPointValue(3);
        testBehaviorType.setActive(true);
        entityManager.persist(testBehaviorType);
        testBragLog = new BragLog();
        testBragLog.setStudent(testStudent);
        testBragLog.setTeacher(testTeacher);
        testBragLog.setPointsGenerated(3);
        testBragLog.setTimestamp(LocalDateTime.now());
        testBragLog.setBehaviors(Set.of(testBehaviorType));
        entityManager.persist(testBragLog);
        entityManager.flush();
    }

    @Test
    @DisplayName("findByStudent returns correct brag logs")
    void shouldFindByStudent() {
        List<BragLog> result = bragLogDAO.findByStudent(testStudent);
        assertEquals(1, result.size());
        assertTrue(result.contains(testBragLog));
    }

    @Test
    @DisplayName("findAll returns all brag logs")
    void shouldFindAll() {
        List<BragLog> result = bragLogDAO.findAll();
        assertEquals(1, result.size());
        assertTrue(result.contains(testBragLog));
    }

    @Test
    @DisplayName("findBySyncedToSheetsFalse returns unsynced brag logs")
    void shouldFindBySyncedToSheetsFalse() {
        List<BragLog> result = bragLogDAO.findBySyncedToSheetsFalse();
        assertEquals(1, result.size());
        assertTrue(result.contains(testBragLog));
    }

    @Test
    @DisplayName("findByTimestampAfter returns recent brag logs")
    void shouldFindByTimestampAfter() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(1);
        List<BragLog> result = bragLogDAO.findByTimestampAfter(cutoff);
        assertEquals(1, result.size());
        assertTrue(result.contains(testBragLog));
    }

    @Test
    @DisplayName("Saving brag log persists correctly")
    void shouldSaveBragLog() {
        BragLog newLog = new BragLog();
        newLog.setStudent(testStudent);
        newLog.setTeacher(testTeacher);
        newLog.setPointsGenerated(3);
        newLog.setBehaviors(Set.of(testBehaviorType));
        BragLog saved = bragLogDAO.save(newLog);
        assertNotNull(saved.getId());
        assertEquals(3, saved.getPointsGenerated());
    }

    @Test
    @DisplayName("Version is automatically set after persistence")
    void versionIsSetAfterPersistence() {
        BragLog newLog = new BragLog();
        newLog.setStudent(testStudent);
        newLog.setTeacher(testTeacher);
        newLog.setPointsGenerated(3);
        newLog.setBehaviors(Set.of(testBehaviorType));
        assertNull(newLog.getVersion());
        BragLog saved = bragLogDAO.save(newLog);
        assertNotNull(saved.getVersion());
        assertEquals(0L, saved.getVersion());
    }
}
