package com.bearpoints.api.unit.entity;

import com.bearpoints.api.entity.User;
import com.bearpoints.api.entity.BehaviorType;
import com.bearpoints.api.entity.Syncable;
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
 * Unit tests for {@link BehaviorType} entity validation and functionality.
 * <p>Tests include:
 * <ul>
 *     <li>Field validation constraints</li>
 *     <li>{@link Syncable} interface implementation</li>
 * </ul>
 * <p>Validation tests cover:
 * <ul>
 *   <li>Name (blank, null, length boundaries)</li>
 *   <li>Point Value (null, value boundaries)</li>
 *   <li>Active (null)</li>
 *  </ul>
 *
 * @see BehaviorType
 * @version 1.0
 * @author Dylan Mercer
 */
public class BehaviorTypeTests {
    private Validator validator;
    private BehaviorType validBehaviorType;

    @BeforeEach
    public void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        validBehaviorType = createValidBehaviorType();
    }

    private BehaviorType createValidBehaviorType() {
        BehaviorType behaviorType = new BehaviorType();
        behaviorType.setName("valid behavior type");
        return behaviorType;
    }

    /** Tests valid behavior type creation */
    @Test
    @DisplayName("Valid behavior type passes all constraints")
    public void testBehaviorTypeValid() {
        Set<ConstraintViolation<BehaviorType>> violations = validator.validate(validBehaviorType);
        assertThat(violations).isEmpty();
    }

    /** Tests name validation */
    @Nested
    @DisplayName("Name validation tests")
    class NameValidation {
        /** Tests blank name validation */
        @Test
        @DisplayName("Blank name fails validation")
        public void behaviorTypeNameBlank() {
            validBehaviorType.setName("");
            Set<ConstraintViolation<BehaviorType>> violations = validator.validate(validBehaviorType);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("name"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsAnyOf("Behavior name is required");
        }

        /** Tests null name validation */
        @Test
        @DisplayName("Null name fails validation")
        public void behaviorTypeNameNull() {
            validBehaviorType.setName(null);
            Set<ConstraintViolation<BehaviorType>> violations = validator.validate(validBehaviorType);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("name"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Behavior name is required");
        }

        /** Tests name length boundary validation */
        @Test
        @DisplayName("1-character name passes validation")
        public void behaviorTypeNameMinLength() {
            validBehaviorType.setName("A");
            Set<ConstraintViolation<BehaviorType>> violations = validator.validate(validBehaviorType);
            assertThat(violations).isEmpty();
        }

        /** Tests name length boundary validation */
        @Test
        @DisplayName("50-character name passes validation")
        public void behaviorTypeNameMaxLength() {
            validBehaviorType.setName("A".repeat(50));
            Set<ConstraintViolation<BehaviorType>> violations = validator.validate(validBehaviorType);
            assertThat(violations).isEmpty();
        }

        /** Tests name length boundary validation */
        @Test
        @DisplayName("51-character and over name fails validation")
        public void behaviorTypeNameTooLong() {
            validBehaviorType.setName("A".repeat(51));
            Set<ConstraintViolation<BehaviorType>> violations = validator.validate(validBehaviorType);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("name"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Name must be between 1 and 50 characters");
        }
    }

    /** Tests point value validation */
    @Nested
    @DisplayName("Point value validation tests")
    class PointValueValidation {
        /** Tests null point value validation */
        @Test
        @DisplayName("Null point value fails validation")
        public void behaviorTypePointValueNull() {
            validBehaviorType.setPointValue(null);
            Set<ConstraintViolation<BehaviorType>> violations = validator.validate(validBehaviorType);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("pointValue"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Point value is required");
        }

        /** Tests point value boundary validation */
        @Test
        @DisplayName("Point value of 1 passes validation")
        public void behaviorTypePointValueMin() {
            validBehaviorType.setPointValue(1);
            Set<ConstraintViolation<BehaviorType>> violations = validator.validate(validBehaviorType);
            assertThat(violations).isEmpty();
        }

        /** Tests point value boundary validation */
        @Test
        @DisplayName("Point value of 5 passes validation")
        public void behaviorTypePointValueMax() {
            validBehaviorType.setPointValue(5);
            Set<ConstraintViolation<BehaviorType>> violations = validator.validate(validBehaviorType);
            assertThat(violations).isEmpty();
        }

        /** Tests point value boundary validation */
        @Test
        @DisplayName("Point value of 6 and over fails validation")
        public void behaviorTypePointValueAboveMax() {
            validBehaviorType.setPointValue(6);
            Set<ConstraintViolation<BehaviorType>> violations = validator.validate(validBehaviorType);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("pointValue"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Maximum point value is 5");
        }

        /** Tests point value boundary validation */
        @Test
        @DisplayName("Point value below 0 and below fails validation")
        public void behaviorTypePointValueBelowMin() {
            validBehaviorType.setPointValue(0);
            Set<ConstraintViolation<BehaviorType>> violations = validator.validate(validBehaviorType);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("pointValue"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Minimum point value is 1");
        }
    }

    /** Tests null active status validation */
    @Test
    @DisplayName("Null active status fails validation")
    public void behaviorTypeActiveStatusNull() {
        validBehaviorType.setActive(null);
        Set<ConstraintViolation<BehaviorType>> violations = validator.validate(validBehaviorType);
        assertThat(violations)
                .filteredOn(v -> v.getPropertyPath().toString().equals("active"))
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("Active status is required");
    }

    /** Version field tests */
    @Nested
    @DisplayName("Version field tests")
    class VersionTests {/** Tests version field setter functionality */
        @Test
        @DisplayName("Version field can be set and retrieved")
        public void versionFieldCanBeSetAndRetrieved() {
            validBehaviorType.setVersion(5L);
            assertThat(validBehaviorType.getVersion()).isEqualTo(5L);
        }

        /** Tests that version field doesn't affect validation */
        @Test
        @DisplayName("Version field changes don't affect validation")
        public void versionChangesDontAffectValidation() {
            validBehaviorType.setVersion(10L);
            Set<ConstraintViolation<BehaviorType>> violations = validator.validate(validBehaviorType);
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
            validBehaviorType.setSyncedToSheets(true);
            assertThat(validBehaviorType.getSyncedToSheets()).isTrue();
            validBehaviorType.setSyncedToSheets(false);
            assertThat(validBehaviorType.getSyncedToSheets()).isFalse();
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
            validBehaviorType.setLastSynced(now);
            assertThat(validBehaviorType.getLastSynced()).isEqualTo(now);
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
            validBehaviorType.setSheetRowId(42);
            assertThat(validBehaviorType.getSheetRowId()).isEqualTo(42);
            validBehaviorType.setSheetRowId(null);
            assertThat(validBehaviorType.getSheetRowId()).isNull();
        }
    }
}
