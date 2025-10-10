package com.bearpoints.api.unit.entity;

import com.bearpoints.api.entity.*;
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
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link BragLog} entity validation and functionality.
 * <p>Tests include:
 * <ul>
 *     <li>Field validation constraints</li>
 *     <li>{@link Syncable} interface implementation</li>
 * </ul>
 * <p>Validation tests cover:
 * <ul>
 *     <li>Student (null)</li>
 *     <li>Teacher (null)</li>
 *     <li>Behaviors (empty)</li>
 *     <li>Points generated (null, min)</li>
 *     <li>Notes (max)</li>
 *  </ul>
 *
 * @see BragLog*
 *
 * @version 1.0
 * @author Dylan Mercer
 */
public class BragLogTests {
    private Validator validator;
    private BragLog validBragLog;

    @BeforeEach
    public void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        validBragLog = createValidBragLog();
    }

    private BragLog createValidBragLog() {
        Teacher teacher = createValidTeacher();
        Student student = createValidStudent(teacher);
        BehaviorType behaviorType = createValidBehaviorType();
        BragLog bragLog = new BragLog();
        bragLog.setStudent(student);
        bragLog.setTeacher(teacher);
        bragLog.setBehaviors(Set.of(behaviorType));
        bragLog.setPointsGenerated(behaviorType.getPointValue());
        bragLog.setNotes("test notes");
        return bragLog;
    }

    private BehaviorType createValidBehaviorType() {
        BehaviorType behaviorType = new BehaviorType();
        behaviorType.setName("valid behavior type");
        return behaviorType;
    }

    private Student createValidStudent(Teacher teacher) {
        User studentUser = createValidUser("valid.student@okcps.org", Role.STUDENT);
        Student student = new Student();
        student.setUser(studentUser);
        student.setTeacher(teacher);
        student.generateToken();
        return student;
    }

    private Teacher createValidTeacher() {
        User teacherUser = createValidUser("valid.teacher@okcps.org", Role.TEACHER);
        Teacher  teacher = new Teacher();
        teacher.setUser(teacherUser);
        teacher.setGrade(GradeLevel.PRE_K);
        return teacher;
    }

    private User createValidUser(String email, Role role) {
        User user = new User();
        user.setEmail(email);
        user.setFirstName("ValidFirstName");
        user.setLastName("ValidLastName");
        user.setRole(role);
        return user;
    }

    /** Tests null student validation */
    @Test
    @DisplayName("Null student fails validation")
    public void testBragLogStudentNull() {
        validBragLog.setStudent(null);
        Set<ConstraintViolation<BragLog>> violations = validator.validate(validBragLog);
        assertThat(violations)
                .filteredOn(v -> v.getPropertyPath().toString().equals("student"))
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("Student is required");
    }

    /** Tests null teacher validation */
    @Test
    @DisplayName("Null teacher fails validation")
    public void testBragLogTeacherNull() {
        validBragLog.setTeacher(null);
        Set<ConstraintViolation<BragLog>> violations = validator.validate(validBragLog);
        assertThat(violations)
                .filteredOn(v -> v.getPropertyPath().toString().equals("teacher"))
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("Teacher is required");
    }

    /** Tests empty behaviors validation */
    @Test
    @DisplayName("Empty behaviors fails validation")
    public void testBragLogEmptyBehaviors() {
        validBragLog.setBehaviors(Set.of());
        Set<ConstraintViolation<BragLog>> violations = validator.validate(validBragLog);
        assertThat(violations)
                .filteredOn(v -> v.getPropertyPath().toString().equals("behaviors"))
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("At least one behavior is required");
    }

    /** Tests valid behavior type creation */
    @Test
    @DisplayName("Valid behavior type passes all constraints")
    public void testBragLogValid() {
        Set<ConstraintViolation<BragLog>> violations = validator.validate(validBragLog);
        assertThat(violations).isEmpty();
    }

    /** Tests points generated validation */
    @Nested
    @DisplayName("Points generated validation")
    class PointsGeneratedValidation {
        /** Tests null points generated validation */
        @Test
        @DisplayName("Null points generated fails validation")
        public void behaviorTypePointValueNull() {
            validBragLog.setPointsGenerated(null);
            Set<ConstraintViolation<BragLog>> violations = validator.validate(validBragLog);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("pointsGenerated"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Points generated is required");
        }

        /** Tests minimum points generated validation */
        @Test
        @DisplayName("Minimum points generated fails validation")
        public void bragLogPointsGeneratedMin() {
            validBragLog.setPointsGenerated(0);
            Set<ConstraintViolation<BragLog>> violations = validator.validate(validBragLog);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("pointsGenerated"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Minimum points is 1");
        }
    }

    /** Tests notes validation */
    @Nested
    @DisplayName("Notes validation")
    class NotesValidation {
        /** Tests notes too long */
        @Test
        @DisplayName("Notes over max fails validation")
        public void bragLogNotesOverMax() {
            validBragLog.setNotes("A".repeat(501));
            Set<ConstraintViolation<BragLog>> violations = validator.validate(validBragLog);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("notes"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Notes cannot exceed 500 characters");
        }

        /** Tests notes at max */
        @Test
        @DisplayName("Notes at max passes validation")
        public void bragLogNotesAtMax() {
            validBragLog.setNotes("A".repeat(500));
            Set<ConstraintViolation<BragLog>> violations = validator.validate(validBragLog);
            assertThat(violations).isEmpty();
        }
    }

    /** Version field tests */
    @Nested
    @DisplayName("Version field tests")
    class VersionTests {
        /** Tests version field setter functionality */
        @Test
        @DisplayName("Version field can be set and retrieved")
        public void versionFieldCanBeSetAndRetrieved() {
            validBragLog.setVersion(5L);
            assertThat(validBragLog.getVersion()).isEqualTo(5L);
        }

        /** Tests that version field doesn't affect validation */
        @Test
        @DisplayName("Version field changes don't affect validation")
        public void versionChangesDontAffectValidation() {
            validBragLog.setVersion(10L);
            Set<ConstraintViolation<BragLog>> violations = validator.validate(validBragLog);
            assertThat(violations).isEmpty();
        }
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
            validBragLog.setSyncedToSheets(true);
            assertThat(validBragLog.getSyncedToSheets()).isTrue();
            validBragLog.setSyncedToSheets(false);
            assertThat(validBragLog.getSyncedToSheets()).isFalse();
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
            validBragLog.setLastSynced(now);
            assertThat(validBragLog.getLastSynced()).isEqualTo(now);
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
            validBragLog.setSheetRowId(42);
            assertThat(validBragLog.getSheetRowId()).isEqualTo(42);
            validBragLog.setSheetRowId(null);
            assertThat(validBragLog.getSheetRowId()).isNull();
        }
    }
}
