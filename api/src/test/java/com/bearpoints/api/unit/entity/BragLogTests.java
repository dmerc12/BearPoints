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
 *     <li>Points generated (min when set)</li>
 *     <li>Notes (max)</li>
 *  </ul>
 *
 * @see BragLog*
 *
 * @version 1.1
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
        bragLog.setGrade(teacher.getGrade());
        bragLog.setBehaviors(Set.of(behaviorType));
        bragLog.setPointsGenerated(behaviorType.getPointValue());
        bragLog.setNotes("test notes");
        return bragLog;
    }

    private BehaviorType createValidBehaviorType() {
        BehaviorType behaviorType = new BehaviorType();
        behaviorType.setName("valid behavior type" + System.currentTimeMillis());
        behaviorType.setPointValue(3);
        behaviorType.setActive(true);
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

    /** Tests valid brag log passes all constraints */
    @Test
    @DisplayName("Valid brag log passes all constraints")
    public void testBragLogValid() {
        Set<ConstraintViolation<BragLog>> violations = validator.validate(validBragLog);
        assertThat(violations).isEmpty();
    }

    /** Tests @PrePersist */
    @Nested
    @DisplayName("Tests @PrePersist")
    class PrePersistTests {
        /** Tests @PrePersist calculates points generated from behaviors */
        @Test
        @DisplayName("@PrePersist calculates points generated from behaviors when null")
        public void testPrePersistCalculatesPointsGenerated() {
            Teacher teacher = createValidTeacher();
            Student student = createValidStudent(teacher);
            BehaviorType behaviorTyp = createValidBehaviorType();
            BragLog bragLog = new BragLog();
            bragLog.setStudent(student);
            bragLog.setTeacher(teacher);
            bragLog.setBehaviors(Set.of(behaviorTyp));
            bragLog.setDefaultsBeforePersist();
            assertThat(bragLog.getPointsGenerated())
                    .isEqualTo(behaviorTyp.getPointValue());
        }

        /** Tests @PrePersist sets grade level from teacher when null */
        @Test
        @DisplayName("@PrePersist sets grade level from teacher when null")
        public void testPrePersistSetsGradeLevelFromTeacher() {
            Teacher teacher = createValidTeacher();
            Student student = createValidStudent(teacher);
            BehaviorType behaviorType = createValidBehaviorType();
            BragLog bragLog = new BragLog();
            bragLog.setStudent(student);
            bragLog.setTeacher(teacher);
            bragLog.setBehaviors(Set.of(behaviorType));
            bragLog.setNotes("test notes");
            bragLog.setDefaultsBeforePersist();
            assertThat(bragLog.getGrade()).isEqualTo(teacher.getGrade());
        }

        /** Tests @PrePersist doesn't override existing grade level */
        @Test
        @DisplayName("@PrePersist doesn't override existing grade level")
        public void testPrePersistDoesNotOverrideExistingGradeLevel() {
            Teacher teacher = createValidTeacher();
            teacher.setGrade(GradeLevel.PRE_K);
            Student student = createValidStudent(teacher);
            BehaviorType behaviorType = createValidBehaviorType();
            BragLog bragLog = new BragLog();
            bragLog.setStudent(student);
            bragLog.setTeacher(teacher);
            bragLog.setGrade(GradeLevel.FIRST);
            bragLog.setBehaviors(Set.of(behaviorType));
            bragLog.setNotes("test notes");
            bragLog.setDefaultsBeforePersist();
            assertThat(bragLog.getGrade()).isEqualTo(GradeLevel.FIRST);
        }

        /** Tests @PrePersist doesn't override existing points generated */
        @Test
        @DisplayName("@PrePersist doesn't override existing points generated")
        public void testPrePersistDoesNotOverrideExistingPointsGenerated() {
            Teacher teacher = createValidTeacher();
            Student student = createValidStudent(teacher);
            BehaviorType behaviorType = createValidBehaviorType();
            behaviorType.setPointValue(1);
            BragLog bragLog = new BragLog();
            bragLog.setStudent(student);
            bragLog.setTeacher(teacher);
            bragLog.setBehaviors(Set.of(behaviorType));
            bragLog.setPointsGenerated(10);
            bragLog.setNotes("test notes");
            bragLog.setDefaultsBeforePersist();
            assertThat(bragLog.getPointsGenerated()).isEqualTo(10);
        }

        /** Tests @PrePersist when teacher is null */
        @Test
        @DisplayName("@PrePersist doesn't set grade level when teacher is null")
        public void testPrePersistDoesNotSetGradeLevelWhenTeacherIsNull() {
            BehaviorType behaviorType = createValidBehaviorType();
            BragLog bragLog = new BragLog();
            bragLog.setBehaviors(Set.of(behaviorType));
            bragLog.setNotes("test notes");
            bragLog.setDefaultsBeforePersist();
            assertThat(bragLog.getGrade()).isNull();
        }

        /** Tests @PrePersist when behaviors are null */
        @Test
        @DisplayName("@PrePersist doesn't set points generated when behaviors are null")
        public void testPrePersistDoesNotSetPointsGeneratedWhenBehaviorsAreNull() {
            Teacher teacher = createValidTeacher();
            Student student = createValidStudent(teacher);
            BragLog bragLog = new BragLog();
            bragLog.setStudent(student);
            bragLog.setTeacher(teacher);
            bragLog.setNotes("test notes");
            bragLog.setDefaultsBeforePersist();
            assertThat(bragLog.getPointsGenerated()).isNull();
        }

        /** Tests @PrePersist doesn't set points generated when behaviors are empty (not null) */
        @Test
        @DisplayName("@PrePersist doesn't set points generated when behaviors are empty (not null)")
        public void testPrePersistDoesNotSetPointsGeneratedWhenBehaviorsAreEmpty() {
            Teacher teacher = createValidTeacher();
            Student student = createValidStudent(teacher);
            BragLog bragLog = new BragLog();
            bragLog.setStudent(student);
            bragLog.setTeacher(teacher);
            bragLog.setBehaviors(Set.of());
            bragLog.setNotes("test notes");
            bragLog.setDefaultsBeforePersist();
            assertThat(bragLog.getPointsGenerated()).isNull();
        }

    }

    /** Tests points generated validation */
    @Nested
    @DisplayName("Points Generated validation")
    class PointsGeneratedValidation {
        /** Tests points generated validation when below minimum fails validation */
        @Test
        @DisplayName("Points generated below minimum fails validation")
        public void testBragLogPointsGeneratedBelowMin() {
            validBragLog.setPointsGenerated(0);
            Set<ConstraintViolation<BragLog>> violations = validator.validate(validBragLog);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("pointsGenerated"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Minimum points is 1");
        }

        /** Tests points generated passes when null (server will calculate) */
        @Test
        @DisplayName("Points generated null passes validation (server will calculate)")
        public void testBragLogPointsGeneratedNull() {
            validBragLog.setPointsGenerated(null);
            Set<ConstraintViolation<BragLog>> violations = validator.validate(validBragLog);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("pointsGenerated"))
                    .isEmpty();
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
