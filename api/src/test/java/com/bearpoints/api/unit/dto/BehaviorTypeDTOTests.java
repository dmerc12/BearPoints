package com.bearpoints.api.unit.dto;

import com.bearpoints.api.dto.BehaviorTypeDTO;
import com.bearpoints.api.entity.BehaviorType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link BehaviorTypeDTO} functionality.
 * <p>Verifies DTO-specific behavior including:
 * <ul>
 *     <li>Correct mapping from BehaviorType entity to DTO</li>
 *     <li>Proper field population and null handling</li>
 *     <li>JSON deserialization constructor</li>
 *     <li>Validation constraints</li>
 *     <li>Edge cases and boundary conditions</li>
 * </ul>
 *
 * @see BehaviorTypeDTO
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("BehaviorTypeDTO Tests")
public class BehaviorTypeDTOTests {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("When mapping from BehaviorType entity")
    class WhenMappingFromBehaviorTypeEntityTests {
        @Test
        @DisplayName("Should correctly map all fields from BehaviorType entity")
        void shouldCorrectlyMapAllFieldsFromBehaviorTypeEntity() {
            BehaviorType behavior = createBehaviorType(1L, "Acted Responsibly", 2, true);
            BehaviorTypeDTO dto = new BehaviorTypeDTO(behavior);
            assertEquals(behavior.getId(), dto.getId());
            assertEquals(behavior.getName(), dto.getName());
            assertEquals(behavior.getPointValue(), dto.getPointValue());
            assertEquals(behavior.getActive(), dto.getActive());
        }

        @Test
        @DisplayName("Should handle behavior type with null ID")
        void shouldHandleBehaviorTypeWithNullId() {
            BehaviorType behavior = createBehaviorType(null, "Acted Responsibly", 2, true);
            BehaviorTypeDTO dto = new BehaviorTypeDTO(behavior);
            assertNull(dto.getId());
            assertEquals(behavior.getName(), dto.getName());
            assertEquals(behavior.getPointValue(), dto.getPointValue());
            assertEquals(behavior.getActive(), dto.getActive());
        }
    }

    @Nested
    @DisplayName("When using JSON creator constructor")
    class WhenUsingJSONCreatorConstructorTests {
        @Test
        @DisplayName("Should create BehaviorTypeDTO with all fields provided")
        void shouldCreateBehaviorTypeDTOWithAllFieldsProvided() {
            Long id = 1L;
            String name = "Showed Self-Control";
            Integer pointValue = 3;
            Boolean active = true;
            BehaviorTypeDTO dto = new BehaviorTypeDTO(id, name, pointValue, active);
            assertThat(dto.getId()).isEqualTo(id);
            assertThat(dto.getName()).isEqualTo(name);
            assertThat(dto.getPointValue()).isEqualTo(pointValue);
            assertThat(dto.getActive()).isEqualTo(active);
        }

        @Test
        @DisplayName("Should create BehaviorTypeDTO with null ID")
        void shouldCreateBehaviorTypeDTOWithNullId() {
            String name = "Showed Self-Control";
            Integer pointValue = 3;
            Boolean active = true;
            BehaviorTypeDTO dto = new BehaviorTypeDTO(null, name, pointValue, active);
            assertThat(dto.getId()).isNull();
            assertThat(dto.getName()).isEqualTo(name);
            assertThat(dto.getPointValue()).isEqualTo(pointValue);
            assertThat(dto.getActive()).isEqualTo(active);
        }
    }

    @Nested
    @DisplayName("Validation Constraints")
    class ValidationConstraintTests {
        @Test
        @DisplayName("Should validate point value cannot be negative")
        void shouldValidatePointValueCannotBeNegative() {
            BehaviorTypeDTO dto = new BehaviorTypeDTO(1L, "Acted Responsibly", -1, true);
            Set<ConstraintViolation<BehaviorTypeDTO>> violations = validator.validate(dto);
            assertThat(violations).isNotNull();
            assertThat(violations.size()).isGreaterThan(0);
            assertThat(violations.toString()).contains("Minimum point value is 1");
        }

        @Test
        @DisplayName("Should validate point value cannot be zero")
        void shouldValidatePointValueCannotBeZero() {
            BehaviorTypeDTO dto = new BehaviorTypeDTO(1L, "Acted Responsibly", 0, true);
            Set<ConstraintViolation<BehaviorTypeDTO>> violations = validator.validate(dto);
            assertThat(violations).isNotNull();
            assertThat(violations.size()).isGreaterThan(0);
            assertThat(violations.toString()).contains("Minimum point value is 1");
        }

        @Test
        @DisplayName("Should validate point value cannot be more than five")
        void shouldValidatePointValueCannotBeMoreThanFive() {
            BehaviorTypeDTO dto = new BehaviorTypeDTO(1L, "Acted Responsibly", 6, true);
            Set<ConstraintViolation<BehaviorTypeDTO>> violations = validator.validate(dto);
            assertThat(violations).isNotNull();
            assertThat(violations.size()).isGreaterThan(0);
            assertThat(violations.toString()).contains("Maximum point value is 5");
        }

        @Test
        @DisplayName("Should validate point value cannot be null")
        void shouldValidatePointValueCannotBeNull() {
            BehaviorTypeDTO dto = new BehaviorTypeDTO(1L, "Acted Responsibly", null, true);
            Set<ConstraintViolation<BehaviorTypeDTO>> violations = validator.validate(dto);
            assertThat(violations).isNotNull();
            assertThat(violations.size()).isGreaterThan(0);
            assertThat(violations.toString()).contains("Point value is required");
        }

