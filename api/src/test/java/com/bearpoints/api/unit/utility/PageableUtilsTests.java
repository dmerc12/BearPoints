package com.bearpoints.api.unit.utility;

import com.bearpoints.api.utility.PageableUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link PageableUtils}.
 * <p>Comprehensive test suite for the pagination and sorting utility class that provides
 * robust creation and parsing of {@link Pageable} objects with flexible sorting options.
 *
 * <p>Tests validate the utility's ability to:
 * <ul>
 *     <li>Create {@link Pageable} objects with various sorting configurations</li>
 *     <li>Parse complex sort strings with single and multiple sort parameters</li>
 *     <li>Handle edge cases and invalid input gracefully</li>
 *     <li>Validate sort string formats and extract sort properties</li>
 *     <li>Convert between sort strings and {@link Sort} objects</li>
 *     <li>Provide safe defaults and validation for pagination parameters</li>
 * </ul>
 *
 * <p>The utility supports:
 * <ul>
 *     <li>Single and multiple sort parameters (separated by semicolons)</li>
 *     <li>Custom default values for page, size, and sort properties</li>
 *     <li>Flexible direction parsing (case-insensitive "asc" and "desc")</li>
 *     <li>Whitespace trimming and normalization</li>
 *     <li>Boundary validation for pagination parameters</li>
 * </ul>
 *
 * <p>This utility enables consistent pagination and sorting behavior across the application,
 * reducing boilerplate code and ensuring proper handling of pagination parameters.
 *
 * @see PageableUtils
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("PageableUtils Unit Tests")
public class PageableUtilsTests {
    @Nested
    @DisplayName("createPageable method")
    class CreatePageableTests {
        @Test
        @DisplayName("should create Pageable with default values when sort is empty")
        void shouldCreatePageableWithDefaultValuesWhenSortIsEmpty() {
            Pageable pageable = PageableUtils.createPageable(0, 20, "");
            assertAll(
                    () -> assertEquals(0, pageable.getPageNumber()),
                    () -> assertEquals(20, pageable.getPageSize()),
                    () -> assertTrue(pageable.getSort().isSorted()),
                    () -> assertEquals("id", pageable.getSort().iterator().next().getProperty()),
                    () -> assertEquals(Sort.Direction.ASC, pageable.getSort().iterator().next().getDirection())
            );
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("should handle null and empty sort strings")
        void shouldHandleNullAndEmptySortStrings(String sort) {
            Pageable pageable = PageableUtils.createPageable(0, 20, sort);
            assertAll(
                    () -> assertEquals(0, pageable.getPageNumber()),
                    () -> assertEquals(20, pageable.getPageSize()),
                    () -> assertTrue(pageable.getSort().isSorted())
            );
        }

        @ParameterizedTest
        @CsvSource({
                "name,asc,ASC",
                "name,desc,DESC",
                "name,DESC,DESC",
                "name,DeSc,DESC",
                "name,ASC,ASC"
        })
        @DisplayName("should parse single sort parameter with various directions")
        void shouldParseSingleSortParameterWithVariousDirections(String property, String direction,
                                                                 Sort.Direction expectedDirection) {
            String sort = property + "," + direction;
            Pageable pageable = PageableUtils.createPageable(0, 20, sort);
            Sort.Order order = pageable.getSort().iterator().next();
            assertAll(
                    () -> assertEquals(property, order.getProperty()),
                    () -> assertEquals(expectedDirection, order.getDirection())
            );
        }

        @Test
        @DisplayName("should handle single sort parameter without direction")
        void shouldHandleSingleSortParameterWithoutDirection() {
            String property = "name";
            Pageable pageable = PageableUtils.createPageable(0, 20, property);
            Sort.Order order = pageable.getSort().iterator().next();
            assertAll(
                    () -> assertEquals(property, order.getProperty()),
                    () -> assertEquals(Sort.Direction.ASC, order.getDirection())
            );
        }

        @Test
        @DisplayName("should create Pageable with multiple sort parameters")
        void shouldCreatePageableWithMultipleSortParameters() {
            List<String> properties = List.of("name", "pointValue", "active");
            List<String> directions = List.of("asc", "desc");
            String sort = properties.getFirst() + "," + directions.getFirst() + ";"
                    + properties.get(1) + "," + directions.get(1) + ";"
                    + properties.get(2);
            Pageable pageable = PageableUtils.createPageable(0, 20, sort);
            List<Sort.Order> orders = pageable.getSort().toList();
            assertAll(
                    () -> assertEquals(properties.size(), orders.size()),
                    () -> assertEquals(properties.getFirst(), orders.getFirst().getProperty()),
                    () -> assertEquals(Sort.Direction.valueOf(directions.getFirst().toUpperCase()), orders.getFirst().getDirection()),
                    () -> assertEquals(properties.get(1), orders.get(1).getProperty()),
                    () -> assertEquals(Sort.Direction.valueOf(directions.get(1).toUpperCase()), orders.get(1).getDirection()),
                    () -> assertEquals(properties.get(2), orders.get(2).getProperty()),
                    () -> assertEquals(Sort.Direction.ASC, orders.get(2).getDirection())
            );
        }

        @Test
        @DisplayName("should handle multiple sort parameters with extra spaces")
        void shouldHandleMultipleSortParametersWithExtraSpaces() {
            List<String> properties = List.of("name", "pointValue", "active");
            List<String> directions = List.of("asc", "desc");
            String sort = " " + properties.getFirst() + " , " + directions.getFirst() + " ; "
                    + properties.get(1) + " , " + directions.get(1) + " ; "
                    + properties.get(2) + " ";
            Pageable pageable = PageableUtils.createPageable(0, 20, sort);
            List<Sort.Order> orders = pageable.getSort().toList();
            assertAll(
                    () -> assertEquals(properties.size(), orders.size()),
                    () -> assertEquals(properties.getFirst(), orders.getFirst().getProperty()),
                    () -> assertEquals(Sort.Direction.valueOf(directions.getFirst().toUpperCase()), orders.getFirst().getDirection()),
                    () -> assertEquals(properties.get(1), orders.get(1).getProperty()),
                    () -> assertEquals(Sort.Direction.valueOf(directions.get(1).toUpperCase()), orders.get(1).getDirection()),
                    () -> assertEquals(properties.get(2), orders.get(2).getProperty()),
                    () -> assertEquals(Sort.Direction.ASC, orders.get(2).getDirection())
            );
        }

        @Test
        @DisplayName("should default to ASC for invalid direction")
        void shouldDefaultToAscForInvalidDirection() {
            String property = "name";
            String direction = "invalid";
            String sort = property + "," + direction;
            Pageable pageable = PageableUtils.createPageable(0, 20, sort);
            Sort.Order order = pageable.getSort().iterator().next();
            assertAll(
                    () -> assertEquals(property, order.getProperty()),
                    () -> assertEquals(Sort.Direction.ASC, order.getDirection())
            );
        }
    }

