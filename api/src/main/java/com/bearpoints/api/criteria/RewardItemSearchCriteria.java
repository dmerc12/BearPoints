package com.bearpoints.api.criteria;

import lombok.Getter;
import lombok.Setter;

/**
 * Search criteria for reward item filtering operations.
 * <p>Provides flexible filtering options for reward item search queries.
 * All fields are optional and can be combined for precise result filtering.
 *
 * <p>Supported filters:
 * <ul>
 *     <li>{@code name} - Partial match on reward item name (case-insensitive)</li>
 *     <li>{@code minPointCost} - Minimum point cost threshold (inclusive)</li>
 *     <li>{@code maxPointCost} - Maximum point cost threshold (inclusive)</li>
 *     <li>{@code minStock} - Minimum stock threshold (inclusive)</li>
 *     <li>{@code maxStock} - Maximum stock threshold (inclusive)</li>
 * </ul>
 *
 * <p>Usage examples:
 * <ul>
 *     <li>Search by name: {@code name=pencil}</li>
 *     <li>Filter by point cost range {@code minPointCost=1&maxPointCost=20}</li>
 *     <li>Filter by stock quantity range {@code minStock=1&maxStock=20}</li>
 *     <li>Combined search {@code name=pencil&minPointCost=2&maxPointCost=20&minStock=1&maxStock=20}</li>
 * </ul>
 *
 * @version 1.0
 * @author Dylan Mercer
 */
@Getter
@Setter
public class RewardItemSearchCriteria {
    private String name;
    private Integer minPointCost;
    private Integer maxPointCost;
    private Integer minStock;
    private Integer maxStock;

    /**
     * Determines if any search filters have been specified.
     *
     * @return true if at least one filter is set, false if all filters are null
     */
    public boolean hasFilters() {
        return name != null || minPointCost != null || maxPointCost != null || minStock != null || maxStock != null;
    }
}
