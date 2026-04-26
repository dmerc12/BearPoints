package com.bearpoints.api.controller;

import com.bearpoints.api.annotation.PaginationAndSorting;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.TeacherDTO;
import com.bearpoints.api.criteria.TeacherSearchCriteria;
import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.service.TeacherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for teacher management operations.
 * <p>Provides endpoints for managing teachers with pagination, sorting, and filtering.
 *
 * <p>Endpoints:
 * <ul>
 *     <li>GET /api/teachers - Retrieve all teachers (any authenticated user)</li>
 *     <li>GET /api/teachers/search - Search teachers by with flexible criteria (any authenticated user)</li>
 *     <li>GET /api/teachers/{id} - Retrieve teacher by ID (any authenticated user)</li>
 *     <li>POST /api/teachers - Create new teacher (ADMIN/STAFF only)</li>
 *     <li>PUT /api/teachers/{id} - Update existing teacher (ADMIN/STAFF only)</li>
 *     <li>DELETE /api/teachers/{id} - Delete teacher (ADMIN/STAFF only)</li>
 * </ul>
 *
 * <p>Security:
 * <ul>
 *     <li>GET endpoints - Any authenticated user</li>
 *     <li>POST, PUT, DELETE endpoints - ADMIN/STAFF role required</li>
 * </ul>
 *
 * @version 2.1
 * @author Dylan Mercer
 */
@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/teachers")
@PreAuthorize("isAuthenticated()")
public class TeacherController {
    private final TeacherService teacherService;

    /**
     * Retrieves all teachers with pagination and sorting.
     * <p>Accessible to any authenticated user.
     *
     * @param pageable Automatically resolved pagination and sorting parameters
     * @return Paginated response of teachers
     */
    @GetMapping
    public ResponseEntity<PagedResponseDTO<TeacherDTO>> getAllTeachers(
            @PaginationAndSorting(
                    defaultSort = "user.lastName,asc",
                    allowedSortProperties = {"id, grade, user.firstName, user.lastName, user.email"}
            ) Pageable pageable
    ) {
        log.debug("Retrieving all teachers - page: {}, size: {}, sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        PagedResponseDTO<TeacherDTO> response = teacherService.getAllTeachers(pageable);
        log.info("Retrieved {} teachers", response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    /**
     * Searches teachers with flexible criteria including email, name and grade.
     * <p>Accessible to any authenticated user.
     *
     * @param email Email search term (optional)
     * @param firstName First name search term (optional)
     * @param lastName Last name search term (optional)
     * @param grade Grade level (optional)
     * @param pageable Automatically resolved pagination and sorting parameters
     * @return Paginated response of matching teachers
     */
    @GetMapping("/search")
    public ResponseEntity<PagedResponseDTO<TeacherDTO>> searchTeachers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) GradeLevel grade,
            @PaginationAndSorting(
                    defaultSort = "user.lastName,asc",
                    allowedSortProperties = {"id, grade, user.firstName, user.lastName, user.email"}
            ) Pageable pageable
    ) {
        log.debug("Searching teachers - email: {}, firstName: {}, lastName: {}, grade: {} " +
                "- page: {}, size: {}, sort: {}", email, firstName, lastName, grade,
                pageable.getPageSize(), pageable.getPageSize(), pageable.getSort());
        TeacherSearchCriteria criteria = new TeacherSearchCriteria();
        criteria.setEmail(email);
        criteria.setFirstName(firstName);
        criteria.setLastName(lastName);
        criteria.setGrade(grade);
        PagedResponseDTO<TeacherDTO> response = teacherService.searchTeachers(criteria, pageable);
        log.info("Retrieved {} teachers matching search criteria", response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a teacher by ID.
     * <p>Accessible to any authenticated user.
     *
     * @param id Teacher ID
     * @return Teacher details
     */
    @GetMapping("/{id}")
    public ResponseEntity<TeacherDTO> getTeacherById(
            @PathVariable Long id
    ) {
        log.debug("Retrieving teacher with ID: {}", id);
        TeacherDTO teacher = teacherService.getTeacherById(id);
        log.info("Retrieved teacher with ID: {}", id);
        return ResponseEntity.ok(teacher);
    }

    /**
     * Creates a new teacher.
     * <p>Accessible only to ADMIN users.
     *
     * @param teacherDTO Teacher data
     * @return Created teacher details
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<TeacherDTO> createTeacher(
            @Valid @RequestBody TeacherDTO teacherDTO
    ) {
        log.debug("Creating new teacher with email: {}", teacherDTO.getUser().getEmail());
        TeacherDTO createdTeacher = teacherService.createTeacher(teacherDTO);
        log.info("Created teacher ID: {}", createdTeacher.getId());
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(createdTeacher);
    }

    /**
     * Updates an existing teacher.
     * <p>Accessible only to ADMIN users.
     *
     * @param id Teacher ID
     * @param teacherDTO Updated teacher data
     * @return Updated teacher details
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<TeacherDTO> updateTeacher(
            @PathVariable Long id,
            @Valid @RequestBody TeacherDTO teacherDTO
    ) {
        log.debug("Updating teacher with ID: {}", id);
        TeacherDTO updatedTeacher = teacherService.updateTeacher(id, teacherDTO);
        log.info("Updated teacher with ID: {}", id);
        return ResponseEntity.ok(updatedTeacher);
    }

    /**
     * Deletes a teacher by ID.
     * <p>Accessible only to ADMIN users.
     *
     * @param id Teacher ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    public ResponseEntity<TeacherDTO> deleteTeacher(
            @PathVariable Long id
    ) {
        log.debug("Deleting teacher with ID: {}", id);
        teacherService.deleteTeacher(id);
        log.info("Deleted teacher with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
