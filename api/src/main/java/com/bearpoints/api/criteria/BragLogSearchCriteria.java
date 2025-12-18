package com.bearpoints.api.criteria;

import com.bearpoints.api.entity.GradeLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Search criteria for brag log filtering operations.
 * <p>Provides flexible filtering options for brag log search queries.
 * All fields are optional and can be combined for precise result filtering.
 *
 * <p>Supported filters:
 * <ul>
 *     <li>{@code studentName} - Partial match on student name (case-insensitive)</li>
 *     <li>{@code teacherName} - Partial match on teacher name (case-insensitive)</li>
 *     <li>{@code grade} - Grade level filter</li>
 *     <li>{@code minPoints} - Minimum points threshold (inclusive)</li>
 *     <li>{@code maxPoints} - Maximum points threshold (inclusive)</li>
 *     <li>{@code startDate} - Start date for timestamp (inclusive)</li>
 *     <li>{@code endDate} - End date for timestamp (inclusive)</li>
 *     <li>{@code studentId} - Student ID filter</li>
 *     <li>{@code teacherId} - Teacher ID filter</li>
 *     <li>{@code notes} - Partial match on notes (case-insensitive)</li>
 * </ul>
 *
 * <p>Usage examples:
 * <ul>
 *     <li>Search by student name: {@code studentName=John D}</li>
 *     <li>Search by teacher name: {@code teacherName=Jane S}</li>
 *     <li>Filter by grade level: {@code grade=FIRST}</li>
 *     <li>Filter by minimum points generated: {@code minPoints=3}</li>
 *     <li>Filter by maximum points generated: {@code maxPoints=5}</li>
 *     <li>Filter by timestamp start date: {@code startDate=01-01-2025T00:00:00}</li>
 *     <li>Filter by timestamp end date: {@code endDate=01-01-2025T00:00:00}</li>
 *     <li>Filter by teacherId: {@code teacherId=1}</li>
 *     <li>Filter by studentId: {@code studentId=1}</li>
 *     <li>Search by notes: {@code notes=Good}</li>
 * </ul>
 *
 * @version 1.0
 * @author Dylan Mercer
 */
@Getter
@Setter
public class BragLogSearchCriteria {
    private String studentName;
    private String teacherName;
    private GradeLevel grade;
    private Integer minPoints;
    private Integer maxPoints;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long teacherId;
    private Long studentId;
    private String notes;

    /**
     * Determines if any search filters have been specified.
     *
     * @return true if at least one filter is set, false if all filters are null
     */
    public boolean hasFilters() {
        return studentName != null || teacherName != null || grade != null || minPoints != null || maxPoints != null
                || startDate != null || endDate != null || teacherId != null || studentId != null || notes != null;
    }
}
