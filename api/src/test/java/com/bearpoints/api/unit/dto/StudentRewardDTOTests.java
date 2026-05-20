package com.bearpoints.api.unit.dto;

import com.bearpoints.api.dto.StudentRewardDTO;
import com.bearpoints.api.entity.RewardItem;
import com.bearpoints.api.entity.Student;
import com.bearpoints.api.entity.StudentReward;
import com.bearpoints.api.entity.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link StudentRewardDTO} functionality.
 * <p>Tests include:
 * <ul>
 *     <li>Validation constraints for request scenarios</li>
 *     <li>Entity constructor for response scenarios</li>
 *     <li>JSON deserialization and constructor</li>
 *     <li>Edge cases and null handling</li>
 * </ul>
 *
 * @see StudentRewardDTO
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("StudentRewardDTO Tests")
public class StudentRewardDTOTests {
    private static Validator validator;

    private static Student createStudent() {
        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        Student student = new Student();
        student.setId(10L);
        student.setUser(user);
        return student;
    }

    private static RewardItem createRewardItem() {
        RewardItem rewardItem = new RewardItem();
        rewardItem.setId(20L);
        rewardItem.setName("Stickers");
        rewardItem.setPointCost(6);
        return rewardItem;
    }

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("JSON Creator Constructor Tests")
    class JSONConstructorTests {
        @Test
        @DisplayName("should create StudentRewardDTO with all fields via JSON creator")
        void shouldCreateStudentRewardDTOWithAllFieldsViaJSONCreator() {
            Long id = 1L;
            Long studentId = 10L;
            Long itemId = 22L;
            LocalDateTime timestamp = LocalDateTime.now();
            String studentName = "John Doe";
            String itemName = "Pack of Stickers";
            Integer pointsUsed = 7;
            StudentRewardDTO dto = new StudentRewardDTO(id, studentId, itemId, timestamp, studentName, itemName, pointsUsed);
            assertThat(dto.getId()).isEqualTo(id);
            assertThat(dto.getStudentId()).isEqualTo(studentId);
            assertThat(dto.getItemId()).isEqualTo(itemId);
            assertThat(dto.getTimestamp()).isEqualTo(timestamp);
            assertThat(dto.getStudentName()).isEqualTo(studentName);
            assertThat(dto.getItemName()).isEqualTo(itemName);
            assertThat(dto.getPointsUsed()).isEqualTo(pointsUsed);
        }

        @Test
        @DisplayName("should create StudentRewardDTO with minimal fields for creation request")
        void shouldCreateStudentRewardDTOWithMinimalFieldsForCreationRequest() {
            Long studentId = 10L;
            Long itemId = 22L;
            StudentRewardDTO dto = new StudentRewardDTO(null, studentId, itemId, null, null, null, null);
            assertThat(dto.getId()).isNull();
            assertThat(dto.getStudentId()).isEqualTo(studentId);
            assertThat(dto.getItemId()).isEqualTo(itemId);
            assertThat(dto.getTimestamp()).isNull();
            assertThat(dto.getStudentName()).isNull();
            assertThat(dto.getItemName()).isNull();
            assertThat(dto.getPointsUsed()).isNull();
        }
    }

    @Nested
    @DisplayName("Entity Constructor Tests")
    class EntityConstructorTests {
        @Test
        @DisplayName("Should correctly map all fields from StudentReward entity")
        void shouldCorrectlyMapAllFieldsFromStudentRewardEntity() {
            StudentReward studentReward = new StudentReward();
            studentReward.setId(1L);
            Student student = createStudent();
            studentReward.setStudent(student);
            RewardItem rewardItem = createRewardItem();
            studentReward.setRewardItem(rewardItem);
            studentReward.setRedeemedAt(LocalDateTime.now());
            StudentRewardDTO dto = new StudentRewardDTO(studentReward);
            assertThat(dto.getId()).isEqualTo(studentReward.getId());
            assertThat(dto.getStudentId()).isEqualTo(studentReward.getStudent().getId());
            assertThat(dto.getItemId()).isEqualTo(studentReward.getRewardItem().getId());
            assertThat(dto.getTimestamp()).isEqualTo(studentReward.getRedeemedAt());
            assertThat(dto.getStudentName()).isEqualTo(studentReward.getStudent().getUser().getFirstName()
                    + " " + studentReward.getStudent().getUser().getLastName());
            assertThat(dto.getItemName()).isEqualTo(studentReward.getRewardItem().getName());
            assertThat(dto.getPointsUsed()).isEqualTo(studentReward.getRewardItem().getPointCost());
        }

        @Test
        @DisplayName("Should handle null student and reward item relationships")
        void shouldHandleNullStudentAndRewardItemRelationships() {
            StudentReward studentReward = new StudentReward();
            studentReward.setId(1L);
            studentReward.setStudent(null);
            studentReward.setRewardItem(null);
            studentReward.setRedeemedAt(LocalDateTime.now());
            StudentRewardDTO dto = new StudentRewardDTO(studentReward);
            assertThat(dto.getId()).isEqualTo(studentReward.getId());
            assertThat(dto.getStudentId()).isNull();
            assertThat(dto.getItemId()).isNull();
            assertThat(dto.getTimestamp()).isEqualTo(studentReward.getRedeemedAt());
            assertThat(dto.getStudentName()).isNull();
            assertThat(dto.getItemName()).isNull();
            assertThat(dto.getPointsUsed()).isNull();
        }

