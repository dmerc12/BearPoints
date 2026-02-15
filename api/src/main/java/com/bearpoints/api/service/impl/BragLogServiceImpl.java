package com.bearpoints.api.service.impl;

import com.bearpoints.api.dao.UserDAO;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.criteria.BragLogSearchCriteria;
import com.bearpoints.api.dao.BehaviorTypeDAO;
import com.bearpoints.api.dao.BragLogDAO;
import com.bearpoints.api.dao.StudentDAO;
import com.bearpoints.api.dto.BragLogDTO;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.entity.*;
import com.bearpoints.api.service.BragLogService;
import com.bearpoints.api.specification.BragLogSpecification;
import com.bearpoints.api.specification.UserSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of {@link BragLogService} for brag log management.
 *
 * @see BragLogService
 * @version 3.1
 * @author Dylan Mercer
 */
@Slf4j
@Service
public class BragLogServiceImpl implements BragLogService {
    private final BragLogDAO bragLogDAO;
    private final StudentDAO studentDAO;
    private final BehaviorTypeDAO behaviorTypeDAO;
    private final UserDAO userDAO;

    public BragLogServiceImpl(BragLogDAO bragLogDAO, StudentDAO studentDAO, BehaviorTypeDAO behaviorTypeDAO, UserDAO userDAO) {
        this.bragLogDAO = bragLogDAO;
        this.studentDAO = studentDAO;
        this.behaviorTypeDAO = behaviorTypeDAO;
        this.userDAO = userDAO;
    }

