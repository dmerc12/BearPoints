package com.bearpoints.api.service.impl;

import com.bearpoints.api.dao.StudentDAO;
import com.bearpoints.api.entity.Student;
import com.bearpoints.api.exception.InsufficientResourcesException;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.service.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link PointService} using {@link StudentDAO}.
 * <p>Performs point adjustments within a transaction, ensuring data consistency.
 * All operations validate input and throw appropriate exceptions.
 *
 * @see PointService
 * @version 1.0
 * @author Dylan Mercer
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {
    private final StudentDAO studentDAO;

    /**
     * {@inheritDoc}
     */
    @Override
    public void addPoints(Long studentId, int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Points to add must be non-negative");
        }
        Student student = studentDAO.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
        int newPoints = student.getPoints() + points;
        student.setPoints(newPoints);
        studentDAO.save(student);
        log.debug("Added {} points to student ID: {}, new total: {}", points, studentId, newPoints);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void subtractPoints(Long studentId, int points) {
        if (points < 0) {
            throw new IllegalArgumentException("Points to subtract must be non-negative");
        }
        Student student = studentDAO.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
        int newPoints = student.getPoints() - points;
        if (newPoints < 0) {
            throw new InsufficientResourcesException("Insufficient points to subtract " + points + " from student " + studentId);
        }
        student.setPoints(newPoints);
        studentDAO.save(student);
        log.debug("Subtracted {} points to student ID: {}, new total: {}", points, studentId, newPoints);
    }
}
