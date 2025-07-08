package com.bearpoints.api.service.impl;

import com.bearpoints.api.dao.BehaviorTypeDAO;
import com.bearpoints.api.dao.BragLogRepository;
import com.bearpoints.api.dao.StudentRepository;
import com.bearpoints.api.dao.TeacherDAO;
import com.bearpoints.api.dto.BragLogRequest;
import com.bearpoints.api.entity.*;
import com.bearpoints.api.service.BragLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents service responsible for public brag log submissions.
 * <p>Implements with {@link BragLogService}
 *
 * @see BragLogRequest
 * @see Student
 * @see StudentRepository
 * @see Teacher
 * @see TeacherDAO
 * @see BehaviorType
 * @see BehaviorTypeDAO
 * @see BragLog
 * @see BragLogRepository
 *
 * @version 1.0
 * @author Dylan Mercer
 */
@Slf4j
@Service
public class BragLogServiceImpl implements BragLogService {
    private final BragLogRepository bragLogRepository;
    private final StudentRepository studentRepository;
    private final TeacherDAO teacherRepository;
    private final BehaviorTypeDAO behaviorTypeRepository;

    public BragLogServiceImpl(BragLogRepository bragLogRepository, StudentRepository studentRepository, TeacherDAO teacherRepository, BehaviorTypeDAO behaviorTypeRepository) {
        this.bragLogRepository = bragLogRepository;
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.behaviorTypeRepository = behaviorTypeRepository;
    }

    /** Service to assist in submitting brag logs */
    @Override
    @Transactional
    public BragLog submitBragLog(BragLogRequest request) {
        log.info("Submitting brag log for student {}", request.getStudentId());
        // Validate behaviors are not empty
        if (request.getBehaviorIds().isEmpty()) {
            throw new IllegalArgumentException("At least one behavior must be selected");
        }
        // Validate student exists
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid student ID"));
        // Validate teacher exists
        Teacher teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid teacher ID"));
        // Validate student is in teachers class
        if (!student.getTeacher().getId().equals(teacher.getId())) {
            throw new IllegalArgumentException("Teacher does not teach this student");
        }
        // Get behaviors
        Set<BehaviorType> behaviors = request.getBehaviorIds().stream()
                .map(id -> behaviorTypeRepository.findById(id)
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
        return bragLogRepository.save(bragLog);
    }
}