    /**
     *{@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<BragLogDTO> getAllBragLogs(Pageable pageable) {
        log.debug("Retrieving all brag logs with pagination {}", pageable);
        Page<BragLogDTO> bragLogPage = bragLogDAO.findAll(pageable).map(BragLogDTO::new);
        log.info("Retrieved {} brag logs", bragLogPage.getNumberOfElements());
        return PagedResponseDTO.of(bragLogPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<BragLogDTO> searchBragLogs(BragLogSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching brag logs with criteria: {} and pagination: {}", criteria, pageable);
        if (!criteria.hasFilters()) {
            // If no filters provided return all brag logs
            return getAllBragLogs(pageable);
        }
        Specification<BragLog> spec = BragLogSpecification.withCriteria(criteria);
        Page<BragLogDTO> bragLogPage = bragLogDAO.findAll(spec, pageable).map(BragLogDTO::new);
        log.info("Found {} brag logs matching search criteria", bragLogPage.getNumberOfElements());
        return PagedResponseDTO.of(bragLogPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public BragLogDTO getBragLogById(Long id) {
        log.debug("Retrieving brag log by ID: {}", id);
        BragLog bragLog = bragLogDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brag log not found with ID: " + id));
        return new BragLogDTO(bragLog);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public BragLogDTO createBragLog(BragLogDTO bragLogDTO) {
        log.debug("Creating brag log with student ID: {}", bragLogDTO.getStudentId());
        Student student = studentDAO.findById(bragLogDTO.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + bragLogDTO.getStudentId()));
        Set<BehaviorType> behaviors = new HashSet<>(Set.of());
        for (Long behaviorTypeId : bragLogDTO.getBehaviorIds()) {
            BehaviorType behaviorType = behaviorTypeDAO.findById(behaviorTypeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Behavior type not found with ID: " + behaviorTypeId));
            behaviors.add(behaviorType);
        }
        BragLog bragLog = new BragLog();
        bragLog.setStudent(student);
        bragLog.setBehaviors(behaviors);
        bragLog.setNotes(bragLogDTO.getNotes());
        String submitterName = bragLogDTO.getSubmitterName();
        bragLog.setSubmitterName(submitterName);
        resolveAndSetSubmitterUser(bragLog, submitterName);
        bragLog.setDefaultsBeforePersist();
        BragLog savedBragLog = bragLogDAO.save(bragLog);
        student.setPoints(student.getPoints() + savedBragLog.getPointsGenerated());
        studentDAO.save(student);
        log.debug("Added {} points to student ID: {}", savedBragLog.getPointsGenerated(), student.getId());
        log.info("Successfully created a brag log with ID: {}", savedBragLog.getId());
        return new BragLogDTO(savedBragLog);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public BragLogDTO updateBragLog(Long id, BragLogDTO bragLogDTO) {
        log.debug("Updating brag log with ID: {}", id);
        BragLog existingBragLog = bragLogDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brag log not found with ID: " + id));
        Student oldStudent = existingBragLog.getStudent();
        int oldPoints = existingBragLog.getPointsGenerated();
        Long newStudentId = bragLogDTO.getStudentId();
        Set<Long> newBehaviorIds = bragLogDTO.getBehaviorIds();
        boolean studentChanged = !existingBragLog.getStudent().getId().equals(newStudentId);
        boolean behaviorsChanged = !existingBragLog.getBehaviors().stream()
                .mapToLong(BehaviorType::getId).allMatch(newBehaviorIds::contains);
        Student newStudent = null;
        if (studentChanged) {
            newStudent = studentDAO.findById(newStudentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + newStudentId));
            existingBragLog.setStudent(newStudent);
        }
        if (behaviorsChanged) {
            Set<BehaviorType> behaviors = new HashSet<>();
            for (Long behaviorTypeId : bragLogDTO.getBehaviorIds()) {
                BehaviorType behaviorType = behaviorTypeDAO.findById(behaviorTypeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Behavior type not found with ID: " + behaviorTypeId));
                behaviors.add(behaviorType);
            }
            existingBragLog.setBehaviors(behaviors);
        }
        // Recalculate points if student or behaviors changed
        if (studentChanged || behaviorsChanged) {
            existingBragLog.setDefaultsBeforePersist();
        }
        String newSubmitterName = bragLogDTO.getSubmitterName();
        if (!existingBragLog.getSubmitterName().equals(newSubmitterName)) {
            existingBragLog.setSubmitterName(newSubmitterName);
            resolveAndSetSubmitterUser(existingBragLog, newSubmitterName);
        }
        if (!existingBragLog.getNotes().equals(bragLogDTO.getNotes())) {
            existingBragLog.setNotes(bragLogDTO.getNotes());
        }
        BragLog updatedBragLog = bragLogDAO.save(existingBragLog);
        int newPoints = updatedBragLog.getPointsGenerated();
        int pointDifference = newPoints - oldPoints;
        if (studentChanged) {
            // Remove points from old student
            oldStudent.setPoints(oldStudent.getPoints() - oldPoints);
            studentDAO.save(oldStudent);
            log.debug("Removed {} points from old student ID: {}", oldPoints, oldStudent.getId());
            // Add points to new student
            newStudent.setPoints(newStudent.getPoints() + newPoints);
            studentDAO.save(newStudent);
            log.debug("Added {} points to new student ID: {}", newPoints, newStudent.getId());
        } else if (pointDifference != 0) {
            oldStudent.setPoints(oldStudent.getPoints() + pointDifference);
            studentDAO.save(oldStudent);
            log.debug("Adjusted student ID {} points by {} (new total: {})",
                    oldStudent.getId(), pointDifference, oldStudent.getPoints());
        }
        log.info("Successfully updated brag log with ID: {}", updatedBragLog.getId());
        return new BragLogDTO(updatedBragLog);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteBragLog(Long id) {
        log.debug("Deleting brag log with ID: {}", id);
        BragLog bragLog = bragLogDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brag log not found with ID: " + id));
        Student student = bragLog.getStudent();
        student.setPoints(student.getPoints() - bragLog.getPointsGenerated());
        studentDAO.save(student);
        log.debug("Removed {} points from student ID: {}", bragLog.getPointsGenerated(), student.getId());
        bragLogDAO.delete(bragLog);
        log.info("Successfully deleted brag log with ID: {}", id);
    }

    private void resolveAndSetSubmitterUser(BragLog bragLog, String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Submitter name must not be blank");
        }
        String[] nameParts = fullName.trim().split("\\s+");
        if (nameParts.length < 2) {
            throw new IllegalArgumentException("Submitter name must contain both first and last name");
        }
        Specification<User> spec = createExactNameSpec(fullName);
        Optional<User> userOptional = userDAO.findOne(spec);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getRole() == Role.STUDENT) {
                throw new IllegalArgumentException("Students cannot submit brag logs");
            }
            bragLog.setSubmitterUser(user);
        } else {
            bragLog.setSubmitterUser(null);
        }
    }

    private Specification<User> createExactNameSpec(String fullName) {
        String[] nameParts = fullName.trim().split("\\s+");
        String firstName = nameParts[0];
        String lastName = fullName.trim().substring(firstName.length()).trim();
        return UserSpecification.byExactName(firstName, lastName);
    }
}
