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
 *     <li>Submitter name (blank, min, max)</li>
 *  </ul>
 *
 * @see BragLog*
 *
 * @version 1.2
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
        bragLog.setSubmitterName("John Doe");
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
        User teacherUser = createValidUser("valid.teacher" + 1L + "@okcps.org", Role.TEACHER);
        Teacher  teacher = new Teacher();
        teacher.setId(1L);
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

        /** Tests @PrePersist sets teacher from student when null */
        @Test
        @DisplayName("@PrePersist sets teacher from student when null")
        public void testPrePersistSetsTeacherFromStudent() {
            Teacher teacher = createValidTeacher();
            Student student = createValidStudent(teacher);
            BehaviorType behaviorType = createValidBehaviorType();
            BragLog bragLog = new BragLog();
            bragLog.setStudent(student);
            bragLog.setGrade(GradeLevel.PRE_K);
            bragLog.setBehaviors(Set.of(behaviorType));
            bragLog.setNotes("test notes");
            bragLog.setDefaultsBeforePersist();
            assertThat(bragLog.getTeacher().getId()).isEqualTo(teacher.getId());
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

    /** Tests grade validation */
    @Nested
    @DisplayName("Grade validation")
    class GradeValidation {
        /** Tests grade validation for all valid grade levels */
        @Test
        @DisplayName("All valid grade levels pass grade validation")
        void allValidGradeLevelsPassGradeValidation() {
            for (GradeLevel grade : GradeLevel.values()) {
                validBragLog.setGrade(grade);
                Set<ConstraintViolation<BragLog>> violations = validator.validate(validBragLog);
                assertThat(violations).isEmpty();
            }
        }

        /** Tests null grade validation */
        @Test
        @DisplayName("Null grade level fails validation")
        void nullGradeLevelFailsValidation() {
            validBragLog.setGrade(null);
            Set<ConstraintViolation<BragLog>> violations = validator.validate(validBragLog);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("grade"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Grade is required");
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

    /** Submitter name tests */
    @Nested
    @DisplayName("Submitter name validation tests")
    class SubmitterNameValidation {
        /** Tests blank submitter name validation */
        @Test
        @DisplayName("Blank submitter name fails validation")
        public void submitterNameBlank() {
            validBragLog.setSubmitterName("");
            Set<ConstraintViolation<BragLog>> violations = validator.validate(validBragLog);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("submitterName"))
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Submitter name is required")
                    .contains("Submitter name must be between 2 and 250 characters");
        }

        /** Tests null submitter name validation */
        @Test
        @DisplayName("Null submitter name fails validation")
        public void submitterNameNull() {
            validBragLog.setSubmitterName(null);
            Set<ConstraintViolation<BragLog>> violations = validator.validate(validBragLog);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("submitterName"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Submitter name is required");
        }

        /** Tests submitter name below min length validation */
        @Test
        @DisplayName("1-character submitter name fails validation")
        public void submitterNameOneCharacter() {
            validBragLog.setSubmitterName("A");
            Set<ConstraintViolation<BragLog>> violations = validator.validate(validBragLog);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("submitterName"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Submitter name must be between 2 and 250 characters");
        }

        /** Tests submitter name min length validation */
        @Test
        @DisplayName("2-character submitter name passes validation")
        public void submitterNameTwoCharacters() {
            validBragLog.setSubmitterName("AJ");
            Set<ConstraintViolation<BragLog>> violations = validator.validate(validBragLog);
            assertThat(violations).isEmpty();
        }

        /** Tests submitter name max length validation */
        @Test
        @DisplayName("250-character submitter name passes validation")
        public void submitterNameTwoHundredFiftyCharacters() {
            String validName = "A".repeat(250);
            validBragLog.setSubmitterName(validName);
            Set<ConstraintViolation<BragLog>> violations = validator.validate(validBragLog);
            assertThat(violations).isEmpty();
        }

        /** Tests submitter name over max length validation */
        @Test
        @DisplayName("251-character submitter name fails validation")
        public void submitterNameTwoHundredFiftyOneCharacters() {
            String longName = "A".repeat(251);
            validBragLog.setSubmitterName(longName);
            Set<ConstraintViolation<BragLog>> violations = validator.validate(validBragLog);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("submitterName"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Submitter name must be between 2 and 250 characters");
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
