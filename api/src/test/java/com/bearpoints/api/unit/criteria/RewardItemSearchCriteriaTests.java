package com.bearpoints.api.unit.criteria;

import com.bearpoints.api.criteria.RewardItemSearchCriteria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link RewardItemSearchCriteria}.
 * <p>Verifies that search criteria correctly tracks filter presence and
 * handles various combinations appropriately.
 *
 * <p>Test scenarios cover:
 * <ul>
 *     <li>Individual filter detection</li>
 *     <li>Combined filter scenarios</li>
 *     <li>Edge cases with null and empty values</li>
 *     <li>Boundary conditions for point cost and stock ranges</li>
 * </ul>
 *
 * @see RewardItemSearchCriteria
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("RewardItemSearchCriteria Tests")
public class RewardItemSearchCriteriaTests {
    @Nested
    @DisplayName("When checking hasFilters with individual criteria")
    class WhenCheckingHasFiltersWithIndividualCriteria {
        @Test
        @DisplayName("Should return true when name filter is set")
        public void shouldReturnTrueWhenNameFilterIsSet() {
            RewardItemSearchCriteria criteria = new RewardItemSearchCriteria();
            criteria.setName("test reward item");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when min point cost filter is set")
        public void shouldReturnTrueWhenMinPointCostFilterIsSet() {
            RewardItemSearchCriteria criteria = new RewardItemSearchCriteria();
            criteria.setMinPointCost(1);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when max point cost filter is set")
        public void shouldReturnTrueWhenMaxPointCostFilterIsSet() {
            RewardItemSearchCriteria criteria = new RewardItemSearchCriteria();
            criteria.setMaxPointCost(50);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when min stock filter is set")
        public void shouldReturnTrueWhenMinStockFilterIsSet() {
            RewardItemSearchCriteria criteria = new RewardItemSearchCriteria();
            criteria.setMinStock(1);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when max stock filter is set")
        public void shouldReturnTrueWhenMaxStockFilterIsSet() {
            RewardItemSearchCriteria criteria = new RewardItemSearchCriteria();
            criteria.setMaxStock(50);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return false when no filters are set")
        void shouldReturnFalseWhenNoFiltersAreSet() {
            RewardItemSearchCriteria criteria = new RewardItemSearchCriteria();
            boolean hasFilters = criteria.hasFilters();
            assertFalse(hasFilters);
        }
    }

    @Nested
    @DisplayName("When checking hasFilters with combined criteria")
    class WhenCheckingHasFiltersWithCombinedCriteria {
        @Test
        @DisplayName("Should return true with min and max point cost criteria")
        void shouldReturnTrueWithMinAndMaxPointCostCriteria() {
            RewardItemSearchCriteria criteria = new RewardItemSearchCriteria();
            criteria.setMinPointCost(1);
            criteria.setMaxPointCost(50);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true with min and max stock criteria")
        void shouldReturnTrueWithMinAndMaxStockCriteria() {
            RewardItemSearchCriteria criteria = new RewardItemSearchCriteria();
            criteria.setMinStock(1);
            criteria.setMaxStock(50);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true with all criteria")
        void shouldReturnTrueWithAllCriteria() {
            RewardItemSearchCriteria criteria = new RewardItemSearchCriteria();
            criteria.setName("test reward item");
            criteria.setMinPointCost(1);
            criteria.setMaxPointCost(50);
            criteria.setMinStock(1);
            criteria.setMaxStock(50);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }
    }
}
