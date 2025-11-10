package com.bearpoints.api.controller;

import com.bearpoints.api.converter.StringToGradeLevelConverter;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.TeacherDTO;
import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.service.TeacherService;
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

/**
 * REST controller for teacher management operations.
 * <p>Provides endpoints for managing teachers with pagination, sorting, and filtering.
 *
 * <p>Endpoints:
 * <ul>
 *     <li>GET /api/teachers - Retrieve all teachers (any authenticated user)</li>
 *     <li>GET /api/teachers/search/email - Search teachers by email (any authenticated user)</li>
 *     <li>GET /api/teachers/search/first-name - Search teachers by first name (any authenticated user)</li>
 *     <li>GET /api/teachers/search/last-name - Search teachers by last name (any authenticated user)</li>
 *     <li>GET /api/teachers/search/grade - Search teachers by grade level (any authenticated user)</li>
 *     <li>GET /api/teachers/{id} - Retrieve teacher by ID (any authenticated user)</li>
 *     <li>POST /api/teachers - Create new teacher (ADMIN only)</li>
 *     <li>PUT /api/teachers/{id} - Update existing teacher (ADMIN only)</li>
 *     <li>DELETE /api/teachers/{id} - Delete teacher (ADMIN only)</li>
 * </ul>
 *
 * <p>Security:
 * <ul>
 *     <li>GET endpoints - Any authenticated user</li>
 *     <li>POST, PUT, DELETE endpoints - ADMIN role required</li>
 * </ul>
 *
 * @version 1.0
 * @author Dylan Mercer
 */
@Slf4j
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
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @param sort Sort criteria (default: lastName, asc)
     * @return Paginated response of teachers
     */
    @GetMapping
    public ResponseEntity<PagedResponseDTO<TeacherDTO>> getAllTeachers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "user.lastName,asc") String sort
    ) {
        log.debug("Retrieving all teachers - page: {}, size: {}, sort: {}", page, size, sort);
        String[] sortParams = splitSortParams(sort);
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        PagedResponseDTO<TeacherDTO> response = teacherService.getAllTeachers(pageable);
        log.info("Retrieved {} teachers", response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    /**
     * Searches teachers by email with pagination and sorting.
     * <p>Accessible to any authenticated user.
     *
     * @param email Email search term
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @param sort Sort criteria (default: lastName,asc)
     * @return Paginated response of matching teachers
     */
    @GetMapping("/search/email")
    public ResponseEntity<PagedResponseDTO<TeacherDTO>> searchTeachersByEmail(
            @RequestParam String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "user.lastName,asc") String sort
    ) {
        log.debug("Searching teachers by email: {} - page: {}, size: {}, sort: {}", email, page, size, sort);
        String[] sortParams = splitSortParams(sort);
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        PagedResponseDTO<TeacherDTO> response = teacherService.searchTeachersByEmail(email, pageable);
        log.info("Found {} teachers matching email: {}", response.getNumberOfElements(), email);
        return ResponseEntity.ok(response);
    }

    /**
     * Searches teachers by first name with pagination and sorting.
     * <p>Accessible to any authenticated user.
     *
     * @param firstName First name search term
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @param sort Sort criteria (default: lastName,asc)
     * @return Paginated response of matching teachers
     */
    @GetMapping("/search/first-name")
    public ResponseEntity<PagedResponseDTO<TeacherDTO>> searchTeachersByFirstName(
            @RequestParam String firstName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "user.lastName,asc") String sort
    ) {
        log.debug("Searching teachers by first name: {} - page: {}, size: {}, sort: {}", firstName, page, size, sort);
        String[] sortParams = splitSortParams(sort);
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        PagedResponseDTO<TeacherDTO> response = teacherService.searchTeachersByFirstName(firstName, pageable);
        log.info("Found {} teachers matching first name: {}", response.getNumberOfElements(), firstName);
        return ResponseEntity.ok(response);
    }

    /**
     * Searches teachers by last name with pagination and sorting.
     * <p>Accessible to any authenticated user.
     *
     * @param lastName Last name search term
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @param sort Sort criteria (default: lastName,asc)
     * @return Paginated response of matching teachers
     */
    @GetMapping("/search/last-name")
    public ResponseEntity<PagedResponseDTO<TeacherDTO>> searchTeachersByLastName(
            @RequestParam String lastName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "user.lastName,asc") String sort
    ) {
        log.debug("Searching teachers by last name: {} - page: {}, size: {}, sort: {}", lastName, page, size, sort);
        String[] sortParams = splitSortParams(sort);
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        PagedResponseDTO<TeacherDTO> response = teacherService.searchTeachersByLastName(lastName, pageable);
        log.info("Found {} teachers matching last name: {}", response.getNumberOfElements(), lastName);
        return ResponseEntity.ok(response);
    }

    /**
     * Searches teachers by grade level with pagination and sorting.
     * <p>Accessible to any authenticated user.
     *
     * @param grade GradeLevel search term converted by {@link StringToGradeLevelConverter}
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @param sort Sort criteria (default: lastName,asc)
     * @return Paginated response of matching teachers
     */
    @GetMapping("/search/grade")
    public ResponseEntity<PagedResponseDTO<TeacherDTO>> searchTeachersByGrade(
            @RequestParam GradeLevel grade,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "user.lastName,asc") String sort
    ) {
        log.debug("Searching teachers by grade: {} - page: {}, size: {}, sort: {}", grade.name(), page, size, sort);
        String[] sortParams = splitSortParams(sort);
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        PagedResponseDTO<TeacherDTO> response = teacherService.searchTeachersByGrade(grade, pageable);
        log.info("Found {} teachers matching grade: {}", response.getNumberOfElements(), grade);
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TeacherDTO> deleteTeacher(
            @PathVariable Long id
    ) {
        log.debug("Deleting teacher with ID: {}", id);
        teacherService.deleteTeacher(id);
        log.info("Deleted teacher with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    private String[] splitSortParams(String sort) {
        return sort.split(",");
    }
}
