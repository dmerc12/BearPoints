package com.bearpoints.api.unit.dto;

import com.bearpoints.api.dto.RewardItemDTO;
import com.bearpoints.api.entity.RewardItem;
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
 * Unit tests for {@link RewardItemDTO} functionality.
 * <p>Verifies DTO-specific behavior including:
 * <ul>
 *     <li>Correct mapping from RewardItem entity to DTO</li>
 *     <li>Proper field population and null handling</li>
 *     <li>JSON deserialization constructor</li>
 *     <li>Validation constraints</li>
 *     <li>Edge cases and boundary conditions</li>
 * </ul>
 *
 * @see RewardItemDTO
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("RewardItemDTO Tests")
public class RewardItemDTOTests {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("When mapping from RewardItem entity")
    class WhenMappingFromRewardItemEntityTests {
        @Test
        @DisplayName("Should correctly map all fields from RewardItem entity")
        void shouldCorrectlyMapAllFieldsFromRewardItemEntity() {
            RewardItem rewardItem = createRewardItem(1L, "Pencil", 3, 25);
            RewardItemDTO dto = new RewardItemDTO(rewardItem);
            assertEquals(rewardItem.getId(), dto.getId());
            assertEquals(rewardItem.getName(), dto.getName());
            assertEquals(rewardItem.getPointCost(), dto.getPointCost());
            assertEquals(rewardItem.getStock(), dto.getStock());
        }

        @Test
        @DisplayName("Should handle reward item with null ID")
        void shouldHandleRewardItemWithNullId() {
            RewardItem rewardItem = createRewardItem(null, "Pencil", 3, 25);
            RewardItemDTO dto = new RewardItemDTO(rewardItem);
            assertNull(dto.getId());
            assertEquals(rewardItem.getName(), dto.getName());
            assertEquals(rewardItem.getPointCost(), dto.getPointCost());
            assertEquals(rewardItem.getStock(), dto.getStock());
        }
    }

    @Nested
    @DisplayName("When using JSON creator constructor")
    class WhenUsingJSONCreatorConstructorTests {
        @Test
        @DisplayName("Should create RewardItemDTO with all fields provided")
        void shouldCreateRewardItemDTOWithAllFieldsProvided() {
            Long id = 1L;
            String name = "Pencil";
            Integer pointCost = 5;
            Integer stock = 25;
            RewardItemDTO dto = new RewardItemDTO(id, name, pointCost, stock);
            assertThat(dto.getId()).isEqualTo(id);
            assertThat(dto.getName()).isEqualTo(name);
            assertThat(dto.getPointCost()).isEqualTo(pointCost);
            assertThat(dto.getStock()).isEqualTo(stock);
        }

        @Test
        @DisplayName("Should create RewardItemDTO with null ID")
        void shouldCreateRewardItemDTOWithNullId() {
            String name = "Pencil";
            Integer pointCost = 5;
            Integer stock = 25;
            RewardItemDTO dto = new RewardItemDTO(null, name, pointCost, stock);
            assertThat(dto.getId()).isNull();
            assertThat(dto.getName()).isEqualTo(name);
            assertThat(dto.getPointCost()).isEqualTo(pointCost);
            assertThat(dto.getStock()).isEqualTo(stock);
        }
    }

    @Nested
    @DisplayName("Validation Constraints")
    class ValidationConstraintsTests {
        @Test
        @DisplayName("Should validate name cannot be blank")
        void shouldValidateNameCannotBeBlank() {
            RewardItemDTO dto = new RewardItemDTO(1L, "", 3, 25);
            Set<ConstraintViolation<RewardItemDTO>> violations = validator.validate(dto);
            assertThat(violations).isNotNull();
            assertThat(violations.size()).isGreaterThan(0);
            assertThat(violations.toString()).contains("Reward item name is required");
            assertThat(violations.toString()).contains("Reward item name must be between 1 and 50 characters");
        }

        @Test
        @DisplayName("Should validate name cannot be longer than 50 characters")
        void shouldValidateNameCannotBeLongerThan50Characters() {
            RewardItemDTO dto = new RewardItemDTO(1L, "A".repeat(51), 3, 25);
            Set<ConstraintViolation<RewardItemDTO>> violations = validator.validate(dto);
            assertThat(violations).isNotNull();
            assertThat(violations.size()).isGreaterThan(0);
            assertThat(violations.toString()).contains("Reward item name must be between 1 and 50 characters");
        }

