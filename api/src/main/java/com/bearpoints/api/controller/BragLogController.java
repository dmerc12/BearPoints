package com.bearpoints.api.controller;

import com.bearpoints.api.annotation.PaginationAndSorting;
import com.bearpoints.api.criteria.BragLogSearchCriteria;
import com.bearpoints.api.dto.BragLogDTO;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.service.BragLogService;
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
 * REST controller for brag log management operations.
 * <p>Provides endpoints for managing brag logs with pagination, sorting, and filtering.
 *
 * <p>Endpoint:
 * <ul>
 *     <li>GET /api/brags - Retrieve all brag logs (any authenticated user)</li>
 *     <li>GET /api/brags/search - Search brag logs (any authenticated user)</li>
 *     <li>GET /api/brags/{id} - Retrieve brag log by ID (any authenticated user)</li>
 *     <li>POST /api/brags - Create new brag log (any unauthenticated user)</li>
 *     <li>PUT /api/brags/{id} - Update existing brag log (ADMIN and TEACHER only)</li>
 *     <li>DELETE /api/brags/{id} - Delete existing brag log (ADMIN and TEACHER only)</li>
 * </ul>
 *
 * <p>Security:
 * <ul>
 *     <li>POST endpoint - Any unauthenticated user</li>
 *     <li>GET endpoints - Any authenticated user</li>
 *     <li>PUT, DELETE endpoints - ADMIN or TEACHER role required</li>
 * </ul>
 *
 * @version 2.2
 * @author Dylan Mercer
 */
@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@PreAuthorize("permitAll()")
@RequestMapping("/api/brags")
public class BragLogController {
    private final BragLogService bragLogService;

    /**
     * Retrieves all brag logs with pagination and sorting.
     * <p>Accessible to any authenticated user.
     *
     * @param pageable Automatically resolved pagination and sorting parameters
     * @return Paginated response of brag logs
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedResponseDTO<BragLogDTO>> getAllBragLogs(
            @PaginationAndSorting(
                    defaultSort = "timestamp,desc",
                    allowedSortProperties = {"id", "student.id", "student.user.firstName", "student.user.lastName",
                            "student.user.email", "teacher.id", "teacher.user.firstName", "teacher.user.lastName",
                            "teacher.user.email", "grade", "pointsGenerated", "notes", "timestamp",
                            "submitterName", "submitterUserId"}
            ) Pageable pageable
    ) {
        log.debug("Retrieving all brag logs - page: {}, size: {}, sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        PagedResponseDTO<BragLogDTO> response = bragLogService.getAllBragLogs(pageable);
        log.info("Retrieved {} brag logs", response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    /**
     * Searches brag logs with flexible criteria including student name, teacher name, grade, points generated range,
     * timestamp date range, teacher ID, student ID, notes, "submitter name", and "submitter user ID".
     * <p>Accessible ot any authenticated user.
     *
     * @param studentName Student name search term (optional)
     * @param teacherName Teacher name search term (optional)
     * @param grade Grade level filter (optional)
     * @param minPoints Minimum points generated threshold (optional)
     * @param maxPoints Maximum points generated threshold (optional)
     * @param startDate Timestamp start date (optional)
     * @param endDate Timestamp end date (optional)
     * @param teacherId Teacher ID filter (optional)
     * @param studentId Student ID filter (optional)
     * @param notes Notes search term (optional)
     * @param submitterName Submitter name search term (optional)
     * @param submitterUserId Submitter user ID filter (optional)
     * @param pageable Automatically resolved pagination and sorting parameters
     * @return Paginated response of matching brag logs
     */
    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedResponseDTO<BragLogDTO>> searchBragLogs(
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String teacherName,
            @RequestParam(required = false) GradeLevel grade,
            @RequestParam(required = false) Integer minPoints,
            @RequestParam(required = false) Integer maxPoints,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) String submitterName,
            @RequestParam(required = false) Long submitterUserId,
            @PaginationAndSorting(
                    defaultSort = "timestamp,desc",
                    allowedSortProperties = {"id", "student.id", "student.user.firstName", "student.user.lastName",
                            "student.user.email", "teacher.id", "teacher.user.firstName", "teacher.user.lastName",
                            "teacher.user.email", "grade", "pointsGenerated", "notes", "timestamp",
                            "submitterName", "submitterUserId"}
            ) Pageable pageable
    ) {
        log.debug("Searching brag logs - studentName: {}, teacherName: {}, grade: {}, minPoints: {}, maxPoints: {} " +
                "startDate: {}, endDate: {}, teacherId: {} studentId: {}, notes: {}, submitterName: {}, " +
                        "submitterUserId: {} - page: {}, size: {}, sort: {}",
                studentName, teacherName, grade, minPoints, maxPoints, startDate, endDate, teacherId, studentId,
                notes, submitterName, submitterUserId, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        BragLogSearchCriteria criteria = new BragLogSearchCriteria();
        criteria.setStudentName(studentName);
        criteria.setTeacherName(teacherName);
        criteria.setGrade(grade);
        criteria.setMinPoints(minPoints);
        criteria.setMaxPoints(maxPoints);
        criteria.setStartDate(startDate);
        criteria.setEndDate(endDate);
        criteria.setTeacherId(teacherId);
        criteria.setStudentId(studentId);
        criteria.setNotes(notes);
        criteria.setSubmitterName(submitterName);
        criteria.setSubmitterUserId(submitterUserId);
        PagedResponseDTO<BragLogDTO> response = bragLogService.searchBragLogs(criteria, pageable);
        log.info("Found {} brag logs matching search criteria", response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a brag log by ID.
     * <p>Accessible to any authenticated user.
     *
     * @param id Brag log ID
     * @return Brag log details
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BragLogDTO> getBragLogById(@PathVariable Long id) {
        log.debug("Retrieving brag log with ID: {}", id);
        BragLogDTO bragLog = bragLogService.getBragLogById(id);
        log.info("Retrieved brag log with ID: {}", id);
        return ResponseEntity.ok(bragLog);
    }

    /**
     * Creates a new brag log.
     * <p>Accessible to any unauthenticated user.
     *
     * @param bragLogDTO Brag log data
     * @return Created brag log details
     */
    @PostMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<BragLogDTO> createBragLog(@Valid @RequestBody BragLogDTO bragLogDTO) {
        log.debug("Creating new brag log for student with ID: {}", bragLogDTO.getStudentId());
        BragLogDTO createdBragLog = bragLogService.createBragLog(bragLogDTO);
        log.info("Created brag log with ID: {}", createdBragLog.getId());
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(createdBragLog);
    }

    /**
     * Updates an existing brag log.
     * <p>Accessible only to ADMIN and TEACHER users.
     *
     * @param id Brag log ID
     * @param bragLogDTO Updated brag log data
     * @return Updated brag log details
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<BragLogDTO> updateBragLog(
            @PathVariable Long id,
            @Valid @RequestBody BragLogDTO bragLogDTO
    ) {
        log.debug("Updating brag log with ID: {}", id);
        BragLogDTO updatedBragLog = bragLogService.updateBragLog(id, bragLogDTO);
        log.info("Updated brag log with ID: {}", updatedBragLog.getId());
        return ResponseEntity.ok(updatedBragLog);
    }

    /**
     * Deletes a brag log by ID.
     * <p>Accessible only to ADMIN and TEACHER users.
     *
     * @param id Brag log ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public ResponseEntity<Void> deleteBragLog(@PathVariable Long id) {
        log.debug("Deleting brag log with ID: {}", id);
        bragLogService.deleteBragLog(id);
        log.info("Deleted brag log with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
