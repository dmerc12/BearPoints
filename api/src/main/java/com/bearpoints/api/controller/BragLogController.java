package com.bearpoints.api.controller;

import com.bearpoints.api.dto.BragLogRequest;
import com.bearpoints.api.entity.BragLog;
import com.bearpoints.api.service.BragLogService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@CrossOrigin
@RestController
@RequestMapping("/api/public/brag-logs")
public class BragLogController {
    private final BragLogService bragLogService;

    public BragLogController(BragLogService bragLogService) {
        this.bragLogService = bragLogService;
    }

    @PostMapping
    public ResponseEntity<Void> submitBragLog(
            @Valid @RequestBody BragLogRequest request) {
        BragLog log = bragLogService.submitBearBrag(request);
        return ResponseEntity
                .created(URI.create("api/brag-logs/" + log.getId()))
                .build();
    }
}
