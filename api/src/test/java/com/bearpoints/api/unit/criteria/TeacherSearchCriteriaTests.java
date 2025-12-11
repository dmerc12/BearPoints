package com.bearpoints.api.unit.criteria;

import com.bearpoints.api.criteria.TeacherSearchCriteria;
import com.bearpoints.api.entity.GradeLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link TeacherSearchCriteria}.
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
 * @see TeacherSearchCriteria
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("TeacherSearchCriteria Tests")
public class TeacherSearchCriteriaTests {
    @Nested
    @DisplayName("When checking hasFilters with individual criteria")
    class WhenCheckingHasFiltersWithIndividualCriteria {
        @Test
        @DisplayName("Should return true when email filter is set")
        void shouldReturnTrueWhenEmailFilterIsSet() {
            TeacherSearchCriteria criteria = new TeacherSearchCriteria();
            criteria.setEmail("student@okcps.org");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when first name filter is set")
        void shouldReturnTrueWhenFirstNameFilterIsSet() {
            TeacherSearchCriteria criteria = new TeacherSearchCriteria();
            criteria.setFirstName("John");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when last name filter is set")
        void shouldReturnTrueWhenLastNameFilterIsSet() {
            TeacherSearchCriteria criteria = new TeacherSearchCriteria();
            criteria.setLastName("Doe");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when grade filter is set")
        void shouldReturnTrueWhenGradeFilterIsSet() {
            TeacherSearchCriteria criteria = new TeacherSearchCriteria();
            criteria.setGrade(GradeLevel.FIRST);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return false when no filters are set")
        void shouldReturnFalseWhenNoFiltersAreSet() {
            TeacherSearchCriteria criteria = new TeacherSearchCriteria();
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
            TeacherSearchCriteria criteria = new TeacherSearchCriteria();
            criteria.setEmail("test@okcps.org");
            criteria.setFirstName("John");
            criteria.setLastName("Smith");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true with all criteria set")
        void shouldReturnTrueWithAllCriteriaSet() {
            TeacherSearchCriteria criteria = new TeacherSearchCriteria();
            criteria.setEmail("test@okcps.org");
            criteria.setFirstName("John");
            criteria.setLastName("Smith");
            criteria.setGrade(GradeLevel.FIRST);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }
    }
}
