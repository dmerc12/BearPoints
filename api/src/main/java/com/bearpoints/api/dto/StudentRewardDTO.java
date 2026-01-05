package com.bearpoints.api.dto;

import com.bearpoints.api.entity.StudentReward;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class StudentRewardDTO {
    // Request / response fields (client sends, server returns)
    private final Long id;

    @NotNull(message = "Student ID is required")
    private final Long studentId;

    @NotNull(message = "Item ID is required")
    private final Long itemId;

    // Response fields (never sent by client)
    private final LocalDateTime timestamp;
    private final String studentName;
    private final String itemName;
    private final Integer pointsUsed;

    /**
     * Constructor for JSON deserialization (create/update operations)
     */
    @JsonCreator
    public StudentRewardDTO(@JsonProperty("id") Long id,
                            @JsonProperty("studentId") Long studentId,
                            @JsonProperty("itemId") Long itemId,
                            @JsonProperty("timestamp") LocalDateTime timestamp,
                            @JsonProperty("studentName") String studentName,
                            @JsonProperty("itemName") String itemName,
                            @JsonProperty("pointsUsed") Integer pointsUsed
    ) {
        this.id = id;
        this.studentId = studentId;
        this.itemId = itemId;
        this.timestamp = timestamp;
        this.studentName = studentName;
        this.itemName = itemName;
        this.pointsUsed = pointsUsed;
    }

    /**
     * Constructs a StudentRewardDTO from a Student Reward entity
     *
     * @param studentReward Source student reward entity
     */
    public StudentRewardDTO(StudentReward studentReward) {
        this.id = studentReward.getId();
        // IDs from relationships
        this.studentId = studentReward.getStudent() != null ? studentReward.getStudent().getId() : null;
        this.itemId = studentReward.getRewardItem() != null ? studentReward.getRewardItem().getId() : null;
        // Names from entities
        if (studentReward.getStudent() != null && studentReward.getStudent().getUser() != null) {
            String firstName = studentReward.getStudent().getUser().getFirstName();
            String lastName = studentReward.getStudent().getUser().getLastName();
            this.studentName = (firstName != null && lastName != null) ? firstName + " " + lastName : null;
        } else {
            this.studentName = null;
        }
        this.itemName = studentReward.getRewardItem() != null ?
                studentReward.getRewardItem().getName() : null;
        // Server-calculated fields
        this.pointsUsed = studentReward.getRewardItem() != null ?
                studentReward.getRewardItem().getPointCost() : null;
        this.timestamp = studentReward.getRedeemedAt();
    }
}
