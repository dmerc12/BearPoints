package com.bearpoints.api.unit.dto;

import com.bearpoints.api.dto.ErrorResponseDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

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
            assertThat(errorResponse.getFieldErrors()).isNull();
        }

        @Test
        @DisplayName("should create ErrorResponseDTO with null message")
        void shouldCreateErrorResponseWithNullMessage() {
            ErrorResponseDTO errorResponse = new ErrorResponseDTO(null);
            assertThat(errorResponse.getMessage()).isNull();
            assertThat(errorResponse.getTimestamp()).isNotNull();
            assertThat(errorResponse.getFieldErrors()).isNull();
        }

        @Test
        @DisplayName("should create ErrorResponseDTO with empty message")
        void shouldCreateErrorResponseWithEmptyMessage() {
            ErrorResponseDTO errorResponse = new ErrorResponseDTO("");
            assertThat(errorResponse.getMessage()).isEmpty();
            assertThat(errorResponse.getTimestamp()).isNotNull();
            assertThat(errorResponse.getFieldErrors()).isNull();
        }

        @Test
        @DisplayName("should create ErrorResponseDTO with message and field errors")
        void shouldCreateErrorResponseWithMessageAndFieldErrors() {
            String errorMessage = "Validation failed";
            Map<String, String> fieldErrors = Map.of(
                    "email", "Email must be valid",
                    "firstName", "First name must be between 1 and 100 characters"
            );
            ErrorResponseDTO errorResponse = new ErrorResponseDTO(errorMessage, fieldErrors);
            assertThat(errorResponse.getMessage()).isEqualTo(errorMessage);
            assertThat(errorResponse.getTimestamp()).isNotNull();
            assertNotNull(errorResponse.getFieldErrors());
            assertThat(errorResponse.getFieldErrors()).isEqualTo(fieldErrors);
            assertThat(errorResponse.getFieldErrors().size()).isEqualTo(2);
            assertThat(errorResponse.getFieldErrors().containsKey("email"));
            assertThat(errorResponse.getFieldErrors().containsKey("firstName"));
        }

        @Test
        @DisplayName("should create ErrorResponseDTO with null message and field errors")
        void shouldCreateErrorResponseWithNullMessageAndFieldErrors() {
            Map<String, String> fieldErrors = Map.of("field", "error");
            ErrorResponseDTO errorResponse = new ErrorResponseDTO(null, fieldErrors);
            assertThat(errorResponse.getMessage()).isNull();
            assertThat(errorResponse.getTimestamp()).isNotNull();
            assertThat(errorResponse.getFieldErrors()).isEqualTo(fieldErrors);
        }

        @Test
        @DisplayName("should create ErrorResponseDTO with empty message and field errors")
        void shouldCreateErrorResponseWithEmptyMessageAndFieldErrors() {
            Map<String, String> fieldErrors = Map.of("field", "error");
            ErrorResponseDTO errorResponse = new ErrorResponseDTO("", fieldErrors);
            assertThat(errorResponse.getMessage()).isEmpty();
            assertThat(errorResponse.getTimestamp()).isNotNull();
            assertThat(errorResponse.getFieldErrors()).isEqualTo(fieldErrors);
        }

        @Test
        @DisplayName("should create ErrorResponseDTO with message and null field errors")
        void shouldCreateErrorResponseWithMessageAndNullFieldErrors() {
            String errorMessage = "Validation failed";
            ErrorResponseDTO errorResponse = new ErrorResponseDTO(errorMessage, null);
            assertThat(errorResponse.getMessage()).isEqualTo(errorMessage);
            assertThat(errorResponse.getTimestamp()).isNotNull();
            assertThat(errorResponse.getFieldErrors()).isNull();
        }

        @Test
        @DisplayName("should create ErrorResponseDTO with message and empty field errors")
        void shouldCreateErrorResponseWithMessageAndEmptyFieldErrors() {
            String errorMessage = "Validation failed";
            Map<String, String> fieldErrors = Collections.emptyMap();
            ErrorResponseDTO errorResponse = new ErrorResponseDTO(errorMessage, fieldErrors);
            assertThat(errorResponse.getMessage()).isEqualTo(errorMessage);
            assertThat(errorResponse.getTimestamp()).isNotNull();
            assertNotNull(errorResponse.getFieldErrors());
            assertThat(errorResponse.getFieldErrors()).isEqualTo(fieldErrors);
            assertThat(errorResponse.getFieldErrors().isEmpty());
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

        @Test
        @DisplayName("getFieldErrors should return correct field errors")
        void getFieldErrors_shouldReturnCorrectFieldErrors() {
            Map<String, String> expectedFieldErrors = Map.of(
                    "email", "Email is required",
                    "firstName", "First Name is required"
            );
            ErrorResponseDTO errorResponse = new ErrorResponseDTO("Validation error", expectedFieldErrors);
            Map<String, String> actualFieldErrors = errorResponse.getFieldErrors();
            assertThat(actualFieldErrors).isEqualTo(expectedFieldErrors);
        }

        @Test
        @DisplayName("getFieldErrors should return null when no field errors provided")
        void getFieldErrors_shouldReturnNullWhenNoFieldErrorsProvided() {
            ErrorResponseDTO errorResponse = new ErrorResponseDTO("Error");
            Map<String, String> fieldErrors = errorResponse.getFieldErrors();
            assertThat(fieldErrors).isNull();
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

        @Test
        @DisplayName("toString should include field errors when present")
        void toString_shouldIncludeFieldErrorsWhenPresent() {
            String message = "Validation error";
            Map<String, String> fieldErrors = Map.of("field", "error message");
            ErrorResponseDTO errorResponse = new ErrorResponseDTO(message, fieldErrors);
            String toStringResult = errorResponse.toString();
            assertThat(toStringResult).contains(message);
            assertThat(toStringResult).contains("timestamp");
            assertThat(toStringResult).contains("fieldErrors");
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

        @Test
        @DisplayName("should handle large field errors map")
        void shouldHandleLargeFieldErrorsMap() {
            Map<String, String> largeFieldErrors = new HashMap<>();
            for (int i = 0; i < 100; i++) {
                largeFieldErrors.put("key" + i, "value" + i);
            }
            ErrorResponseDTO errorResponse = new ErrorResponseDTO("Large validation errors", largeFieldErrors);
            assertNotNull(errorResponse.getFieldErrors());
            assertThat(errorResponse.getFieldErrors().size()).isEqualTo(100);
            assertThat(errorResponse.getFieldErrors().containsKey("field0"));
            assertThat(errorResponse.getFieldErrors().containsKey("field99"));
        }

        @Test
        @DisplayName("should handle nested field names in field errors")
        void shouldHandleNestedFieldNamesInFieldErrors() {
            Map<String, String> nestedFieldErrors = Map.of(
                    "user.address.street", "Street is required",
                    "user.email", "Email is invalid"
            );
            ErrorResponseDTO errorResponse = new ErrorResponseDTO("Nested validation errors", nestedFieldErrors);
            assertNotNull(errorResponse.getFieldErrors());
            assertThat(errorResponse.getFieldErrors()).isEqualTo(nestedFieldErrors);
            assertThat(errorResponse.getFieldErrors().containsKey("user.address.street"));
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
            assertDoesNotThrow(errorResponse::getFieldErrors);
        }

        @Test
        @DisplayName("should follow JavaBean naming conventions")
        void shouldFollowJavaBeanNamingConventions() {
            Map<String, String> fieldErrors = Map.of("field", "error message");
            ErrorResponseDTO errorResponse = new ErrorResponseDTO("Test", fieldErrors);
            assertThat(errorResponse.getMessage()).isInstanceOf(String.class);
            assertThat(errorResponse.getTimestamp()).isInstanceOf(LocalDateTime.class);
            assertThat(errorResponse.getFieldErrors()).isInstanceOf(Map.class);

        }
    }
}
