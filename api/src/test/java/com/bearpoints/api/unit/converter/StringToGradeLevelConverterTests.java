package com.bearpoints.api.unit.converter;

import com.bearpoints.api.converter.StringToGradeLevelConverter;
import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.unit.utility.GradeLevelUtilsTests;
import com.bearpoints.api.utility.GradeLevelUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link StringToGradeLevelConverter}.
 * <p>Validates Spring converter functionality by ensuring proper delegation to {@link GradeLevelUtils}.
 *
 * <p>Test scenarios:
 * <ul>
 *     <li>Delegation to GradeLevelUtils for conversion logic</li>
 *     <li>Proper handling of Spring's @Nullable annotation</li>
 *     <li>Exception propagation from utility class</li>
 * </ul>
 * <p>Note: Detailed grade level conversion logic is tested in {@link GradeLevelUtilsTests}
 *
 * @see StringToGradeLevelConverter
 * @see GradeLevel
 * @version 2.0
 * @author Dylan Mercer
 */
public class StringToGradeLevelConverterTests {
    private StringToGradeLevelConverter converter;

    @BeforeEach
    void setUp() {
        converter = new StringToGradeLevelConverter();
    }

    @Test
    @DisplayName("Convert delegates to GradeLevelUtils for valid input")
    void convert_DelegatesToGradeLevelUtils_ForValidInput() {
        GradeLevel result = converter.convert("FIRST");
        assertEquals(GradeLevel.FIRST, result);
    }

    @Test
    @DisplayName("Convert delegates to GradeLevelUtils for null input")
    void convert_DelegatesToGradeLevelUtils_ForNullInput() {
        GradeLevel result = converter.convert(null);
        assertNull(result);
    }

    @Test
    @DisplayName("Convert delegates to GradeLevelUtils for empty input")
    void convert_DelegatesToGradeLevelUtils_ForEmptyInput() {
        GradeLevel result = converter.convert("");
        assertNull(result);
    }

    @Test
    @DisplayName("Convert delegates to GradeLevelUtils for invalid input")
    void convert_DelegatesToGradeLevelUtils_ForInvalidInput() {
        assertThrows(
                IllegalArgumentException.class,
                () -> converter.convert("INVALID")
        );
    }

    @Test
    @DisplayName("Convert handles Spring's @Nullable annotation correctly")
    void convert_HandlesNullableAnnotation_Correctly() {
        assertNull(converter.convert(null));
        assertNull(converter.convert(""));
        assertNull(converter.convert("   "));
    }

    @Test
    @DisplayName("Convert propagates exceptions from GradeLevelUtils")
    void convert_PropagatesExceptions_FromGradeLevelUtils() {
        String invalidInput = "INVALID_GRADE";
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> converter.convert(invalidInput)
        );
        assertTrue(exception.getMessage().contains("Invalid grade level: " + invalidInput));
    }

    @Test
    @DisplayName("Convert delegates all format handling to GradeLevelUtils")
    void convert_DelegatesAllFormatHandling_ToGradeLevelUtils() {
        assertEquals(GradeLevel.PRE_K, converter.convert("pre-k"));
        assertEquals(GradeLevel.FIRST, converter.convert("  first   "));
        assertEquals(GradeLevel.SECOND, converter.convert("Second"));
    }
}
