package com.bearpoints.api.dto;

import com.bearpoints.api.entity.RewardItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link RewardItemSummary} projection.
 * <p>Verifies:
 * <ul>
 *     <li>Correct mapping of all reward item fields</li>
 *     <li>Proper handling of positive and zero values</li>
 *     <li>Graceful handling of null values</li>
 * </ul>
 * @see RewardItemSummary
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("Reward Item Summary Tests")
public class RewardItemSummaryTests {
    /**
     * Tests complete reward item data mapping.
     * <p>Verifies:
     * <ul>
     *     <li>All fields are correctly projected</li>
     *     <li>Values match the source entity</li>
     *     <li>Data integrity is maintained</li>
     * </ul>
     */
    @Test
    @DisplayName("Should correctly map all reward item fields")
    void shouldReturnCorrectRewardItemSummary() {
        RewardItem item = new RewardItem();
        item.setId(1L);
        item.setName("Homework Pass");
        item.setPointCost(50);
        item.setStock(10);
        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        RewardItemSummary projection = factory.createProjection(RewardItemSummary.class, item);
        assertEquals(item.getId(), projection.getId());
        assertEquals(item.getName(), projection.getName());
        assertEquals(item.getPointCost(), projection.getPointCost());
        assertEquals(item.getStock(), projection.getStock());
    }

    /**
     * Tests reward item with zero values.
     * <p>Verifies:
     * <ul>
     *     <li>Zero point cost is properly projected</li>
     *     <li>Zero stock quantity is properly projected</li>
     *     <li>No exceptions are thrown</li>
     * </ul>
     */
    @Test
    @DisplayName("Should handle zero values correctly")
    void shouldHandleZeroValues() {
        RewardItem item = new RewardItem();
        item.setId(2L);
        item.setName("Free Item");
        item.setPointCost(0);
        item.setStock(0);
        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        RewardItemSummary projection = factory.createProjection(RewardItemSummary.class, item);
        assertEquals(item.getId(), projection.getId());
        assertEquals(item.getName(), projection.getName());
        assertEquals(item.getPointCost(), projection.getPointCost());
        assertEquals(item.getStock(), projection.getStock());
    }

    /**
     * Tests reward item with null values.
     * <p>Verifies:
     * <ul>
     *     <li>Null values are gracefully handled</li>
     *     <li>No exceptions are thrown</li>
     *     <li>Fields return null when not set</li>
     * </ul>
     */
    @Test
    @DisplayName("Should handle null values gracefully")
    void shouldHandleNullValues() {
        RewardItem item = new RewardItem();
        item.setId(1L);
        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        RewardItemSummary projection = factory.createProjection(RewardItemSummary.class, item);
        assertEquals(item.getId(), projection.getId());
        assertNull(projection.getName());
        assertNull(projection.getPointCost());
        assertNull(projection.getStock());
    }
}
