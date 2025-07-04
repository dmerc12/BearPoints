package com.bearpoints.api.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StudentReward} entity validation and functionality.
 * <p>Tests include:
 * <ul>
 *     <li>Field validation constraints</li>
 *     <li>{@link Syncable} interface implementation</li>
 * </ul>
 * <p>Validation tests cover:
 * <ul>
 *     <li>Student ID (null)</li>
 *     <li>Reward Item ID (null)</li>
 *  </ul>
 *
 * @see StudentReward
 *
 * @version 1.0
 * @author Dylan Mercer
 */
public class StudentRewardTests {
    private Validator validator;
    private StudentReward validStudentReward;

    @BeforeEach
    public void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        validStudentReward = createValidStudentReward();
    }

    private StudentReward createValidStudentReward() {
        User teacherUser = new User();
        teacherUser.setEmail("valid.teacher@okcps.org");
        teacherUser.setFirstName("ValidFirstName");
        teacherUser.setLastName("ValidLastName");
        teacherUser.setRole(Role.TEACHER);
        Teacher  teacher = new Teacher();
        teacher.setUser(teacherUser);
        teacher.setGrade(GradeLevel.PRE_K);
        User studentUser = new User();
        studentUser.setEmail("valid.student@okcps.org");
        studentUser.setFirstName("ValidFirstName");
        studentUser.setLastName("ValidLastName");
        studentUser.setRole(Role.STUDENT);
        Student student = new Student();
        student.setUser(studentUser);
        student.setTeacher(teacher);
        student.generateToken();
        RewardItem rewardItem = new RewardItem();
        rewardItem.setName("valid reward item");
        rewardItem.setPointCost(1);
        rewardItem.setStock(4);
        StudentReward studentReward = new StudentReward();
        studentReward.setStudent(student);
        studentReward.setRewardItem(rewardItem);
        return studentReward;
    }

    /** Tests valid student reward creation */
    @Test
    @DisplayName("Valid student reward passes validation")
    public void testStudentRewardValid() {
        Set<ConstraintViolation<StudentReward>> violations = validator.validate(validStudentReward);
        assertThat(violations).isEmpty();
    }

    /** Tests null student validation */
    @Test
    @DisplayName("Null student fails validation")
    public void testStudentRewardStudentNull() {
        validStudentReward.setStudent(null);
        Set<ConstraintViolation<StudentReward>> violations = validator.validate(validStudentReward);
        assertThat(violations)
                .filteredOn(v -> v.getPropertyPath().toString().equals("student"))
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("Student is required");
    }

    /** Tests null reward item validation */
    @Test
    @DisplayName("Null reward item fails validation")
    public void testStudentRewardRewardItemNull() {
        validStudentReward.setRewardItem(null);
        Set<ConstraintViolation<StudentReward>> violations = validator.validate(validStudentReward);
        assertThat(violations)
                .filteredOn(v -> v.getPropertyPath().toString().equals("rewardItem"))
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("Reward item is required");
    }

    /** Tests for {@link Syncable} interface methods implemented in {@link User}. */
    @Nested
    @DisplayName("Syncable interface implementation tests")
    class SyncableTests {
        /**
         * Verifies that setting and getting the sync status works correctly.
         * <p>
         * Sets the sync status to true and false, then verifies the values
         * through getter the getter method.
         */
        @Test
        @DisplayName("Set and get synced status")
        void testSyncedStatus() {
            validStudentReward.setSyncedToSheets(true);
            assertThat(validStudentReward.getSyncedToSheets()).isTrue();
            validStudentReward.setSyncedToSheets(false);
            assertThat(validStudentReward.getSyncedToSheets()).isFalse();
        }

        /**
         * Verifies that setting and getting the last sync timestamp works correctly.
         * <p>
         * Sets the last synced timestamp to the current time, then verifies the value matches
         * through the getter method.
         */
        @Test
        @DisplayName("Set and get last synced timestamp")
        void testLastSynced() {
            LocalDateTime now = LocalDateTime.now();
            validStudentReward.setLastSynced(now);
            assertThat(validStudentReward.getLastSynced()).isEqualTo(now);
        }

        /**
         * Verifies that setting and getting the sheet row ID works correctly.
         * <p>
         * Sets the sheet row ID to a non-null value and null, then verifies the value
         * through the getter method
         */
        @Test
        @DisplayName("Set and get sheet row ID")
        void testSheetRowId() {
            validStudentReward.setSheetRowId(42);
            assertThat(validStudentReward.getSheetRowId()).isEqualTo(42);
            validStudentReward.setSheetRowId(null);
            assertThat(validStudentReward.getSheetRowId()).isNull();
        }
    }
}
