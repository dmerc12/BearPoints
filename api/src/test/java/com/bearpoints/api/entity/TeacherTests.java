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
 * Unit tests for {@link Teacher} entity validation and functionality.
 * <p>Tests include:
 * <ul>
 *     <li>Field validation constraints</li>
 *     <li>{@link Syncable} interface implementation</li>
 *     <li>User assignment validation</li>
 * </ul>
 * <p>Validation tests cover:
 * <ul>
 *   <li>Grade (blank, null, invalid)</li>
 *   <li>User (null)</li>
 *  </ul>
 *
 * @see Teacher
 * @version 1.0
 * @author Dylan Mercer
 */
public class TeacherTests {
    private Validator validator;
    private Teacher validTeacher;

    @BeforeEach
    public void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        validTeacher = createValidTeacher();
    }

    private Teacher createValidTeacher() {
        User user = new User();
        user.setEmail("valid.user@okcps.org");
        user.setFirstName("ValidFirstName");
        user.setLastName("ValidLastName");
        user.setRole(Role.TEACHER);
        Teacher  teacher = new Teacher();
        teacher.setUser(user);
        teacher.setGrade(GradeLevel.PRE_K);
        return teacher;
    }

    /** Tests valid teacher creation */
    @Test
    @DisplayName("Valid teacher passes all constraints")
    public void testTeacherValid() {
        Set<ConstraintViolation<Teacher>> violations = validator.validate(validTeacher);
        assertThat(violations).isEmpty();
    }

    /** Tests null grade validation */
    @Test
    @DisplayName("Null grade fails validation")
    public void testTeacherGradeNull() {
        validTeacher.setGrade(null);
        Set<ConstraintViolation<Teacher>> violations = validator.validate(validTeacher);
        assertThat(violations)
                .filteredOn(v -> v.getPropertyPath().toString().equals("grade"))
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("Grade is required");
    }

    /** Tests null user validation */
    @Test
    @DisplayName("Null user fails validation")
    public void testTeacherUserNull() {
        validTeacher.setUser(null);
        Set<ConstraintViolation<Teacher>> violations = validator.validate(validTeacher);
        assertThat(violations)
                .filteredOn(v -> v.getPropertyPath().toString().equals("user"))
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("User reference is required");
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
            validTeacher.setSyncedToSheets(true);
            assertThat(validTeacher.getSyncedToSheets()).isTrue();
            validTeacher.setSyncedToSheets(false);
            assertThat(validTeacher.getSyncedToSheets()).isFalse();
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
            validTeacher.setLastSynced(now);
            assertThat(validTeacher.getLastSynced()).isEqualTo(now);
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
            validTeacher.setSheetRowId(42);
            assertThat(validTeacher.getSheetRowId()).isEqualTo(42);
            validTeacher.setSheetRowId(null);
            assertThat(validTeacher.getSheetRowId()).isNull();
        }
    }
}
