package com.bearpoints.api.controller;

import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.StudentDTO;
import com.bearpoints.api.dto.StudentSearchCriteria;
import com.bearpoints.api.service.StudentService;
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
 * REST controller for student management operations.
 * <p>Provides endpoints for managing students with pagination, sorting, and filtering.
 *
 * <p>Endpoints:
 * <ul>
 *     <li>GET /api/students - Retrieve all students (any unauthenticated user)</li>
 *     <li>GET /api/students/search - Search students with flexible criteria (any authenticated user)</li>
 *     <li>GET /api/students/leaderboard - Retrieve classroom leaderboard (any authenticated user)</li>
 *     <li>GET /api/students/{id} - Retrieve student by ID (any authenticated user)</li>
 *     <li>GET /api/students/token/{token} - Retrieve student by token (any authenticated user)</li>
 *     <li>POST /api/students - Create new student (ADMIN only)</li>
 *     <li>PUT /api/students - Update existing student (Admin only)</li>
 *     <li>DELETE /api/students/{id} - Delete student (Admin only)</li>
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
@RequestMapping("/api/students")
@PreAuthorize("isAuthenticated()")
public class StudentController {
    private final StudentService studentService;

    /**
     * Retrieves all students with pagination and sorting.
     * <p>Accessible to any authenticated user.
     *
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @param sort Sort criteria (default: user.lastName,asc)
     * @return Paginated response of students
     */
    @GetMapping
    public ResponseEntity<PagedResponseDTO<StudentDTO>> getAllStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "user.lastName,asc") String sort
    ) {
        log.debug("Retrieving all students - page: {}, size: {}, sort: {}", page, size, sort);
        String[] sortParams = splitSortParams(sort);
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        PagedResponseDTO<StudentDTO> response = studentService.getAllStudents(pageable);
        log.info("Retrieved {} students", response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    /**
     * Searches students with flexible criteria including email, name, teacher, and points range.
     * <p>Accessible to any authenticated user.
     *
     * @param email Email search term (optional)
     * @param firstName First name search term (optional)
     * @param lastName Last name search term (optional)
     * @param teacherId Teacher ID filter (optional)
     * @param minPoints Minimum points threshold (optional)
     * @param maxPoints Maximum points threshold (optional)
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @param sort Sort criteria (default: user.lastName,asc)
     * @return Paginated response of matching students
     */
    @GetMapping("/search")
    public ResponseEntity<PagedResponseDTO<StudentDTO>> searchStudents(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) Long teacherId,
            @RequestParam(required = false) Integer minPoints,
            @RequestParam(required = false) Integer maxPoints,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "user.lastName,asc") String sort
    ) {
        log.debug("Searching students - email: {}, firstName: {}, lastName: {}, teacherId: {}, minPoints: {}, " +
                "maxPoints: {} - page: {}, size: {}, sort: {}", email, firstName, lastName, teacherId, minPoints,
                maxPoints, page, size, sort);
        StudentSearchCriteria criteria = new StudentSearchCriteria();
        criteria.setEmail(email);
        criteria.setFirstName(firstName);
        criteria.setLastName(lastName);
        criteria.setTeacherId(teacherId);
        criteria.setMinPoints(minPoints);
        criteria.setMaxPoints(maxPoints);
        String[] sortParams = splitSortParams(sort);
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        PagedResponseDTO<StudentDTO> response = studentService.searchStudents(criteria, pageable);
        log.info("Found {} students matching search criteria", response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves classroom leaderboard for a specific teacher, ordered by points descending.
     * <p>Accessible to any authenticated user.
     *
     * @param teacherId Teacher ID for classroom filter
     * @param page Page number (default: 0)
     * @param size Page size (default: 20)
     * @param sort Sort criteria (default: points,desc)
     * @return Paginated response of students in leaderboard order
     */
    @GetMapping("/leaderboard")
    public ResponseEntity<PagedResponseDTO<StudentDTO>> getClassRoomLeaderboard(
            @RequestParam Long teacherId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "points,desc") String sort
    ) {
        log.debug("Retrieving classroom leaderboard for teacher ID: {} - page: {}, size: {}, sort: {}",
                teacherId, page, size, sort);
        String[] sortParams = splitSortParams(sort);
        Sort.Direction direction = sortParams.length > 1 && "desc".equalsIgnoreCase(sortParams[1])
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortParams[0]));
        PagedResponseDTO<StudentDTO> response = studentService.getClassRoomLeaderboard(teacherId, pageable);
        log.info("Retrieved {} students for classroom leaderboard", response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a student by ID.
     * <p>Accessible to any authenticated user.
     *
     * @param id Student ID
     * @return Student details
     */
    @GetMapping("/{id}")
    public ResponseEntity<StudentDTO> getStudentById(@PathVariable Long id) {
        log.debug("Retrieving student with ID: {}", id);
        StudentDTO student = studentService.getStudentById(id);
        log.info("Retrieved student with ID: {}", id);
        return ResponseEntity.ok(student);
    }

    /**
     * Retrieves a student by access token.
     * <p>Accessible to any authenticated user.
     *
     * @param token Student's unique access token
     * @return Student details
     */
    @GetMapping("/token/{token}")
    public ResponseEntity<StudentDTO> getStudentByToken(@PathVariable String token) {
        log.debug("Retrieving student with token: {}", token);
        StudentDTO student = studentService.getStudentByToken(token);
        log.info("Retrieved student with token: {}", token);
        return ResponseEntity.ok(student);
    }

    /**
     * Creates a new student.
     * <p>Accessible only to ADMIN users.
     *
     * @param studentDTO Student data
     * @return Created student details
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentDTO> createStudent(@Valid @RequestBody StudentDTO studentDTO) {
        log.debug("Creating new student with email: {}", studentDTO.getUser().getEmail());
        StudentDTO createdStudent = studentService.createStudent(studentDTO);
        log.info("Created student with ID: {}", createdStudent.getId());
        return ResponseEntity.status(HttpStatus.CREATED).contentType(MediaType.APPLICATION_JSON).body(createdStudent);
    }

    /**
     * Updates an existing student.
     * <p>Accessible only to ADMIN users.
     *
     * @param id Student ID
     * @param studentDTO Updated student data
     * @return Updated student details
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StudentDTO> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentDTO studentDTO) {
        log.debug("Updating student with ID: {}", id);
        StudentDTO updatedStudent = studentService.updateStudent(id, studentDTO);
        log.info("Updated student with ID: {}", id);
        return ResponseEntity.ok(updatedStudent);
    }

    /**
     * Deletes a student by ID.
     * <p>Accessible only to ADMIN users.
     *
     * @param id Student ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
        log.debug("Deleting student with ID: {}", id);
        studentService.deleteStudent(id);
        log.info("Deleted student with ID: {}", id);
        return ResponseEntity.noContent().build();
    }

    private String[] splitSortParams(String sort) {
        return sort.split(",");
    }
}
