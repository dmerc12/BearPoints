package com.bearpoints.api.unit.converter;

import com.bearpoints.api.converter.StringToGradeLevelConverter;
import com.bearpoints.api.entity.GradeLevel;
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
 * Unit tests for {@link StringToGradeLevelConverter}.
 * <p>Validates conversion of string values to GradeLevel enum with proper case handling,
 * trimming, and error scenarios.
 *
 * <p>Test scenarios:
 * <ul>
 *     <li>Valid grade level conversions (case insensitive)</li>
 *     <li>Empty and blank string handling</li>
 *     <li>Invalid grade level scenarios</li>
 *     <li>Null input handling</li>
 *     <li>All enum values coverage</li>
 * </ul>
 *
 * @see StringToGradeLevelConverter
 * @see GradeLevel
 * @version 1.0
 * @author Dylan Mercer
 */
public class StringToGradeLevelConverterTests {
    private StringToGradeLevelConverter converter;

    @BeforeEach
    void setUp() {
        converter = new StringToGradeLevelConverter();
    }

    /**
     * Tests successful conversion of valid grade level strings.
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
    @MethodSource("provideValidGradeLevels")
    @DisplayName("Convert with valid grade level string returns correct enum")
    void validGradeLevel_ReturnsCorrectEnum(String input, GradeLevel expected) {
        GradeLevel result = converter.convert(input);
        assertEquals(expected, result);
    }

    private static Stream<Arguments> provideValidGradeLevels() {
        return Stream.of(
                // Uppercase
                Arguments.of("PRE_K", GradeLevel.PRE_K),
                Arguments.of("K", GradeLevel.K),
                Arguments.of("FIRST", GradeLevel.FIRST),
                Arguments.of("SECOND", GradeLevel.SECOND),
                Arguments.of("THIRD", GradeLevel.THIRD),
                Arguments.of("FOURTH", GradeLevel.FOURTH),
                // Lowercase
                Arguments.of("pre_k", GradeLevel.PRE_K),
                Arguments.of("k", GradeLevel.K),
                Arguments.of("first", GradeLevel.FIRST),
                Arguments.of("second", GradeLevel.SECOND),
                Arguments.of("third", GradeLevel.THIRD),
                Arguments.of("fourth", GradeLevel.FOURTH),
                // Mixed case
                Arguments.of("Pre_K", GradeLevel.PRE_K),
                Arguments.of("First", GradeLevel.FIRST),
                Arguments.of("Second", GradeLevel.SECOND),
                Arguments.of("Third", GradeLevel.THIRD),
                Arguments.of("Fourth", GradeLevel.FOURTH),
                // With whitespace
                Arguments.of("  PRE_K  ", GradeLevel.PRE_K),
                Arguments.of("K  ", GradeLevel.K),
                Arguments.of("  first  ", GradeLevel.FIRST),
                Arguments.of(" SECOND  ", GradeLevel.SECOND),
                Arguments.of(" THIRD ", GradeLevel.THIRD),
                Arguments.of("  FOURTH   ", GradeLevel.FOURTH)
        );
    }

    /**
     * Tests handling of empty and blank strings.
     *
     * <p>Validates that empty and whitespace-only strings return null without throwing exceptions.
     */
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    @DisplayName("Convert with empty or blank string returns null")
    void emptyOrBlankString_ReturnsNull(String input) {
        GradeLevel result = converter.convert(input);
        assertNull(result);
    }

    /**
     * Tests conversion of all enum values to ensure complete coverage.
     *
     * <p>Verifies that every GradeLevel enum value can be converted from its string representation.
     */
    @Test
    @DisplayName("Convert handles all GradeLevel enum values")
    void allEnumVales_ConvertSuccessfully() {
        for (GradeLevel gradeLevel : GradeLevel.values()) {
            GradeLevel result = converter.convert(gradeLevel.name().toLowerCase());
            assertEquals(gradeLevel, result);
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
            "INVALID", "NONEXISTENT", "fifth", "SIXTH", "1st", "2nd"
    })
    @DisplayName("Convert with invalid grade level throws IllegalArgumentException")
    void invalidGradeLevel_ThrowsException(String invalidInput) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> converter.convert(invalidInput)
        );
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Invalid grade level: " + invalidInput));
        assertTrue(exception.getMessage().contains(java.util.Arrays.toString(GradeLevel.values())));
    }

    /**
     * Tests that null input is handled gracefully.
     *
     * <p>Validates that null input returns null without throwing exceptions, supporting optional request parameters.
     */
    @Test
    @DisplayName("Convert with null input returns null")
    void nullInput_ReturnsNull() {
        GradeLevel result = converter.convert(null);
        assertNull(result);
    }

    /**
     * Tests error message format and content for invalid inputs.
     *
     * <p>Ensures the exception message is user-friendly and includes both the invalid value and available options.
     */
    @Test
    @DisplayName("Convert with invalid input provides helpful error message")
    void invalidInput_ProvidesHelpfulErrorMessage() {
        String invalidInput = "FIFTH_GRADE";
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> converter.convert(invalidInput)
        );
        String message = exception.getMessage();
        assertTrue(message.contains("Invalid grade level: " + invalidInput));
        assertTrue(message.contains("Valid values are:"));
        for (GradeLevel grade : GradeLevel.values()) {
            assertTrue(message.contains(grade.name()));
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
        assertNull(converter.convert("           "));
        assertNull(converter.convert("\t"));
        assertNull(converter.convert("\n"));
        assertNull(converter.convert("   \t  \n "));
    }

    /**
     * Tests that the converter is case-insensitive for valid inputs.
     *
     * <p>Verifies various case combinations all map to the correct enum values.
     */
    @Test
    @DisplayName("Convert is case insensitive for valid grade levels")
    void caseInsensitive_ReturnsCorrectEnum() {
        assertEquals(GradeLevel.PRE_K, converter.convert("pre_k"));
        assertEquals(GradeLevel.PRE_K, converter.convert("PRE_K"));
        assertEquals(GradeLevel.PRE_K, converter.convert("Pre_K"));
        assertEquals(GradeLevel.PRE_K, converter.convert("pRe_K"));
        assertEquals(GradeLevel.FIRST, converter.convert("first"));
        assertEquals(GradeLevel.FIRST, converter.convert("FIRST"));
        assertEquals(GradeLevel.FIRST, converter.convert("First"));
        assertEquals(GradeLevel.FIRST, converter.convert("fIrSt"));
    }
}
