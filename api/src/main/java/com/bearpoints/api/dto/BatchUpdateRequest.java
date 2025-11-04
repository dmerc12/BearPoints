package com.bearpoints.api.dto;

import java.util.List;

/**
 * Record representing a batch update operation for Google Sheets.
 * <p>Used in batch update operations to specify:
 * <ul>
 *     <li>{@code rowNumber} - Target row index (1-based)</li>
 *     <li>{@code data} - Cell values for the row</li>
 * </ul>
 *
 * @param rowNumber Sheet row number (starting from 1)
 * @param data List of cell values
 *
 * @version 1.0
 * @author Dylan Mercer
 */
public record BatchUpdateRequest(int rowNumber, List<String> data) {
}
