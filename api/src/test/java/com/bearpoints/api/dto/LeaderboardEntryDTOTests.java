package com.bearpoints.api.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link LeaderboardEntryDTO} functionality.
 * <p>Verifies:
 * <ul>
 *     <li>Constructor properly initializes all fields</li>
 *     <li>Getter methods return expected values</li>
 * </ul>
 *
 * @see LeaderboardEntryDTO
 * @version 1.0
 * @author Dylan Mercer
 */
public class LeaderboardEntryDTOTests {
    @Test
    @DisplayName("Constructor initializes all fields correctly")
    void shouldInitializeAllFieldsViaConstructor() {
        LeaderboardEntryDTO dto = new LeaderboardEntryDTO(
                101L,
                "John Smith",
                "John Doe",
                "5th Grade",
                150
        );
        assertEquals(101L, dto.getStudentId());
        assertEquals("John Smith", dto.getStudentName());
        assertEquals("John Doe", dto.getTeacherName());
        assertEquals("5th Grade", dto.getGrade());
        assertEquals(150, dto.getPoints());
    }
}
