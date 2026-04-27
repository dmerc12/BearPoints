package com.bearpoints.api.controller;

import com.bearpoints.api.annotation.PaginationAndSorting;
import com.bearpoints.api.criteria.StudentRewardSearchCriteria;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.StudentRewardDTO;
import com.bearpoints.api.service.StudentRewardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * REST controller for student reward management operations.
 * <p>Provides endpoints for managing student rewards with pagination, sorting, and filtering.
 *
 * <p>Endpoints:
 * <ul>
 *     <li>GET /api/rewards - Retrieve all student rewards (any authenticated user)</li>
 *     <li>GET /api/rewards/search - Search student rewards (any authenticated user)</li>
 *     <li>GET /api/rewards/{id} - Retrieve student reward by ID (any authenticated user)</li>
 *     <li>POST /api/rewards - Create a new student reward (any authenticated user)</li>
 *     <li>PUT /api/rewards/{id} - Update existing student rewards (any authenticated user)</li>
 *     <li>DELETE /api/rewards/{id} - Delete a student reward (any authenticated user)</li>
 * </ul>
 *
 * <p>Security:
 * <ul>
 *     <li>GET, POST, PUT, and Delete endpoints - Any authenticated user</li>
 * </ul>
 *
 * @version 1.1
 * @author Dylan Mercer
 */
@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rewards")
@PreAuthorize("isAuthenticated()")
public class StudentRewardController {
    private final StudentRewardService studentRewardService;

    /**
     * Retrieves all student rewards with pagination and sorting.
     * <p>Accessible to any authenticated user.
     *
     * @param pageable Automatically resolved pagination and sorting parameters
     * @return Paginated response of student rewards
     */
    @GetMapping
    public ResponseEntity<PagedResponseDTO<StudentRewardDTO>> getAllStudentRewards(
            @PaginationAndSorting(
                    defaultSort = "redeemedAt,desc",
                    allowedSortProperties = {"id", "student.id", "student.firstName", "student.lastName",
                            "student.teacher.id", "student.teacher.firstName", "student.teacher.lastName",
                            "student.teacher.grade", "rewardItem.id", "rewardItem.name", "rewardItem.pointCost",
                            "redeemedAt"}
            ) Pageable pageable) {
        log.debug("Retrieving all student rewards - page: {}, size: {}, sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        PagedResponseDTO<StudentRewardDTO> response = studentRewardService.getAllStudentRewards(pageable);
        log.info("Retrieved {} student rewards", response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    /**
     * Searches student rewards with flexible criteria including student name, item name, points used range,
     * redeemed date range, student ID, and reward item ID.
     * <p>Accessible to any authenticated user.
     *
     * @param studentName Student name search term (optional)
     * @param studentId Student ID filter (optional)
     * @param itemName Reward item name search term (optional)
     * @param itemId Reward item ID filter (optional)
     * @param minPointsUsed Minimum points used threshold (optional)
     * @param maxPointsUsed Maximum points used threshold (optional)
     * @param startDate RedeemedAt start date (optional)
     * @param endDate RedeemedAt end date (optional)
     * @param pageable Automatically resolved pagination and sorting parameters
     * @return Paginated response of matching student rewards
     */
    @GetMapping("/search")
    public ResponseEntity<PagedResponseDTO<StudentRewardDTO>> searchStudentRewards(
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String itemName,
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) Integer minPointsUsed,
            @RequestParam(required = false) Integer maxPointsUsed,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @PaginationAndSorting(
                    defaultSort = "redeemedAt,desc",
                    allowedSortProperties = {"id", "student.id", "student.firstName", "student.lastName",
                            "student.teacher.id", "student.teacher.firstName", "student.teacher.lastName",
                            "student.teacher.grade", "rewardItem.id", "rewardItem.name", "rewardItem.pointCost",
                            "redeemedAt"}
            ) Pageable pageable) {
        log.debug("Searching student rewards - page: {}, size: {}, sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        StudentRewardSearchCriteria criteria = new StudentRewardSearchCriteria();
        criteria.setStudentName(studentName);
        criteria.setStudentId(studentId);
        criteria.setItemName(itemName);
        criteria.setItemId(itemId);
        criteria.setMinPointsUsed(minPointsUsed);
        criteria.setMaxPointsUsed(maxPointsUsed);
        criteria.setStartDate(startDate);
        criteria.setEndDate(endDate);
        PagedResponseDTO<StudentRewardDTO> response = studentRewardService.searchStudentRewards(criteria, pageable);
        log.info("Found {} student rewards matching search criteria", response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a student reward by ID.
     * <p>Accessible to any authenticated user.
     *
     * @param id Student reward ID
     * @return Student reward details
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudentRewardDTO> getStudentRewardById(@PathVariable Long id) {
        log.debug("Retrieving student reward with ID: {}", id);
        StudentRewardDTO studentReward = studentRewardService.getStudentRewardById(id);
        log.info("Retrieved student reward with ID: {}", id);
        return ResponseEntity.ok(studentReward);
    }

    /**
     * Creates a new student reward.
     * <p>Accessible only to any authenticated user
     *
     * @param studentRewardDTO Student reward data
     * @return Created student reward details
     */
    @PostMapping
    public ResponseEntity<StudentRewardDTO> createStudentReward(@Valid @RequestBody StudentRewardDTO studentRewardDTO) {
        log.debug("Creating new student reward with student ID: {} and reward item name: {}",
                studentRewardDTO.getStudentId(), studentRewardDTO.getItemName());
        StudentRewardDTO createdStudentReward = studentRewardService.createStudentReward(studentRewardDTO);
        log.info("Created student reward with ID: {}", createdStudentReward.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createdStudentReward);
    }

    /**
     * Updates an existing student reward.
     * <p>Accessible only to any authenticated user
     *
     * @param id Student reward ID
     * @param studentRewardDTO Updated student reward data
     * @return Updated student reward details
     */
    @PutMapping("/{id}")
    public ResponseEntity<StudentRewardDTO> updateStudentReward(
            @PathVariable Long id,
            @Valid @RequestBody StudentRewardDTO studentRewardDTO
    ) {
        log.debug("Updating student reward with ID: {}", id);
        StudentRewardDTO updatedStudentReward = studentRewardService.updateStudentReward(id, studentRewardDTO);
        log.info("Updated student reward with ID: {}", id);
        return ResponseEntity.ok(updatedStudentReward);
    }

    /**
     * Deletes a student reward by ID.
     * <p>Accessible only to any authenticated user
     *
     * @param id StudentReward ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudentReward(@PathVariable Long id) {
        log.debug("Deleting student reward with ID: {}", id);
        studentRewardService.deleteStudentReward(id);
        log.info("Deleted student reward with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
