package com.bearpoints.api.dto;

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
 *     <li>student - Associated student via StudentSummary</li>
 *     <li>rewardItem - Redeemed item via RewardItemSummary</li>
 * </ul>
 * @see StudentSummary
 * @see RewardItemSummary
 * @version 1.0
 * @author Dylan Mercer
 */
@Projection(name = "studentRewardSummary", types = StudentReward.class)
public interface StudentRewardSummary {
    Long getId();
    LocalDateTime getRedeemedAt();
    StudentSummary getStudent();
    RewardItemSummary getRewardItem();
}
