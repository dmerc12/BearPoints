package com.bearpoints.api.dto;

import com.bearpoints.api.entity.RewardItem;
import org.springframework.data.rest.core.config.Projection;

/**
 * Projection interface for condensed reward item information.
 * <p>
 * Provides a summary view of RewardItem entities with essential fields only.
 * Used in APIs where full reward item details with sync metadata are not required.
 *
 * <p>Fields:
 * <ul>
 *     <li>id - Unique item identifier</li>
 *     <li>name - Item name</li>
 *     <li>pointCost - Points required to redeem</li>
 *     <li>stock - Current available quantity</li>
 * </ul>
 * @version 1.0
 * @author Dylan Mercer
 */
@Projection(name = "rewardItemProjection", types = RewardItem.class)
public interface RewardItemProjection {
    Long getId();
    String getName();
    Integer getPointCost();
    Integer getStock();
}
