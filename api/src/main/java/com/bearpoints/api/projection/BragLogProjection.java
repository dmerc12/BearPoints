package com.bearpoints.api.projection;

import com.bearpoints.api.entity.BragLog;
import org.springframework.data.rest.core.config.Projection;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Projection interface for condensed brag log information.
 * <p>
 * Provides a summary view of BragLog entities with essential fields only.
 * Used in APIs where full brag log details with sync metadata are not required.
 *
 * <p>Fields:
 * <ul>
 *     <li>id - Unique log identifier</li>
 *     <li>student - Associated student via StudentProjection</li>
 *     <li>teacher - Associated teacher via TeacherProjection</li>
 *     <li>behaviors - Associated behaviors via BehaviorTypeProjection</li>
 *     <li>pointsGenerated - Total points earned</li>
 *     <li>notes - Optional notes</li>
 *     <li>timestamp - Creation timestamp</li>
 * </ul>
 * @see StudentProjection
 * @see TeacherProjection
 * @see BehaviorTypeProjection
 * @version 1.0
 * @author Dylan Mercer
 */
@Projection(name = "bragLogProjection", types = BragLog.class)
public interface BragLogProjection {
    Long getId();
    StudentProjection getStudent();
    TeacherProjection getTeacher();
    Set<BehaviorTypeProjection> getBehaviors();
    Integer getPointsGenerated();
    String getNotes();
    LocalDateTime getTimestamp();
}
