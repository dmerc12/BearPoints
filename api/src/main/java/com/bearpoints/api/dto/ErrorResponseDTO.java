package com.bearpoints.api.dto;

import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@ToString
public class ErrorResponseDTO {
    private final String message;
    private final LocalDateTime timestamp;

    public ErrorResponseDTO(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
