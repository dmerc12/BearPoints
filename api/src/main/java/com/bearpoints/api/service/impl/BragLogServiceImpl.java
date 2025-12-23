package com.bearpoints.api.service.impl;

import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.criteria.BragLogSearchCriteria;
import com.bearpoints.api.dao.BehaviorTypeDAO;
import com.bearpoints.api.dao.BragLogDAO;
import com.bearpoints.api.dao.StudentDAO;
import com.bearpoints.api.dao.TeacherDAO;
import com.bearpoints.api.dto.BragLogDTO;
import com.bearpoints.api.dto.BragLogRequest;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.entity.*;
import com.bearpoints.api.service.BragLogService;
import com.bearpoints.api.specification.BragLogSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents service responsible for public brag log submissions.
 * <p>Implements with {@link BragLogService}
 *
 * @see BragLogRequest
 * @see Student
 * @see StudentDAO
 * @see Teacher
 * @see TeacherDAO
 * @see BehaviorType
 * @see BehaviorTypeDAO
 * @see BragLog
 * @see BragLogDAO
 *
 * @version 2.0
 * @author Dylan Mercer
 */
@Slf4j
@Service
public class BragLogServiceImpl implements BragLogService {
    private final BragLogDAO bragLogDAO;
    private final StudentDAO studentDAO;
    private final TeacherDAO teacherDAO;
    private final BehaviorTypeDAO behaviorTypeDAO;

    public BragLogServiceImpl(BragLogDAO bragLogDAO, StudentDAO studentDAO, TeacherDAO teacherDAO, BehaviorTypeDAO behaviorTypeDAO) {
        this.bragLogDAO = bragLogDAO;
        this.studentDAO = studentDAO;
        this.teacherDAO = teacherDAO;
        this.behaviorTypeDAO = behaviorTypeDAO;
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
        bragLog.setDefaultsBeforePersist();
        BragLog savedBragLog = bragLogDAO.save(bragLog);
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
        Long studentId = bragLogDTO.getStudentId();
        Set<Long> behaviorIds = bragLogDTO.getBehaviorIds();
        boolean studentChanged = !existingBragLog.getStudent().getId().equals(studentId);
        boolean behaviorsChanged = !existingBragLog.getBehaviors().stream()
                .mapToLong(BehaviorType::getId).allMatch(behaviorIds::contains);
        if (studentChanged || behaviorsChanged) {
            if (studentChanged) {
                Student newStudent = studentDAO.findById(studentId)
                        .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
                existingBragLog.setStudent(newStudent);
            }
            if (behaviorsChanged) {
                Set<BehaviorType> behaviors = new HashSet<>(Set.of());
                for (Long behaviorTypeId : bragLogDTO.getBehaviorIds()) {
                    BehaviorType behaviorType = behaviorTypeDAO.findById(behaviorTypeId)
                            .orElseThrow(() -> new ResourceNotFoundException("Behavior type not found with ID: " + behaviorTypeId));
                    behaviors.add(behaviorType);
                }
                existingBragLog.setBehaviors(behaviors);
            }
            existingBragLog.setDefaultsBeforePersist();
        }
        if (!existingBragLog.getNotes().equals(bragLogDTO.getNotes())) {
            existingBragLog.setNotes(bragLogDTO.getNotes());
        }
        BragLog updatedBragLog = bragLogDAO.save(existingBragLog);
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
        bragLogDAO.delete(bragLog);
        log.info("Successfully deleted brag log with ID: {}", id);
    }

    /**
     * DEPRECATED
     * Service to assist in submitting brag logs
     */
    @Override
    @Transactional
    public BragLog submitBragLog(BragLogRequest request) {
        log.info("Submitting brag log for student {}", request.getStudentId());
        // Validate behaviors are not empty
        if (request.getBehaviorIds().isEmpty()) {
            throw new IllegalArgumentException("At least one behavior must be selected");
        }
        // Validate student exists
        Student student = studentDAO.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid student ID"));
        // Validate teacher exists
        Teacher teacher = teacherDAO.findById(request.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid teacher ID"));
        // Validate student is in teachers class
        if (!student.getTeacher().getId().equals(teacher.getId())) {
            throw new IllegalArgumentException("Teacher does not teach this student");
        }
        // Get behaviors
        Set<BehaviorType> behaviors = request.getBehaviorIds().stream()
                .map(id -> behaviorTypeDAO.findById(id)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid behavior ID: " + id)))
                        .collect(Collectors.toSet());
        // Create brag log
        BragLog bragLog = new BragLog();
        bragLog.setStudent(student);
        bragLog.setTeacher(teacher);
        bragLog.setBehaviors(behaviors);
        bragLog.setPointsGenerated(
                behaviors.stream().mapToInt(BehaviorType::getPointValue).sum()
        );
        bragLog.setNotes(request.getNotes());
        return bragLogDAO.save(bragLog);
    }
}
