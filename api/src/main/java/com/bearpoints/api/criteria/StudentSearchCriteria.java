package com.bearpoints.api.criteria;

import lombok.Getter;
import lombok.Setter;

/**
 * Search criteria for student filtering operations.
 * <p>Provides flexible filtering options for student search queries.
 * All fields are optional and can be combined for precise result filtering.
 *
 * <p>Supported filters:
 * <ul>
 *     <li>{@code email} - Partial match on student email (case-insensitive)</li>
 *     <li>{@code firstName} - Partial match on first name (case-insensitive)</li>
 *     <li>{@code lastName} - Partial match on last name (case-insensitive)</li>
 *     <li>{@code teacherId} - Exact match on assigned teacher ID</li>
 *     <li>{@code minPoints} - Minimum points threshold (inclusive)</li>
 *     <li>{@code maxPoints} - Maximum points threshold (inclusive)</li>
 * </ul>
 *
 * <p>Usage examples:
 * <ul>
 *     <li>Find students by teacher: {@code teacherId=1}</li>
 *     <li>Search by name: {@code firstName=john&lastName=doe}</li>
 *     <li>Filter by points range: {@code minPoints=50&maxPoints=100}</li>
 *     <li>Combined search: {@code teacherId=1&minPoints=50}</li>
 * </ul>
 *
 * @version 1.0
 * @author Dylan Mercer
 */
@Getter
@Setter
public class StudentSearchCriteria {
    private String email;
    private String firstName;
    private String lastName;
    private Long teacherId;
    private Integer minPoints;
    private Integer maxPoints;

    /**
     * Determines if any search filters have been specified.
     *
     * @return true if at least one filter is set, false if all filters are null
     */
    public boolean hasFilters() {
        return email != null || firstName != null || lastName != null || teacherId != null
                || minPoints != null || maxPoints != null;
    }
}
