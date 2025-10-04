package com.bearpoints.api.dao.impl;

import com.bearpoints.api.dao.LeaderboardDAO;
import com.bearpoints.api.dto.LeaderboardEntryDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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
 * @version 1.0
 * @author Dylan Mercer
 */
@Repository
public class LeaderboardDAOImpl implements LeaderboardDAO {
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<LeaderboardEntryDTO> findRankedLeaderboard(
            LocalDateTime startDate, Long teacherId, String grade,
            Pageable pageable) {
        String jpql = """
                SELECT NEW com.bearpoints.api.dto.LeaderboardEntryDTO(
                    RANK() OVER (ORDER BY COALESCE(SUM(bl.pointsGenerated), 0) DESC),
                    NEW com.bearpoints.api.dto.PersonDTO(
                        s.id,
                        u_s.firstName,
                        u_s.lastName
                    ),
                    NEW com.bearpoints.api.dto.PersonDTO(
                        t.id,
                        u_t.firstName,
                        u_t.lastName
                    ),
                    t.grade,
                    COALESCE(SUM(bl.pointsGenerated), 0)
                )
                FROM Student s
                JOIN s.user u_s
                JOIN s.teacher t
                JOIN t.user u_t
                LEFT JOIN BragLog bl ON bl.student = s
                    AND bl.timestamp >= :startDate
                WHERE (:teacherId IS NULL OR t.id = :teacherId)
                    AND (:grade IS NULL OR t.grade = :grade)
                GROUP BY s.id, u_s.firstName, u_s.lastName,
                         t.id, u_t.firstName, u_t.lastName
                ORDER BY COALESCE(SUM(bl.pointsGenerated), 0) DESC
                """;
        TypedQuery<LeaderboardEntryDTO> query = entityManager.createQuery(jpql, LeaderboardEntryDTO.class)
                .setParameter("startDate", startDate)
                .setParameter("teacherId", teacherId)
                .setParameter("grade", grade);
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<LeaderboardEntryDTO> content = query.getResultList();
        Long total = getTotalCount(startDate, teacherId, grade);
        return new PageImpl<>(content, pageable, total);
    }

    private Long getTotalCount(LocalDateTime startDate, Long teacherId, String grade) {
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
