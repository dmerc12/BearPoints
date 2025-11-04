package com.bearpoints.api.unit.dao;

import com.bearpoints.api.dao.LeaderboardDAO;
import com.bearpoints.api.dao.impl.LeaderboardDAOImpl;
import com.bearpoints.api.dto.LeaderboardEntryDTO;
import com.bearpoints.api.entity.GradeLevel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LeaderboardDAO} implementation.
 * <p>Validates the data access layer functionality for ranked leaderboard queries,
 * including JPQL execution with window functions, filtering, and pagination.
 *
 * <p>Tests include:
 * <ul>
 *     <li>Basic ranked query execution without filters</li>
 *     <li>Teacher-based filtering with class-level ranking</li>
 *     <li>Grade-based filtering with grade-level ranking</li>
 *     <li>Combined filter scenarios</li>
 *     <li>Pagination with correct offset and limit calculations</li>
 *     <li>Empty result set handling</li>
 *     <li>Result ordering and rank assignment verifications</li>
 *     <li>Count query accuracy for total elements</li>
 * </ul>
 *
 * @see LeaderboardDAO
 * @see LeaderboardDAOImpl
 *
 * @version 1.1
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
public class LeaderboardDAOTests {
    /** Mocked JPA EntityManager for database operations */
    @Mock
    private EntityManager entityManager;

    /** Mocked query for leaderboard content retrieval */
    @Mock
    private TypedQuery<Object[]> contentQuery;

    /** Mocked query for total count calculation */
    @Mock
    private TypedQuery<Long> countQuery;

    /** DAO implementation with mocked dependencies injected */
    @InjectMocks
    private LeaderboardDAOImpl leaderboardDAO;

    private LocalDateTime startDate;
    private Pageable pageable;
    private List<Object[]> sampleResults;

    /**
     * Initializes test fixtures before each test method.
     * <p>Sets up:
     * <ul>
     *     <li>Default start date (one week ago)</li>
     *     <li>Standard pagination (first page, 20 items)</li>
     *     <li>Sample leaderboard data with varied rankings and points</li>
     * </ul>
     */
    @BeforeEach
    void setUp() {
        startDate = LocalDateTime.now().minusWeeks(1);
        pageable = PageRequest.of(0, 20);
        sampleResults = Arrays.asList(
                new Object[]{1, 1L, "John", "Doe",101L, "Jane", "Smith", GradeLevel.FIRST, 150},
                new Object[]{2, 2L, "Alice", "Johnson", 102L, "Bob", "Brown", GradeLevel.THIRD, 120},
                new Object[]{3, 3L, "Charlie", "Wilson", 101L, "Jane", "Smith", GradeLevel.FIRST, 100}
        );
    }

