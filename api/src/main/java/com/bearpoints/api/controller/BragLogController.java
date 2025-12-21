package com.bearpoints.api.controller;

import com.bearpoints.api.criteria.BragLogSearchCriteria;
import com.bearpoints.api.dto.BragLogDTO;
import com.bearpoints.api.dto.BragLogRequest;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.entity.BragLog;
import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.service.BragLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;

/**
 * REST controller for brag log management operations.
 * <p>Provides endpoints for managing behavior types with pagination, sorting, and filtering.
 *
 * <p>Endpoint:
 * <ul>
 *     <li>GET /api/brag-logs - Retrieve all brag logs (any authenticated user)</li>
 *     <li>GET /api/brag-logs/search - Search brag logs (any authenticated user)</li>
 *     <li>GET /api/brag-logs/{id} - Retrieve brag log by ID (any authenticated user)</li>
 *     <li>POST /api/brag-logs - Create new brag log (any unauthenticated user)</li>
 *     <li>PUT /api/brag-logs/{id} - Update existing brag log (ADMIN and TEACHER only)</li>
 *     <li>DELETE /api/brag-logs/{id} - Delete existing brag log (ADMIN and TEACHER only)</li>
 *     <li>{@code POST /api/public/brag-logs} - Submits a new brag log</li>
 * </ul>
 *
 * <p>Security:
 * <ul>
 *     <li>POST endpoint - Any unauthenticated user</li>
 *     <li>GET endpoints - Any authenticated user</li>
 *     <li>PUT, DELETE endpoints - ADMIN or TEACHER role required</li>
 * </ul>
 *
 * @version 2.0
 * @author Dylan Mercer
 */
@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
@RequestMapping("/api/brag-logs")
public class BragLogController {
    private final BragLogService bragLogService;

    /**
     * Retrieves all brag logs with pagination and sorting.
     * <p>Accessible to any authenticated user.
     *
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @param sort Sort criteria (default: timestamp,desc)
     * @return Paginated response of brag logs
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedResponseDTO<BragLogDTO>> getAllBragLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp,desc") String sort
    ) {
        log.debug("Retrieving all brag logs - page: {}, size: {}, sort: {}", page, size, sort);
        String[] sortParams = splitSortParams(sort);
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        PagedResponseDTO<BragLogDTO> response = bragLogService.getAllBragLogs(pageable);
        log.info("Retrieved {} brag logs", response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    /**
     * Searches brag logs with flexible criteria including student name, teacher name, grade, points generated range,
     * timestamp date range, teacher ID, student ID, and notes.
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
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @param sort Sort criteria (default: timestamp,desc)
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "timestamp,desc") String sort
    ) {
        log.debug("Searching brag logs - studentName: {}, teacherName: {}, grade: {}, minPoints: {}, maxPoints: {} " +
                "startDate: {}, endDate: {}, teacherId: {} studentId: {}, notes: {} - page: {}, size: {}, sort: {}",
                studentName, teacherName, grade, minPoints, maxPoints, startDate, endDate, teacherId, studentId,
                notes, page, size, sort);
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
        String[] sortParams = splitSortParams(sort);
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('TEACHER')")
    public ResponseEntity<Void> deleteBragLog(@PathVariable Long id) {
        log.debug("Deleting brag log with ID: {}", id);
        bragLogService.deleteBragLog(id);
        log.info("Deleted brag log with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    private String[] splitSortParams(String sort) {
        return sort.split(",");
    }

    /**
     * DEPRECATED
     * Submits a new brag log entry.
     * <p>Processes valid brag log submissions and returns the resource location.
     *
     * @param request Brag log submission data (required)
     * @return HTTP 201 Created with location header pointing to the new resource
     * @throws IllegalArgumentException for invalid request data (HTTP 400)
     */
    @PostMapping("/submit")
    public ResponseEntity<Void> submitBragLog(
            @Valid @RequestBody BragLogRequest request) {
        BragLog log = bragLogService.submitBragLog(request);
        return ResponseEntity
                .created(URI.create("api/brag-logs/" + log.getId()))
                .build();
    }
}
