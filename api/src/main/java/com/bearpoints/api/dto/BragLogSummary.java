package com.bearpoints.api.dto;

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
 *     <li>student - Associated student via StudentSummary</li>
 *     <li>teacher - Associated teacher via TeacherSummary</li>
 *     <li>behaviors - Associated behaviors via BehaviorTypeSummary</li>
 *     <li>pointsGenerated - Total points earned</li>
 *     <li>notes - Optional notes</li>
 *     <li>timestamp - Creation timestamp</li>
 * </ul>
 * @see StudentSummary
 * @see TeacherSummary
 * @see BehaviorTypeSummary
 * @version 1.0
 * @author Dylan Mercer
 */
@Projection(name = "bragLogSummary", types = BragLog.class)
public interface BragLogSummary {
    Long getId();
    StudentSummary getStudent();
    TeacherSummary getTeacher();
    Set<BehaviorTypeSummary> getBehaviors();
    Integer getPointsGenerated();
    String getNotes();
    LocalDateTime getTimestamp();
}
