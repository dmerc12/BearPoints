package com.bearpoints.api.dto;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Generic paginated response wrapper for API responses.
 * <p>Provides a standardized structure for paginated results similar to Spring Data REST.
 *
 * <p>Fields:
 * <ul>
 *     <li>{@code content} - The page of items</li>
 *     <li>{@code totalElements} - Total number of elements across all pages</li>
 *     <li>{@code totalPages} - Total number of pages</li>
 *     <li>{@code size} - Number of elements per page</li>
 *     <li>{@code number} - Current page number (0-indexed)</li>
 *     <li>{@code first} - Whether this is the first page</li>
 *     <li>{@code last} - Whether this is the last page</li>
 *     <li>{@code numberOfElements} - Number of elements in current page</li>
 *     <li>{@code empty} - Whether the page is empty</li>
 * </ul>
 *
 * @param <T> Type of the content elements
 * @version 1.0
 * @author Dylan Mercer
 */
@Getter
public class PagedResponseDTO<T> {
    private final List<T> content;
    private final long totalElements;
    private final int totalPages;
    private final int size;
    private final int number;
    private final boolean first;
    private final boolean last;
    private final int numberOfElements;
    private final boolean empty;

    /**
     * Constructs a PagedResponseDTO from a Spring Data Page.
     *
     * @param page Spring Data Page object
     */
    public PagedResponseDTO(Page<T> page) {
        this.content = page.getContent();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.size = page.getSize();
        this.number = page.getNumber();
        this.first = page.isFirst();
        this.last = page.isLast();
        this.numberOfElements = page.getNumberOfElements();
        this.empty = page.isEmpty();
    }

    /**
     * Creates a PagedResponseDTO from a Spring Data Page.
     *
     * @param page Spring Data Page object
     * @param <T> Type of the content elements
     * @return PagedResponseDTO containing the paginated data
     */
    public static <T> PagedResponseDTO<T> of(Page<T> page) {
        return new PagedResponseDTO<>(page);
    }
}
