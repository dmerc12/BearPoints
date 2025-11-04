package com.bearpoints.api.dto;

import com.bearpoints.api.entity.BehaviorType;
import org.springframework.data.rest.core.config.Projection;

/**
 * Projection interface for condensed behavior type information.
 * <p>
 * Provides a summary view of BehaviorType entities with essential fields only.
 * Used in APIs where full behavior type details with synchronization metadata are not required.
 *
 * <p>Fields:
 * <ul>
 *     <li>id - Unique behavior type identifier</li>
 *     <li>name - Behavior name</li>
 *     <li>pointValue - Point value assigned to this behavior</li>
 *     <li>active - Current activation status</li>
 * </ul>
 * @version 1.0
 * @author Dylan Mercer
 */
@Projection(name = "behaviorTypeProjection", types = BehaviorType.class)
public interface BehaviorTypeProjection {
    Long getId();
    String getName();
    Integer getPointValue();
    Boolean getActive();
}
