package com.bearpoints.api.controller;

import com.bearpoints.api.annotation.PaginationAndSorting;
import com.bearpoints.api.criteria.BehaviorTypeSearchCriteria;
import com.bearpoints.api.dto.BehaviorTypeDTO;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.service.BehaviorTypeService;
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
 * REST controller for behavior type management operations.
 * <p>Provides endpoints for managing behavior types with pagination, sorting, and filtering.
 *
 * <p>Endpoints:
 * <ul>
 *     <li>GET /api/behaviors - Retrieve all behavior types (any authenticated user)</li>
 *     <li>GET /api/behaviors/search - Search behavior types with (any authenticated user)</li>
 *     <li>GET /api/behaviors/{id} - Retrieve behavior type by ID (any authenticated user)</li>
 *     <li>POST /api/behaviors - Create a new behavior type (ADMIN only)</li>
 *     <li>PUT /api/behaviors/{id} - Update existing behavior type (ADMIN only)</li>
 *     <li>DELETE /api/behaviors/{id} - Delete a behavior type (ADMIN only)</li>
 * </ul>
 *
 * <p>Security:
 * <ul>
 *     <li>GET endpoints - Any authenticated user</li>
 *     <li>POST, PUT, DELETE endpoints - ADMIN role required</li>
 * </ul>
 *
 * @version 2.0
 * @author Dylan Mercer
 */
@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/behaviors")
@PreAuthorize("isAuthenticated()")
public class BehaviorTypeController {
    private final BehaviorTypeService behaviorTypeService;

    /**
     * Retrieves all behavior types with pagination and sorting.
     * <p>Accessible to any authenticated user.
     *
     * @param pageable Automatically resolved pagination and sorting parameters
     * @return Paginated response of behavior types
     */
    @GetMapping
    public ResponseEntity<PagedResponseDTO<BehaviorTypeDTO>> getAllBehaviorTypes(
            @PaginationAndSorting(
                    defaultSort = "name,asc",
                    allowedSortProperties = {"id", "name", "pointValue", "active"}
            ) Pageable pageable
    ) {
        log.debug("Retrieving all behavior types - page: {}, size: {}, sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        PagedResponseDTO<BehaviorTypeDTO> response = behaviorTypeService.getAllBehaviorTypes(pageable);
        log.info("Retrieved {} behavior types", response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    /**
     * Searches behavior types with flexible criteria including name, active, and point value range.
     * <p>Accessible to any authenticated user.
     *
     * @param name Name search term (optional)
     * @param active Active status (optional)
     * @param minPointValue Minimum point value threshold (optional)
     * @param maxPointValue Maximum point value threshold (optional)
     * @param pageable Automatically resolved pagination and sorting parameters
     * @return Paginated response of matching behavior types
     */
    @GetMapping("/search")
    public ResponseEntity<PagedResponseDTO<BehaviorTypeDTO>> searchBehaviorTypes(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) Integer minPointValue,
            @RequestParam(required = false) Integer maxPointValue,
            @PaginationAndSorting(
                    defaultSort = "name,asc",
                    allowedSortProperties = {"id", "name", "pointValue", "active"}
            ) Pageable pageable
    ) {
        log.debug("Searching behavior types - name: {}, active: {}, minPointValue: {}, maxPointValue: {} - " +
                "page: {}, size: {}, sort: {}", name, active, minPointValue, maxPointValue,
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        BehaviorTypeSearchCriteria criteria = new BehaviorTypeSearchCriteria();
        criteria.setName(name);
        criteria.setActive(active);
        criteria.setMinPointValue(minPointValue);
        criteria.setMaxPointValue(maxPointValue);
        PagedResponseDTO<BehaviorTypeDTO> response = behaviorTypeService.searchBehaviorTypes(criteria, pageable);
        log.info("Found {} behavior types matching search criteria", response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a behavior type by ID.
     * <p>Accessible to any authenticated user.
     *
     * @param id Behavior type ID
     * @return Behavior type details
     */
    @GetMapping("/{id}")
    public ResponseEntity<BehaviorTypeDTO> getBehaviorTypeById(@PathVariable Long id) {
        log.debug("Retrieving behavior type with ID: {}", id);
        BehaviorTypeDTO behaviorType = behaviorTypeService.getBehaviorTypeById(id);
        log.info("Retrieved behavior type with ID: {}", id);
        return ResponseEntity.ok(behaviorType);
    }

    /**
     * Creates a new behavior type.
     * <p>Accessible only to ADMIN users.
     *
     * @param behaviorTypeDTO Behavior type data
     * @return Created behavior type details
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BehaviorTypeDTO> createBehaviorType(@Valid @RequestBody BehaviorTypeDTO behaviorTypeDTO) {
        log.debug("Creating new behavior type with name: {}", behaviorTypeDTO.getName());
        BehaviorTypeDTO createdBehaviorType = behaviorTypeService.createBehaviorType(behaviorTypeDTO);
        log.info("Created behavior type with ID: {}", createdBehaviorType.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createdBehaviorType);
    }

    /**
     * Updates an existing behavior type.
     * <p>Accessible only to ADMIN users.
     *
     * @param id Behavior type ID
     * @param behaviorTypeDTO Updated behavior type data
     * @return Updated behavior type details
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BehaviorTypeDTO> updateBehaviorType(
            @PathVariable Long id,
            @Valid @RequestBody BehaviorTypeDTO behaviorTypeDTO
    ) {
        log.debug("Updating behavior type with ID: {}", id);
        BehaviorTypeDTO updatedBehaviorType = behaviorTypeService.updateBehaviorType(id, behaviorTypeDTO);
        log.info("Updated behavior type with ID: {}", id);
        return ResponseEntity.ok(updatedBehaviorType);
    }

    /**
     * Deletes a behavior type by ID.
     * <p>Accessible only to ADMIN users.
     *
     * @param id Behavior type ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBehaviorType(@PathVariable Long id) {
        log.debug("Deleting behavior type with ID: {}", id);
        behaviorTypeService.deleteBehaviorType(id);
        log.info("Deleted behavior type with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