        @Test
        @DisplayName("Should validate name can be 1 character")
        void shouldValidateNameCanBe1Character() {
            RewardItemDTO dto = new RewardItemDTO(1L, "A", 3, 25);
            Set<ConstraintViolation<RewardItemDTO>> violations = validator.validate(dto);
            assertThat(violations).isNotNull();
            assertThat(violations.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should validate name can be 50 characters")
        void shouldValidateNameCanBe50Characters() {
            RewardItemDTO dto = new RewardItemDTO(1L, "A".repeat(50), 3, 25);
            Set<ConstraintViolation<RewardItemDTO>> violations = validator.validate(dto);
            assertThat(violations).isNotNull();
            assertThat(violations.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should validate point cost cannot be null")
        void shouldValidatePointCostNull() {
            RewardItemDTO dto = new RewardItemDTO(1L, "Pencil", null, 25);
            Set<ConstraintViolation<RewardItemDTO>> violations = validator.validate(dto);
            assertThat(violations).isNotNull();
            assertThat(violations.size()).isGreaterThan(0);
            assertThat(violations.toString()).contains("Point cost is required");
        }

        @Test
        @DisplayName("Should validate point cost cannot be negative")
        void shouldValidatePointCostNegative() {
            RewardItemDTO dto = new RewardItemDTO(1L, "Pencil", -1, 25);
            Set<ConstraintViolation<RewardItemDTO>> violations = validator.validate(dto);
            assertThat(violations).isNotNull();
            assertThat(violations.size()).isGreaterThan(0);
            assertThat(violations.toString()).contains("Minimum cost is 0 points");
        }

        @Test
        @DisplayName("Should validate point cost can be zero")
        void shouldValidatePointCostZero() {
            RewardItemDTO dto = new RewardItemDTO(1L, "Pencil", 0, 25);
            Set<ConstraintViolation<RewardItemDTO>> violations = validator.validate(dto);
            assertThat(violations).isNotNull();
            assertThat(violations.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should validate stock cannot be null")
        void shouldValidateStockNull() {
            RewardItemDTO dto = new RewardItemDTO(1L, "Pencil", 3, null);
            Set<ConstraintViolation<RewardItemDTO>> violations = validator.validate(dto);
            assertThat(violations).isNotNull();
            assertThat(violations.size()).isGreaterThan(0);
            assertThat(violations.toString()).contains("Stock quantity is required");
        }

        @Test
        @DisplayName("Should validate stock cannot be negative")
        void shouldValidateStockNegative() {
            RewardItemDTO dto = new RewardItemDTO(1L, "Pencil", 3, -1);
            Set<ConstraintViolation<RewardItemDTO>> violations = validator.validate(dto);
            assertThat(violations).isNotNull();
            assertThat(violations.size()).isGreaterThan(0);
            assertThat(violations.toString()).contains("Minimum stock quantity is 0");
        }

        @Test
        @DisplayName("Should validate stock can be zero")
        void shouldValidateStockZero() {
            RewardItemDTO dto = new RewardItemDTO(1L, "Pencil", 3, 0);
            Set<ConstraintViolation<RewardItemDTO>> violations = validator.validate(dto);
            assertThat(violations).isNotNull();
            assertThat(violations.size()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Object Equality and Comparison")
    class ObjectEqualityAndComparisonTests {
        @Test
        @DisplayName("Two RewardItemDTOs with same field values should have equal field values")
        void twoRewardItemDTOsWithSameFieldValuesShouldHaveEqualFieldValues() {
            Long id = 1L;
            String name = "Pencil";
            Integer pointCost = 3;
            Integer stockQuantity = 25;
            RewardItemDTO dto1 = new RewardItemDTO(id, name, pointCost, stockQuantity);
            RewardItemDTO dto2 = new RewardItemDTO(id, name, pointCost, stockQuantity);
            assertThat(dto1.getId()).isEqualTo(dto2.getId());
            assertThat(dto1.getName()).isEqualTo(dto2.getName());
            assertThat(dto1.getPointCost()).isEqualTo(dto2.getPointCost());
            assertThat(dto1.getStock()).isEqualTo(dto2.getStock());
        }

        @Test
        @DisplayName("RewardItemDTO from entity constructor should match JSON constructor")
        void rewardItemDTOFromEntityConstructorShouldMatchJSONConstructor() {
            Long id = 1L;
            String name = "Pencil";
            Integer pointCost = 3;
            Integer stockQuantity = 25;
            RewardItem rewardItem = createRewardItem(id, name, pointCost, stockQuantity);
            RewardItemDTO fromEntity = new RewardItemDTO(rewardItem);
            RewardItemDTO fromJSON = new RewardItemDTO(id, name, pointCost, stockQuantity);
            assertThat(fromEntity.getId()).isEqualTo(fromJSON.getId());
            assertThat(fromEntity.getName()).isEqualTo(fromJSON.getName());
            assertThat(fromEntity.getPointCost()).isEqualTo(fromJSON.getPointCost());
            assertThat(fromEntity.getStock()).isEqualTo(fromJSON.getStock());
        }
    }

    private RewardItem createRewardItem(Long id, String name, Integer pointCost, Integer stock) {
        RewardItem rewardItem = new RewardItem();
        rewardItem.setId(id);
        rewardItem.setName(name);
        rewardItem.setPointCost(pointCost);
        rewardItem.setStock(stock);
        return rewardItem;
    }
}
