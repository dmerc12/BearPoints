package com.bearpoints.api.unit.utility;

import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.utility.GradeLevelUtils;
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
 * Unit tests for {@link GradeLevelUtils}.
 * <p>Comprehensively tests grade level validation and conversion logic including:
 * <ul>
 *     <li>Valid grade level conversions with various cases and formats</li>
 *     <li>Null, empty, and blank string handling</li>
 *     <li>Invalid grade level exception scenarios</li>
 *     <li>Edge cases and boundary conditions</li>
 *     <li>Case insensitivity and whitespace trimming</li>
 *     <li>Hyphen to underscore conversion</li>
 * </ul>
 *
 * @see GradeLevelUtils
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("GradeLevelUtils Tests")
public class GradeLevelUtilsTests {
    @ParameterizedTest
    @MethodSource("provideValidGradeLevels")
    @DisplayName("Validate and convert with valid grade level string returns correct enum")
    void validGradeLevel_ReturnsCorrectEnum(String input, GradeLevel expected) {
        GradeLevel result = GradeLevelUtils.validateAndConvertGrade(input);
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
                Arguments.of("  SECOND  ", GradeLevel.SECOND),
                Arguments.of("  THIRD   ", GradeLevel.THIRD),
                Arguments.of(" FOURTH   ", GradeLevel.FOURTH),
                // Hyphen conversion
                Arguments.of("pre-k", GradeLevel.PRE_K),
                Arguments.of("PRE-K", GradeLevel.PRE_K),
                Arguments.of("Pre-K", GradeLevel.PRE_K),
                Arguments.of("   pre-k  ", GradeLevel.PRE_K),
                Arguments.of("pre-k  ", GradeLevel.PRE_K),
                Arguments.of("   pre-k", GradeLevel.PRE_K),
                Arguments.of("pRe-K", GradeLevel.PRE_K),
                Arguments.of("PrE-k", GradeLevel.PRE_K)
        );
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  ", "\t", "\n"})
    @DisplayName("Validate and convert with empty or blank string returns null")
    void emptyOrBlankString_ReturnsNull(String input) {
        GradeLevel result = GradeLevelUtils.validateAndConvertGrade(input);
        assertNull(result);
    }

    @Test
    @DisplayName("Validate and convert handles all GradeLevel enum values")
    void allEnumValues_ConvertSuccessfully() {
        for (GradeLevel gradeLevel : GradeLevel.values()) {
            GradeLevel result = GradeLevelUtils.validateAndConvertGrade(gradeLevel.name().toLowerCase());
            assertEquals(gradeLevel, result);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "INVALID", "NONEXISTENT", "fifth", "SIXTH", "1st", "2nd"
    })
    @DisplayName("Validate and convert with invalid grade level throws IllegalArgumentException")
    void invalidGradeLevel_ThrowsException(String invalidInput) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> GradeLevelUtils.validateAndConvertGrade(invalidInput)
        );
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("Invalid grade level: " + invalidInput));
        assertTrue(exception.getMessage().contains(java.util.Arrays.toString(GradeLevel.values())));
    }

    @Test
    @DisplayName("Validate and convert with invalid input provides helpful error message")
    void invalidInput_ProvidesHelpfulErrorMessage() {
        String invalidInput = "FIFTH_GRADE";
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> GradeLevelUtils.validateAndConvertGrade(invalidInput)
        );
        String message = exception.getMessage();
        assertTrue(message.contains("Invalid grade level: " + invalidInput));
        assertTrue(message.contains("Valid values are:"));
        for (GradeLevel grade : GradeLevel.values()) {
            assertTrue(message.contains(grade.name()));
        }
    }

    @Test
    @DisplayName("Validate and convert handles edge cases correctly")
    void edgeCases_HandleCorrectly() {
        assertNull(GradeLevelUtils.validateAndConvertGrade("  "));
        assertNull(GradeLevelUtils.validateAndConvertGrade("\t"));
        assertNull(GradeLevelUtils.validateAndConvertGrade("\n"));
        assertNull(GradeLevelUtils.validateAndConvertGrade("  \t    \n  "));
    }

    @Test
    @DisplayName("Validate and convert is case insensitive for valid grade levels")
    void caseInsensitive_ReturnsCorrectEnum() {
        assertEquals(GradeLevel.PRE_K, GradeLevelUtils.validateAndConvertGrade("pre_k"));
        assertEquals(GradeLevel.PRE_K, GradeLevelUtils.validateAndConvertGrade("PRE_K"));
        assertEquals(GradeLevel.PRE_K, GradeLevelUtils.validateAndConvertGrade("Pre_K"));
        assertEquals(GradeLevel.PRE_K, GradeLevelUtils.validateAndConvertGrade("pRe_K"));
        assertEquals(GradeLevel.FIRST, GradeLevelUtils.validateAndConvertGrade("first"));
        assertEquals(GradeLevel.FIRST, GradeLevelUtils.validateAndConvertGrade("FIRST"));
        assertEquals(GradeLevel.FIRST, GradeLevelUtils.validateAndConvertGrade("First"));
        assertEquals(GradeLevel.FIRST, GradeLevelUtils.validateAndConvertGrade("fIrSt"));
    }
}
