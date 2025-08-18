package com.bearpoints.api.dto;

import com.bearpoints.api.entity.BehaviorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link BehaviorTypeProjection} projection.
 * <p>Verifies:
 * <ul>
 *     <li>Correct mapping of all behavior type fields</li>
 *     <li>Proper handling of active status</li>
 *     <li>Graceful handling of null values</li>
 * </ul>
 * @see BehaviorTypeProjection
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("Behavior Type Projection Tests")
public class BehaviorTypeProjectionTests {
    /**
     * Tests complete behavior type data mapping.
     * <p>Verifies:
     * <ul>
     *     <li>All direct fields are correctly projected</li>
     *     <li>Values match the source entity</li>
     *     <li>Active status is properly represented</li>
     * </ul>
     */
    @Test
    @DisplayName("Should correctly map all behavior type fields")
    void shouldReturnCorrectBehaviorTypeProjection() {
        BehaviorType behavior = new BehaviorType();
        behavior.setId(1L);
        behavior.setName("Helping others");
        behavior.setPointValue(3);
        behavior.setActive(true);
        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        BehaviorTypeProjection projection = factory.createProjection(BehaviorTypeProjection.class, behavior);
        assertEquals(behavior.getId(), projection.getId());
        assertEquals(behavior.getName(), projection.getName());
        assertEquals(behavior.getPointValue(), projection.getPointValue());
        assertEquals(behavior.getActive(), projection.getActive());
    }

    /**
     * Tests behavior type with minimum valid data.
     * <p>Verifies:
     * <ul>
     *     <li>Default values are properly projected</li>
     *     <li>Null handling for optional fields</li>
     *     <li>Active status defaults to true</li>
     * </ul>
     */
    @Test
    @DisplayName("Should handle default values correctly")
    void shouldHandleDefaultValues() {
        BehaviorType behavior = new BehaviorType();
        behavior.setId(1L);
        behavior.setName("Participation");
        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        BehaviorTypeProjection projection = factory.createProjection(BehaviorTypeProjection.class, behavior);
        assertEquals(behavior.getId(), projection.getId());
        assertEquals(behavior.getName(), projection.getName());
        assertEquals(1, projection.getPointValue());
        assertTrue(projection.getActive());
    }

    /**
     * Tests behavior type with inactive status.
     * <p>Verifies:
     * <ul>
     *     <li>Active status false is properly projected</li>
     *     <li>All other values maintain integrity</li>
     * </ul>
     */
    @Test
    @DisplayName("Should correctly map inactive behavior types")
    void shouldMapInactiveBehaviorTypes() {
        BehaviorType behavior = new BehaviorType();
        behavior.setId(1L);
        behavior.setName("Outdated behavior");
        behavior.setPointValue(2);
        behavior.setActive(false);
        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        BehaviorTypeProjection projection = factory.createProjection(BehaviorTypeProjection.class, behavior);
        assertEquals(behavior.getId(), projection.getId());
        assertEquals(behavior.getName(), projection.getName());
        assertEquals(behavior.getPointValue(), projection.getPointValue());
        assertFalse(projection.getActive());
    }
}
