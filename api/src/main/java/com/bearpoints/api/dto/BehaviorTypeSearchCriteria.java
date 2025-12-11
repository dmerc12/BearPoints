package com.bearpoints.api.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Search criteria for behavior type filtering operations.
 * <p>Provides flexible filtering options for behavior type search queries.
 * All fields are optional and can be combined for precise result filtering.
 *
 * <p>Supported filters:
 * <ul>
 *     <li>{@code name} - Partial match on behavior type name (case-insensitive)</li>
 *     <li>{@code active} - Active boolean filter</li>
 *     <li>{@code minPointValue} - Minimum point value threshold (inclusive)</li>
 *     <li>{@code maxPointValue} - Maximum point value threshold (inclusive)</li>
 * </ul>
 *
 * <p>Usage examples:
 * <ul>
 *     <li>Search by name: {@code name=participated}</li>
 *     <li>Filter by active: {@code active=true}</li>
 *     <li>Filter by points range: {@code minPoints=4&maxPoints=5}</li>
 *     <li>Combined search {@code name=participated&minPoints=2&maxPoints=3&active=true}</li>
 * </ul>
 *
 * @version 1.0
 * @author Dylan Mercer
 */
@Getter
@Setter
public class BehaviorTypeSearchCriteria {
    private String name;
    private Boolean active;
    private Integer minPointValue;
    private Integer maxPointValue;

    /**
     * Determines if any search filters have been specified.
     *
     * @return true if at least one filter is set, false if all filters are null
     */
    public boolean hasFilters() {
        return name != null || minPointValue != null || maxPointValue != null || active != null;
    }
}
