package com.bearpoints.api.unit.specification;

import com.bearpoints.api.criteria.RewardItemSearchCriteria;
import com.bearpoints.api.dao.RewardItemDAO;
import com.bearpoints.api.entity.RewardItem;
import com.bearpoints.api.specification.RewardItemSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link RewardItemSpecification}.
 * <p>Verifies that specification correctly builds predicates based on search criteria
 * and handles various filter combinations appropriately.
 *
 * @see RewardItemSpecification
 * @version 1.0
 * @author Dylan Mercer
 */
@DataJpaTest
@DisplayName("RewardItemSpecification Tests")
public class RewardItemSpecificationTests {
    @Autowired
    private RewardItemDAO rewardItemDAO;

    @BeforeEach
    void setUp() {
        createRewardItem("Pencil", 5, 150);
        createRewardItem("Sticker", 8, 40);
        createRewardItem("Chips", 15, 300);
        createRewardItem("Candy", 30, 1000);
    }

    @Test
    @DisplayName("Should create predicate with name criteria")
    void shouldCreatePredicateWithNameCriteria() {
        RewardItemSearchCriteria criteria = new RewardItemSearchCriteria();
        criteria.setName("ca");
        Specification<RewardItem> spec = RewardItemSpecification.withCriteria(criteria);
        Page<RewardItem> results = rewardItemDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertTrue(results.getContent().stream().anyMatch(s -> s.getName().equals("Candy")));
    }

    @Test
    @DisplayName("Should create predicate with point value range criteria")
    void shouldCreatePredicateWithPointValueRangeCriteria() {
        RewardItemSearchCriteria criteria = new RewardItemSearchCriteria();
        criteria.setMinPointCost(3);
        criteria.setMaxPointCost(10);
        Specification<RewardItem> spec = RewardItemSpecification.withCriteria(criteria);
        Page<RewardItem> results = rewardItemDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(2, results.getTotalElements());
        assertTrue(results.getContent().stream().anyMatch(s -> s.getName().equals("Pencil")));
        assertTrue(results.getContent().stream().anyMatch(s -> s.getName().equals("Sticker")));
    }

    @Test
    @DisplayName("Should create predicate with stock range criteria")
    void shouldCreatePredicateWithStockRangeCriteria() {
        RewardItemSearchCriteria criteria = new RewardItemSearchCriteria();
        criteria.setMinStock(50);
        criteria.setMaxStock(500);
        Specification<RewardItem> spec = RewardItemSpecification.withCriteria(criteria);
        Page<RewardItem> results = rewardItemDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(2, results.getTotalElements());
        assertTrue(results.getContent().stream().anyMatch(s -> s.getName().equals("Pencil")));
        assertTrue(results.getContent().stream().anyMatch(s -> s.getName().equals("Chips")));
    }

    @Test
    @DisplayName("Should create predicate with all criteria")
    void shouldCreatePredicateWithAllCriteria() {
        RewardItemSearchCriteria criteria = new RewardItemSearchCriteria();
        criteria.setName("P");
        criteria.setMinPointCost(3);
        criteria.setMaxPointCost(10);
        criteria.setMinStock(50);
        criteria.setMaxStock(500);
        Specification<RewardItem> spec = RewardItemSpecification.withCriteria(criteria);
        Page<RewardItem> results = rewardItemDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertTrue(results.getContent().stream().anyMatch(s -> s.getName().equals("Pencil")));
    }

    @Test
    @DisplayName("Should create specification with empty criteria")
    void shouldCreateSpecificationWithEmptyCriteria() {
        RewardItemSearchCriteria criteria = new RewardItemSearchCriteria();
        Specification<RewardItem> spec = RewardItemSpecification.withCriteria(criteria);
        Page<RewardItem> results = rewardItemDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(4, results.getTotalElements());
    }

    @Test
    @DisplayName("Should ignore empty string criteria")
    void shouldIgnoreEmptyStringCriteria() {
        RewardItemSearchCriteria criteria = new RewardItemSearchCriteria();
        criteria.setName(" ");
        Specification<RewardItem> spec = RewardItemSpecification.withCriteria(criteria);
        Page<RewardItem> results = rewardItemDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(4, results.getTotalElements());
    }

    @Test
    @DisplayName("Should handle case-insensitive search")
    void shouldHandleCaseInsensitiveSearch() {
        RewardItemSearchCriteria criteria = new RewardItemSearchCriteria();
        criteria.setName("PENCIL");
        Specification<RewardItem> spec = RewardItemSpecification.withCriteria(criteria);
        Page<RewardItem> results = rewardItemDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertTrue(results.getContent().stream().anyMatch(s -> s.getName().equals("Pencil")));
    }

    private void createRewardItem(String name, Integer pointCost, Integer stock) {
        RewardItem rewardItem = new RewardItem();
        rewardItem.setName(name);
        rewardItem.setPointCost(pointCost);
        rewardItem.setStock(stock);
        rewardItemDAO.save(rewardItem);
    }
}
