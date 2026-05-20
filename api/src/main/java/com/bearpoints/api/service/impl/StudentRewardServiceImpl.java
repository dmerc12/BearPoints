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
import com.bearpoints.api.service.PointService;
import com.bearpoints.api.service.StockService;
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
 * @version 1.3
 * @author Dylan Mercer
 */
@Slf4j
@Service
public class StudentRewardServiceImpl implements StudentRewardService {
    private final StudentRewardDAO studentRewardDAO;
    private final StudentDAO studentDAO;
    private final RewardItemDAO rewardItemDAO;
    private final PointService pointService;
    private final StockService stockService;

    public StudentRewardServiceImpl(StudentRewardDAO studentRewardDAO, StudentDAO studentDAO, RewardItemDAO rewardItemDAO,
                                    PointService pointService, StockService stockService) {
        this.studentRewardDAO = studentRewardDAO;
        this.studentDAO = studentDAO;
        this.rewardItemDAO = rewardItemDAO;
        this.pointService = pointService;
        this.stockService = stockService;
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
        pointService.hasSufficientPoints(student.getId(), rewardItem.getPointCost());
        stockService.hasSufficientStock(rewardItem.getId());
        pointService.subtractPoints(student.getId(), rewardItem.getPointCost());
        stockService.decrementStock(rewardItem.getId());
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
        Student originalStudent = existingStudentReward.getStudent();
        RewardItem originalItem = existingStudentReward.getRewardItem();
        boolean studentChanged = !originalStudent.getId().equals(studentId);
        boolean itemChanged = !originalItem.getId().equals(itemId);
        if (!studentChanged && !itemChanged) {
            log.info("No changes detected for student reward with ID: {}", id);
            return new StudentRewardDTO(existingStudentReward);
        }
        Student newStudent = studentChanged ? studentDAO.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with ID: " + studentId))
                : originalStudent;
        RewardItem newItem = itemChanged ? rewardItemDAO.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Reward item not found with ID: " + itemId))
                : originalItem;
        // Reverse original transaction
        pointService.addPoints(originalStudent.getId(), originalItem.getPointCost());
        stockService.incrementStock(originalItem.getId());
        // Apply new transaction
        pointService.hasSufficientPoints(newStudent.getId(), newItem.getPointCost());
        stockService.hasSufficientStock(newItem.getId());
        pointService.subtractPoints(newStudent.getId(), newItem.getPointCost());
        stockService.decrementStock(newItem.getId());
        existingStudentReward.setStudent(newStudent);
        existingStudentReward.setRewardItem(newItem);
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
        // Reverse transaction
        pointService.addPoints(studentReward.getStudent().getId(), studentReward.getRewardItem().getPointCost());
        stockService.incrementStock(studentReward.getRewardItem().getId());
        // Perform delete
        studentRewardDAO.delete(studentReward);
        log.info("Successfully deleted student reward with ID: {}", id);
    }
}