    @Nested
    @DisplayName("createPageable with default sort method")
    class CreatePageableWithDefaultSortTests {
        @Test
        @DisplayName("should use provided default sort when sort is empty")
        void shouldUseProvidedDefaultSortWhenSortIsEmpty() {
            String property = "name";
            String direction = "desc";
            String sort = property + "," + direction;
            Pageable pageable = PageableUtils.createPageable(0, 20, "", sort);
            Sort.Order order = pageable.getSort().iterator().next();
            assertAll(
                    () -> assertEquals(property, order.getProperty()),
                    () -> assertEquals(Sort.Direction.valueOf(direction.toUpperCase()), order.getDirection())
            );
        }

        @Test
        @DisplayName("should use provided sort when not empty")
        void shouldUseProvidedSortWhenNotEmpty() {
            String property = "pointValue";
            String direction = "asc";
            String sort = property + "," + direction;
            Pageable pageable = PageableUtils.createPageable(0, 20, sort, "name,desc");
            Sort.Order order = pageable.getSort().iterator().next();
            assertAll(
                    () -> assertEquals(property, order.getProperty()),
                    () -> assertEquals(Sort.Direction.valueOf(direction.toUpperCase()), order.getDirection())
            );
        }
    }

    @Nested
    @DisplayName("createPageableWithDefaultProperty method")
    class CreatePageableWithDefaultPropertyTests {
        @Test
        @DisplayName("should use default property when sort is empty")
        void shouldUseDefaultPropertyWhenSortIsEmpty() {
            String property = "createdDate";
            Pageable pageable = PageableUtils.createPageableWithDefaultProperty(0, 20, "", property);
            Sort.Order order = pageable.getSort().iterator().next();
            assertAll(
                    () -> assertEquals(property, order.getProperty()),
                    () -> assertEquals(Sort.Direction.ASC, order.getDirection())
            );
        }

