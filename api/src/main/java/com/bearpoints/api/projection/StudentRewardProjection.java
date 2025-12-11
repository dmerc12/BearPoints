package com.bearpoints.api.projection;

import com.bearpoints.api.entity.StudentReward;
import org.springframework.data.rest.core.config.Projection;

import java.time.LocalDateTime;

/**
 * Projection interface for condensed student reward information.
 * <p>
 * Provides a summary view of StudentReward entities with essential fields only.
 * Used in APIs where full redemption details with sync metadata are not required.
 *
 * <p>Fields:
 * <ul>
 *     <li>id - Unique redemption identifier</li>
 *     <li>redeemedAt - Timestamp of redemption</li>
 *     <li>student - Associated student via StudentProjection</li>
 *     <li>rewardItem - Redeemed item via RewardItemProjection</li>
 * </ul>
 * @see StudentProjection
 * @see RewardItemProjection
 * @version 1.0
 * @author Dylan Mercer
 */
@Projection(name = "studentRewardProjection", types = StudentReward.class)
public interface StudentRewardProjection {
    Long getId();
    LocalDateTime getRedeemedAt();
    StudentProjection getStudent();
    RewardItemProjection getRewardItem();
}
