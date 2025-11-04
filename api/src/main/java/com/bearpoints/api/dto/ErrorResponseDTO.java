package com.bearpoints.api.dto;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@ToString
public class ErrorResponseDTO {
    private final String message;
    private final LocalDateTime timestamp;
    private final Map<String, String> fieldErrors;

    public ErrorResponseDTO(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.fieldErrors = null;
    }

    public ErrorResponseDTO(String message, Map<String, String> fieldErrors) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.fieldErrors = fieldErrors;
    }
}