        @Test
        @DisplayName("should use provided sort when not empty")
        void shouldUseProvidedSortWhenNotEmpty() {
            String property = "name";
            String direction = "desc";
            String sort = property + "," + direction;
            Pageable pageable = PageableUtils.createPageableWithDefaultProperty(0, 20, sort, "createdDate");
            Sort.Order order = pageable.getSort().iterator().next();
            assertAll(
                    () -> assertEquals(property, order.getProperty()),
                    () -> assertEquals(Sort.Direction.valueOf(direction.toUpperCase()), order.getDirection())
            );
        }

        @Test
        @DisplayName("should handle empty default property")
        void shouldHandleEmptyDefaultProperty() {
            Pageable pageable = PageableUtils.createPageableWithDefaultProperty(0, 10, "", "");
            Sort.Order order = pageable.getSort().iterator().next();
            assertEquals("id", order.getProperty());
            assertEquals(Sort.Direction.ASC, order.getDirection());
        }

        @Test
        @DisplayName("should handle null default property")
        void shouldHandleNullDefaultProperty() {
            Pageable pageable = PageableUtils.createPageableWithDefaultProperty(0, 10, "", null);
            Sort.Order order = pageable.getSort().iterator().next();
            assertEquals("id", order.getProperty());
            assertEquals(Sort.Direction.ASC, order.getDirection());
        }
    }

    @Nested
    @DisplayName("parseSort method")
    class ParseSortTests {
        @Test
        @DisplayName("should parse empty string to default sort")
        void shouldParseEmptyStringToDefaultSort() {
            Sort sort = PageableUtils.parseSort("");
            Sort.Order order = sort.iterator().next();
            assertAll(
                    () -> assertEquals("id", order.getProperty()),
                    () -> assertEquals(Sort.Direction.ASC, order.getDirection())
            );
        }

        @Test
        @DisplayName("should parse single sort with direction")
        void shouldParseSingleSortWithDirection() {
            String property = "name";
            String direction = "desc";
            String singleSort = property + "," + direction;
            Sort sort = PageableUtils.parseSort(singleSort);
            Sort.Order order = sort.iterator().next();
            assertAll(
                    () -> assertEquals(property, order.getProperty()),
                    () -> assertEquals(Sort.Direction.valueOf(direction.toUpperCase()), order.getDirection())
            );
        }

        @Test
        @DisplayName("should parse multiple sorts")
        void shouldParseMultipleSorts() {
            List<String> properties = List.of("name", "pointValue", "active");
            List<String> directions = List.of("asc", "desc");
            String multipleSort = properties.getFirst() + "," + directions.getFirst() + ";"
                    + properties.get(1) + "," + directions.get(1) + ";"
                    + properties.get(2);
            Sort sort = PageableUtils.parseSort(multipleSort);
            List<Sort.Order> orders = sort.toList();
            assertAll(
                    () -> assertEquals(properties.size(), orders.size()),
                    () -> assertEquals(properties.getFirst(), orders.getFirst().getProperty()),
                    () -> assertEquals(Sort.Direction.valueOf(directions.getFirst().toUpperCase()), orders.getFirst().getDirection()),
                    () -> assertEquals(properties.get(1), orders.get(1).getProperty()),
                    () -> assertEquals(Sort.Direction.valueOf(directions.get(1).toUpperCase()), orders.get(1).getDirection()),
                    () -> assertEquals(properties.get(2), orders.get(2).getProperty()),
                    () -> assertEquals(Sort.Direction.ASC, orders.get(2).getDirection())
            );
        }

