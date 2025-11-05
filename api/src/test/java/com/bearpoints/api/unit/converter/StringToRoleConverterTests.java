package com.bearpoints.api.unit.converter;

import com.bearpoints.api.converter.StringToRoleConverter;
import com.bearpoints.api.entity.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StringToRoleConverter}.
 * <p>Validates conversion of string values to Role enum with proper case handling,
 * trimming, and error scenarios.
 *
 * <p>Test scenarios:
 * <ul>
 *     <li>Valid role conversions (case insensitive)</li>
 *     <li>Empty and blank string handling</li>
 *     <li>Invalid grade level scenarios</li>
 *     <li>All enum values coverage</li>
 * </ul>
 *
 * @see StringToRoleConverter
 * @see Role
 * @version 1.0
 * @author Dylan Mercer
 */
public class StringToRoleConverterTests {
    private StringToRoleConverter converter;

    @BeforeEach
    void setup() {
        converter = new StringToRoleConverter();
    }

    /**
     * Tests successful conversion of valid role strings.
     *
     * <p>Verifies that:
     * <ul>
     *     <li>All uppercase inputs convert correctly</li>
     *     <li>All lowercase inputs convert correctly</li>
     *     <li>Mixed case inputs convert correctly</li>
     *     <li>Leading/trailing whitespace is properly trimmed</li>
     * </ul>
     */
    @ParameterizedTest
    @MethodSource("provideValidRoles")
    @DisplayName("Convert with valid role string returns correct enum")
    void validRole_ReturnsCorrectEnum(String input, Role expected) {
        Role result = converter.convert(input);
        assertEquals(expected, result);
    }

    private static Stream<Arguments> provideValidRoles() {
        return Stream.of(
                // Uppercase
                Arguments.of("ADMIN", Role.ADMIN),
                Arguments.of("TEACHER", Role.TEACHER),
                Arguments.of("STUDENT", Role.STUDENT),
                // Lowercase
                Arguments.of("admin", Role.ADMIN),
                Arguments.of("teacher", Role.TEACHER),
                Arguments.of("student", Role.STUDENT),
                // Mixed case
                Arguments.of("Admin", Role.ADMIN),
                Arguments.of("Teacher", Role.TEACHER),
                Arguments.of("Student", Role.STUDENT),
                // With whitespace
                Arguments.of("    ADMIN  ", Role.ADMIN),
                Arguments.of(" teacher  ", Role.TEACHER),
                Arguments.of("  Student  ", Role.STUDENT)
        );
    }

    /**
     * Tests handling of empty and blank strings.
     *
     * <p>Validates that empty and whitespace-only strings return null without throwing exceptions.
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "    ", "\t", "\n"})
    @DisplayName("Convert with empty or blank string returns null")
    void emptyOrBlankString_ReturnsNull(String input) {
        Role result = converter.convert(input);
        assertNull(result);
    }

    /**
     * Tests conversion of all enum values to ensure complete coverage.
     *
     * <p>Verifies that every Role enum value can be converted from its string representation.
     */
    @Test
    @DisplayName("Convert handles all Role enum values")
    void allEnumValues_ConvertSuccessfully() {
        for (Role role : Role.values()) {
            Role result = converter.convert(role.name());
            assertEquals(role, result);
        }
    }

    /**
     * Tests error handling for invalid grade level strings.
     *
     * <p>Verifies that:
     * <ul>
     *     <li>Invalid inputs throw IllegalArgumentException</li>
     *     <li>Error message contains the invalid value</li>
     *     <li>Error message lists all valid values</li>
     * </ul>
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "INVALID", "NONEXISTENT", "pope", "administrator", "peer", "educator"
    })
    @DisplayName("Convert with invalid role throws IllegalArgumentException")
    void invalidRole_ThrowsException(String invalidInput) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> converter.convert(invalidInput)
        );
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Invalid role: " + invalidInput));
        assertTrue(exception.getMessage().contains(java.util.Arrays.toString(Role.values())));
    }

    /**
     * Tests that null input is handled gracefully.
     *
     * <p>Validates that null input returns null without throwing exceptions, supporting optional request parameters.
     */
    @Test
    @DisplayName("Convert with null inputs returns null")
    void nullInput_ReturnsNull() {
        Role result = converter.convert(null);
        assertNull(result);
    }

    /**
     * Tests error message format and content for invalid inputs.
     *
     * <p>Ensures that exception message is user-friendly and includes both the invalid value and available options.
     */
    @Test
    @DisplayName("Convert with invalid input provides helpful error message")
    void invalidInput_ProvidesHelpfulErrorMessage() {
        String invalidInput = "PROFESSOR";
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> converter.convert(invalidInput)
        );
        String message = exception.getMessage();
        assertTrue(message.contains("Invalid role: " + invalidInput));
        assertTrue(message.contains("Valid values are:"));
        for (Role role :  Role.values()) {
            assertTrue(message.contains(role.name()));
        }
    }

    /**
     * Tests edge cases with unusual but valid inputs.
     *
     * <p>Validates robust handling of edge cases like:
     * <ul>
     *     <li>Very long whitespace</li>
     *     <li>Special characters in valid format</li>
     * </ul>
     */
    @Test
    @DisplayName("Convert handles edge cases correctly")
    void edgeCases_HandleCorrectly() {
        assertNull(converter.convert("              "));
        assertNull(converter.convert("\t"));
        assertNull(converter.convert("\n"));
        assertNull(converter.convert("     \t    \n   "));
    }

    /**
     * Tests that the converter is case-insensitive for valid inputs.
     *
     * <p>Verifies various case combinations all map to the correct enum values.
     */
    @Test
    @DisplayName("Convert is case insensitive for valid roles")
    void caseInsensitive_ReturnsCorrectEnum() {
        assertEquals(Role.ADMIN, converter.convert("admin"));
        assertEquals(Role.ADMIN, converter.convert("ADMIN"));
        assertEquals(Role.ADMIN, converter.convert("Admin"));
        assertEquals(Role.ADMIN, converter.convert("aDmin"));
    }
}
