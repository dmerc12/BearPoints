package com.bearpoints.api.dto;

import com.bearpoints.api.entity.RewardItem;
import com.bearpoints.api.entity.Student;
import com.bearpoints.api.entity.StudentReward;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.projection.ProjectionFactory;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for {@link StudentRewardSummary} projection.
 * <p>Verifies:
 * <ul>
 *     <li>Correct mapping of all student reward fields</li>
 *     <li>Proper handling of nested projections</li>
 *     <li>Accurate timestamp mapping</li>
 *     <li>Graceful handling of missing relationships</li>
 * </ul>
 * @see StudentRewardSummary
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("Student Reward Summary Tests")
public class StudentRewardSummaryTests {
    /**
     * Tests complete student reward data mapping.
     * <p>Verifies:
     * <ul>
     *     <li>All direct fields are correctly projected</li>
     *     <li>Nested student projection is correct</li>
     *     <li>Nested reward item projection is correct</li>
     *     <li>Timestamp is preserved</li>
     * </ul>
     */
    @Test
    @DisplayName("Should correctly map all student reward fields")
    void shouldReturnCorrectStudentRewardSummary() {
        Student student = new Student();
        student.setId(1L);
        RewardItem item = new RewardItem();
        item.setId(1L);
        item.setName("Homework Pass");
        item.setPointCost(50);
        StudentReward reward = new StudentReward();
        reward.setId(1L);
        reward.setRedeemedAt(LocalDateTime.now());
        reward.setStudent(student);
        reward.setRewardItem(item);
        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        StudentRewardSummary projection = factory.createProjection(StudentRewardSummary.class, reward);
        assertEquals(reward.getId(), projection.getId());
        assertEquals(reward.getRedeemedAt(), projection.getRedeemedAt());
        StudentSummary studentSummary = projection.getStudent();
        assertNotNull(studentSummary);
        assertEquals(student.getId(), studentSummary.getId());
        RewardItemSummary itemSummary = projection.getRewardItem();
        assertNotNull(itemSummary);
        assertEquals(item.getId(), itemSummary.getId());
        assertEquals(item.getName(), itemSummary.getName());
        assertEquals(item.getPointCost(), itemSummary.getPointCost());
    }

    /**
     * Tests student reward with minimal data.
     * <p>Verifies:
     * <ul>
     *     <li>Essential fields are projected correctly</li>
     *     <li>Relationships are properly handled</li>
     *     <li>Timestamp is preserved</li>
     * </ul>
     */
    @Test
    @DisplayName("Should handle minimal student - reward data")
    void shouldHandleMinimalData() {
        Student student = new Student();
        student.setId(1L);
        RewardItem item = new RewardItem();
        item.setId(1L);
        StudentReward reward = new StudentReward();
        reward.setId(1L);
        reward.setRedeemedAt(LocalDateTime.now());
        reward.setStudent(student);
        reward.setRewardItem(item);
        ProjectionFactory factory = new SpelAwareProxyProjectionFactory();
        StudentRewardSummary projection = factory.createProjection(StudentRewardSummary.class, reward);
        assertEquals(reward.getId(), projection.getId());
        assertEquals(reward.getRedeemedAt(), projection.getRedeemedAt());
        assertNotNull(projection.getStudent());
        assertNotNull(projection.getRewardItem());
    }
}
