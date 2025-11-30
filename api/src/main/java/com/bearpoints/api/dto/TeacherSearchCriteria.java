package com.bearpoints.api.dto;

import com.bearpoints.api.entity.GradeLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * Search criteria for teacher filtering operations
 * <p>Provides flexible filtering options for teacher search queries.
 *
 * <p>Supported filters:
 * <ul>
 *     <li>{@code email} - Partial match on teacher email (case-insensitive)</li>
 *     <li>{@code firstName} - Partial match on first name (case-insensitive)</li>
 *     <li>{@code lastName} - Partial match on last name (case-insensitive)</li>
 *     <li>{@code grade} - Match on grade level</li>
 * </ul>
 *
 * <p>Usage examples:
 * <ul>
 *     <li>Find teacher by email: {@code email=j.doe}</li>
 *     <li>Find teacher by name: {@code firstName=john&lastName=doe}</li>
 *     <li>Find teacher by grade: {@code grade=FIRST}</li>
 *     <li>Combined search: {@code email=j.doe&firstName=john&lastName=doe}</li>
 * </ul>
 *
 * @version 1.0
 * @author Dylan Mercer
 */
@Getter
@Setter
public class TeacherSearchCriteria {
    private String email;
    private String firstName;
    private String lastName;
    private GradeLevel grade;

    /**
     * Determines if any search filters have been specified.
     *
     * @return true if at least one filter is set, false if all filters are null
     */
    public boolean hasFilters() {
        return email != null || firstName != null || lastName != null
                || grade != null;
    }
}
