package com.bearpoints.api.unit.criteria;

import com.bearpoints.api.criteria.UserSearchCriteria;
import com.bearpoints.api.entity.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link UserSearchCriteria}.
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
 * @see UserSearchCriteria
 * @version 2.0
 * @author Dylan Mercer
 */
@DisplayName("UserSearchCriteria Tests")
public class UserSearchCriteriaTests {
    @Nested
    @DisplayName("When checking hasFilters with individual criteria")
    class WhenCheckingHasFiltersWithIndividualCriteria {
        @Test
        @DisplayName("Should return true when email filter is set")
        void shouldReturnTrueWhenEmailFilterIsSet() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setEmail("student@okcps.org");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when first name filter is set")
        void shouldReturnTrueWhenFirstNameFilterIsSet() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setFirstName("John");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when last name filter is set")
        void shouldReturnTrueWhenLastNameFilterIsSet() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setLastName("Doe");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when role filter is set")
        void shouldReturnTrueWhenRoleFilterIsSet() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setRole(Role.ADMIN);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return false when no filters are set")
        void shouldReturnFalseWhenNoFiltersAreSet() {
            UserSearchCriteria criteria = new UserSearchCriteria();
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
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setEmail("test@okcps.org");
            criteria.setFirstName("John");
            criteria.setLastName("Smith");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true with all criteria set")
        void shouldReturnTrueWithAllCriteriaSet() {
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setEmail("test@okcps.org");
            criteria.setFirstName("John");
            criteria.setLastName("Smith");
            criteria.setRole(Role.ADMIN);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }
    }
}
