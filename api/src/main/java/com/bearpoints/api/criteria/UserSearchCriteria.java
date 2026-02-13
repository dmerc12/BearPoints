package com.bearpoints.api.criteria;

import com.bearpoints.api.entity.Role;
import lombok.Getter;
import lombok.Setter;

/**
 * Search criteria for user filtering operations
 * <p>Provides flexible filtering options for user search queries.
 *
 * <p>Supported filters:
 * <ul>
 *     <li>{@code email} - Partial match on user email (case-insensitive)</li>
 *     <li>{@code firstName} - Partial match on user first name (case-insensitive)</li>
 *     <li>{@code lastName} - Partial match on user last name (case-insensitive)</li>
 *     <li>{@code role} - Role filter</li>
 * </ul>
 *
 * <p>Usage examples:
 * <ul>
 *     <li>Find user by email: {@code email=j.doe}</li>
 *     <li>Find user by name: {@code firstName=john&lastName=doe}</li>
 *     <li>Find users by role: {@code role=ADMIN}</li>
 *     <li>Combined search: {@code email=j.doe&firstName=john&lastName=doe&role=ADMIN}</li>
 * </ul>
 *
 * @version 2.0
 * @author Dylan Mercer
 */
@Getter
@Setter
public class UserSearchCriteria {
    private String email;
    private String firstName;
    private String lastName;
    private Role role;

    /**
     * Determines if any search filters have been specified.
     *
     * @return true if at least one filter is set, false if all filters are null
     */
    public boolean hasFilters() {
        return email != null || firstName != null || lastName != null || role != null;
    }
}
