package com.bearpoints.api.unit.service;

import com.bearpoints.api.dao.StudentDAO;
import com.bearpoints.api.entity.Student;
import com.bearpoints.api.exception.InsufficientResourcesException;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.service.impl.PointServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link PointServiceImpl}.
 * <p>Verifies point management functionality including addition, subtraction,
 * and point checks with proper validation and exception handling.
 *
 * @see PointServiceImpl
 * @version 1.1
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PointService Unit Tests")
public class PointServiceTests {
    String INSUFFICIENT_POINTS_EXCEPTION_MESSAGE = "Insufficient points to redeem this reward";
    String STUDENT_NOT_FOUND_EXCEPTION_MESSAGE = "Student not found with ID: ";
    String POINTS_NEGATIVE_EXCEPTION_MESSAGE = "Points must be non-negative";

    @Mock
    private StudentDAO studentDAO;

    @InjectMocks
    private PointServiceImpl pointService;

    private Student student;
    private final Long studentId = 1L;

    @BeforeEach
    void setUp() {
        student = new Student();
        student.setId(studentId);
        student.setPoints(100);
    }

    @Nested
    @DisplayName("When adding points")
    class AddPoints {
        @Test
        @DisplayName("should add points successfully")
        void addPointsSuccessfully() {
            when(studentDAO.findById(studentId)).thenReturn(Optional.of(student));
            pointService.addPoints(studentId, 50);
            assertEquals(150, student.getPoints());
            verify(studentDAO).save(student);
        }

        @Test
        @DisplayName("should throw exception when points negative")
        void addPointsNegative() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> pointService.addPoints(studentId, -5)
            );
            assertEquals(ex.getMessage(), POINTS_NEGATIVE_EXCEPTION_MESSAGE);
            verify(studentDAO, never()).findById(any());
        }

        @Test
        @DisplayName("should throw exception when student not found")
        void addPointsStudentNotFound() {
            when(studentDAO.findById(studentId)).thenReturn(Optional.empty());
            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> pointService.addPoints(studentId, 10)
            );
            assertTrue(ex.getMessage().contains(STUDENT_NOT_FOUND_EXCEPTION_MESSAGE));
            verify(studentDAO, never()).save(any());
        }
    }

    @Nested
    @DisplayName("When subtracting points")
    class SubtractPoints {
        @Test
        @DisplayName("should subtract points successfully")
        void subtractPointsSuccessfully() {
            when(studentDAO.findById(studentId)).thenReturn(Optional.of(student));
            pointService.subtractPoints(studentId, 30);
            assertEquals(70, student.getPoints());
            verify(studentDAO).save(student);
        }

        @Test
        @DisplayName("should throw exception when points negative")
        void subtractPointsNegative() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> pointService.subtractPoints(studentId, -5)
            );
            assertEquals(ex.getMessage(), POINTS_NEGATIVE_EXCEPTION_MESSAGE);
            verify(studentDAO, never()).findById(any());
        }

        @Test
        @DisplayName("should throw exception when student not found")
        void subtractPointsStudentNotFound() {
            when(studentDAO.findById(studentId)).thenReturn(Optional.empty());
            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> pointService.subtractPoints(studentId, 10)
            );
            assertTrue(ex.getMessage().contains(STUDENT_NOT_FOUND_EXCEPTION_MESSAGE));
            verify(studentDAO, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception when insufficient points")
        void subtractPointsInsufficient() {
            when(studentDAO.findById(studentId)).thenReturn(Optional.of(student));
            InsufficientResourcesException ex = assertThrows(
                    InsufficientResourcesException.class,
                    () -> pointService.subtractPoints(studentId, 150)
            );
            assertEquals(ex.getMessage(), INSUFFICIENT_POINTS_EXCEPTION_MESSAGE);
            assertEquals(100, student.getPoints());
            verify(studentDAO, never()).save(any());
        }
    }

    @Nested
    @DisplayName("When checking sufficient points")
    class HasSufficientPoints {
        @Test
        @DisplayName("should not throw when student has enough points")
        void hasSufficientPoints() {
            when(studentDAO.findById(studentId)).thenReturn(Optional.of(student));
            assertDoesNotThrow(() -> pointService.hasSufficientPoints(studentId, 50));
            assertDoesNotThrow(() -> pointService.hasSufficientPoints(studentId, 100));
            verify(studentDAO, times(2)).findById(studentId);
            verify(studentDAO, never()).save(any(Student.class));
        }

        @Test
        @DisplayName("should throw when student does not have enough points")
        void hasSufficientPointsInsufficient() {
            when(studentDAO.findById(studentId)).thenReturn(Optional.of(student));
            InsufficientResourcesException ex = assertThrows(
                    InsufficientResourcesException.class,
                    () -> pointService.hasSufficientPoints(studentId, 150)
            );
            assertEquals(ex.getMessage(), INSUFFICIENT_POINTS_EXCEPTION_MESSAGE);
            verify(studentDAO).findById(studentId);
        }

        @Test
        @DisplayName("should throw when required points is negative")
        void hasSufficientPointsNegativeRequiredPoints() {
            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> pointService.hasSufficientPoints(studentId, -1)
            );
            assertEquals(ex.getMessage(), POINTS_NEGATIVE_EXCEPTION_MESSAGE);
            verify(studentDAO, never()).findById(anyLong());
        }

        @Test
        @DisplayName("should throw when student not found")
        void hasSufficientPointsStudentNotFound() {
            when(studentDAO.findById(studentId)).thenReturn(Optional.empty());
            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> pointService.hasSufficientPoints(studentId, 150)
            );
            assertTrue(ex.getMessage().contains(STUDENT_NOT_FOUND_EXCEPTION_MESSAGE));
            verify(studentDAO).findById(studentId);
        }
    }
}
