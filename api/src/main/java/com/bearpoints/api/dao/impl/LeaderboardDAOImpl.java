package com.bearpoints.api.dao.impl;

import com.bearpoints.api.dao.LeaderboardDAO;
import com.bearpoints.api.dto.LeaderboardEntryDTO;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.PersonDTO;
import com.bearpoints.api.entity.GradeLevel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link LeaderboardDAO} using JPQL for database-agnostic ranking queries.
 * <p>Key Features:
 * <ul>
 *     <li>Uses RANK() OVER (ORDER BY total_points DESC) for server-side ranking</li>
 *     <li>JPQL implementation works with both H2 (testing) and PostgreSQL/MySQL (production)</li>
 *     <li>Dynamic WHERE clauses for optional filtering</li>
 *     <li>Manual pagination handling for complex aggregated queries</li>
 * </ul>
 *
 * <p>Ranking Behavior:
 * <ul>
 *     <li>Overall Leaderboard: Global ranking across all students</li>
 *     <li>Teacher Filter: Ranking resets to 1,2,3... within that teacher's class</li>
 *     <li>Grade Filter: Ranking resets to 1,2,3... within that grade level</li>
 *     <li>Maintains pagination while preserving ranking context</li>
 * </ul>
 *
 * @see LeaderboardDAO
 * @version 1.3
 * @author Dylan Mercer
 */
@Slf4j
@Repository
public class LeaderboardDAOImpl implements LeaderboardDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public PagedResponseDTO<LeaderboardEntryDTO> findRankedLeaderboard(
            LocalDateTime startDate, Long teacherId, GradeLevel grade,
            Pageable pageable) {
        log.debug("Executing ranked leaderboard query - startDate: {}, teacherId: {}, grade: {}, page: {}, size: {}",
                startDate, teacherId, grade, pageable.getPageNumber(), pageable.getPageSize());
        String jpql = """
                SELECT
                    RANK() OVER (ORDER BY COALESCE(SUM(bl.pointsGenerated), 0) DESC),
                    s.id as studentId,
                    u_s.firstName as studentFirstName,
                    u_s.lastName as studentLastName,
                    t.id as teacherId,
                    u_t.firstName as teacherFirstName,
                    u_t.lastName as teacherLastName,
                    t.grade as grade,
                    COALESCE(SUM(bl.pointsGenerated), 0) as points
                FROM Student s
                JOIN s.user u_s
                JOIN s.teacher t
                JOIN t.user u_t
                LEFT JOIN BragLog bl ON bl.student = s
                    AND bl.timestamp >= :startDate
                WHERE (:teacherId IS NULL OR t.id = :teacherId)
                    AND (:grade IS NULL OR t.grade = :grade)
                GROUP BY s.id, u_s.firstName, u_s.lastName,
                         t.id, u_t.firstName, u_t.lastName, t.grade
                ORDER BY COALESCE(SUM(bl.pointsGenerated), 0) DESC
                """;
        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class)
                .setParameter("startDate", startDate)
                .setParameter("teacherId", teacherId)
                .setParameter("grade", grade);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        long queryStart = System.currentTimeMillis();
        List<Object[]> results = query.getResultList();
        long queryTime = System.currentTimeMillis() - queryStart;
        log.debug("Query returned {} rows in {} ms", results.size(), queryTime);
        List<LeaderboardEntryDTO> content = results.stream()
                .map(row -> {
                    Integer rank = ((Number) row[0]).intValue();
                    Long studentId = (Long) row[1];
                    String studentFirstName = (String) row[2];
                    String studentLastName = (String) row[3];
                    Long storedTeacherId = (Long) row[4];
                    String teacherFirstName = (String) row[5];
                    String teacherLastName = (String) row[6];
                    GradeLevel storedGrade = (GradeLevel) row[7];
                    Integer points = ((Number) row[8]).intValue();
                    PersonDTO student = new PersonDTO(studentId, studentFirstName, studentLastName);
                    PersonDTO teacher = new PersonDTO(storedTeacherId, teacherFirstName, teacherLastName);
                    return new LeaderboardEntryDTO(rank, student, teacher, storedGrade, points);
                }).collect(Collectors.toList());
        Long total = getTotalCount(startDate, teacherId, grade);
        log.debug("Total count matching filters: {}", total);
        Page<LeaderboardEntryDTO> page = new PageImpl<>(content, pageable, total);
        return PagedResponseDTO.of(page);
    }

    private Long getTotalCount(LocalDateTime startDate, Long teacherId, GradeLevel grade) {
        String jpql = """
                SELECT COUNT(DISTINCT s.id)
                FROM Student s
                JOIN s.teacher t
                LEFT JOIN s.bragLogs bl ON bl.timestamp >= :startDate
                WHERE (:teacherId IS NULL OR t.id = :teacherId)
                    AND (:grade IS NULL OR t.grade = :grade)
                """;
        TypedQuery<Long> countQuery = entityManager.createQuery(jpql, Long.class)
                .setParameter("startDate", startDate)
                .setParameter("teacherId", teacherId)
                .setParameter("grade", grade);
        return countQuery.getSingleResult();
    }
}
