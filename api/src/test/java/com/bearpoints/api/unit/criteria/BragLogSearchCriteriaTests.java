package com.bearpoints.api.unit.criteria;

import com.bearpoints.api.criteria.BragLogSearchCriteria;
import com.bearpoints.api.entity.GradeLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link BragLogSearchCriteria}.
 * <p>Verifies that search criteria correctly tracks filter presence and
 * handles various combinations appropriately.
 *
 * <p>Test scenarios cover:
 * <ul>
 *     <li>Individual filter detection</li>
 *     <li>Combined filter scenarios</li>
 * </ul>
 *
 * @see BragLogSearchCriteria
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("BragLogSearchCriteria Tests")
public class BragLogSearchCriteriaTests {
    @Nested
    @DisplayName("When checking hasFilters with individual criteria")
    class WhenCheckingHasFiltersWithIndividualCriteria {
        @Test
        @DisplayName("Should return true when studentName filter is set")
        void shouldReturnTrueWhenStudentNameFilterIsSet() {
            BragLogSearchCriteria criteria = new BragLogSearchCriteria();
            criteria.setStudentName("John D");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("Should return true when teacherName filter is set")
        void shouldReturnTrueWhenTeacherNameFilterIsSet() {
            BragLogSearchCriteria criteria = new BragLogSearchCriteria();
            criteria.setTeacherName("Jane S");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("should return true when grade filter is set")
        void shouldReturnTrueWhenGradeFilterIsSet() {
            BragLogSearchCriteria criteria = new BragLogSearchCriteria();
            criteria.setGrade(GradeLevel.THIRD);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("should return true when min points filter is set")
        void shouldReturnTrueWhenMinPointsFilterIsSet() {
            BragLogSearchCriteria criteria = new BragLogSearchCriteria();
            criteria.setMinPoints(5);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("should return true when max points filter is set")
        void shouldReturnTrueWhenMaxPointsFilterIsSet() {
            BragLogSearchCriteria criteria = new BragLogSearchCriteria();
            criteria.setMaxPoints(3);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("should return true when start date filter is set")
        void shouldReturnTrueWhenStartDateFilterIsSet() {
            BragLogSearchCriteria criteria = new BragLogSearchCriteria();
            criteria.setStartDate(LocalDateTime.now().minusDays(5));
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("should return true when end date filter is set")
        void shouldReturnTrueWhenEndDateFilterIsSet() {
            BragLogSearchCriteria criteria = new BragLogSearchCriteria();
            criteria.setEndDate(LocalDateTime.now().minusDays(3));
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("should return true when teacher ID filter is set")
        void shouldReturnTrueWhenTeacherIdFilterIsSet() {
            BragLogSearchCriteria criteria = new BragLogSearchCriteria();
            criteria.setTeacherId(1L);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("should return true when student ID filter is set")
        void shouldReturnTrueWhenStudentIdFilterIsSet() {
            BragLogSearchCriteria criteria = new BragLogSearchCriteria();
            criteria.setStudentId(1L);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("should return true when notes filter is set")
        void shouldReturnTrueWhenNotesFilterIsSet() {
            BragLogSearchCriteria criteria = new BragLogSearchCriteria();
            criteria.setNotes("test notes");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("should return false when no filters are set")
        void shouldReturnFalseWhenNoFiltersAreSet() {
            BragLogSearchCriteria criteria = new BragLogSearchCriteria();
            boolean hasFilters = criteria.hasFilters();
            assertFalse(hasFilters);
        }
    }

    @Nested
    @DisplayName("When checking hasFilters with combined criteria")
    class WhenCheckingHasFiltersWithCombinedCriteria {
        @Test
        @DisplayName("Should return true with studentName and teacherName criteria")
        void shouldReturnTrueWithStudentNameAndTeacherNameCriteria() {
            BragLogSearchCriteria criteria = new BragLogSearchCriteria();
            criteria.setStudentName("John D");
            criteria.setTeacherName("Jane S");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("should return true when points range filters are set")
        void shouldReturnTrueWhenPointsRangeFiltersAreSet() {
            BragLogSearchCriteria criteria = new BragLogSearchCriteria();
            criteria.setMinPoints(5);
            criteria.setMaxPoints(3);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("should return true when grade and points range filters are set")
        void shouldReturnTrueWhenGradeAndPointsRangeFiltersAreSet() {
            BragLogSearchCriteria criteria = new BragLogSearchCriteria();
            criteria.setGrade(GradeLevel.THIRD);
            criteria.setMinPoints(5);
            criteria.setMaxPoints(3);
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("should return true when date range filters are set")
        void shouldReturnTrueWhenDateRangeFiltersAreSet() {
            BragLogSearchCriteria criteria = new BragLogSearchCriteria();
            criteria.setStartDate(LocalDateTime.now().minusDays(5));
            criteria.setEndDate(LocalDateTime.now().minusDays(3));
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("should return true when teacher ID, student ID, and notes filters are set")
        void shouldReturnTrueWhenTeacherIdStudentIdAndTeacherIdAndNotesFiltersAreSet() {
            BragLogSearchCriteria criteria = new BragLogSearchCriteria();
            criteria.setTeacherId(1L);
            criteria.setStudentId(1L);
            criteria.setNotes("test notes");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }

        @Test
        @DisplayName("should return true when all filters are set")
        void shouldReturnTrueWhenAllFiltersAreSet() {
            BragLogSearchCriteria criteria = new BragLogSearchCriteria();
            criteria.setStudentName("John D");
            criteria.setTeacherName("Jane S");
            criteria.setGrade(GradeLevel.THIRD);
            criteria.setMinPoints(3);
            criteria.setMaxPoints(5);
            criteria.setStartDate(LocalDateTime.now().minusDays(5));
            criteria.setEndDate(LocalDateTime.now().minusDays(3));
            criteria.setTeacherId(1L);
            criteria.setStudentId(1L);
            criteria.setNotes("test notes");
            boolean hasFilters = criteria.hasFilters();
            assertTrue(hasFilters);
        }
    }
}