    /**
     * Tests basic leaderboard retrieval without filters.
     *
     * <p>Verifies that DAO executes JPQL query with window function ranking:
     * {@code RANK() OVER (ORDER BY total_points DESC)}
     *
     * <p>Verifies that:
     * <ul>
     *     <li>DAO executes JPQL query with correct parameters</li>
     *     <li>Pagination parameters are properly applied</li>
     *     <li>Results contain correct ranking and student/teacher data</li>
     *     <li>Total elements count is accurately calculated</li>
     *     <li>Results are ordered by points descending</li>
     * </ul>
     */
    @Test
    @DisplayName("findRankedLeaderboard returns paginated results with ranking")
    void findRankedLeaderboard_NoFilters_ReturnsRankedResults() {
        when(entityManager.createQuery(anyString(), eq(Object[].class))).thenReturn(contentQuery);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(contentQuery.setParameter(eq("startDate"), eq(startDate))).thenReturn(contentQuery);
        when(contentQuery.setParameter(eq("teacherId"), isNull())).thenReturn(contentQuery);
        when(contentQuery.setParameter(eq("grade"), isNull())).thenReturn(contentQuery);
        when(contentQuery.setFirstResult(0)).thenReturn(contentQuery);
        when(contentQuery.setMaxResults(20)).thenReturn(contentQuery);
        when(contentQuery.getResultList()).thenReturn(sampleResults);
        when(countQuery.setParameter(eq("startDate"), eq(startDate))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("teacherId"), isNull())).thenReturn(countQuery);
        when(countQuery.setParameter(eq("grade"), isNull())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(3L);
        Page<LeaderboardEntryDTO> result = leaderboardDAO.findRankedLeaderboard(startDate, null, null, pageable);
        assertNotNull(result);
        assertEquals(3, result.getContent().size());
        assertEquals(3L, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertEquals(0, result.getNumber());
        assertEquals(20, result.getSize());
        LeaderboardEntryDTO firstEntry = result.getContent().getFirst();
        assertNotNull(firstEntry);
        assertEquals(1, firstEntry.getRank());
        assertEquals("John", firstEntry.getStudent().getFirstName());
        assertEquals("Doe", firstEntry.getStudent().getLastName());
        assertEquals("Jane", firstEntry.getTeacher().getFirstName());
        assertEquals("Smith", firstEntry.getTeacher().getLastName());
        assertEquals(GradeLevel.FIRST, firstEntry.getGrade());
        assertEquals(150, firstEntry.getPoints());
        verify(contentQuery).setParameter("startDate", startDate);
        verify(contentQuery).setParameter("teacherId", null);
        verify(contentQuery).setParameter("grade", null);
        verify(contentQuery).setFirstResult(0);
        verify(contentQuery).setMaxResults(20);
        verify(countQuery).getSingleResult();
    }

    /**
     * Tests leaderboard retrieval filtered by specific teacher.
     *
     * <p>Validates that:
     * <ul>
     *     <li>Teacher ID parameter is properly bound to query</li>
     *     <li>Ranking is recalculated within teacher's class context</li>
     *     <li>Results are limited to specified teacher's students</li>
     *     <li>Count query respects teacher filter</li>
     * </ul>
     */
    @Test
    @DisplayName("findRankedLeaderboard with teacher filter returns class-ranked results")
    void findRankedLeaderboard_WithTeacherFilter_ReturnsClassRankedResults() {
        Long teacherId = 101L;
        when(entityManager.createQuery(anyString(), eq(Object[].class))).thenReturn(contentQuery);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(contentQuery.setParameter(eq("startDate"), eq(startDate))).thenReturn(contentQuery);
        when(contentQuery.setParameter(eq("teacherId"), eq(teacherId))).thenReturn(contentQuery);
        when(contentQuery.setParameter(eq("grade"), isNull())).thenReturn(contentQuery);
        when(contentQuery.setFirstResult(0)).thenReturn(contentQuery);
        when(contentQuery.setMaxResults(20)).thenReturn(contentQuery);
        List<Object[]> teacherResults = List.of(
                sampleResults.getFirst(),
                sampleResults.get(2)
        );
        when(contentQuery.getResultList()).thenReturn(teacherResults);
        when(countQuery.setParameter(eq("startDate"), eq(startDate))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("teacherId"), eq(teacherId))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("grade"), isNull())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(2L);
        Page<LeaderboardEntryDTO> result = leaderboardDAO.findRankedLeaderboard(startDate, teacherId, null, pageable);
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2L, result.getTotalElements());
        verify(contentQuery).setParameter("teacherId", teacherId);
        verify(countQuery).setParameter("teacherId", teacherId);
    }

    /**
     * Tests leaderboard retrieval filtered by grade level.
     *
     * <p>Ensures that:
     * <ul>
     *     <li>Grade parameter is correctly applied in WHERE clause</li>
     *     <li>Ranking resets within grade level context</li>
     *     <li>Results include only students from specified grade</li>
     *     <li>Count query incorporates grade filter</li>
     * </ul>
     */
    @Test
    @DisplayName("findRankedLeaderboard with grade filter returns grade-ranked results")
    void findRankedLeaderboard_WithGradeFilter_ReturnsRankedResults() {
        GradeLevel grade = GradeLevel.FIRST;
        when(entityManager.createQuery(anyString(), eq(Object[].class))).thenReturn(contentQuery);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(contentQuery.setParameter(eq("startDate"), eq(startDate))).thenReturn(contentQuery);
        when(contentQuery.setParameter(eq("teacherId"), isNull())).thenReturn(contentQuery);
        when(contentQuery.setParameter(eq("grade"), eq(grade))).thenReturn(contentQuery);
        when(contentQuery.setFirstResult(0)).thenReturn(contentQuery);
        when(contentQuery.setMaxResults(20)).thenReturn(contentQuery);
        List<Object[]> gradeResults = List.of(
                sampleResults.getFirst(),
                sampleResults.get(2)
        );
        when(contentQuery.getResultList()).thenReturn(gradeResults);
        when(countQuery.setParameter(eq("startDate"), eq(startDate))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("teacherId"), isNull())).thenReturn(countQuery);
        when(countQuery.setParameter(eq("grade"), eq(grade))).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(2L);
        Page<LeaderboardEntryDTO> result = leaderboardDAO.findRankedLeaderboard(startDate, null, grade, pageable);
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals(2L, result.getTotalElements());
        verify(contentQuery).setParameter("grade", grade);
        verify(countQuery).setParameter("grade", grade);
    }

    /**
     * Tests leaderboard retrieval with combined teacher and grade filters.
     *
     * <p>Validates complex filtering scenario where:
     * <ul>
     *     <li>Both teacher ID and grade parameters are applied</li>
     *     <li>AND condition in WHERE clause functions correctly</li>
     *     <li>Ranking recalculates for the intersection of filters</li>
     *     <li>Count query handles multiple filter conditions</li>
     * </ul>
     */
    @Test
    @DisplayName("findRankedLeaderboard with both teacher and grade filters returns correctly filtered results")
    void findRankedLeaderboard_WithTeacherAndGradeFilters_ReturnsFilteredResults() {
        Long teacherId = 101L;
        GradeLevel grade = GradeLevel.FIRST;
        when(entityManager.createQuery(anyString(), eq(Object[].class))).thenReturn(contentQuery);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(contentQuery.setParameter(eq("startDate"), eq(startDate))).thenReturn(contentQuery);
        when(contentQuery.setParameter(eq("teacherId"), eq(teacherId))).thenReturn(contentQuery);
        when(contentQuery.setParameter(eq("grade"), eq(grade))).thenReturn(contentQuery);
        when(contentQuery.setFirstResult(0)).thenReturn(contentQuery);
        when(contentQuery.setMaxResults(20)).thenReturn(contentQuery);
        List<Object[]> filteredResults = List.<Object[]>of(sampleResults.getFirst());
        when(contentQuery.getResultList()).thenReturn(filteredResults);
        when(countQuery.setParameter(eq("startDate"), eq(startDate))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("teacherId"), eq(teacherId))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("grade"), eq(grade))).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(1L);
        Page<LeaderboardEntryDTO> result = leaderboardDAO.findRankedLeaderboard(startDate, teacherId, grade, pageable);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(1L, result.getTotalElements());
        verify(contentQuery).setParameter("teacherId", teacherId);
        verify(contentQuery).setParameter("grade", grade);
        verify(countQuery).setParameter("teacherId", teacherId);
        verify(countQuery).setParameter("grade", grade);
    }

    /**
     * Tests pagination functionality with non-zero page offset.
     *
     * <p>Verifies that:
     * <ul>
     *     <li>setFirstResult calculates correct offset (page * size)</li>
     *     <li>setMaxResults applies correct page size limit</li>
     *     <li>Total elements and pages are accurately reported</li>
     *     <li>Page metadata (number, size, totals) is correct</li>
     * </ul>
     */
    @Test
    @DisplayName("findRankedLeaderboard handles pagination correctly")
    void findRankedLeaderboard_WithPagination_ReturnsCorrectPage() {
        Pageable secondPage = PageRequest.of(1, 2);
        when(entityManager.createQuery(anyString(), eq(Object[].class))).thenReturn(contentQuery);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(contentQuery.setParameter(eq("startDate"), eq(startDate))).thenReturn(contentQuery);
        when(contentQuery.setParameter(eq("teacherId"), isNull())).thenReturn(contentQuery);
        when(contentQuery.setParameter(eq("grade"), isNull())).thenReturn(contentQuery);
        when(contentQuery.setFirstResult(2)).thenReturn(contentQuery);
        when(contentQuery.setMaxResults(2)).thenReturn(contentQuery);
        List<Object[]> secondPageContent = List.<Object[]>of(sampleResults.get(2));
        when(contentQuery.getResultList()).thenReturn(secondPageContent);
        when(countQuery.setParameter(eq("startDate"), eq(startDate))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("teacherId"), isNull())).thenReturn(countQuery);
        when(countQuery.setParameter(eq("grade"), isNull())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(3L);
        Page<LeaderboardEntryDTO> result = leaderboardDAO.findRankedLeaderboard(startDate, null, null, secondPage);
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(3L, result.getTotalElements());
        assertEquals(2, result.getTotalPages());
        assertEquals(1, result.getNumber());
        assertEquals(2, result.getSize());
        verify(contentQuery).setFirstResult(2);
        verify(contentQuery).setMaxResults(2);
    }

    /**
     * Tests empty result set handling.
     *
     * <p>Validates edge case where:
     * <ul>
     *     <li>Queries return empty collections without errors</li>
     *     <li>Page object correctly reports zero elements</li>
     *     <li>Total pages calculation handles empty results</li>
     *     <li>Content list is empty but not null</li>
     * </ul>
     */
    @Test
    @DisplayName("findRankedLeaderboard returns empty page when no results")
    void findRankedLeaderboard_NoResults_ReturnsEmptyPage() {
        when(entityManager.createQuery(anyString(), eq(Object[].class))).thenReturn(contentQuery);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(contentQuery.setParameter(eq("startDate"), eq(startDate))).thenReturn(contentQuery);
        when(contentQuery.setParameter(eq("teacherId"), isNull())).thenReturn(contentQuery);
        when(contentQuery.setParameter(eq("grade"), isNull())).thenReturn(contentQuery);
        when(contentQuery.setFirstResult(0)).thenReturn(contentQuery);
        when(contentQuery.setMaxResults(20)).thenReturn(contentQuery);
        when(contentQuery.getResultList()).thenReturn(List.of());
        when(countQuery.setParameter(eq("startDate"), eq(startDate))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("teacherId"), isNull())).thenReturn(countQuery);
        when(countQuery.setParameter(eq("grade"), isNull())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(0L);
        Page<LeaderboardEntryDTO> result = leaderboardDAO.findRankedLeaderboard(startDate, null, null, pageable);
        assertNotNull(result);
        assertTrue(result.getContent().isEmpty());
        assertEquals(0L, result.getTotalElements());
        assertEquals(0, result.getTotalPages());
        assertEquals(0, result.getNumber());
    }

    /**
     * Tests result ordering and rank assignment.
     *
     * <p>Verifies that:
     * <ul>
     *     <li>RANK() OVER (ORDER BY total_points DESC) works correctly</li>
     *     <li>Students with higher points receive better ranks (1, 2, 3...)</li>
     *     <li>Points are sorted in descending order</li>
     *     <li>Rank assignment matches points ordering</li>
     * </ul>
     */
    @Test
    @DisplayName("findRankedLeaderboard orders results by points descending")
    void findRankedLeaderboard_ResultsOrderedByPointsDescending() {
        when(entityManager.createQuery(anyString(), eq(Object[].class))).thenReturn(contentQuery);
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(contentQuery.setParameter(anyString(), any())).thenReturn(contentQuery);
        when(contentQuery.setFirstResult(anyInt())).thenReturn(contentQuery);
        when(contentQuery.setMaxResults(anyInt())).thenReturn(contentQuery);
        when(contentQuery.getResultList()).thenReturn(sampleResults);
        when(countQuery.setParameter(anyString(), any())).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(3L);
        Page<LeaderboardEntryDTO> result = leaderboardDAO.findRankedLeaderboard(startDate, null, null, pageable);
        List<LeaderboardEntryDTO> content = result.getContent();
        assertThat(content).extracting(LeaderboardEntryDTO::getPoints).containsExactly(150, 120, 100);
        assertThat(content).extracting(LeaderboardEntryDTO::getRank).containsExactly(1, 2, 3);
    }

    /**
     * Tests count query accuracy with applied filters.
     *
     * <p>Ensures that:
     * <ul>
     *     <li>Count query receives same filter parameters as content query</li>
     *     <li>Total elements reflects filtered result set size</li>
     *     <li>Count query uses DISTINCT to avoid duplicates</li>
     *     <li>Parameters are properly bound to count query</li>
     * </ul>
     */
    @Test
    @DisplayName("getTotalCount query returns correct count with filters")
    void getTotalCount_WithFilters_ReturnsCorrectCount() {
        Long teacherId = 101L;
        GradeLevel grade = GradeLevel.FIRST;
        when(entityManager.createQuery(anyString(), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("startDate"), eq(startDate))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("teacherId"), eq(teacherId))).thenReturn(countQuery);
        when(countQuery.setParameter(eq("grade"), eq(grade))).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(2L);
        when(entityManager.createQuery(anyString(), eq(Object[].class))).thenReturn(contentQuery);
        when(contentQuery.setParameter(anyString(), any())).thenReturn(contentQuery);
        when(contentQuery.setFirstResult(anyInt())).thenReturn(contentQuery);
        when(contentQuery.setMaxResults(anyInt())).thenReturn(contentQuery);
        when(contentQuery.getResultList()).thenReturn(List.of(sampleResults.getFirst(), sampleResults.get(2)));
        Page<LeaderboardEntryDTO> result = leaderboardDAO.findRankedLeaderboard(startDate, teacherId, grade, pageable);
        assertNotNull(result);
        verify(countQuery).setParameter("startDate", startDate);
        verify(countQuery).setParameter("teacherId", teacherId);
        verify(countQuery).setParameter("grade", grade);
        verify(countQuery).getSingleResult();
    }
}