        @Test
        @DisplayName("should handle trailing semicolon")
        void shouldHandleTrailingSemicolon() {
            String property = "name";
            String direction = "asc";
            String singleSort = property + "," + direction + ";";
            Sort sort = PageableUtils.parseSort(singleSort);
            Sort.Order order = sort.iterator().next();
            assertAll(
                    () -> assertEquals(property, order.getProperty()),
                    () -> assertEquals(Sort.Direction.valueOf(direction.toUpperCase()), order.getDirection())
            );
        }

        @Test
        @DisplayName("should handle empty sort clauses")
        void shouldHandleEmptySortClauses() {
            List<String> properties = List.of("name", "pointValue");
            List<String> directions = List.of("asc", "desc");
            String multipleSort = properties.getFirst() + "," + directions.getFirst() + ";" + ";"
                    + properties.get(1) + "," + directions.get(1);
            Sort sort = PageableUtils.parseSort(multipleSort);
            List<Sort.Order> orders = sort.toList();
            assertAll(
                    () -> assertEquals(properties.size(), orders.size()),
                    () -> assertEquals(properties.getFirst(), orders.getFirst().getProperty()),
                    () -> assertEquals(Sort.Direction.valueOf(directions.getFirst().toUpperCase()), orders.getFirst().getDirection()),
                    () -> assertEquals(properties.get(1), orders.get(1).getProperty()),
                    () -> assertEquals(Sort.Direction.valueOf(directions.get(1).toUpperCase()), orders.get(1).getDirection())
            );
        }

        @Test
        @DisplayName("should return default when all clauses are empty in multi-sort")
        void shouldReturnDefaultWhenAllClausesAreEmptyInMultiSort() {
            Sort sort = PageableUtils.parseSort(";;;");
            Sort.Order order = sort.iterator().next();
            assertAll(
                    () -> assertEquals("id", order.getProperty()),
                    () -> assertEquals(Sort.Direction.ASC, order.getDirection())
            );
        }

        @Test
        @DisplayName("should return default when multi-sort has only whitespace clauses")
        void shouldReturnDefaultWhenMultiSortHasOnlyWhitespaceClauses() {
            Sort sort = PageableUtils.parseSort("   ;   ;   ");
            Sort.Order order = sort.iterator().next();
            assertAll(
                    () -> assertEquals("id", order.getProperty()),
                    () -> assertEquals(Sort.Direction.ASC, order.getDirection())
            );
        }
    }

    @Nested
    @DisplayName("isValidSortString method")
    class IsValidSortStringTests {
        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"  ", "\t", "\n"})
        @DisplayName("should return true for null, empty, or blank strings")
        void shouldReturnTrueForNullOrEmptyOrBlankStrings(String sort) {
            assertTrue(PageableUtils.isValidSortString(sort));
        }

        @ParameterizedTest
        @ValueSource(strings =
                {"name", "name,asc", "name,desc", "name,ASC", "name,DESC",
                "name,asc;pointValue,desc","name;pointValue;active"})
        @DisplayName("should return true for valid sort strings")
        void shouldReturnTrueForValidSortStrings(String sort) {
            assertTrue(PageableUtils.isValidSortString(sort));
        }

        @ParameterizedTest
        @ValueSource(strings =
                {",asc", "name,invalid", "name,asc;;pointValue,desc", ","})
        @DisplayName("should return false for invalid sort strings")
        void shouldReturnFalseForInvalidSortStrings(String sort) {
            assertFalse(PageableUtils.isValidSortString(sort));
        }

        @Test
        @DisplayName("should return true for valid sort with extra spaces")
        void shouldReturnTrueForValidSortWithExtraSpaces() {
            String sort = " name , asc ; pointValue , desc ";
            assertTrue(PageableUtils.isValidSortString(sort));
        }

        @Test
        @DisplayName("should test direction validation branch with empty direction")
        void shouldTestDirectionValidationBranchWithEmptyDirection() {
            assertTrue(PageableUtils.isValidSortString("name,"));
            assertTrue(PageableUtils.isValidSortString("name, "));
            assertTrue(PageableUtils.isValidSortString("name,  "));
        }

