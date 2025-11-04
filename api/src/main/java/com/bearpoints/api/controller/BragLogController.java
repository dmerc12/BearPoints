package com.bearpoints.api.controller;

import com.bearpoints.api.dto.BragLogRequest;
import com.bearpoints.api.entity.BragLog;
import com.bearpoints.api.service.BragLogService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * REST controller for public brag log submissions.
 * <p>Handles creation of brag logs through a public endpoint that doesn't require authentication.
 *
 * <p>Endpoint:
 * <ul>
 *     <li>{@code POST /api/public/brag-logs} - Submits a new brag log</li>
 * </ul>
 *
 * <p>Request validation:
 * <ul>
 *     <li>Student ID must reference an existing student</li>
 *     <li>Teacher ID must reference an existing teacher</li>
 *     <li>Student must be assigned to the specified teacher</li>
 *     <li>At least one valid behavior ID must be provided</li>
 * </ul>
 *
 * @see BragLogService
 * @see BragLogRequest
 * @see BragLog
 * @version 1.0
 * @author Dylan Mercer
 */
@CrossOrigin
@RestController
@RequestMapping("/api/public/brag-logs")
public class BragLogController {
    private final BragLogService bragLogService;

    public BragLogController(BragLogService bragLogService) {
        this.bragLogService = bragLogService;
    }

    /**
     * Submits a new brag log entry.
     * <p>Processes valid brag log submissions and returns the resource location.
     *
     * @param request Brag log submission data (required)
     * @return HTTP 201 Created with location header pointing to the new resource
     * @throws IllegalArgumentException for invalid request data (HTTP 400)
     */
    @PostMapping
    public ResponseEntity<Void> submitBragLog(
            @Valid @RequestBody BragLogRequest request) {
        BragLog log = bragLogService.submitBragLog(request);
        return ResponseEntity
                .created(URI.create("api/brag-logs/" + log.getId()))
                .build();
    }
}
