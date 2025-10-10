package com.bearpoints.api.integration.dao;

import com.bearpoints.api.dao.RewardItemDAO;
import com.bearpoints.api.entity.RewardItem;
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
 * Functional integration tests for {@link RewardItemDAO}.
 * <p>Verifies data access operations:
 * <ul>
 *     <li>Alphabetical ordering of items</li>
 *     <li>Item retrieval</li>
 *     <li>Synchronization status filtering</li>
 *     <li>Database constraints</li>
 * </ul>
 *
 * @see DataJpaTest
 * @see TestEntityManager
 * @version 1.0
 * @author Dylan Mercer
 */
@DataJpaTest
public class RewardItemDAOTests {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RewardItemDAO rewardItemDAO;

    private RewardItem testItem1;
    private RewardItem testItem2;

    @BeforeEach
    void setup() {
        entityManager.clear();
        testItem1 = new RewardItem();
        testItem1.setName("Book");
        testItem1.setPointCost(50);
        testItem1.setStock(10);
        entityManager.persist(testItem1);
        testItem2 = new RewardItem();
        testItem2.setName("Art Supplies");
        testItem2.setPointCost(75);
        testItem2.setStock(5);
        entityManager.persist(testItem2);
        entityManager.flush();
    }

    @Test
    @DisplayName("findAllByOrderByNameAsc returns items alphabetically")
    void shouldReturnItemsAlphabetically() {
        List<RewardItem> result = rewardItemDAO.findAllByOrderByNameAsc();
        assertEquals(testItem2.getName(), result.getFirst().getName());
        assertEquals(testItem1.getName(), result.getLast().getName());
    }

    @Test
    @DisplayName("findAll returns all reward items")
    void shouldReturnAllItems() {
        List<RewardItem> result = rewardItemDAO.findAll();
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("findBySyncedToSheetsFalse returns unsynced items")
    void shouldReturnUnsyncedItems() {
        List<RewardItem> result = rewardItemDAO.findBySyncedToSheetsFalse();
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Saving reward item persists correctly")
    void shouldSaveRewardItem() {
        RewardItem newItem = new RewardItem();
        newItem.setName("New Puzzle");
        newItem.setPointCost(40);
        newItem.setStock(15);
        RewardItem savedItem = rewardItemDAO.save(newItem);
        assertNotNull(savedItem.getId());
        assertEquals(newItem.getName(), savedItem.getName());
    }

    @Test
    @DisplayName("Saving duplicate name throws DataIntegrityViolation")
    void shouldPreventDuplicateNames() {
        RewardItem duplicate = new RewardItem();
        duplicate.setName("Book");
        duplicate.setPointCost(25);
        duplicate.setStock(8);
        assertThrows(DataIntegrityViolationException.class, () -> {
            rewardItemDAO.save(duplicate);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Version is automatically set after persistence")
    void versionIsSetAfterPersistence() {
        RewardItem newItem = new RewardItem();
        newItem.setName("Version Test");
        newItem.setPointCost(25);
        newItem.setStock(100);
        assertNull(newItem.getVersion());
        RewardItem saved = rewardItemDAO.save(newItem);
        assertNotNull(saved.getVersion());
        assertEquals(0L, saved.getVersion());
    }
}
