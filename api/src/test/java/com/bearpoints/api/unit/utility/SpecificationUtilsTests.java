package com.bearpoints.api.unit.utility;

import com.bearpoints.api.entity.User;
import com.bearpoints.api.utility.SpecificationUtils;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SpecificationUtils}.
 * Verifies that each utility method correctly delegates to the {@link CriteriaBuilder}
 * with the expected arguments.
 *
 * @version 1.1
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SpecificationUtils Unit Tests")
public class SpecificationUtilsTests {
    @Mock
    private CriteriaBuilder cb;

    @Mock
    private Path<String> stringPath;

    @Mock
    private Path<Integer> intPath;

    @Mock
    private Join<Object, User> userJoin;

    @Mock
    private Path<String> firstNamePath;

    @Mock
    private Path<String> lastNamePath;

    @Mock
    private Expression<String> concatStep1;

    @Mock
    private Expression<String> concatStep2;

    @Mock
    private Expression<String> lowerExpr;

    @Mock
    private Predicate mockPredicate;

    @Nested
    @DisplayName("isNotBlank method")
    class IsNotBlank {
        @Test
        @DisplayName("Should return false for null")
        void shouldReturnFalseForNull() {
            assertFalse(SpecificationUtils.isNotBlank(null));
        }

        @Test
        @DisplayName("Should return false for empty string")
        void shouldReturnFalseForEmpty() {
            assertFalse(SpecificationUtils.isNotBlank(""));
        }

        @Test
        @DisplayName("Should return false for whitespace-only string")
        void shouldReturnFalseForWhitespaceOnly() {
            assertFalse(SpecificationUtils.isNotBlank("     "));
            assertFalse(SpecificationUtils.isNotBlank("\t\n"));
        }

        @Test
        @DisplayName("Should return true for non-blank string")
        void shouldReturnTrueForNonBlank() {
            assertTrue(SpecificationUtils.isNotBlank("test"));
            assertTrue(SpecificationUtils.isNotBlank(" a "));
        }
    }

    @Nested
    @DisplayName("likeIgnoreCase method")
    class LikeIgnoreCase {
        @Test
        @DisplayName("Should call cb.lower and cb.like with correct pattern")
        void shouldCreateLikeIgnoreCasePredicate() {
            String searchValue = "test";
            String expectedPattern = "%test%";
            when(cb.lower(stringPath)).thenReturn(lowerExpr);
            when(cb.like(lowerExpr, expectedPattern)).thenReturn(mockPredicate);
            Predicate result = SpecificationUtils.likeIgnoreCase(stringPath, searchValue, cb);
            assertSame(mockPredicate, result);
            verify(cb).lower(stringPath);
            verify(cb).like(lowerExpr, expectedPattern);
        }

        @Test
        @DisplayName("Should convert search value to lower case")
        void shouldConvertToLowerCase() {
            String mixedCase = "TeSt";
            String expectedPattern = "%test%";
            when(cb.lower(stringPath)).thenReturn(lowerExpr);
            when(cb.like(lowerExpr, expectedPattern)).thenReturn(mockPredicate);
            SpecificationUtils.likeIgnoreCase(stringPath, mixedCase, cb);
            verify(cb).like(lowerExpr, expectedPattern);
        }
    }

    @Nested
    @DisplayName("equal method")
    class EqualTests {
        @Test
        @DisplayName("Should call cb.equal with path and value")
        void shouldCreateEqualPredicate() {
            Object value = "someValue";
            when(cb.equal(stringPath, value)).thenReturn(mockPredicate);
            Predicate result = SpecificationUtils.equal(stringPath, value, cb);
            assertSame(mockPredicate, result);
            verify(cb).equal(stringPath, value);
        }
    }

    @Nested
    @DisplayName("greaterThanOrEqualTo method")
    class GreaterThanOrEqualToTests {
        @Test
        @DisplayName("Should call path.as(Number.class) and cb.greaterThanOrEqualTo")
        void shouldCreateGreaterThanOrEqualToPredicate() {
            Number value = 10;
            when(cb.ge(intPath, value)).thenReturn(mockPredicate);
            Predicate result = SpecificationUtils.greaterThanOrEqualTo(intPath, value, cb);
            assertSame(mockPredicate, result);
            verify(cb).ge(intPath, value);
        }
    }

    @Nested
    @DisplayName("lessThanOrEqualTo method")
    class LessThanOrEqualToTests {
        @Test
        @DisplayName("Should call path.as(Number.class) and cb.lessThanOrEqualTo")
        void shouldCreateLessThanOrEqualToPredicate() {
            Number value = 20;
            when(cb.le(intPath, value)).thenReturn(mockPredicate);
            Predicate result = SpecificationUtils.lessThanOrEqualTo(intPath, value, cb);
            assertSame(mockPredicate, result);
            verify(cb).le(intPath, value);
        }
    }

    @Nested
    @DisplayName("fullNameLikeIgnoreCase method")
    class FullNameLikeIgnoreCaseTests {
        @BeforeEach
        void setUp() {
            doReturn(firstNamePath).when(userJoin).get("firstName");
            doReturn(lastNamePath).when(userJoin).get("lastName");
            when(cb.concat(firstNamePath, " ")).thenReturn(concatStep1);
            when(cb.concat(concatStep1, lastNamePath)).thenReturn(concatStep2);
            when(cb.lower(concatStep2)).thenReturn(lowerExpr);
        }

        @Test
        @DisplayName("Should build full name expression and apply like ignore case")
        void shouldCreateFullNameLikeIgnoreCasePredicate() {
            String fullName = "john doe";
            String expectedPattern = "%john doe%";
            when(cb.like(lowerExpr, expectedPattern)).thenReturn(mockPredicate);
            Predicate result = SpecificationUtils.fullNameLikeIgnoreCase(userJoin, fullName, cb);
            assertSame(mockPredicate, result);
            verify(userJoin).get("firstName");
            verify(userJoin).get("lastName");
            verify(cb).concat(firstNamePath, " ");
            verify(cb).concat(concatStep1, lastNamePath);
            verify(cb).lower(concatStep2);
            verify(cb).like(lowerExpr, expectedPattern);
        }

        @Test
        @DisplayName("Should convert fullName to lower case")
        void shouldConvertFullNameToLowerCase() {
            String mixedCase = "JoHn DoE";
            String expectedPattern = "%john doe%";
            when(cb.like(lowerExpr, expectedPattern)).thenReturn(mockPredicate);
            SpecificationUtils.fullNameLikeIgnoreCase(userJoin, mixedCase, cb);
            verify(cb).like(lowerExpr, expectedPattern);
        }
    }
}