        @Test
        @DisplayName("Should handle null user in student")
        void shouldHandleNullUserInStudent() {
            StudentReward studentReward = new StudentReward();
            studentReward.setId(1L);
            Student student = new Student();
            student.setId(10L);
            student.setUser(null);
            studentReward.setStudent(student);
            RewardItem rewardItem = createRewardItem();
            studentReward.setRewardItem(rewardItem);
            studentReward.setRedeemedAt(LocalDateTime.now());
            StudentRewardDTO dto = new StudentRewardDTO(studentReward);
            assertThat(dto.getId()).isEqualTo(studentReward.getId());
            assertThat(dto.getStudentId()).isEqualTo(studentReward.getStudent().getId());
            assertThat(dto.getItemId()).isEqualTo(studentReward.getRewardItem().getId());
            assertThat(dto.getTimestamp()).isEqualTo(studentReward.getRedeemedAt());
            assertThat(dto.getStudentName()).isNull();
            assertThat(dto.getItemName()).isEqualTo(studentReward.getRewardItem().getName());
            assertThat(dto.getPointsUsed()).isEqualTo(studentReward.getRewardItem().getPointCost());
        }

        @Test
        @DisplayName("Should handle null user first name in student")
        void shouldHandleNullUserFirstNameInStudent() {
            StudentReward studentReward = new StudentReward();
            studentReward.setId(1L);
            User user = new User();
            user.setFirstName(null);
            user.setLastName("Doe");
            Student student = new Student();
            student.setId(10L);
            student.setUser(user);
            studentReward.setStudent(student);
            RewardItem rewardItem = createRewardItem();
            studentReward.setRewardItem(rewardItem);
            studentReward.setRedeemedAt(LocalDateTime.now());
            StudentRewardDTO dto = new StudentRewardDTO(studentReward);
            assertThat(dto.getId()).isEqualTo(studentReward.getId());
            assertThat(dto.getStudentId()).isEqualTo(studentReward.getStudent().getId());
            assertThat(dto.getItemId()).isEqualTo(studentReward.getRewardItem().getId());
            assertThat(dto.getTimestamp()).isEqualTo(studentReward.getRedeemedAt());
            assertThat(dto.getStudentName()).isNull();
            assertThat(dto.getItemName()).isEqualTo(studentReward.getRewardItem().getName());
            assertThat(dto.getPointsUsed()).isEqualTo(studentReward.getRewardItem().getPointCost());
        }

        @Test
        @DisplayName("Should handle null user last name in student")
        void shouldHandleNullUserLastNameInStudent() {
            StudentReward studentReward = new StudentReward();
            studentReward.setId(1L);
            User user = new User();
            user.setFirstName("John");
            user.setLastName(null);
            Student student = new Student();
            student.setId(10L);
            student.setUser(user);
            studentReward.setStudent(student);
            RewardItem rewardItem = createRewardItem();
            studentReward.setRewardItem(rewardItem);
            studentReward.setRedeemedAt(LocalDateTime.now());
            StudentRewardDTO dto = new StudentRewardDTO(studentReward);
            assertThat(dto.getId()).isEqualTo(studentReward.getId());
            assertThat(dto.getStudentId()).isEqualTo(studentReward.getStudent().getId());
            assertThat(dto.getItemId()).isEqualTo(studentReward.getRewardItem().getId());
            assertThat(dto.getTimestamp()).isEqualTo(studentReward.getRedeemedAt());
            assertThat(dto.getStudentName()).isNull();
            assertThat(dto.getItemName()).isEqualTo(studentReward.getRewardItem().getName());
            assertThat(dto.getPointsUsed()).isEqualTo(studentReward.getRewardItem().getPointCost());
        }
    }

    @Nested
    @DisplayName("Validation Tests for Request Scenarios")
    class ValidationTests {
        @Test
        @DisplayName("Valid creation request passes all constraints")
        void validCreationRequestPassesAllConstraints() {
            StudentRewardDTO dto = new StudentRewardDTO(null, 1L, 2L, null,
                    null, null, null);
            Set<ConstraintViolation<StudentRewardDTO>> violations = validator.validate(dto);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Null studentId violates @NotNull constraint")
        void nullStudentIdViolatesNotNullConstraint() {
            StudentRewardDTO dto = new StudentRewardDTO(null, null, 2L, null,
                    null, null, null);
            Set<ConstraintViolation<StudentRewardDTO>> violations = validator.validate(dto);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Student ID is required");
        }

        @Test
        @DisplayName("Null itemId violates @NotNull constraint")
        void nullItemIdViolatesNotNullConstraint() {
            StudentRewardDTO dto = new StudentRewardDTO(null, 1L, null, null,
                    null, null, null);
            Set<ConstraintViolation<StudentRewardDTO>> violations = validator.validate(dto);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Item ID is required");
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    class EdgeCasesAndBoundaryConditions {
        @Test
        @DisplayName("Update request with ID passes validation")
        void updateRequestWithIdPassesValidation() {
            StudentRewardDTO dto = new StudentRewardDTO(1L, 1L, 2L, null,
                    null, null, null);
            Set<ConstraintViolation<StudentRewardDTO>> violations = validator.validate(dto);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Response fields do not affect validation for requests")
        void responseFieldsDoNotAffectValidationForRequests() {
            StudentRewardDTO dto = new StudentRewardDTO(1L, 1L, 2L, LocalDateTime.now(),
                    "John Doe", "Stencils", 18);
            Set<ConstraintViolation<StudentRewardDTO>> violations = validator.validate(dto);
            assertThat(violations).isEmpty();
        }
    }
}
