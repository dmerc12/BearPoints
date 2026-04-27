package com.bearpoints.api.controller;

import com.bearpoints.api.service.GoogleSheetsSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for Google Sheets synchronization operations.
 * <p>Provides endpoints to manually trigger synchronization between the database and Google Sheets.
 *
 * <p>Endpoints:
 * <ul>
 *     <li>POST /api/sync - Trigger full synchronization of all entity types</li>
 * </ul>
 *
 * <p>Security:
 * <ul>
 *     <li>All sync endpoints require ADMIN or STAFF role</li>
 *     <li>Scheduled sync runs automatically via cron (8AM and 8PM)</li>
 * </ul>
 *
 * <p>Features:
 * <ul>
 *     <li>Manual trigger for on-demand synchronization</li>
 *     <li>Comprehensive logging of sync operations</li>
 *     <li>Error handling with appropriate HTTP responses</li>
 * </ul>
 *
 * @see GoogleSheetsSyncService
 * @version 1.1
 * @author Dylan Mercer
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sync")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
@Tag(name = "Google Sheets Sync", description = "Endpoints for managing Google Sheets synchronization")
public class GoogleSheetsSyncController {
    private final GoogleSheetsSyncService googleSheetsSyncService;

    /**
     * Triggers full synchronization of all application data with Google Sheets.
     *
     * <p>Performs bidirectional synchronization for all entity types:
     * <ul>
     *     <li>Users</li>
     *     <li>Teachers</li>
     *     <li>Students</li>
     *     <li>BehaviorTypes</li>
     *     <li>BragLogs</li>
     *     <li>RewardItems</li>
     *     <li>StudentRewards</li>
     * </ul>
     *
     * <p>This endpoint is intended for:
     * <ul>
     *     <li>Manual sync triggers outside scheduled times</li>
     *     <li>Recovery operations after failures</li>
     *     <li>Initial data populations</li>
     * </ul>
     *
     * @return ResponseEntity with sync status message
     */
    @PostMapping
    @Operation(summary = "Trigger full Google Sheets synchronization",
            description = "Manually triggers bidirectional sync between database and Google Sheets for all entity types")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Synchronization completed successfully"),
            @ApiResponse(responseCode = "500", description = "Synchronization failed due to internal error")
    })
    public ResponseEntity<String> syncAllData() {
        log.info("Manual Google Sheets sync triggered by ADMIN user");
        try {
            googleSheetsSyncService.syncAllData();
            log.info("Manual Google Sheets sync completed successfully");
            return ResponseEntity.ok("Google Sheets synchronization completed successfully");
        } catch (Exception e) {
            log.error("Manual Google Sheets sync failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Synchronization failed: " + e.getMessage());
        }
    }
}
