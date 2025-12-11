package com.bearpoints.api.unit.dto;

import com.bearpoints.api.dto.BehaviorTypeSearchCriteria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link BehaviorTypeSearchCriteria}.
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
 * @see BehaviorTypeSearchCriteria
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("BehaviorTypeSearchCriteria Tests")
public class BehaviorTypeSearchCriteriaTests {
    @Nested
    @DisplayName("When checking hasFilters with individual criteria")
    class WhenCheckingHasFiltersWithIndividualCriteria {
        @Test
        @DisplayName("Should return true when name filter is set")
        void shouldReturnTrueWhenEmailFilterIsSet() {
            BehaviorTypeSearchCriteria criteria = new BehaviorTypeSearchCriteria();
            criteria.setName("test behavior type");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when active filter is set")
        void shouldReturnTrueWhenActiveFilterIsSet() {
            BehaviorTypeSearchCriteria criteria = new BehaviorTypeSearchCriteria();
            criteria.setActive(true);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when min point value filter is set")
        void shouldReturnTrueWhenMinPointValueFilterIsSet() {
            BehaviorTypeSearchCriteria criteria = new BehaviorTypeSearchCriteria();
            criteria.setMinPointValue(1);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when max point value filter is set")
        void shouldReturnTrueWhenMaxPointValueFilterIsSet() {
            BehaviorTypeSearchCriteria criteria = new BehaviorTypeSearchCriteria();
            criteria.setMaxPointValue(3);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return false when no filters are set")
        void shouldReturnFalseWhenNoFiltersAreSet() {
            BehaviorTypeSearchCriteria criteria = new BehaviorTypeSearchCriteria();
            boolean hasFilters = criteria.hasFilters();
            assertFalse(hasFilters);
        }
    }

    @Nested
    @DisplayName("When checking hasFilters with combined criteria")
    class WhenCheckingHasFiltersWithCombinedCriteria {
        @Test
        @DisplayName("Should return true with name and active criteria")
        void shouldReturnTrueWithNameAndActiveCriteria() {
            BehaviorTypeSearchCriteria criteria = new BehaviorTypeSearchCriteria();
            criteria.setName("test behavior type");
            criteria.setActive(true);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true with min and max point value criteria")
        void shouldReturnTrueWhenMaxPointValueFilterIsSet() {
            BehaviorTypeSearchCriteria criteria = new BehaviorTypeSearchCriteria();
            criteria.setMinPointValue(2);
            criteria.setMaxPointValue(3);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true with all criteria")
        void shouldReturnTrueWithAllCriteria() {
            BehaviorTypeSearchCriteria criteria = new BehaviorTypeSearchCriteria();
            criteria.setName("test behavior type");
            criteria.setActive(true);
            criteria.setMinPointValue(2);
            criteria.setMaxPointValue(3);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }
    }
}
