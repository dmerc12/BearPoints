package com.bearpoints.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@Getter
@AllArgsConstructor
public class BragLogRequest {
    @NotNull
    private Long studentId;

    @NotNull
    private Long teacherId;

    @NotEmpty
    private Set<Long> behaviorIds;

    @Size(max = 500)
    private String notes;
}
