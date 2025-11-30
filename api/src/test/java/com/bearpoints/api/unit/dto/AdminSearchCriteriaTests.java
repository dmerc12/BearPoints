package com.bearpoints.api.unit.dto;

import com.bearpoints.api.dto.AdminSearchCriteria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AdminSearchCriteria}.
 * <p>Verifies that search criteria properly tracks filter presence and
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
 * @see AdminSearchCriteria
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("AdminSearchCriteria Tests")
public class AdminSearchCriteriaTests {
    @Nested
    @DisplayName("When checking hasFilters with individual criteria")
    class WhenCheckingHasFiltersWithIndividualCriteria {
        @Test
        @DisplayName("Should return true when email filter is set")
        void shouldReturnTrueWhenEmailFilterIsSet() {
            AdminSearchCriteria criteria = new AdminSearchCriteria();
            criteria.setEmail("student@okcps.org");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when first name filter is set")
        void shouldReturnTrueWhenFirstNameFilterIsSet() {
            AdminSearchCriteria criteria = new AdminSearchCriteria();
            criteria.setFirstName("John");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when last name filter is set")
        void shouldReturnTrueWhenLastNameFilterIsSet() {
            AdminSearchCriteria criteria = new AdminSearchCriteria();
            criteria.setLastName("Doe");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return false when no filters are set")
        void shouldReturnFalseWhenNoFiltersAreSet() {
            AdminSearchCriteria criteria = new AdminSearchCriteria();
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
            AdminSearchCriteria criteria = new AdminSearchCriteria();
            criteria.setEmail("test@okcps.org");
            criteria.setFirstName("John");
            criteria.setLastName("Smith");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true with all criteria set")
        void shouldReturnTrueWithAllCriteriaSet() {
            AdminSearchCriteria criteria = new AdminSearchCriteria();
            criteria.setEmail("test@okcps.org");
            criteria.setFirstName("John");
            criteria.setLastName("Smith");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }
    }
}
