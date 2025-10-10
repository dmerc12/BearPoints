package com.bearpoints.api.integration.dao;

import com.bearpoints.api.dao.StudentRewardDAO;
import com.bearpoints.api.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Functional integration tests for {@link StudentRewardDAO}.
 * <p>Verifies data access operations:
 * <ul>
 *     <li>Entity persistence and retrieval</li>
 *     <li>Synchronization status filtering</li>
 *     <li>Database constraints</li>
 * </ul>
 *
 * <p>Test Setup:
 * <ul>
 *     <li>Creates complete entity graph (Student + RewardItem)</li>
 *     <li>Clears database before each test</li>
 *     <li>Tests reward redemption scenarios</li>
 * </ul>
 *
 * @see DataJpaTest
 * @see TestEntityManager
 * @version 1.0
 * @author Dylan Mercer
 */
@DataJpaTest
public class StudentRewardDAOTests {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private StudentRewardDAO studentRewardDAO;

    private Student testStudent;
    private RewardItem testReward;

    @BeforeEach
    void setup() {
        entityManager.clear();
        User teacherUser = new User();
        teacherUser.setFirstName("Teacher");
        teacherUser.setLastName("User");
        teacherUser.setEmail("teacher.user@okcps.org");
        teacherUser.setRole(Role.TEACHER);
        entityManager.persist(teacherUser);
        Teacher testTeacher = new Teacher();
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
        testStudent.generateToken();
        testStudent.setTeacher(testTeacher);
        testStudent.setPoints(100);
        entityManager.persist(testStudent);
        testReward = new RewardItem();
        testReward.setName("Art Kit");
        testReward.setPointCost(50);
        testReward.setStock(10);
        entityManager.persist(testReward);
        StudentReward testRedemption = new StudentReward();
        testRedemption.setStudent(testStudent);
        testRedemption.setRewardItem(testReward);
        entityManager.persist(testRedemption);
        entityManager.flush();
    }

    @Test
    @DisplayName("Saving student reward persists correctly")
    void shouldSaveStudentReward() {
        StudentReward newRedemption = new StudentReward();
        newRedemption.setStudent(testStudent);
        newRedemption.setRewardItem(testReward);
        StudentReward saved = studentRewardDAO.save(newRedemption);
        assertNotNull(saved.getId());
        assertNotNull(saved.getRedeemedAt());
        assertEquals(testStudent.getId(), saved.getStudent().getId());
    }

    @Test
    @DisplayName("findBySyncedToSheetsFalse returns unsynced records")
    void shouldReturnUnsyncedRedemptions() {
        List<StudentReward> result = studentRewardDAO.findBySyncedToSheetsFalse();
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("RedeemedAt timestamp is automatically generated")
    void shouldGenerateRedeemedAtTimestamp() {
        StudentReward newRedemption = new StudentReward();
        newRedemption.setStudent(testStudent);
        newRedemption.setRewardItem(testReward);
        StudentReward saved = studentRewardDAO.save(newRedemption);
        assertNotNull(saved.getRedeemedAt());
        assertTrue(saved.getRedeemedAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    @DisplayName("Version is automatically set after persistence")
    void versionIsSetAfterPersistence() {
        StudentReward newRedemption = new StudentReward();
        newRedemption.setStudent(testStudent);
        newRedemption.setRewardItem(testReward);
        assertNull(newRedemption.getVersion());
        StudentReward saved = studentRewardDAO.save(newRedemption);
        assertNotNull(saved.getVersion());
        assertEquals(0L, saved.getVersion());
    }
}
