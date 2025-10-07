package com.bearpoints.api.unit.dto;

import com.bearpoints.api.dto.PersonDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link PersonDTO} functionality.
 * <p>Verifies:
 * <ul>
 *     <li>Constructor properly initializes all fields</li>
 *     <li>Getter methods return expected values</li>
 *     <li>Field-level documentation accuracy</li>
 * </ul>
 *
 * <p>Tests validate that:
 * <ul>
 *     <li>ID field stores and retrieves unique identifiers correctly</li>
 *     <li>First name field handles typical and edge cases</li>
 *     <li>Last name field behaves as documented</li>
 *     <li>All fields maintain integrity through getter access</li>
 * </ul>
 *
 * @see PersonDTO
 * @version 1.0
 * @author Dylan Mercer
 */
public class PersonDTOTests {
    /**
     * Tests complete initialization of PersonDTO through constructor.
     * <p>Verifies:
     * <ul>
     *     <li>All constructor parameters are properly assigned</li>
     *     <li>Getter methods return expected values</li>
     *     <li>Field-level integrity is maintained</li>
     * </ul>
     */
    @Test
    @DisplayName("Constructor initializes all fields correctly")
    void shouldInitializeAllFieldsViaConstructor() {
        PersonDTO dto = new PersonDTO(10L, "John", "Doe");
        assertEquals(10L, dto.getId());
        assertEquals("John", dto.getFirstName());
        assertEquals("Doe", dto.getLastName());
    }

    /**
     * Tests behavior with minimum valid values.
     * <p>Verifies:
     * <ul>
     *     <li>Minimum ID value (0) is handled correctly</li>
     *     <li>Empty name strings are accepted</li>
     *     <li>Null values are not explicitly prevented</li>
     * </ul>
     */
    @Test
    @DisplayName("Handles minimum valid values correctly")
    void shouldHandleMinValidValues() {
        PersonDTO dto = new PersonDTO(0L, "", "");
        assertEquals(0L, dto.getId());
        assertEquals("", dto.getFirstName());
        assertEquals("", dto.getLastName());
    }

    /**
     * Tests behavior with null values.
     * <p>Verifies:
     * <ul>
     *     <li>Null names are accepted without exceptions</li>
     *     <li>Getter methods return null as expected</li>
     *     <li>Documented behavior matches implementation</li>
     * </ul>
     */
    @Test
    @DisplayName("Handles null values without exceptions")
    void shouldHandleNullValues() {
        PersonDTO dto = new PersonDTO(37L, null, null);
        assertEquals(37L, dto.getId());
        assertNull(dto.getFirstName());
        assertNull(dto.getLastName());
    }

    /**
     * Tests behavior with maximum length names.
     * <p>Verifies:
     * <ul>
     *     <li>Long names are stored and retrieved correctly</li>
     *     <li>No implicit truncation occurs</li>
     *     <li>Documented behavior matches implementation</li>
     * </ul>
     */
    @Test
    @DisplayName("Handles maximum length names correctly")
    void shouldHandleLongNames() {
        String longFirstName = "A".repeat(255);
        String longLastName = "B".repeat(255);
        PersonDTO dto = new PersonDTO(89L, longFirstName, longLastName);
        assertEquals(89L, dto.getId());
        assertEquals(longFirstName, dto.getFirstName());
        assertEquals(longLastName, dto.getLastName());
    }
}
