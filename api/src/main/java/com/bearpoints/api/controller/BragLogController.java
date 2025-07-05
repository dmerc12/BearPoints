package com.bearpoints.api.controller;

import com.bearpoints.api.dto.BragLogRequest;
import com.bearpoints.api.entity.BragLog;
import com.bearpoints.api.service.BragLogService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Represents route controller responsible for public brag log submissions.
 *
 * @see BragLogService
 * @see BragLogRequest
 * @see BragLog
 *
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

    /** Public brag log submission route controller */
    @PostMapping
    public ResponseEntity<Void> submitBragLog(
            @Valid @RequestBody BragLogRequest request) {
        BragLog log = bragLogService.submitBragLog(request);
        return ResponseEntity
                .created(URI.create("api/brag-logs/" + log.getId()))
                .build();
    }
}
