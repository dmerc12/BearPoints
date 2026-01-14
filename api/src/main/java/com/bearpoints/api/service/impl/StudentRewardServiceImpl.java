package com.bearpoints.api.service.impl;

import com.bearpoints.api.criteria.StudentRewardSearchCriteria;
import com.bearpoints.api.dao.RewardItemDAO;
import com.bearpoints.api.dao.StudentDAO;
import com.bearpoints.api.dao.StudentRewardDAO;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.StudentRewardDTO;
import com.bearpoints.api.entity.RewardItem;
import com.bearpoints.api.entity.Student;
import com.bearpoints.api.entity.StudentReward;
import com.bearpoints.api.exception.ResourceNotFoundException;
import com.bearpoints.api.service.StudentRewardService;
import com.bearpoints.api.specification.StudentRewardSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of {@link StudentRewardService} for student reward management.
 *
 * @see StudentRewardService
 * @version 1.0
 * @author Dylan Mercer
 */
@Slf4j
@Service
public class StudentRewardServiceImpl implements StudentRewardService {
    private final StudentRewardDAO studentRewardDAO;
    private final StudentDAO studentDAO;
    private final RewardItemDAO rewardItemDAO;

    public StudentRewardServiceImpl(StudentRewardDAO studentRewardDAO, StudentDAO studentDAO, RewardItemDAO rewardItemDAO) {
        this.studentRewardDAO = studentRewardDAO;
        this.studentDAO = studentDAO;
        this.rewardItemDAO = rewardItemDAO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<StudentRewardDTO> getAllStudentRewards(Pageable pageable) {
        log.debug("Retrieving all student rewards with pagination: {}", pageable);
        Page<StudentRewardDTO> studentRewardPage = studentRewardDAO.findAll(pageable).map(StudentRewardDTO::new);
        log.info("Retrieved {} student rewards", studentRewardPage.getNumberOfElements());
        return PagedResponseDTO.of(studentRewardPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public PagedResponseDTO<StudentRewardDTO> searchStudentRewards(StudentRewardSearchCriteria criteria, Pageable pageable) {
        log.debug("Searching student rewards with criteria: {} and pagination: {}", criteria, pageable);
        if (!criteria.hasFilters()) {
            // If no filters provided return all student rewards
            return getAllStudentRewards(pageable);
        }
        Specification<StudentReward> spec = StudentRewardSpecification.withCriteria(criteria);
        Page<StudentRewardDTO> studentRewardPage = studentRewardDAO.findAll(spec, pageable).map(StudentRewardDTO::new);
        log.info("Found {} student rewards matching search criteria", studentRewardPage.getNumberOfElements());
        return PagedResponseDTO.of(studentRewardPage);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional(readOnly = true)
    public StudentRewardDTO getStudentRewardById(Long id) {
        log.debug("Retrieving student reward by ID: {}", id);
        StudentReward studentReward = studentRewardDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student reward not found with ID: " + id));
        return new StudentRewardDTO(studentReward);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public StudentRewardDTO createStudentReward(StudentRewardDTO studentRewardDTO) {
        log.debug("Creating student reward with student ID: {}", studentRewardDTO.getStudentId());
        Student student = studentDAO.findById(studentRewardDTO.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentRewardDTO.getStudentId()));
        RewardItem rewardItem = rewardItemDAO.findById(studentRewardDTO.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Reward item not found with ID: " + studentRewardDTO.getItemId()));
        StudentReward studentReward = new StudentReward();
        studentReward.setStudent(student);
        studentReward.setRewardItem(rewardItem);
        StudentReward savedStudentReward = studentRewardDAO.save(studentReward);
        log.info("Successfully created a student reward with ID: {}", savedStudentReward.getId());
        return new StudentRewardDTO(savedStudentReward);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public StudentRewardDTO updateStudentReward(Long id, StudentRewardDTO studentRewardDTO) {
        log.debug("Updating student reward with ID: {}", id);
        StudentReward existingStudentReward = studentRewardDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student reward not found with ID: " + id));
        Long studentId = studentRewardDTO.getStudentId();
        Long itemId = studentRewardDTO.getItemId();
        boolean studentChanged = !existingStudentReward.getStudent().getId().equals(studentId);
        boolean itemChanged = !existingStudentReward.getRewardItem().getId().equals(itemId);
        if (studentChanged) {
            Student newStudent = studentDAO.findById(studentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId));
            existingStudentReward.setStudent(newStudent);
        }
        if (itemChanged) {
            RewardItem newRewardItem = rewardItemDAO.findById(itemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Reward item not found with ID: " + itemId));
            existingStudentReward.setRewardItem(newRewardItem);
        }
        StudentReward updatedStudentReward = studentRewardDAO.save(existingStudentReward);
        log.info("Successfully updated student reward with ID: {}", updatedStudentReward.getId());
        return new StudentRewardDTO(updatedStudentReward);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteStudentReward(Long id) {
        log.debug("Deleting student reward with ID: {}", id);
        StudentReward studentReward = studentRewardDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student reward not found with ID: " + id));
        studentRewardDAO.delete(studentReward);
        log.info("Successfully deleted student reward with ID: {}", id);
    }
}
