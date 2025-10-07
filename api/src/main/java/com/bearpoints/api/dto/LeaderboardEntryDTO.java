package com.bearpoints.api.dto;

import com.bearpoints.api.entity.GradeLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Data Transfer Object for leaderboard entries.
 * <p>Represents a student's position in the points' leaderboard.
 *
 * <p>Fields:
 * <ul>
 *     <li>{@code rank} - Student's position in the leaderboard (1-based)</li>
 *     <li>{@code student} - Student information (id, first name, last name)</li>
 *     <li>{@code teacher} - Teacher's information (id, first name, last name)</li>
 *     <li>{@code grade} - Student's grade level</li>
 *     <li>{@code points} - Current point total</li>
 * </ul>
 *
 * @version 1.0
 * @author Dylan Mercer
 */
@Getter
@AllArgsConstructor
public class LeaderboardEntryDTO {
    private Integer rank;
    private PersonDTO student;
    private PersonDTO teacher;
    private GradeLevel grade;
    private Integer points;
}
