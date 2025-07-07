package com.bearpoints.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Data Transfer Object for leaderboard entries.
 * <p>Represents a student's position in the points' leaderboard.
 *
 * <p>Fields:
 * <ul>
 *     <li>{@code studentId} - Unique student identifier</li>
 *     <li>{@code studentName} - Combined first and last name</li>
 *     <li>{@code teacherName} - Student's teacher's name</li>
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
    private Long studentId;
    private String studentName;
    private String teacherName;
    private String grade;
    private Integer points;
}
