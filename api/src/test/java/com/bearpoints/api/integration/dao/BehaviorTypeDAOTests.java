package com.bearpoints.api.integration.dao;

import com.bearpoints.api.dao.BehaviorTypeDAO;
import com.bearpoints.api.entity.BehaviorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link BehaviorTypeDAO} data access operations.
 * <p>Verifies:
 * <ul>
 *     <li>Derived query methods</li>
 *     <li>Database constraints</li>
 *     <li>Entity persistence behavior</li>
 * </ul>
 *
 * <p>Test focus:
 * <ul>
 *     <li>Active behavior type filtering</li>
 *     <li>Internal synchronization queries</li>
 *     <li>Name-based lookups</li>
 * </ul>
 *
 * @see DataJpaTest
 * @see TestEntityManager
 * @version 1.0
 * @author Dylan Mercer
 */
@DataJpaTest
public class BehaviorTypeDAOTests {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BehaviorTypeDAO behaviorTypeDAO;

    private BehaviorType activeBehavior;

    @BeforeEach
    void setup() {
        entityManager.clear();
        entityManager.flush();
        activeBehavior = new BehaviorType();
        activeBehavior.setName("Respectful");
        activeBehavior.setPointValue(5);
        activeBehavior.setActive(true);
        activeBehavior.setSyncedToSheets(false);
        entityManager.persist(activeBehavior);
        BehaviorType inactiveBehavior = new BehaviorType();
        inactiveBehavior.setName("Kind");
        inactiveBehavior.setPointValue(2);
        inactiveBehavior.setActive(false);
        inactiveBehavior.setSyncedToSheets(false);
        entityManager.persist(inactiveBehavior);
        entityManager.flush();
    }

    @Test
    @DisplayName("findByActiveTrue returns only active behavior types")
    void shouldReturnOnlyActiveBehaviorTypes() {
        List<BehaviorType> result = behaviorTypeDAO.findByActiveTrue();
        assertEquals(1, result.size());
        assertEquals("Respectful", result.getFirst().getName());
        assertTrue(result.getFirst().getActive());
    }

    @Test
    @DisplayName("findBySyncedToSheetsFalse returns un-synchronized records")
    void shouldReturnUnsyncedBehaviorTypes() {
        activeBehavior.setSyncedToSheets(true);
        entityManager.persist(activeBehavior);
        List<BehaviorType> result = behaviorTypeDAO.findBySyncedToSheetsFalse();
        assertEquals(1, result.size());
        assertEquals("Kind", result.getFirst().getName());
    }

    @Test
    @DisplayName("findByName returns correct behavior type")
    void shouldFindBehaviorTypeByName() {
        BehaviorType result = behaviorTypeDAO.findByName("Respectful");
        assertNotNull(result);
        assertEquals(5, result.getPointValue());
        assertTrue(result.getActive());
    }

    @Test
    @DisplayName("findByName returns null for non-existent names")
    void shouldReturnNullForInvalidNames() {
        assertNull(behaviorTypeDAO.findByName("NonExistent"));
    }

    @Test
    @DisplayName("Saving duplicate names throws DataIntegrityViolation")
    void shouldPreventDuplicateBehaviorNames() {
        BehaviorType duplicate = new BehaviorType();
        duplicate.setName("Respectful");
        duplicate.setPointValue(3);
        duplicate.setActive(true);
        assertThrows(DataIntegrityViolationException.class, () -> {
            behaviorTypeDAO.save(duplicate);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Version is automatically set after persistence")
    void versionIsSetAfterPersistence() {
        BehaviorType newBehavior = new BehaviorType();
        newBehavior.setName("Version Test");
        newBehavior.setPointValue(5);
        newBehavior.setActive(true);
        assertNull(newBehavior.getVersion());
        BehaviorType saved = behaviorTypeDAO.save(newBehavior);
        assertNotNull(saved.getVersion());
        assertEquals(0L, saved.getVersion());
    }
}
