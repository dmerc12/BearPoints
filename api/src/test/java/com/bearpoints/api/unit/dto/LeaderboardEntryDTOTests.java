package com.bearpoints.api.unit.dto;

import com.bearpoints.api.dto.LeaderboardEntryDTO;
import com.bearpoints.api.dto.PersonDTO;
import com.bearpoints.api.entity.GradeLevel;
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
 * @version 1.1
 * @author Dylan Mercer
 */
public class LeaderboardEntryDTOTests {
    @Test
    @DisplayName("Constructor initializes all fields correctly")
    void shouldInitializeAllFieldsViaConstructor() {
        LeaderboardEntryDTO dto = new LeaderboardEntryDTO(
                1,
                new PersonDTO(
                        101L,
                        "John",
                        "Smith"
                ),
                new PersonDTO(
                        203L,
                        "John",
                        "Doe"
                ),
                GradeLevel.THIRD,
                150
        );
        assertEquals(1, dto.getRank());
        assertEquals(101L, dto.getStudent().getId());
        assertEquals("John", dto.getStudent().getFirstName());
        assertEquals("Smith", dto.getStudent().getLastName());
        assertEquals("John", dto.getTeacher().getFirstName());
        assertEquals("Doe", dto.getTeacher().getLastName());
        assertEquals(GradeLevel.THIRD, dto.getGrade());
        assertEquals(150, dto.getPoints());
    }
}
