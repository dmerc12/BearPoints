package com.bearpoints.api.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link BatchUpdateRequest} functionality.
 * <p>Verifies:
 * <ul>
 *     <li>Record components are properly initialized</li>
 *     <li>Accessor methods return correct values</li>
 * </ul>
 *
 * @see BatchUpdateRequest
 * @version 1.0
 * @author Dylan Mercer
 */
public class BatchUpdateRequestTests {
    @Test
    @DisplayName("Record components are correctly initialized")
    void shouldInitializeRecordComponents() {
        List<String> data = List.of("1", "John", "Doe");
        BatchUpdateRequest request = new BatchUpdateRequest(5, data);
        assertEquals(5, request.rowNumber());
        assertEquals(data, request.data());
    }

    @Test
    @DisplayName("toString returns expected format")
    void shouldGenerateExpectedToString() {
        BatchUpdateRequest request = new BatchUpdateRequest(3, List.of("A", "B"));
        assertEquals("BatchUpdateRequest[rowNumber=3, data=[A, B]]", request.toString());
    }
}
