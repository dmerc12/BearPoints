package com.bearpoints.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

/**
 * Request object for creating brag log entries.
 * <p>Validates input constraints for brag log creation.
 *
 * <p>Constraints:
 * <ul>
 *     <li>studentId - Required</li>
 *     <li>teacherId - Required</li>
 *     <li>behaviorIds - At least one behavior required</li>
 *     <li>notes - Max 500 characters</li>
 * </ul>
 *
 * @version 1.0
 * @author Dylan Mercer
 */
@Getter
@AllArgsConstructor
public class BragLogRequest {
    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    @NotEmpty(message = "At least one behavior is required")
    private Set<Long> behaviorIds;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
