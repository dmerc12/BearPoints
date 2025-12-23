package com.bearpoints.api.dto;

import com.bearpoints.api.entity.RewardItem;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.Getter;

@Getter
public class RewardItemDTO {
    private final Long id;

    @NotBlank(message = "Reward item name is required")
    @Size(min = 1, max = 50, message = "Reward item name must be between 1 and 50 characters")
    private final String name;

    @NotNull(message = "Point cost is required")
    @Min(value = 0, message = "Minimum cost is 0 points")
    private final Integer pointCost;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Minimum stock quantity is 0")
    private final Integer stock;

    /**
     * Constructor for Jackson deserialization
     */
    @JsonCreator
    public RewardItemDTO(@JsonProperty("id") Long id,
                         @JsonProperty("name") String name,
                         @JsonProperty("pointCost") Integer pointCost,
                         @JsonProperty("stock") Integer stock
    ) {
        this.id = id;
        this.name = name;
        this.pointCost = pointCost;
        this.stock = stock;
    }

    /**
     * Constructs a RewardItemDTO from a Reward Item entity
     *
     * @param rewardItem Source reward item entity
     */
    public RewardItemDTO(RewardItem rewardItem) {
        this.id = rewardItem.getId();
        this.name = rewardItem.getName();
        this.pointCost = rewardItem.getPointCost();
        this.stock = rewardItem.getStock();
    }
}
