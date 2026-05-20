package com.bearpoints.api.unit.criteria;

import com.bearpoints.api.criteria.StudentRewardSearchCriteria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * Unit tests for {@link StudentRewardSearchCriteria}.
 * <p>Verifies that search criteria correctly tracks filter presence and
 * handles various combinations appropriately.
 *
 * <p>Test scenarios cover:
 * <ul>
 *     <li>Individual filter detection</li>
 *     <li>Combined filter scenarios</li>
 * </ul>
 *
 * @see StudentRewardSearchCriteria
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("StudentRewardSearchCriteria Tests")
public class StudentRewardSearchCriteriaTests {
    @Nested
    @DisplayName("When checking hasFilters with individual criteria")
    class WhenCheckingHasFiltersWithIndividualCriteria {
        @Test
        @DisplayName("Should return true when studentName filter is set")
        void shouldReturnTrueWhenStudentNameFilterIsSet() {
            StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
            criteria.setStudentName("John D");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when itemName filter is set")
        void shouldReturnTrueWhenItemNameFilterIsSet() {
            StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
            criteria.setItemName("Stick");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when minPointsUsed filter is set")
        void shouldReturnTrueWhenMinPointsUsedFilterIsSet() {
            StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
            criteria.setMinPointsUsed(5);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when maxPointsUsed filter is set")
        void shouldReturnTrueWhenMaxPointsUsedFilterIsSet() {
            StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
            criteria.setMaxPointsUsed(50);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when startDate filter is set")
        void shouldReturnTrueWhenStartDateFilterIsSet() {
            StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
            criteria.setStartDate(LocalDateTime.now().minusDays(5));
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when endDate filter is set")
        void shouldReturnTrueWhenEndDateFilterIsSet() {
            StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
            criteria.setEndDate(LocalDateTime.now());
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when studentId filter is set")
        void shouldReturnTrueWhenStudentIdFilterIsSet() {
            StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
            criteria.setStudentId(1L);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when itemId filter is set")
        void shouldReturnTrueWhenItemIdFilterIsSet() {
            StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
            criteria.setItemId(1L);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return false when no filters are set")
        void shouldReturnFalseWhenNoFiltersAreSet() {
            StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
            boolean hasFilters = criteria.hasFilters();
            assertFalse(hasFilters);
        }
    }

    @Nested
    @DisplayName("When checking hasFilters with combined criteria")
    class WhenCheckingHasFiltersWithCombinedCriteria {
        @Test
        @DisplayName("Should return true with studentName and itemName criteria")
        void shouldReturnTrueWithStudentNameAndItemNameCriteria() {
            StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
            criteria.setStudentName("John D");
            criteria.setItemName("Stick");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true with points used range criteria")
        void shouldReturnTrueWithPointsUsedRangeCriteria() {
            StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
            criteria.setMinPointsUsed(5);
            criteria.setMaxPointsUsed(50);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true with date range criteria")
        void shouldReturnTrueWithDateRangeCriteria() {
            StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
            criteria.setStartDate(LocalDateTime.now().minusDays(5));
            criteria.setEndDate(LocalDateTime.now());
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true with studentId and itemId criteria")
        void shouldReturnTrueWithStudentIdAndItemIdCriteria() {
            StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
            criteria.setStudentId(1L);
            criteria.setItemId(1L);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when all filters are set")
        void shouldReturnTrueWhenAllFiltersAreSet() {
            StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
            criteria.setStudentName("John D");
            criteria.setItemName("Stick");
            criteria.setMinPointsUsed(5);
            criteria.setMaxPointsUsed(50);
            criteria.setStartDate(LocalDateTime.now().minusDays(5));
            criteria.setEndDate(LocalDateTime.now());
            criteria.setStudentId(1L);
            criteria.setItemId(1L);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }
    }
}
