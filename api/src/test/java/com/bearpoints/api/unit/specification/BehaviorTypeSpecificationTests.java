package com.bearpoints.api.unit.specification;

import com.bearpoints.api.dao.BehaviorTypeDAO;
import com.bearpoints.api.criteria.BehaviorTypeSearchCriteria;
import com.bearpoints.api.entity.BehaviorType;
import com.bearpoints.api.specification.BehaviorTypeSpecification;
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
 * Unit tests for {@link BehaviorTypeSpecification}.
 * <p>Verifies that specification correctly builds predicates based on search criteria
 * and handles various filter combinations appropriately.
 *
 * @see BehaviorTypeSpecification
 * @version 1.0
 * @author Dylan Mercer
 */
@DataJpaTest
@DisplayName("BehaviorTypeSpecification Tests")
public class BehaviorTypeSpecificationTests {
    @Autowired
    private BehaviorTypeDAO behaviorTypeDAO;

    @BeforeEach
    void setUp() {
        createBehaviorType("Behaving Brilliantly", 1, true);
        createBehaviorType("Sensational Bear Time", 3, true);
        createBehaviorType("Deprecated Behavior", 5, false);
        createBehaviorType("Answered Thoughtfully", 2, true);
        createBehaviorType("Kind To Others", 5, true);
    }

    @Test
    @DisplayName("Should create predicate with name criteria")
    void shouldCreatePredicateWithNameCriteria() {
        BehaviorTypeSearchCriteria criteria = new BehaviorTypeSearchCriteria();
        criteria.setName("behav");
        Specification<BehaviorType> spec = BehaviorTypeSpecification.withCriteria(criteria);
        Page<BehaviorType> results = behaviorTypeDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(2, results.getTotalElements());
        assertTrue(results.getContent().stream()
                .anyMatch(s -> s.getName().equals("Behaving Brilliantly")));
        assertTrue(results.getContent().stream()
                .anyMatch(s -> s.getName().equals("Deprecated Behavior")));
    }

    @Test
    @DisplayName("Should create predicate with active criteria")
    void shouldCreatePredicateWithActiveCriteria() {
        BehaviorTypeSearchCriteria criteria = new BehaviorTypeSearchCriteria();
        criteria.setActive(false);
        Specification<BehaviorType> spec = BehaviorTypeSpecification.withCriteria(criteria);
        Page<BehaviorType> results = behaviorTypeDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertTrue(results.getContent().stream()
                .anyMatch(s -> s.getName().equals("Deprecated Behavior")));
    }

    @Test
    @DisplayName("Should create predicate with point value range criteria")
    void shouldCreatePredicateWithPointValueRangeCriteria() {
        BehaviorTypeSearchCriteria criteria = new BehaviorTypeSearchCriteria();
        criteria.setMinPointValue(1);
        criteria.setMaxPointValue(2);
        Specification<BehaviorType> spec = BehaviorTypeSpecification.withCriteria(criteria);
        Page<BehaviorType> results = behaviorTypeDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(2, results.getTotalElements());
        assertTrue(results.getContent().stream()
                .anyMatch(s -> s.getName().equals("Behaving Brilliantly")));
        assertTrue(results.getContent().stream()
                .anyMatch(s -> s.getName().equals("Answered Thoughtfully")));
    }

    @Test
    @DisplayName("Should create predicate with multiple criteria")
    void shouldCreatePredicateWithMultipleCriteria() {
        BehaviorTypeSearchCriteria criteria = new BehaviorTypeSearchCriteria();
        criteria.setName("brilliant");
        criteria.setActive(true);
        criteria.setMinPointValue(1);
        criteria.setMaxPointValue(2);
        Specification<BehaviorType> spec = BehaviorTypeSpecification.withCriteria(criteria);
        Page<BehaviorType> results = behaviorTypeDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertTrue(results.getContent().stream()
                .anyMatch(s -> s.getName().equals("Behaving Brilliantly")));
    }

    @Test
    @DisplayName("Should create specification with empty criteria")
    void shouldCreateSpecificationWithEmptyCriteria() {
        BehaviorTypeSearchCriteria criteria = new BehaviorTypeSearchCriteria();
        Specification<BehaviorType> spec = BehaviorTypeSpecification.withCriteria(criteria);
        Page<BehaviorType> results = behaviorTypeDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(5, results.getTotalElements());
    }

    @Test
    @DisplayName("Should ignore empty string criteria")
    void shouldIgnoreEmptyStringCriteria() {
        BehaviorTypeSearchCriteria criteria = new BehaviorTypeSearchCriteria();
        criteria.setName(" ");
        Specification<BehaviorType> spec = BehaviorTypeSpecification.withCriteria(criteria);
        Page<BehaviorType> results = behaviorTypeDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(5, results.getTotalElements());
    }

    @Test
    @DisplayName("Should handle case-insensitive search")
    void shouldHandleCaseInsensitiveSearch() {
        BehaviorTypeSearchCriteria criteria = new BehaviorTypeSearchCriteria();
        criteria.setName("BRILLIANT");
        Specification<BehaviorType> spec = BehaviorTypeSpecification.withCriteria(criteria);
        Page<BehaviorType> results = behaviorTypeDAO.findAll(spec, PageRequest.of(0, 10));
        assertEquals(1, results.getTotalElements());
        assertTrue(results.getContent().stream()
                .anyMatch(s -> s.getName().equals("Behaving Brilliantly")));
    }

    private void createBehaviorType(String name, Integer pointValue, Boolean active) {
        BehaviorType behaviorType = new BehaviorType();
        behaviorType.setName(name);
        behaviorType.setPointValue(pointValue);
        behaviorType.setActive(active);
        behaviorTypeDAO.save(behaviorType);
    }
}
