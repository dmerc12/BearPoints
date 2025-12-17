package com.bearpoints.api.dto;

import com.bearpoints.api.entity.BehaviorType;
import com.bearpoints.api.entity.BragLog;
import com.bearpoints.api.entity.GradeLevel;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Data Transfer Object for {@link BragLog} operations.
 * <p>Used for both creating / updating brag logs and returning brag log data.
 * <p>For creation: Only studentId, teacherId, and behaviorIds are required; notes are optional.
 * <p>For response: All fields including id, grade, pointsGenerated, and timestamp are populated.
 *
 * @version 1.0
 * @author Dylan Mercer
 */
@Getter
public class BragLogDTO {
    // Response fields (never sent by client)
    private final String studentName;
    private final String teacherName;
    private final GradeLevel grade;
    private final Set<BehaviorTypeDTO> behaviors;
    private final Integer pointsGenerated;
    private final LocalDateTime timestamp;

    // Request / response fields (client sends, server returns)
    private final Long id;
    @NotNull(message = "Student ID is required")
    private final Long studentId;

    @NotNull(message = "Teacher ID is required")
    private final Long teacherId;

    @NotEmpty(message = "At least one behavior is required")
    private final Set<Long> behaviorIds;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private final String notes;

    /**
     * Constructor for JSON deserialization (create/update operations)
     */
    @JsonCreator
    public BragLogDTO(@JsonProperty("id") Long id,
                      @JsonProperty("studentId") Long studentId,
                      @JsonProperty("teacherId") Long teacherId,
                      @JsonProperty("behaviorIds") Set<Long> behaviorIds,
                      @JsonProperty("notes") String notes,
                      @JsonProperty("studentName") String studentName,
                      @JsonProperty("teacherName") String teacherName,
                      @JsonProperty("grade") GradeLevel grade,
                      @JsonProperty("behaviors") Set<BehaviorTypeDTO> behaviors,
                      @JsonProperty("pointsGenerated") Integer pointsGenerated,
                      @JsonProperty("timestamp") LocalDateTime timestamp
    ) {
        this.id = id;
        this.studentId = studentId;
        this.teacherId = teacherId;
        this.behaviorIds = behaviorIds;
        this.notes = notes;
        this.studentName = studentName;
        this.teacherName = teacherName;
        this.grade = grade;
        this.behaviors = behaviors;
        this.pointsGenerated = pointsGenerated;
        this.timestamp = timestamp;
    }

    /**
     * Constructs a BragLogDTO from a Brag Log entity
     *
     * @param bragLog Source brag log entity
     */
    public BragLogDTO(BragLog bragLog) {
        this.id = bragLog.getId();
        // IDs from relationships
        this.studentId = bragLog.getStudent() != null ? bragLog.getStudent().getId() : null;
        this.teacherId = bragLog.getTeacher() != null ? bragLog.getTeacher().getId() : null;
        // Names from user entities
        this.studentName = bragLog.getStudent() != null && bragLog.getStudent().getUser() != null
                ? bragLog.getStudent().getUser().getFirstName() + " " + bragLog.getStudent().getUser().getLastName()
                : null;
        this.teacherName = bragLog.getTeacher() != null && bragLog.getTeacher().getUser() != null
                ? bragLog.getTeacher().getUser().getFirstName() + " " + bragLog.getTeacher().getUser().getLastName()
                : null;
        // Server-calculated fields
        this.grade = bragLog.getGrade();
        this.pointsGenerated = bragLog.getPointsGenerated();
        this.timestamp = bragLog.getTimestamp();
        // Notes
        this.notes = bragLog.getNotes();
        // Behaviors
        this.behaviorIds = bragLog.getBehaviors() != null ?
                bragLog.getBehaviors().stream()
                        .map(BehaviorType::getId)
                        .collect(Collectors.toSet()) : null;
        this.behaviors = bragLog.getBehaviors() != null ?
                bragLog.getBehaviors().stream()
                        .map(BehaviorTypeDTO::new)
                        .collect(Collectors.toSet()) : null;



    }
}
