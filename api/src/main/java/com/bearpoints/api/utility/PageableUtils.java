package com.bearpoints.api.utility;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for creating Pageable objects with sorting support.
 * Supports single and multiple sort parameters.
 */
public class PageableUtils {
    private static final String SORT_DELIMITER = ",";
    private static final String MULTI_SORT_DELIMITER = ";";
    private static final Sort.Direction DEFAULT_DIRECTION = Sort.Direction.ASC;
    private static final String DEFAULT_SORT_PROPERTY = "id";

    public PageableUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Creates a Pageable object with custom defaults
     *
     * @param page Page number
     * @param size Page size
     * @param sort Sort string
     * @return Configured Pageable object
     */
    public static Pageable createPageable(int page, int size, String sort) {
        return PageRequest.of(page, size, parseSort(sort));
    }

    /**
     * Creates a Pageable object with custom defaults
     *
     * @param page Page number
     * @param size Page size
     * @param sort Sort string
     * @param defaultSort Default sort if sort string is empty
     * @return Configured Pageable object
     */
    public static Pageable createPageable(int page, int size, String sort, String defaultSort) {
        String actualSort = StringUtils.hasText(sort) ? sort : defaultSort;
        return createPageable(page, size, actualSort);
    }

    /**
     * Creates a Pageable with default sort property
     *
     * @param page Page number
     * @param size Page size
     * @param sort Sort string
     * @param defaultProperty Default property if sort string is empty
     * @return Configured Pageable object
     */
    public static Pageable createPageableWithDefaultProperty(int page, int size, String sort, String defaultProperty) {
        if (!StringUtils.hasText(sort)) {
            String propertyToUse = StringUtils.hasText(defaultProperty) ? defaultProperty : DEFAULT_SORT_PROPERTY;
            return PageRequest.of(page, size, Sort.by(DEFAULT_DIRECTION, propertyToUse));
        }
        return createPageable(page, size, sort);
    }

    /**
     * Parses sort string into Sort object
     * Supports multiple sort parameters separated by semicolon
     */
    public static Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(DEFAULT_DIRECTION, DEFAULT_SORT_PROPERTY);
        }
        // Check for multiple sort parameters
        if (sort.contains(MULTI_SORT_DELIMITER)) {
            return parseMultiSort(sort);
        }

        // Single sort parameter
        return parseSingleSort(sort);
    }

    /**
     * Parses single sort parameter
     */
    private static Sort parseSingleSort(String sort) {
        String[] parts = sort.split(SORT_DELIMITER);
        String property = parts[0].trim();
        if (parts.length == 1) {
            return Sort.by(DEFAULT_DIRECTION, property);
        }
        Sort.Direction direction = parseDirection(parts[1].trim());
        return Sort.by(direction, property);
    }

    /**
     * Parses multiple sort parameters
     */
    private static Sort parseMultiSort(String sort) {
        String[] sortClauses = sort.split(MULTI_SORT_DELIMITER);
        List<Sort.Order> orders = new ArrayList<>();
        for (String clause : sortClauses) {
            if (StringUtils.hasText(clause)) {
                String[] parts = clause.split(SORT_DELIMITER);
                String property = parts[0].trim();
                Sort.Direction direction = parts.length > 1 ? parseDirection(parts[1].trim()) : DEFAULT_DIRECTION;
                orders.add(new Sort.Order(direction, property));
            }
        }
        if (orders.isEmpty()) {
            return Sort.by(DEFAULT_DIRECTION, DEFAULT_SORT_PROPERTY);
        }
        return Sort.by(orders);
    }

    /**
     * Parses direction string
     */
    private static Sort.Direction parseDirection(String direction) {
        if ("desc".equalsIgnoreCase(direction)) {
            return Sort.Direction.DESC;
        }
        return Sort.Direction.ASC;
    }

    /**
     * Validates sort string format
     */
    public static boolean isValidSortString(String sort) {
        if (!StringUtils.hasText(sort)) {
            return true;
        }
        String[] clauses = sort.split(MULTI_SORT_DELIMITER);
        for (String clause : clauses) {
            String[] parts = clause.split(SORT_DELIMITER);
            if (parts.length == 0 || !StringUtils.hasText(parts[0])) {
                return false;
            }
            if (parts.length > 1) {
                String direction = parts[1].trim().toLowerCase();
                if (!direction.isEmpty() && !"asc".equals(direction) && !"desc".equals(direction)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Extracts sort properties from sort string
     */
    public static List<String> extractSortProperties(String sort) {
        if (!StringUtils.hasText(sort)) {
            return List.of(DEFAULT_SORT_PROPERTY);
        }
        List<String> properties = new ArrayList<>();
        String[] clauses = sort.split(MULTI_SORT_DELIMITER);
        for (String clause : clauses) {
            String[] parts = clause.split(SORT_DELIMITER);
            if (parts.length > 0 && StringUtils.hasText(parts[0])) {
                properties.add(parts[0].trim());
            }
        }
        return properties;
    }

    /**
     * Creates sort string from Sort object
     */
    public static String createSortString(Sort sort) {
        if (sort == null || !sort.iterator().hasNext()) {
            return "";
        }
        return sort.stream()
                .map(order ->
                        order.getProperty()
                                + SORT_DELIMITER
                                + order.getDirection().name().toLowerCase())
                .collect(Collectors.joining(MULTI_SORT_DELIMITER));
    }

    /**
     * Safely creates Pageable with validation
     */
    public static Pageable safeCreatePageable(Integer page, Integer size, String sort, String defaultSort) {
        int safePage = page != null && page >= 0 ? page : 0;
        int safeSize;
        if (size == null) {
            safeSize = 20;
        } else if (size > 100) {
            safeSize = 100;
        } else if (size < 1) {
            safeSize = 1;
        } else {
            safeSize = size;
        }
        String safeSort = StringUtils.hasText(sort) && isValidSortString(sort) ? sort : defaultSort;
        return createPageable(safePage, safeSize, safeSort);
    }
}