        @Test
        @DisplayName("should return false for non-empty invalid direction")
        void shouldReturnFalseForNonEmptyInvalidDirection() {
            assertFalse(PageableUtils.isValidSortString("name,invalid"));
            assertFalse(PageableUtils.isValidSortString("name,ASCEND"));
            assertFalse(PageableUtils.isValidSortString("name,random"));
        }

        @Test
        @DisplayName("should handle mixed valid and invalid direction in multi-sort")
        void shouldHandleMixedValidAndInvalidDirectionInMultiSort() {
            assertFalse(PageableUtils.isValidSortString("name,asc;createdAt,invalid"));
            assertFalse(PageableUtils.isValidSortString("name,desc;pointValue,wrong"));
        }
    }

    @Nested
    @DisplayName("extractSortProperties method")
    class ExtractSortPropertiesTests {
        @Test
        @DisplayName("should extract properties from empty sort string")
        void shouldExtractPropertiesFromEmptySortString() {
            List<String> properties = PageableUtils.extractSortProperties("");
            assertEquals(1, properties.size());
            assertEquals("id", properties.getFirst());
        }

        @Test
        @DisplayName("should extract single property")
        void shouldExtractSingleProperty() {
            String property = "name";
            List<String> properties = PageableUtils.extractSortProperties(property + ",asc");
            assertEquals(1, properties.size());
            assertEquals(property, properties.getFirst());
        }

        @Test
        @DisplayName("should extract multiple properties")
        void shouldExtractMultipleProperties() {
            List<String> propertyList = List.of("name", "pointValue", "active");
            List<String> directions = List.of("asc", "desc");
            String multipleSort = propertyList.getFirst() + "," + directions.getFirst() + ";"
                    + propertyList.get(1) + "," + directions.get(1) + ";"
                    + propertyList.get(2);
            List<String> properties = PageableUtils.extractSortProperties(multipleSort);
            assertEquals(propertyList.size(), properties.size());
            assertThat(properties).containsExactlyElementsOf(propertyList);
        }

        @Test
        @DisplayName("should ignore empty clauses")
        void shouldIgnoreEmptyClauses() {
            List<String> propertyList = List.of("name", "pointValue");
            List<String> directions = List.of("asc", "desc");
            String multipleSort = propertyList.getFirst() + "," + directions.getFirst() + ";" + ";"
                    + propertyList.get(1) + "," + directions.get(1) + ";";
            List<String> properties = PageableUtils.extractSortProperties(multipleSort);
            assertEquals(propertyList.size(), properties.size());
            assertThat(properties).containsExactlyElementsOf(propertyList);
        }

        @Test
        @DisplayName("should trim property names")
        void shouldTrimPropertyNames() {
            List<String> propertyList = List.of("name", "pointValue");
            List<String> directions = List.of("asc", "desc");
            String multipleSort = " " + propertyList.getFirst() + " , " + directions.getFirst() + " ; "
                    + propertyList.get(1) + " , " + directions.get(1) + " ";
            List<String> properties = PageableUtils.extractSortProperties(multipleSort);
            assertEquals(propertyList.size(), properties.size());
            assertThat(properties).containsExactlyElementsOf(propertyList);
        }

        @Test
        @DisplayName("should skip clauses with empty property name")
        void shouldSkipClausesWithEmptyPropertyName() {
            List<String> properties = PageableUtils.extractSortProperties(",asc;name,desc;,desc");
            assertEquals(1, properties.size());
            assertEquals("name", properties.getFirst());
        }

        @Test
        @DisplayName("should skip clauses with only whitespace property")
        void shouldSkipClausesWithOnlyWhitespaceProperty() {
            List<String> properties = PageableUtils.extractSortProperties(",asc;name,desc;   ,desc");
            assertEquals(1, properties.size());
            assertEquals("name", properties.getFirst());
        }

        @Test
        @DisplayName("should handle all empty property names")
        void shouldHandleAllEmptyPropertyNames() {
            List<String> properties = PageableUtils.extractSortProperties(",asc;,desc;,");
            assertEquals(0, properties.size());
            assertTrue(properties.isEmpty());
        }

        @Test
        @DisplayName("should handle empty list when all property names are empty")
        void shouldHandleEmptyListWhenAllPropertyNamesAreEmpty() {
            List<String> properties = PageableUtils.extractSortProperties(",asc;,desc");
            assertTrue(properties.isEmpty());
        }

        @Test
        @DisplayName("should handle null input")
        void shouldHandleNullInput() {
            List<String> properties = PageableUtils.extractSortProperties(null);
            assertEquals(1, properties.size());
            assertEquals("id", properties.getFirst());
        }
    }

    @Nested
    @DisplayName("createSortString method")
    class CreateSortStringTests {
        @Test
        @DisplayName("should return empty string for null Sort")
        void shouldReturnEmptyStringForNullSort() {
            assertEquals("", PageableUtils.createSortString(null));
        }

        @Test
        @DisplayName("should return empty string for unsorted Sort")
        void shouldReturnEmptyStringForUnsortedSort() {
            assertEquals("", PageableUtils.createSortString(Sort.unsorted()));
        }

        @Test
        @DisplayName("should create sort string from single order")
        void shouldCreateSortStringFromSingleOrder() {
            Sort sort = Sort.by(Sort.Order.asc("name"));
            String sortString = PageableUtils.createSortString(sort);
            assertEquals("name,asc", sortString);
        }

        @Test
        @DisplayName("should create sort string from multiple orders")
        void shouldCreateSortStringFromMultipleOrders() {
            Sort sort = Sort.by(
                    Sort.Order.asc("name"),
                    Sort.Order.desc("pointValue"),
                    Sort.Order.asc("active")
            );
            String sortString = PageableUtils.createSortString(sort);
            assertEquals("name,asc;pointValue,desc;active,asc", sortString);
        }
    }

    @Nested
    @DisplayName("safeCreatePageable method")
    class SafeCreatePageableTests {
        @Test
        @DisplayName("should handle null page with default")
        void shouldHandleNullPageWithDefault() {
            Pageable pageable = PageableUtils.safeCreatePageable(null, 20, "name,asc", "id,asc");
            assertEquals(0, pageable.getPageNumber());
        }

        @Test
        @DisplayName("should handle negative page number")
        void shouldHandleNegativePageNumber() {
            Pageable pageable = PageableUtils.safeCreatePageable(-1, 20, "name,asc", "id,asc");
            assertEquals(0, pageable.getPageNumber());
        }

        @Test
        @DisplayName("should handle null size with default")
        void shouldHandleNullSizeWithDefault() {
            Pageable pageable = PageableUtils.safeCreatePageable(0, null, "name,asc", "id,asc");
            assertEquals(20, pageable.getPageSize());
        }

        @Test
        @DisplayName("should enforce maximum page size")
        void shouldEnforceMaximumPageSize() {
            Pageable pageable = PageableUtils.safeCreatePageable(0, 200, "name,asc", "id,asc");
            assertEquals(100, pageable.getPageSize());
        }

        @Test
        @DisplayName("should enforce minimum page size")
        void shouldEnforceMinimumPageSize() {
            Pageable pageable = PageableUtils.safeCreatePageable(0, 0, "name,asc", "id,asc");
            assertEquals(1, pageable.getPageSize());
        }

        @Test
        @DisplayName("should handle invalid sort string")
        void shouldHandleInvalidSortString() {
            Pageable pageable = PageableUtils.safeCreatePageable(0, 200, "name,invalid", "id,asc");
            assertEquals("id,asc", PageableUtils.createSortString(pageable.getSort()));
        }

        @Test
        @DisplayName("should use default sort when sort is empty")
        void shouldUseDefaultSortWhenSortIsEmpty() {
            Pageable pageable = PageableUtils.safeCreatePageable(0, 200, "", "name,desc");
            assertEquals("name,desc", PageableUtils.createSortString(pageable.getSort()));
        }
    }

    @Test
    @DisplayName("should prevent instantiation of utility class")
    void shouldPreventInstantiationOfUtilityClass() {
        assertThrows(IllegalStateException.class, PageableUtils::new);
    }
}
