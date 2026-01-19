package com.bearpoints.api.controller;

import com.bearpoints.api.annotation.PaginationAndSorting;
import com.bearpoints.api.criteria.RewardItemSearchCriteria;
import com.bearpoints.api.dto.PagedResponseDTO;
import com.bearpoints.api.dto.RewardItemDTO;
import com.bearpoints.api.service.RewardItemService;
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
 * REST controller for reward item management operations.
 * <p>Provides endpoints for managing reward items with pagination, sorting, and filtering.
 *
 * <p>Endpoints:
 * <ul>
 *     <li>GET /api/items - Retrieve all reward items (any authenticated user)</li>
 *     <li>GET /api/items/search - Search reward items (any authenticated user)</li>
 *     <li>GET /api/items/{id} - Retrieve reward item by ID (any authenticated user)</li>
 *     <li>POST /api/items - Create a new reward item (ADMIN only)</li>
 *     <li>PUT /api/items/{id} - Update existing reward item (ADMIN only)</li>
 *     <li>DELETE /api/items/{id} - Delete a reward item (ADMIN only)</li>
 * </ul>
 *
 * <p>Security:
 * <ul>
 *     <li>GET endpoints - Any authenticated user</li>
 *     <li>POST, PUT, DELETE endpoints - ADMIN role required</li>
 * </ul>
 *
 * @version 1.1
 * @author Dylan Mercer
 */
@Slf4j
@CrossOrigin
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/items")
@PreAuthorize("isAuthenticated()")
public class RewardItemController {
    private final RewardItemService rewardItemService;

    /**
     * Retrieves all reward items with pagination and sorting.
     * <p>Accessible to any authenticated user.
     *
     * @param pageable Automatically resolved pagination and sorting parameters
     * @return Paginated response of reward items
     */
    @GetMapping
    public ResponseEntity<PagedResponseDTO<RewardItemDTO>> getAllRewardItems(
            @PaginationAndSorting(
                    defaultSort = "name,asc",
                    allowedSortProperties = {"id", "name", "pointCost", "stock"}
            ) Pageable pageable) {
        log.debug("Retrieving all reward items - page: {}, size: {}, sort: {}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        PagedResponseDTO<RewardItemDTO> response = rewardItemService.getAllRewardItems(pageable);
        log.info("Retrieved {} reward items", response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    /**
     * Searches reward items with flexible criteria including name, point cost value range, and stock value range.
     * <p>Accessible to any authenticated user.
     *
     * @param name Name search term (optional)
     * @param minPointCost Minimum point cost threshold (optional)
     * @param maxPointCost Maximum point cost threshold (optional)
     * @param minStock Minimum stock threshold (optional)
     * @param maxStock Maximum stock threshold (optional)
     * @param pageable Automatically resolved pagination and sorting parameters
     * @return Paginated response of matching reward items
     */
    @GetMapping("/search")
    public ResponseEntity<PagedResponseDTO<RewardItemDTO>> searchRewardItems(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer minPointCost,
            @RequestParam(required = false) Integer maxPointCost,
            @RequestParam(required = false) Integer minStock,
            @RequestParam(required = false) Integer maxStock,
            @PaginationAndSorting(
                    defaultSort = "name,asc",
                    allowedSortProperties = {"id", "name", "pointCost", "stock"}
            ) Pageable pageable) {
        log.debug("Searching reward items - name: {}, minPointCost: {}, maxPointCost: {}, " +
                "minStock: {}, maxStock: {} - page: {}, size: {}, sort: {}", name, minPointCost, maxPointCost,
                minStock, maxStock, pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        RewardItemSearchCriteria criteria = new RewardItemSearchCriteria();
        criteria.setName(name);
        criteria.setMinPointCost(minPointCost);
        criteria.setMaxPointCost(maxPointCost);
        criteria.setMinStock(minStock);
        criteria.setMaxStock(maxStock);
        PagedResponseDTO<RewardItemDTO> response = rewardItemService.searchRewardItems(criteria, pageable);
        log.info("Found {} reward items matching search criteria", response.getNumberOfElements());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves a reward item by ID.
     * <p>Accessible to any authenticated user.
     *
     * @param id Reward item ID
     * @return Reward item details
     */
    @GetMapping("/{id}")
    public ResponseEntity<RewardItemDTO> getRewardItemById(@PathVariable Long id) {
        log.debug("Retrieving reward item with ID: {}", id);
        RewardItemDTO rewardItem = rewardItemService.getRewardItemById(id);
        log.info("Retrieved reward item with ID: {}", id);
        return ResponseEntity.ok(rewardItem);
    }

    /**
     * Creates a new reward item.
     * <p>Accessible only to ADMIN users.
     *
     * @param rewardItemDTO Reward item data
     * @return Created reward item details
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RewardItemDTO> createRewardItem(@Valid @RequestBody RewardItemDTO rewardItemDTO) {
        log.debug("Creating new reward item with name: {}", rewardItemDTO.getName());
        RewardItemDTO createdRewardItem = rewardItemService.createRewardItem(rewardItemDTO);
        log.info("Created reward item with ID: {}", createdRewardItem.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .contentType(MediaType.APPLICATION_JSON)
                .body(createdRewardItem);
    }

    /**
     * Updates an existing reward item.
     * <p>Accessible only to ADMIN users.
     *
     * @param id Reward item ID
     * @param rewardItemDTO Updated reward item data
     * @return Updated reward item details
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RewardItemDTO> updateRewardItem(
            @PathVariable Long id,
            @Valid @RequestBody RewardItemDTO rewardItemDTO
    ) {
        log.debug("Updating reward item with ID: {}", id);
        RewardItemDTO updatedRewardItem = rewardItemService.updateRewardItem(id, rewardItemDTO);
        log.info("Updated reward item with ID: {}", id);
        return ResponseEntity.ok(updatedRewardItem);
    }

    /**
     * Deletes a reward item by ID.
     * <p>Accessible only to ADMIN users.
     *
     * @param id Reward item ID
     * @return No content response
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRewardItem(@PathVariable Long id) {
        log.debug("Deleting behavior type with ID: {}", id);
        rewardItemService.deleteRewardItem(id);
        log.info("Deleted behavior type with ID: {}", id);
        return ResponseEntity.noContent().build();
    }
}