        @Test
        @DisplayName("Should validate name cannot be blank")
        void shouldValidateNameCannotBeBlank() {
            BehaviorTypeDTO dto = new BehaviorTypeDTO(1L, "", 5, true);
            Set<ConstraintViolation<BehaviorTypeDTO>> violations = validator.validate(dto);
            assertThat(violations).isNotNull();
            assertThat(violations.size()).isGreaterThan(0);
            assertThat(violations.toString()).contains("Behavior name is required");
        }

        @Test
        @DisplayName("Should validate name cannot be longer than 50 characters")
        void shouldValidateNameCannotBeLongerThanFiftyCharacters() {
            String longName = "a".repeat(51);
            BehaviorTypeDTO dto = new BehaviorTypeDTO(1L, longName, 5, true);
            Set<ConstraintViolation<BehaviorTypeDTO>> violations = validator.validate(dto);
            assertThat(violations).isNotNull();
            assertThat(violations.size()).isGreaterThan(0);
            assertThat(violations.toString()).contains("Name must be between 1 and 50 characters");
        }

        @Test
        @DisplayName("Should validate active cannot be null")
        void shouldValidateActiveCannotBeNull() {
            BehaviorTypeDTO dto = new BehaviorTypeDTO(1L, "Acting Responsibly", 5, null);
            Set<ConstraintViolation<BehaviorTypeDTO>> violations = validator.validate(dto);
            assertThat(violations).isNotNull();
            assertThat(violations.size()).isGreaterThan(0);
            assertThat(violations.toString()).contains("Active status is required");
        }
    }

    @Nested
    @DisplayName("Object Equality and Comparison")
    class ObjectEqualityAndComparisonTests {
        @Test
        @DisplayName("Two BehaviorTypeDTOs with same field values should have equal field values")
        void twoBehaviorTypeDTOsWithSameFieldValuesShouldHaveEqualFieldValues() {
            Long id = 1L;
            String name = "Showed Self-Control";
            Integer pointValue = 4;
            Boolean active = true;
            BehaviorTypeDTO dto1 = new BehaviorTypeDTO(id, name, pointValue, active);
            BehaviorTypeDTO dto2 = new BehaviorTypeDTO(id, name, pointValue, active);
            assertThat(dto1.getId()).isEqualTo(dto2.getId());
            assertThat(dto1.getName()).isEqualTo(dto2.getName());
            assertThat(dto1.getPointValue()).isEqualTo(dto2.getPointValue());
            assertThat(dto1.getActive()).isEqualTo(dto2.getActive());
        }

        @Test
        @DisplayName("BehaviorTypeDTO from entity constructor should match JSON constructor")
        void behaviorTypeDTOFromEntityConstructorShouldMatchJSONConstructor() {
            Long id = 1L;
            String name = "Showed Self-Control";
            Integer pointValue = 4;
            Boolean active = true;
            BehaviorType behavior = createBehaviorType(id, name, pointValue, active);
            BehaviorTypeDTO fromEntity = new BehaviorTypeDTO(behavior);
            BehaviorTypeDTO fromJSON = new BehaviorTypeDTO(id, name, pointValue, active);
            assertThat(fromEntity.getId()).isEqualTo(fromJSON.getId());
            assertThat(fromEntity.getName()).isEqualTo(fromJSON.getName());
            assertThat(fromEntity.getPointValue()).isEqualTo(fromJSON.getPointValue());
            assertThat(fromEntity.getActive()).isEqualTo(fromJSON.getActive());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    class EdgeCaseAndBoundaryConditionTests {
        @Test
        @DisplayName("Should handle max point value of 5")
        void shouldHandleMaxPointValueOf5() {
            BehaviorTypeDTO dto = new BehaviorTypeDTO(1L, "Acted Responsibly", 5, true);
            Set<ConstraintViolation<BehaviorTypeDTO>> violations = validator.validate(dto);
            assertThat(violations.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle min point value of 1")
        void shouldHandleMinPointValueOf1() {
            BehaviorTypeDTO dto = new BehaviorTypeDTO(1L, "Acted Responsibly", 1, true);
            Set<ConstraintViolation<BehaviorTypeDTO>> violations = validator.validate(dto);
            assertThat(violations.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle max name length of 50")
        void shouldHandleMaxNameLengthOf50() {
            BehaviorTypeDTO dto = new BehaviorTypeDTO(1L, "A".repeat(50), 3, true);
            Set<ConstraintViolation<BehaviorTypeDTO>> violations = validator.validate(dto);
            assertThat(violations.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should handle min name length of 1")
        void shouldHandleMinNameLengthOf1() {
            BehaviorTypeDTO dto = new BehaviorTypeDTO(1L, "A", 3, true);
            Set<ConstraintViolation<BehaviorTypeDTO>> violations = validator.validate(dto);
            assertThat(violations.size()).isEqualTo(0);
        }
    }

    /**
     * Helper method to create a BehaviorType entity with all required fields
     */
    private BehaviorType createBehaviorType(Long id, String name, Integer pointValue, Boolean active) {
        BehaviorType behavior = new BehaviorType();
        behavior.setId(id);
        behavior.setName(name);
        behavior.setPointValue(pointValue);
        behavior.setActive(active);
        return behavior;
    }
}
