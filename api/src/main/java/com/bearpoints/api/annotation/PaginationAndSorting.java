package com.bearpoints.api.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * Annotation to automatically resolve pagination and sorting parameters
 * into a Pageable object.
 *
 * <p>Usage:
 * {@code
 * public ResponseEntity<?> getItems(
 *     @PaginationAndSorting Pageable pageable
 * ) { controller logic }
 * }
 */
@Documented
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface PaginationAndSorting {
    /**
     * Alias for 'pageParam'
     */
    @AliasFor("pageParam")
    String value() default "page";

    /**
     * Request parameter name for page number
     */
    @AliasFor("value")
    String pageParam() default "page";

    /**
     * Request parameter name for page size
     */
    String sizeParam() default "size";

    /**
     * Request parameter name for sort string
     */
    String sortParam() default "sort";

    /**
     * Default page number (0-indexed)
     */
    int defaultPage() default 0;

    /**
     * Default page size
     */
    int defaultSize() default 20;

    /**
     * Default sort string
     */
    String defaultSort() default "id,asc";

    /**
     * Maximum allowed page size
     */
    int maxSize() default 100;

    /**
     * Whether to validate sort parameters
     */
    boolean validateSort() default true;

    /**
     * List of allowed sort properties (empty for all)
     */
    String[] allowedSortProperties() default {};
}
