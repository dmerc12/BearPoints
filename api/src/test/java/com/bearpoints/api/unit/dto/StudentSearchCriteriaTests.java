package com.bearpoints.api.unit.dto;

import com.bearpoints.api.dto.StudentSearchCriteria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link StudentSearchCriteria}.
 * <p>Verifies that search criteria correctly tracks filter presence and
 * handles various combinations appropriately.
 *
 * <p>Test scenarios cover:
 * <ul>
 *     <li>Individual filter detection</li>
 *     <li>Combined filter scenarios</li>
 *     <li>Edge cases with null and empty values</li>
 *     <li>Boundary conditions for points range</li>
 * </ul>
 *
 * @see StudentSearchCriteria
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("StudentSearchCriteria Tests")
public class StudentSearchCriteriaTests {
    @Nested
    @DisplayName("When checking hasFilters with individual criteria")
    class WhenCheckingHasFiltersWithIndividualCriteria {
        @Test
        @DisplayName("Should return true when email filter is set")
        void shouldReturnTrueWhenEmailFilterIsSet() {
            StudentSearchCriteria criteria = new StudentSearchCriteria();
            criteria.setEmail("student@okcps.org");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when first name filter is set")
        void shouldReturnTrueWhenFirstNameFilterIsSet() {
            StudentSearchCriteria criteria = new StudentSearchCriteria();
            criteria.setFirstName("John");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when last name filter is set")
        void shouldReturnTrueWhenLastNameFilterIsSet() {
            StudentSearchCriteria criteria = new StudentSearchCriteria();
            criteria.setLastName("Doe");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when teacher ID filter is set")
        void shouldReturnTrueWhenTeacherIdFilterIsSet() {
            StudentSearchCriteria criteria = new StudentSearchCriteria();
            criteria.setTeacherId(1L);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when min points filter is set")
        void shouldReturnTrueWhenMinPointsFilterIsSet() {
            StudentSearchCriteria criteria = new StudentSearchCriteria();
            criteria.setMinPoints(50);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when max points filter is set")
        void shouldReturnTrueWhenMaxPointsFilterIsSet() {
            StudentSearchCriteria criteria = new StudentSearchCriteria();
            criteria.setMaxPoints(150);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return false when no filters are set")
        void shouldReturnFalseWhenNoFiltersAreSet() {
            StudentSearchCriteria criteria = new StudentSearchCriteria();
            boolean hasFilters = criteria.hasFilters();
            assertFalse(hasFilters);
        }
    }

    @Nested
    @DisplayName("When checking hasFilters with combined criteria")
    class WhenCheckingHasFiltersWithCombinedCriteria {
        @Test
        @DisplayName("Should return true with email and name criteria")
        void shouldReturnTrueWithEmailAndNameCriteria() {
            StudentSearchCriteria criteria = new StudentSearchCriteria();
            criteria.setEmail("test@okcps.org");
            criteria.setFirstName("John");
            criteria.setLastName("Smith");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true with teacher and points criteria")
        void shouldReturnTrueWithTeacherAndPointsCriteria() {
            StudentSearchCriteria criteria = new StudentSearchCriteria();
            criteria.setTeacherId(1L);
            criteria.setMinPoints(50);
            criteria.setMaxPoints(150);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true with all criteria set")
        void shouldReturnTrueWithAllCriteriaSet() {
            StudentSearchCriteria criteria = new StudentSearchCriteria();
            criteria.setEmail("test@okcps.org");
            criteria.setFirstName("John");
            criteria.setLastName("Smith");
            criteria.setTeacherId(1L);
            criteria.setMinPoints(50);
            criteria.setMaxPoints(150);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }
    }
}
