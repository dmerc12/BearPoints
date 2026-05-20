package com.bearpoints.api.dto;

import com.bearpoints.api.entity.BehaviorType;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class BehaviorTypeDTO {
    private final Long id;

    @NotBlank(message = "Behavior name is required")
    @Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters")
    private final String name;

    @NotNull(message = "Point value is required")
    @Min(value = 1, message = "Minimum point value is 1")
    @Max(value = 5, message = "Maximum point value is 5")
    private final Integer pointValue;

    @NotNull(message = "Active status is required")
    private final Boolean active;

    /**
     * Constructor for Jackson deserialization
     */
    @JsonCreator
    public BehaviorTypeDTO(@JsonProperty("id") Long id,
                           @JsonProperty("name") String name,
                           @JsonProperty("pointValue") Integer pointValue,
                           @JsonProperty("active") Boolean active
    ) {
        this.id = id;
        this.name = name;
        this.pointValue = pointValue;
        this.active = active;
    }

    /**
     * Constructs a BehaviorTypeDTO from a Behavior Type entity
     *
     * @param behaviorType Source behavior type entity
     */
    public BehaviorTypeDTO(BehaviorType behaviorType) {
        this.id = behaviorType.getId();
        this.name = behaviorType.getName();
        this.pointValue = behaviorType.getPointValue();
        this.active = behaviorType.getActive();
    }
}
