package com.bearpoints.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Search criteria for admin filtering operations
 * <p>Provides flexible filtering options for admin search queries.
 *
 * <p>Supported filters:
 * <ul>
 *     <li>{@code email} - Partial match on admin email (case-insensitive)</li>
 *     <li>{@code firstName} - Partial match on first name (case-insensitive)</li>
 *     <li>{@code lastName} - Partial match on last name (case-insensitive)</li>
 * </ul>
 *
 * <p>Usage examples:
 * <ul>
 *     <li>Find admin by email: {@code email=j.doe}</li>
 *     <li>Find admin by name: {@code firstName=john&lastName=doe}</li>
 *     <li>Combined search: {@code email=j.doe&firstName=john&lastName=doe}</li>
 * </ul>
 *
 * @version 1.0
 * @author Dylan Mercer
 */
@Getter
@Setter
public class AdminSearchCriteria {
    private String email;
    private String firstName;
    private String lastName;

    /**
     * Determines if any search filters have been specified.
     *
     * @return true if at least one filter is set, false if all filters are null
     */
    public boolean hasFilters() {
        return email != null || firstName != null || lastName != null;
    }
}
