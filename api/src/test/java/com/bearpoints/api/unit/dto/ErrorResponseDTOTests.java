package com.bearpoints.api.unit.dto;

import com.bearpoints.api.dto.ErrorResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link ErrorResponseDTO}.
 *
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("ErrorResponseDTO Unit Tests")
public class ErrorResponseDTOTests {
    @Nested
    @DisplayName("Constructor")
    class ConstructorTests {
        @Test
        @DisplayName("should create ErrorResponseDTO with message and current timestamp")
        void shouldCreateErrorResponseWithMessageAndTimestamp() {
            String errorMessage = "Test error message";
            ErrorResponseDTO errorResponse = new ErrorResponseDTO(errorMessage);
            assertThat(errorResponse.getMessage()).isEqualTo(errorMessage);
            assertThat(errorResponse.getTimestamp()).isNotNull();
            assertThat(errorResponse.getTimestamp()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("should create ErrorResponseDTO with null message")
        void shouldCreateErrorResponseWithNullMessage() {
            ErrorResponseDTO errorResponse = new ErrorResponseDTO(null);
            assertThat(errorResponse.getMessage()).isNull();
            assertThat(errorResponse.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("should create ErrorResponseDTO with empty message")
        void shouldCreateErrorResponseWithEmptyMessage() {
            ErrorResponseDTO errorResponse = new ErrorResponseDTO("");
            assertThat(errorResponse.getMessage()).isEmpty();
            assertThat(errorResponse.getTimestamp()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Getters")
    class GetterTests {
        @Test
        @DisplayName("getMessage should return correct message")
        void getMessage_shouldReturnCorrectMessage() {
            String expectedMessage = "Authentication failed";
            ErrorResponseDTO errorResponse = new ErrorResponseDTO(expectedMessage);
            String actualMessage = errorResponse.getMessage();
            assertThat(actualMessage).isEqualTo(expectedMessage);
        }

        @Test
        @DisplayName("getTimestamp should return non-null timestamp")
        void getTimestamp_shouldReturnNonNullTimestamp() {
            ErrorResponseDTO errorResponse = new ErrorResponseDTO("Test message");
            LocalDateTime timestamp = errorResponse.getTimestamp();
            assertThat(timestamp).isNotNull();
            assertThat(timestamp).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("getTimestamp should return recent timestamp")
        void getTimestamp_shouldReturnRecentTimestamp() {
            LocalDateTime beforeCreation = LocalDateTime.now().minusSeconds(1);
            ErrorResponseDTO errorResponse = new ErrorResponseDTO("Test message");
            LocalDateTime afterCreation = LocalDateTime.now().plusSeconds(1);
            LocalDateTime timestamp = errorResponse.getTimestamp();
            assertThat(timestamp).isAfterOrEqualTo(beforeCreation);
            assertThat(timestamp).isBeforeOrEqualTo(afterCreation);
        }
    }

    @Nested
    @DisplayName("Immutability")
    class ImmutabilityTests {
        @Test
        @DisplayName("should be immutable - fields are final")
        void shouldBeImmutable_fieldsAreFinal() throws Exception {
            Field messageField = ErrorResponseDTO.class.getDeclaredField("message");
            Field timestampField = ErrorResponseDTO.class.getDeclaredField("timestamp");
            assertTrue(Modifier.isFinal(messageField.getModifiers()));
            assertTrue(Modifier.isFinal(timestampField.getModifiers()));
        }
    }

    @Nested
    @DisplayName("Object Behavior")
    class ObjectBehaviorTests {
        @Test
        @DisplayName("two ErrorResponseDTO objects with same message should have different timestamps")
        void twoErrorResponsesWithSameMessage_shouldHaveDifferentTimestamps() throws InterruptedException {
            String message = "Duplicate message";
            ErrorResponseDTO first = new ErrorResponseDTO(message);
            Thread.sleep(1);
            ErrorResponseDTO second = new ErrorResponseDTO(message);
            assertThat(first.getMessage()).isEqualTo(second.getMessage());
            assertThat(first.getTimestamp()).isNotEqualTo(second.getTimestamp());
        }

        @Test
        @DisplayName("toString should include message and timestamp")
        void toString_shouldIncludeMessageAndTimestamp() {
            String message = "Test error";
            ErrorResponseDTO errorResponse = new ErrorResponseDTO(message);
            String toStringResult = errorResponse.toString();
            assertThat(toStringResult).contains(message);
            assertThat(toStringResult).contains("timestamp");
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {
        @Test
        @DisplayName("should handle very long error message")
        void shouldHandleVeryLongErrorMessage() {
            String longMessage = "A".repeat(10000);
            ErrorResponseDTO errorResponse = new ErrorResponseDTO(longMessage);
            assertThat(errorResponse.getMessage()).hasSize(10000);
            assertThat(errorResponse.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("should handle special characters in message")
        void shouldHandleSpecialCharactersInMessage() {
            String messageWithSpecialChars = "Error: 404 Not Found - User 'john.doe@example.com' \n\t";
            ErrorResponseDTO errorResponse = new ErrorResponseDTO(messageWithSpecialChars);
            assertThat(errorResponse.getMessage()).isEqualTo(messageWithSpecialChars);
        }
    }

    @Nested
    @DisplayName("Serialization Compatibility")
    class SerializationCompatibilityTests {
        @Test
        @DisplayName("should have Lombok @Getter annotations working")
        void shouldHaveLombokAnnotationsWorking() {
            ErrorResponseDTO errorResponse = new ErrorResponseDTO("Test");
            assertDoesNotThrow(errorResponse::getMessage);
            assertDoesNotThrow(errorResponse::getTimestamp);
        }

        @Test
        @DisplayName("should follow JavaBean naming conventions")
        void shouldFollowJavaBeanNamingConventions() {
            ErrorResponseDTO errorResponse = new ErrorResponseDTO("Test");
            assertThat(errorResponse.getMessage()).isInstanceOf(String.class);
            assertThat(errorResponse.getTimestamp()).isInstanceOf(LocalDateTime.class);
        }
    }
}
