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
 * Unit tests for {@link RewardItem} entity validation and functionality.
 * <p>Tests include:
 * <ul>
 *     <li>Field validation constraints</li>
 *     <li>{@link Syncable} interface implementation</li>
 * </ul>
 * <p>Validation tests cover:
 * <ul>
 *     <li>Name (blank, null, length boundaries)</li>
 *     <li>Point cost (null, min)</li>
 *     <li>Stock (null, min)</li>
 *  </ul>
 *
 * @see RewardItem
 *
 * @version 1.0
 * @author Dylan Mercer
 */
public class RewardItemTests {
    private Validator validator;
    private RewardItem validRewardItem;

    @BeforeEach
    public void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        validRewardItem = createValidRewardItem();
    }

    private RewardItem createValidRewardItem() {
        RewardItem rewardItem = new RewardItem();
        rewardItem.setName("valid reward item");
        rewardItem.setPointCost(1);
        rewardItem.setStock(4);
        return rewardItem;
    }

    @Test
    @DisplayName("Valid reward item passes validation")
    public void testRewardItemValid() {
        Set<ConstraintViolation<RewardItem>> violations = validator.validate(validRewardItem);
        assertThat(violations).isEmpty();
    }

    /** Tests name validation */
    @Nested
    @DisplayName("Tests name validation")
    class NameValidation {
        /** Tests blank name validation */
        @Test
        @DisplayName("Blank name fails validation")
        public void rewardItemNameBlank() {
            validRewardItem.setName("");
            Set<ConstraintViolation<RewardItem>> violations = validator.validate(validRewardItem);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("name"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsAnyOf("Item name is required");
        }

        /** Tests null name validation */
        @Test
        @DisplayName("Null name fails validation")
        public void rewardItemNameNull() {
            validRewardItem.setName(null);
            Set<ConstraintViolation<RewardItem>> violations = validator.validate(validRewardItem);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("name"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Item name is required");
        }

        /** Tests name length boundary validation */
        @Test
        @DisplayName("1-character name passes validation")
        public void RewardItemNameMinLength() {
            validRewardItem.setName("A");
            Set<ConstraintViolation<RewardItem>> violations = validator.validate(validRewardItem);
            assertThat(violations).isEmpty();
        }

        /** Tests name length boundary validation */
        @Test
        @DisplayName("50-character name passes validation")
        public void rewardItemNameMaxLength() {
            validRewardItem.setName("A".repeat(50));
            Set<ConstraintViolation<RewardItem>> violations = validator.validate(validRewardItem);
            assertThat(violations).isEmpty();
        }

        /** Tests name length boundary validation */
        @Test
        @DisplayName("51-character and over name fails validation")
        public void rewardItemNameTooLong() {
            validRewardItem.setName("A".repeat(51));
            Set<ConstraintViolation<RewardItem>> violations = validator.validate(validRewardItem);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("name"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Name must be between 1 and 50 characters");
        }
    }

    /** Tests point cost validation */
    @Nested
    @DisplayName("Point cost validation tests")
    class PointCostValidation {
        /** Tests null point cost validation */
        @Test
        @DisplayName("Null point value fails validation")
        public void rewardItemPointCostNull() {
            validRewardItem.setPointCost(null);
            Set<ConstraintViolation<RewardItem>> violations = validator.validate(validRewardItem);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("pointCost"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Point cost is required");
        }

        /** Tests point cost boundary validation */
        @Test
        @DisplayName("Point cost of 0 passes validation")
        public void rewardItemPointCostMin() {
            validRewardItem.setPointCost(0);
            Set<ConstraintViolation<RewardItem>> violations = validator.validate(validRewardItem);
            assertThat(violations).isEmpty();
        }

        /** Tests point cost boundary validation */
        @Test
        @DisplayName("Point cost below 0 and below fails validation")
        public void rewardItemPointCostBelowMin() {
            validRewardItem.setPointCost(-1);
            Set<ConstraintViolation<RewardItem>> violations = validator.validate(validRewardItem);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("pointCost"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Minimum cost is 0 points");
        }
    }

    /** Tests stock quantity validation */
    @Nested
    @DisplayName("Stock quantity validation tests")
    class PointValueValidation {
        /** Tests null stock quantity validation */
        @Test
        @DisplayName("Null stock quantity fails validation")
        public void rewardItemStockNull() {
            validRewardItem.setStock(null);
            Set<ConstraintViolation<RewardItem>> violations = validator.validate(validRewardItem);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("stock"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Stock quantity is required");
        }

        /** Tests stock quantity boundary validation */
        @Test
        @DisplayName("Stock quantity of 0 passes validation")
        public void rewardItemStockMin() {
            validRewardItem.setStock(0);
            Set<ConstraintViolation<RewardItem>> violations = validator.validate(validRewardItem);
            assertThat(violations).isEmpty();
        }

        /** Tests stock quantity boundary validation */
        @Test
        @DisplayName("Stock quantity below 0 and below fails validation")
        public void rewardItemStockBelowMin() {
            validRewardItem.setStock(-1);
            Set<ConstraintViolation<RewardItem>> violations = validator.validate(validRewardItem);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("stock"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Minimum stock quantity is 0");
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
            validRewardItem.setSyncedToSheets(true);
            assertThat(validRewardItem.getSyncedToSheets()).isTrue();
            validRewardItem.setSyncedToSheets(false);
            assertThat(validRewardItem.getSyncedToSheets()).isFalse();
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
            validRewardItem.setLastSynced(now);
            assertThat(validRewardItem.getLastSynced()).isEqualTo(now);
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
            validRewardItem.setSheetRowId(42);
            assertThat(validRewardItem.getSheetRowId()).isEqualTo(42);
            validRewardItem.setSheetRowId(null);
            assertThat(validRewardItem.getSheetRowId()).isNull();
        }
    }
}
