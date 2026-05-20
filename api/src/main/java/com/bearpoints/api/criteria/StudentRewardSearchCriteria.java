package com.bearpoints.api.criteria;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Search criteria for student reward filtering operations.
 * <p>Provides flexible filtering options for student reward search queries.
 * All fields are optional and can be combined for precise result filtering.
 *
 * <p>Supported filters:
 * <ul>
 *     <li>{@code studentName} - Partial match on student name (case-insensitive)</li>
 *     <li>{@code itemName} - Partial match on item name (case-insensitive)</li>
 *     <li>{@code minPointsUsed} - Minimum points used threshold (inclusive)</li>
 *     <li>{@code maxPointsUsed} - Maximum points used threshold (inclusive)</li>
 *     <li>{@code startDate} - Start date for timestamp (inclusive)</li>
 *     <li>{@code endDate} - End date for timestamp (inclusive)</li>
 *     <li>{@code studentId} - Student ID filter</li>
 *     <li>{@code itemId} - Item ID filter</li>
 * </ul>
 *
 * <p>Usage examples:
 * <ul>
 *     <li>Search by student name: {@code studentName=John D}</li>
 *     <li>Search by item name: {@code itemName=Penc}</li>
 *     <li>Filter by minimum points used: {@code minPointsUsed=5}</li>
 *     <li>Filter by maximum points used: {@code maxPointsUsed=20}</li>
 *     <li>Filter by timestamp start date: {@code startDate=01-01-2026T00:00:00}</li>
 *     <li>Filter by timestamp end date: {@code endDate=01-01-2026T00:00:00}</li>
 *     <li>Filter by studentId: {@code studentId=1}</li>
 *     <li>Filter by itemId: {@code itemId=1}</li>
 * </ul>
 *
 * @version 1.0
 * @author Dylan Mercer
 */
@Getter
@Setter
public class StudentRewardSearchCriteria {
    private String studentName;
    private String itemName;
    private Integer minPointsUsed;
    private Integer maxPointsUsed;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Long studentId;
    private Long itemId;

    /**
     * Determines if any search filters have been specified.
     *
     * @return true if at least one filter is set, false if all filters are null
     */
    public boolean hasFilters() {
        return studentName != null || itemName != null || minPointsUsed != null || maxPointsUsed != null ||
                startDate != null || endDate != null || studentId != null || itemId != null;
    }
}
