package com.bearpoints.api.resolver;

import com.bearpoints.api.annotation.PaginationAndSorting;
import com.bearpoints.api.utility.PageableUtils;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Resolves Pageable objects from request parameters using @PaginationAndSorting annotation
 */
public class PaginationAndSortingArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(PaginationAndSorting.class)
                && Pageable.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  @NonNull NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        PaginationAndSorting annotation = parameter.getParameterAnnotation(PaginationAndSorting.class);
        if (annotation == null) {
            return null;
        }
        // Extract parameters from request
        String pageParam = webRequest.getParameter(annotation.pageParam());
        String sizeParam = webRequest.getParameter(annotation.sizeParam());
        String sortParam = webRequest.getParameter(annotation.sortParam());
        // Parse with defaults
        int page = parseInteger(pageParam, annotation.defaultPage());
        int size = parseInteger(sizeParam, annotation.defaultSize());
        // Validate and limit size
        size = Math.min(size, annotation.maxSize());
        size = Math.max(1, size);
        // Validate page
        page = Math.max(0, page);
        // Get sort string
        String sort = StringUtils.hasText(sortParam) ? sortParam : annotation.defaultSort();
        // Validate sort if enabled
        if (annotation.validateSort() && StringUtils.hasText(sort)) {
            sort = validateSort(sort, annotation.allowedSortProperties());
        }
        // If sort empty after validation, return unsorted Pageable
        if (!StringUtils.hasText(sort)) {
            return Pageable.ofSize(size).withPage(page);
        }
        // Create Pageable using utility
        return PageableUtils.createPageable(page, size, sort);
    }

    private int parseInteger(String value, int defaultValue) {
        if (StringUtils.hasText(value)) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private String validateSort(String sort, String[] allowedProperties) {
        if (!PageableUtils.isValidSortString(sort)) {
            return "";
        }
        // If no allowed properties specified, accept all
        if (allowedProperties.length == 0) {
            return sort;
        }
        // Check if all sort properties are allowed
        Set<String> allowedSet = new HashSet<>(Arrays.asList(allowedProperties));
        boolean allAllowed = allowedSet.containsAll(PageableUtils.extractSortProperties(sort));
        return allAllowed ? sort : "";
    }
}
