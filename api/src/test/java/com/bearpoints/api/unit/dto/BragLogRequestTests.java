package com.bearpoints.api.unit.dto;

import com.bearpoints.api.dto.BragLogRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link BragLogRequest} functionality.
 * <p>Verifies:
 * <ul>
 *     <li>Validation constraints enforce required fields</li>
 *     <li>Notes field respects maximum length</li>
 *     <li>Constructor initializes all fields</li>
 * </ul>
 *
 * @see BragLogRequest
 * @version 1.0
 * @author Dylan Mercer
 */
public class BragLogRequestTests {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("Valid request passes all constraints")
    void shouldPassValidationWithValidData() {
        BragLogRequest request = new BragLogRequest(
                1L,
                2L,
                Set.of(101L, 102L),
                "Good behavior"
        );
        Set<ConstraintViolation<BragLogRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    @DisplayName("Null studentId violates constraint")
    void shouldFailValidationWithNullStudentId() {
        BragLogRequest request = new BragLogRequest(
                null,
                2L,
                Set.of(101L),
                null
        );
        Set<ConstraintViolation<BragLogRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Student ID is required", violations.iterator().next().getMessage());
    }

    @Test
    @DisplayName("Empty behaviorIds violates constraint")
    void shouldFailValidationWithEmptyBehaviorIds() {
        BragLogRequest request = new BragLogRequest(
                1L,
                2L,
                Collections.emptySet(),
                null
        );
        Set<ConstraintViolation<BragLogRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("At least one behavior is required", violations.iterator().next().getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {501, 1000})
    @DisplayName("Long notes violate size constraint")
    void shouldFailValidationWhenNotesTooLong(int length) {
        String longNotes = "A".repeat(length);
        BragLogRequest request = new BragLogRequest(
                1L,
                2L,
                Set.of(101L),
                longNotes
        );
        Set<ConstraintViolation<BragLogRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("Notes cannot exceed 500 characters", violations.iterator().next().getMessage());
    }
}
