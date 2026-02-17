package com.bearpoints.api.unit.utility;

import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.entity.Student;
import com.bearpoints.api.entity.Teacher;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link SpecificationUtils}.
 * Verifies that each utility method correctly delegates to the {@link CriteriaBuilder}
 * with the expected arguments.
 *
 * @version 1.2
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

    @Mock
    private Path<Object> userPath;

    @Mock
    private Path<GradeLevel> gradePath;

    @Mock
    private Join<Object, Student> studentJoin;

    @Mock
    private Join<Object, Teacher> teacherJoin;

    @Mock
    private Join<Student, User> studentUserJoin;

    @Mock
    private Join<Teacher, User> teacherUserJoin;

    private List<Predicate> predicates;

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
        @DisplayName("Should call cb.greaterThanOrEqualTo with path and value")
        void shouldCreateGreaterThanOrEqualToPredicate() {
            Integer value = 10;
            when(cb.greaterThanOrEqualTo(intPath, value)).thenReturn(mockPredicate);
            Predicate result = SpecificationUtils.greaterThanOrEqualTo(intPath, value, cb);
            assertSame(mockPredicate, result);
            verify(cb).greaterThanOrEqualTo(intPath, value);
        }
    }

    @Nested
    @DisplayName("lessThanOrEqualTo method")
    class LessThanOrEqualToTests {
        @Test
        @DisplayName("Should call cb.lessThanOrEqualTo with path and value")
        void shouldCreateLessThanOrEqualToPredicate() {
            Integer value = 20;
            when(cb.lessThanOrEqualTo(intPath, value)).thenReturn(mockPredicate);
            Predicate result = SpecificationUtils.lessThanOrEqualTo(intPath, value, cb);
            assertSame(mockPredicate, result);
            verify(cb).lessThanOrEqualTo(intPath, value);
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

    @Nested
    @DisplayName("addUserTextFilters method")
    class AddUserTextFiltersTests {
        @BeforeEach
        void setUp() {
            predicates = new ArrayList<>();
            lenient().doReturn(stringPath).when(userPath).get("email");
            lenient().doReturn(stringPath).when(userPath).get("firstName");
            lenient().doReturn(stringPath).when(userPath).get("lastName");
            lenient().when(cb.lower(stringPath)).thenReturn(lowerExpr);
            lenient().when(cb.like(eq(lowerExpr), anyString())).thenReturn(mockPredicate);
        }

        @Test
        @DisplayName("Should not add any predicates when all values are null/blank")
        void shouldNotAddPredicatesWhenAllBlank() {
            SpecificationUtils.addUserTextFilters(userPath, null, "", "  ", predicates, cb);
            assertTrue(predicates.isEmpty());
            verifyNoInteractions(cb);
        }

        @Test
        @DisplayName("Should add email predicate when email is provided")
        void shouldAddEmailPredicate() {
            String email = "test@example.com";
            SpecificationUtils.addUserTextFilters(userPath, email, null, null, predicates, cb);
            assertEquals(1, predicates.size());
            assertSame(mockPredicate, predicates.getFirst());
            verify(userPath).get("email");
            verify(cb).lower(stringPath);
            verify(cb).like(lowerExpr, "%" + email.toLowerCase() + "%");
        }

        @Test
        @DisplayName("Should add first name predicate when first name is provided")
        void shouldAddFirstNamePredicate() {
            String firstName = "John";
            SpecificationUtils.addUserTextFilters(userPath, " ", firstName, null, predicates, cb);
            assertEquals(1, predicates.size());
            assertSame(mockPredicate, predicates.getFirst());
            verify(userPath).get("firstName");
            verify(cb).lower(stringPath);
            verify(cb).like(lowerExpr, "%" + firstName.toLowerCase() + "%");
        }

        @Test
        @DisplayName("Should add last name predicate when last name is provided")
        void shouldAddLastNamePredicate() {
            String lastName = "Doe";
            SpecificationUtils.addUserTextFilters(userPath, null, null, lastName, predicates, cb);
            assertEquals(1, predicates.size());
            assertSame(mockPredicate, predicates.getFirst());
            verify(userPath).get("lastName");
            verify(cb).lower(stringPath);
            verify(cb).like(lowerExpr, "%" + lastName.toLowerCase() + "%");
        }

        @Test
        @DisplayName("Should add all three predicate when all are provided")
        void shouldAddAllPredicate() {
            String email = "test@example.com";
            String firstName = "John";
            String lastName = "Doe";
            SpecificationUtils.addUserTextFilters(userPath, email, firstName, lastName, predicates, cb);
            assertEquals(3, predicates.size());
            verify(userPath).get("email");
            verify(userPath).get("firstName");
            verify(userPath).get("lastName");
            verify(cb, times(3)).lower(stringPath);
            verify(cb, times(3)).like(eq(lowerExpr), anyString());
        }
    }

    @Nested
    @DisplayName("addStudentNameIdFilters method")
    class AddStudentNameIdFiltersTests {
        @BeforeEach
        void setUp() {
            predicates = new ArrayList<>();
            lenient().doReturn(studentUserJoin).when(studentJoin).join("user");
            lenient().doReturn(firstNamePath).when(studentUserJoin).get("firstName");
            lenient().doReturn(lastNamePath).when(studentUserJoin).get("lastName");
            lenient().when(cb.concat(firstNamePath, " ")).thenReturn(concatStep1);
            lenient().when(cb.concat(concatStep1, lastNamePath)).thenReturn(concatStep2);
            lenient().when(cb.lower(concatStep2)).thenReturn(lowerExpr);
            lenient().when(cb.like(eq(lowerExpr), anyString())).thenReturn(mockPredicate);
            lenient().doReturn(intPath).when(studentJoin).get("id");
            lenient().when(cb.equal(eq(intPath), anyLong())).thenReturn(mockPredicate);
        }

        @Test
        @DisplayName("Should not add any predicates when both values are null/blank")
        void shouldNotAddPredicatesWhenBothNull() {
            SpecificationUtils.addStudentNameIdFilters(studentJoin, null, null, predicates, cb);
            assertTrue(predicates.isEmpty());
            verifyNoInteractions(cb);
        }

        @Test
        @DisplayName("Should add student name predicate when studentName is provided")
        void shouldAddStudentNamePredicate() {
            String studentName = "John Doe";
            SpecificationUtils.addStudentNameIdFilters(studentJoin, studentName, null, predicates, cb);
            assertEquals(1, predicates.size());
            assertSame(mockPredicate, predicates.getFirst());
            verify(studentJoin).join("user");
            verify(studentUserJoin).get("firstName");
            verify(studentUserJoin).get("lastName");
            verify(cb).concat(firstNamePath, " ");
            verify(cb).concat(concatStep1, lastNamePath);
            verify(cb).lower(concatStep2);
            verify(cb).like(lowerExpr, "%" + studentName.toLowerCase() + "%");
        }

        @Test
        @DisplayName("Should add student ID predicate when studentId is provided")
        void shouldAddStudentIdPredicate() {
            Long studentId = 123L;
            SpecificationUtils.addStudentNameIdFilters(studentJoin, null, studentId, predicates, cb);
            assertEquals(1, predicates.size());
            assertSame(mockPredicate, predicates.getFirst());
            verify(studentJoin).get("id");
            verify(cb).equal(intPath, studentId);
        }

        @Test
        @DisplayName("Should add both predicates when both are provided")
        void shouldAddBothPredicates() {
            String studentName = "John Doe";
            Long studentId = 123L;
            SpecificationUtils.addStudentNameIdFilters(studentJoin, studentName, studentId, predicates, cb);
            assertEquals(2, predicates.size());
            verify(studentJoin).join("user");
            verify(studentJoin).get("id");
            verify(cb).like(lowerExpr, "%" + studentName.toLowerCase() + "%");
            verify(cb).equal(intPath, studentId);
        }
    }

    @Nested
    @DisplayName("addTeacherNameIdFilters method")
    class AddTeacherNameIdFiltersTests {
        @BeforeEach
        void setUp() {
            predicates = new ArrayList<>();
            lenient().doReturn(teacherUserJoin).when(teacherJoin).join("user");
            lenient().doReturn(firstNamePath).when(teacherUserJoin).get("firstName");
            lenient().doReturn(lastNamePath).when(teacherUserJoin).get("lastName");
            lenient().when(cb.concat(firstNamePath, " ")).thenReturn(concatStep1);
            lenient().when(cb.concat(concatStep1, lastNamePath)).thenReturn(concatStep2);
            lenient().when(cb.lower(concatStep2)).thenReturn(lowerExpr);
            lenient().when(cb.like(eq(lowerExpr), anyString())).thenReturn(mockPredicate);
            lenient().doReturn(intPath).when(teacherJoin).get("id");
            lenient().when(cb.equal(eq(intPath), anyLong())).thenReturn(mockPredicate);
        }

        @Test
        @DisplayName("Should not add any predicates when both values are null/blank")
        void shouldNotAddPredicatesWhenBothNull() {
            SpecificationUtils.addTeacherNameIdFilters(teacherJoin, null, null, predicates, cb);
            assertTrue(predicates.isEmpty());
            verifyNoInteractions(cb);
        }

        @Test
        @DisplayName("Should add teacher name predicate when teacherName is provided")
        void shouldAddTeacherNamePredicate() {
            String teacherName = "Jane Smith";
            SpecificationUtils.addTeacherNameIdFilters(teacherJoin, teacherName, null, predicates, cb);
            assertEquals(1, predicates.size());
            assertSame(mockPredicate, predicates.getFirst());
            verify(teacherJoin).join("user");
            verify(teacherUserJoin).get("firstName");
            verify(teacherUserJoin).get("lastName");
            verify(cb).concat(firstNamePath, " ");
            verify(cb).concat(concatStep1, lastNamePath);
            verify(cb).lower(concatStep2);
            verify(cb).like(lowerExpr, "%" + teacherName.toLowerCase() + "%");
        }

        @Test
        @DisplayName("Should add teacher ID predicate when teacherId is provided")
        void shouldAddTeacherIdPredicate() {
            Long teacherId = 456L;
            SpecificationUtils.addTeacherNameIdFilters(teacherJoin, null, teacherId, predicates, cb);
            assertEquals(1, predicates.size());
            assertSame(mockPredicate, predicates.getFirst());
            verify(teacherJoin).get("id");
            verify(cb).equal(intPath, teacherId);
        }

        @Test
        @DisplayName("Should add both predicates when both are provided")
        void shouldAddBothPredicates() {
            String teacherName = "Jane Smith";
            Long teacherId = 456L;
            SpecificationUtils.addTeacherNameIdFilters(teacherJoin, teacherName, teacherId, predicates, cb);
            assertEquals(2, predicates.size());
            verify(teacherJoin).join("user");
            verify(teacherJoin).get("id");
            verify(cb).like(lowerExpr, "%" + teacherName.toLowerCase() + "%");
            verify(cb).equal(intPath, teacherId);
        }
    }

    @Nested
    @DisplayName("addGradeFilter method")
    class AddGradeFilterTests {
        @BeforeEach
        void setUp() {
            predicates = new ArrayList<>();
        }

        @Test
        @DisplayName("Should not add any predicate when grade is null")
        void shouldNotAddPredicateWhenGradeNull() {
            SpecificationUtils.addGradeFilter(gradePath, null, predicates, cb);
            assertTrue(predicates.isEmpty());
            verifyNoInteractions(cb);
        }

        @Test
        @DisplayName("Should add grade equality predicate when grade is provided")
        void shouldAddGradePredicate() {
            GradeLevel grade = GradeLevel.FIRST;
            when(cb.equal(gradePath, grade)).thenReturn(mockPredicate);
            SpecificationUtils.addGradeFilter(gradePath, grade, predicates, cb);
            assertEquals(1, predicates.size());
            assertSame(mockPredicate, predicates.getFirst());
            verify(cb).equal(gradePath, grade);
        }
    }
}
