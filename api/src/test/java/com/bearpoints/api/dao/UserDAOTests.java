package com.bearpoints.api.dao;

import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link UserDAO} data access operations.
 * <p>Verifies:
 * <ul>
 *     <li>Email-based lookups</li>
 *     <li>Internal synchronization queries</li>
 *     <li>Database constraints</li>
 * </ul>
 *
 * @see DataJpaTest
 * @see TestEntityManager
 * @version 1.0
 * @author Dylan Mercer
 */
@DataJpaTest
public class UserDAOTests {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserDAO userDAO;

    private User testUser;

    @BeforeEach
    void setup() {
        entityManager.clear();
        testUser = new User();
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@okcps.org");
        testUser.setRole(Role.ADMIN);
        testUser.setSyncedToSheets(false);
        entityManager.persist(testUser);
        entityManager.flush();
    }

    @Test
    @DisplayName("findByEmail returns correct user")
    void shouldFindUserByEmail() {
        Optional<User> result = userDAO.findByEmail(testUser.getEmail());
        assertTrue(result.isPresent());
        assertEquals(testUser.getFirstName(), result.get().getFirstName());
        assertEquals(testUser.getRole(), result.get().getRole());
    }

    @Test
    @DisplayName("findByEmail returns empty for unknown emails")
    void shouldReturnEmptyForInvalidEmails() {
        Optional<User> result = userDAO.findByEmail("unknown@okcps.org");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("findBySyncedToSheetsFalse returns unsynced users")
    void shouldReturnUnsyncedUsers() {
        List<User> result = userDAO.findBySyncedToSheetsFalse();
        assertEquals(1, result.size());
        assertEquals(testUser.getEmail(), result.getFirst().getEmail());
    }

    @Test
    @DisplayName("Saving duplicate emails throws DataIntegrityViolation")
    void shouldPreventDuplicateEmails() {
        User duplicate = new User();
        duplicate.setFirstName("Duplicate");
        duplicate.setLastName("User");
        duplicate.setEmail(testUser.getEmail());
        duplicate.setRole(Role.STUDENT);
        assertThrows(DataIntegrityViolationException.class, () -> {
            userDAO.save(duplicate);
            entityManager.flush();
        });
    }
}
