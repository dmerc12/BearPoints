package com.bearpoints.api.unit.dto;

import com.bearpoints.api.dto.UserDTO;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
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
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link UserDTO} functionality.
 * <p>Verifies:
 * <ul>
 *     <li>Correct mapping from User entity to DTO</li>
 *     <li>All fields are properly populated</li>
 *     <li>Edge cases and different role mapping</li>
 *     <li>JSON deserialization constructor</li>
 *     <li>Validation constraints</li>
 *     <li>Role validation and conversion logic</li>
 * </ul>
 *
 * @see UserDTO
 * @version 1.4
 * @author Dylan Mercer
 */
@DisplayName("UserDTO Tests")
public class UserDTOTests {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("When mapping from User entity")
    class WhenMappingFromUserEntity {
        @Test
        @DisplayName("Should correctly maps all fields from User entity")
        void shouldMapAllFieldsFromUserEntity() {
            User user = createUser(1L, "test.user@example.com", "Test", "User", Role.TEACHER);
            UserDTO dto = new UserDTO(user);
            assertEquals(user.getId(), dto.getId());
            assertEquals(user.getEmail(), dto.getEmail());
            assertEquals(user.getFirstName(), dto.getFirstName());
            assertEquals(user.getLastName(), dto.getLastName());
            assertEquals(user.getRole(), dto.getRole());
        }

        @Test
        @DisplayName("Should correctly map STUDENT role")
        void shouldCorrectlyMapStudentRole() {
            User user = createUser(2L, "student@okcps.org", "John", "Doe", Role.STUDENT);
            UserDTO dto = new UserDTO(user);
            assertEquals(user.getId(), dto.getId());
            assertEquals(user.getFirstName(), dto.getFirstName());
            assertEquals(user.getLastName(), dto.getLastName());
            assertEquals(user.getEmail(),  dto.getEmail());
            assertEquals(user.getRole(), dto.getRole());
        }

        @Test
        @DisplayName("Should correctly map TEACHER role")
        void shouldCorrectlyMapTeacherRole() {
            User user = createUser(3L, "teacher@okcps.org", "John", "Doe", Role.TEACHER);
            UserDTO dto = new UserDTO(user);
            assertEquals(user.getId(), dto.getId());
            assertEquals(user.getFirstName(), dto.getFirstName());
            assertEquals(user.getLastName(), dto.getLastName());
            assertEquals(user.getEmail(),  dto.getEmail());
            assertEquals(user.getRole(), dto.getRole());
        }

        @Test
        @DisplayName("Should correctly map ADMIN role")
        void shouldCorrectlyMapAdminRole() {
            User user = createUser(4L, "admin@okcps.org", "John", "Doe", Role.ADMIN);
            UserDTO dto = new UserDTO(user);
            assertEquals(user.getId(), dto.getId());
            assertEquals(user.getFirstName(), dto.getFirstName());
            assertEquals(user.getLastName(), dto.getLastName());
            assertEquals(user.getEmail(),  dto.getEmail());
            assertEquals(user.getRole(), dto.getRole());
        }

        @Test
        @DisplayName("Should correctly map STAFF role")
        void shouldCorrectlyMapStaffRole() {
            User user = createUser(5L, "staff@okcps.org", "John", "Doe", Role.STAFF);
            UserDTO dto = new UserDTO(user);
            assertEquals(user.getId(), dto.getId());
            assertEquals(user.getFirstName(), dto.getFirstName());
            assertEquals(user.getLastName(), dto.getLastName());
            assertEquals(user.getEmail(),  dto.getEmail());
            assertEquals(user.getRole(), dto.getRole());
        }

        @Test
        @DisplayName("Should handle user with null ID")
        void shouldHandleUserWithNullId() {
            User user = createUser(null, "user@okcps.org", "First", "Last", Role.STUDENT);
            UserDTO dto = new UserDTO(user);
            assertNull(dto.getId());
            assertEquals(user.getEmail(), dto.getEmail());
            assertEquals(user.getFirstName(), dto.getFirstName());
            assertEquals(user.getLastName(), dto.getLastName());
            assertEquals(user.getRole(), dto.getRole());
        }
    }

    @Nested
    @DisplayName("When using JSON creator constructor")
    class WhenUsingJSONCreatorConstructor {
        @Test
        @DisplayName("Should create UserDTO with all fields provided")
        void shouldCreateUserDTOWithAllFieldsProvided() {
            Long id = 1L;
            String email = "test@okcps.org";
            String firstName = "John";
            String lastName = "Doe";
            String role = "TEACHER";
            UserDTO dto = new UserDTO(id, email, firstName, lastName, role);
            assertThat(dto.getId()).isEqualTo(id);
            assertThat(dto.getEmail()).isEqualTo(email);
            assertThat(dto.getFirstName()).isEqualTo(firstName);
            assertThat(dto.getLastName()).isEqualTo(lastName);
            assertThat(dto.getRole()).isEqualTo(Role.TEACHER);
        }

        @Test
        @DisplayName("Should create UserDTO with null ID")
        void shouldCreateUserDTOWithNullId() {
            String email = "test@okcps.org";
            String firstName = "John";
            String lastName = "Doe";
            String role = "TEACHER";
            UserDTO dto = new UserDTO(null, email, firstName, lastName, role);
            assertThat(dto.getId()).isNull();
            assertThat(dto.getEmail()).isEqualTo(email);
            assertThat(dto.getFirstName()).isEqualTo(firstName);
            assertThat(dto.getLastName()).isEqualTo(lastName);
            assertThat(dto.getRole()).isEqualTo(Role.TEACHER);
        }

        @Test
        @DisplayName("Should create UserDTO with null role")
        void shouldCreateUserDTOWithNullRole() {
            Long id = 1L;
            String email = "test@okcps.org";
            String firstName = "John";
            String lastName = "Doe";
            UserDTO dto = new UserDTO(id, email, firstName, lastName, null);
            assertThat(dto.getId()).isEqualTo(id);
            assertThat(dto.getEmail()).isEqualTo(email);
            assertThat(dto.getFirstName()).isEqualTo(firstName);
            assertThat(dto.getLastName()).isEqualTo(lastName);
            assertThat(dto.getRole()).isNull();
        }

        @Test
        @DisplayName("Should create UserDTO with all null values")
        void shouldCreateUserDTOWithAllNullValues() {
            UserDTO dto = new UserDTO(null, null, null, null, null);
            assertThat(dto.getId()).isNull();
            assertThat(dto.getEmail()).isNull();
            assertThat(dto.getFirstName()).isNull();
            assertThat(dto.getLastName()).isNull();
            assertThat(dto.getRole()).isNull();
        }

        @Test
        @DisplayName("Should handle empty strings for all fields")
        void shouldHandleEmptyStringsForAllFields() {
            Long id = 1L;
            String email = "";
            String firstName = "";
            String lastName = "";
            String role = "";
            UserDTO dto = new UserDTO(id, email, firstName, lastName, role);
            assertThat(dto.getId()).isEqualTo(id);
            assertThat(dto.getEmail()).isEmpty();
            assertThat(dto.getFirstName()).isEmpty();
            assertThat(dto.getLastName()).isEmpty();
            assertThat(dto.getRole()).isNull();
        }

        @Test
        @DisplayName("Should handle different role string cases")
        void shouldHandleDifferentRoleStringCases() {
            UserDTO dto1 = new UserDTO(1L, "test@okcps.org", "John", "Doe", "student");
            UserDTO dto2 = new UserDTO(2L, "test2@okcps.org", "Jane", "Smith", "teacher");
            UserDTO dto3 = new UserDTO(3L, "test3@okcps.org", "Bob", "Johnson", "admin");
            UserDTO dto4 = new UserDTO(4L, "test4@okcps.org", "Greg", "Eastman", "staff");
            assertThat(dto1.getRole()).isEqualTo(Role.STUDENT);
            assertThat(dto2.getRole()).isEqualTo(Role.TEACHER);
            assertThat(dto3.getRole()).isEqualTo(Role.ADMIN);
            assertThat(dto4.getRole()).isEqualTo(Role.STAFF);
        }

        @Test
        @DisplayName("Should handle whitespace role string as null")
        void shouldHandleWhitespaceRoleStringAsNull() {
            UserDTO dto = new UserDTO(1L, "test@okcps.org", "John", "Doe", "  ");
            assertThat(dto.getRole()).isNull();
        }

        @Test
        @DisplayName("Should handle different role string caps cases")
        void shouldHandleDifferentRoleStringCapsCases() {
            UserDTO dto1 = new UserDTO(1L, "test1@okcps.org", "John", "Doe", "student");
            UserDTO dto2 = new UserDTO(2L, "test2@okcps.org", "John", "Doe", "Teacher");
            UserDTO dto3 = new UserDTO(3L, "test3@okcps.org", "John", "Doe", "ADMIN");
            UserDTO dto4 = new UserDTO(4L, "test4@okcps.org", "John", "Doe", "Staff");
            assertThat(dto1.getRole()).isEqualTo(Role.STUDENT);
            assertThat(dto2.getRole()).isEqualTo(Role.TEACHER);
            assertThat(dto3.getRole()).isEqualTo(Role.ADMIN);
            assertThat(dto4.getRole()).isEqualTo(Role.STAFF);
        }

        @Test
        @DisplayName("Should throw exception for invalid role string")
        void shouldThrowExceptionForInvalidRoleString() {
            String invalidRole = "INVALID ROLE";
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new UserDTO(1L, "test@okcps.org", "John", "Doe", invalidRole)
            );
            assertThat(exception.getMessage()).contains("Invalid role: " + invalidRole);
            assertThat(exception.getMessage()).contains("Valid values are: ");
            for (Role role : Role.values()) {
                assertThat(exception.getMessage()).contains(role.name());
            }
        }

        @Test
        @DisplayName("Should throw exception for malformed role string")
        void shouldThrowExceptionForMalformedRoleString() {
            String malformedRole = "STUDENT_TEACHER";
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new UserDTO(1L, "test@okcps.org", "John", "Doe", malformedRole)
            );
            assertThat(exception.getMessage()).contains("Invalid role: " + malformedRole);
        }

        @Test
        @DisplayName("Should trim whitespace from role string before validation")
        void shouldTrimWhitespaceFromRoleStringBeforeValidation() {
            UserDTO dto1 = new UserDTO(1L, "test@okcps.org", "John", "Doe", "   student   ");
            UserDTO dto2 = new UserDTO(2L, "test2@okcps.org", "John", "Doe", "\tteacher\t");
            UserDTO dto3 = new UserDTO(3L, "test3@okcps.org", "John", "Doe", "\nadmin\n");
            UserDTO dto4 = new UserDTO(4L, "test4@okcps.org", "John", "Doe", " staff ");
            assertThat(dto1.getRole()).isEqualTo(Role.STUDENT);
            assertThat(dto2.getRole()).isEqualTo(Role.TEACHER);
            assertThat(dto3.getRole()).isEqualTo(Role.ADMIN);
            assertThat(dto4.getRole()).isEqualTo(Role.STAFF);
        }
    }

    @Nested
    @DisplayName("When validating field consistency")
    class WhenValidatingFieldConsistency {
        @Test
        @DisplayName("Should maintain consistent field values across multiple instances")
        void shouldMaintainConsistentFieldValuesAcrossMultipleInstances() {
            User user = createUser(5L, "consistent@okcps.org", "Consistent", "User", Role.TEACHER);
            UserDTO dto1 = new UserDTO(user);
            UserDTO dto2 = new UserDTO(user);
            assertEquals(dto1.getId(), dto2.getId());
            assertEquals(dto1.getFirstName(), dto2.getFirstName());
            assertEquals(dto1.getLastName(), dto2.getLastName());
            assertEquals(dto1.getEmail(), dto2.getEmail());
            assertEquals(dto1.getRole(), dto2.getRole());
        }

        @Test
        @DisplayName("Should reflect changes in underlying user entity")
        void shouldReflectChangesInUnderlyingUserEntity() {
            String originalEmail = "original@okps.org";
            String originalName = "Original";
            Role originalRole = Role.STUDENT;
            User user = createUser(6L, originalEmail, originalName, originalName, originalRole);
            UserDTO originalDTO = new UserDTO(user);
            String updatedName = "Updated";
            String updatedEmail = "updated@okcps.org";
            Role updatedRole = Role.TEACHER;
            user.setFirstName(updatedName);
            user.setLastName(updatedName);
            user.setEmail(updatedEmail);
            user.setRole(updatedRole);
            UserDTO updatedDTO = new UserDTO(user);
            assertEquals(originalDTO.getId(), updatedDTO.getId());
            assertEquals(updatedName, updatedDTO.getFirstName());
            assertEquals(updatedName, updatedDTO.getLastName());
            assertEquals(updatedEmail, updatedDTO.getEmail());
            assertEquals(updatedRole, updatedDTO.getRole());
            assertEquals(originalName, originalDTO.getFirstName());
            assertEquals(originalName, originalDTO.getLastName());
            assertEquals(originalEmail, originalDTO.getEmail());
            assertEquals(originalRole, originalDTO.getRole());
        }
    }

    @Nested
    @DisplayName("Validation Constraints")
    class ValidationConstraintsTests {
        @Test
        @DisplayName("Should validate email pattern constraint")
        void shouldValidateEmailPatternConstraint() {
            UserDTO dto = new UserDTO(1L, "invalid-email@gmail.com", "John", "Doe", "TEACHER");
            Set<ConstraintViolation<UserDTO>> violations = validator.validate(dto);
            assertThat(violations).isNotNull();
            assertThat(violations.size()).isGreaterThan(0);
            assertThat(violations.toString()).contains("Email must be @okcps.org domain");
        }

        @Test
        @DisplayName("Should accept valid okcps.org email")
        void shouldAcceptValidOkcpsOrgEmail() {
            UserDTO dto = new UserDTO(1L, "valid@okcps.org", "John", "Doe", "TEACHER");
            Set<ConstraintViolation<UserDTO>> violations = validator.validate(dto);
            boolean hasEmailValidation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
            assertThat(hasEmailValidation).isFalse();
        }

        @Test
        @DisplayName("Should validate firstName length constraints")
        void shouldValidateFirstNameLengthConstraints() {
            UserDTO emptyFirstName = new UserDTO(1L, "test@okcps.org", "", "Doe", "TEACHER");
            UserDTO longFirstName = new UserDTO(2L, "test2@okcps.org", "A".repeat(101), "Doe", "TEACHER");
            Set<ConstraintViolation<UserDTO>> violations1 = validator.validate(emptyFirstName);
            Set<ConstraintViolation<UserDTO>> violations2 = validator.validate(longFirstName);
            assertThat(violations1).isNotNull();
            assertThat(violations1.size()).isGreaterThan(0);
            assertThat(violations1.toString()).contains("First name must be between 1 and 100 characters");
            assertThat(violations2).isNotNull();
            assertThat(violations2.size()).isGreaterThan(0);
            assertThat(violations2.toString()).contains("First name must be between 1 and 100 characters");
        }

        @Test
        @DisplayName("Should validate lastName length constraints")
        void shouldValidateLastNameLengthConstraints() {
            UserDTO emptyLastName = new UserDTO(1L, "test@okcps.org", "John", "", "TEACHER");
            UserDTO longLastName = new UserDTO(2L, "test2@okcps.org", "John", "A".repeat(101), "TEACHER");
            Set<ConstraintViolation<UserDTO>> violations1 = validator.validate(emptyLastName);
            Set<ConstraintViolation<UserDTO>> violations2 = validator.validate(longLastName);
            assertThat(violations1).isNotNull();
            assertThat(violations1.size()).isGreaterThan(0);
            assertThat(violations1.toString()).contains("Last name must be between 1 and 100 characters");
            assertThat(violations2).isNotNull();
            assertThat(violations2.size()).isGreaterThan(0);
            assertThat(violations2.toString()).contains("Last name must be between 1 and 100 characters");
        }

        @Test
        @DisplayName("Should accept valid firstName and lastName lengths")
        void shouldAcceptValidFirstNameAndLastNameLengths() {
            UserDTO dto = new UserDTO(1L, "test@okcps.org", "John", "Doe", "TEACHER");
            Set<ConstraintViolation<UserDTO>> violations = validator.validate(dto);
            boolean hasNameViolations = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("firstName")
                            || v.getPropertyPath().toString().equals("lastName"));
            assertThat(hasNameViolations).isFalse();
        }

        @Test
        @DisplayName("Should validate role constraints")
        void shouldValidateRoleConstraints() {
            UserDTO dto = new UserDTO(1L, "test@okcps.org", "John", "Doe", null);
            Set<ConstraintViolation<UserDTO>> violations = validator.validate(dto);
            boolean hasRoleViolations = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("role"));
            assertThat(hasRoleViolations).isTrue();
        }


        @Test
        @DisplayName("Should handle null values in validation")
        void shouldHandleNullValuesInValidation() {
            UserDTO dto = new UserDTO(null, "", "", "", null);
            Set<ConstraintViolation<UserDTO>> violations = validator.validate(dto);
            assertThat(violations).isNotNull();
            assertThat(violations.size()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Object Equality and Comparison")
    class ObjectEqualityAndComparisonTests {
        @Test
        @DisplayName("Two UserDTOs with same field values should have equal field values")
        void twoUserDTOsWithSameFieldValuesShouldHaveEqualFieldValues() {
            UserDTO dto1 = new UserDTO(1L, "test@okcps.org", "John", "Doe", "TEACHER");
            UserDTO dto2 = new UserDTO(1L, "test@okcps.org", "John", "Doe", "TEACHER");
            assertThat(dto1.getId()).isEqualTo(dto2.getId());
            assertThat(dto1.getFirstName()).isEqualTo(dto2.getFirstName());
            assertThat(dto1.getLastName()).isEqualTo(dto2.getLastName());
            assertThat(dto1.getEmail()).isEqualTo(dto2.getEmail());
            assertThat(dto1.getRole()).isEqualTo(dto2.getRole());
        }

        @Test
        @DisplayName("UserDTO from entity constructor should match JSON constructor")
        void userDTOFromEntityConstructorShouldMatchJSONConstructor() {
            User user = createUser(1L, "test@okcps.org", "John", "Doe", Role.TEACHER);
            UserDTO fromEntity = new UserDTO(user);
            UserDTO fromJson = new UserDTO(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole().name());
            assertThat(fromEntity.getId()).isEqualTo(fromJson.getId());
            assertThat(fromEntity.getFirstName()).isEqualTo(fromJson.getFirstName());
            assertThat(fromEntity.getLastName()).isEqualTo(fromJson.getLastName());
            assertThat(fromEntity.getEmail()).isEqualTo(fromJson.getEmail());
            assertThat(fromEntity.getRole()).isEqualTo(fromJson.getRole());
        }

        @Test
        @DisplayName("UserDTOs with different roles should have different role values")
        void userDTOsWithDifferentRolesShouldHaveDifferentRoleValues() {
            UserDTO studentDTO = new UserDTO(1L, "test@okcps.org", "John", "Doe", "STUDENT");
            UserDTO teacherDTO = new UserDTO(1L, "test@okcps.org", "John", "Doe", "TEACHER");
            UserDTO adminDTO = new UserDTO(1L, "test@okcps.org", "John", "Doe", "ADMIN");
            UserDTO staffDTO = new UserDTO(1L, "test@okcps.org", "John", "Doe", "STAFF");
            assertThat(studentDTO.getRole()).isEqualTo(Role.STUDENT);
            assertThat(teacherDTO.getRole()).isEqualTo(Role.TEACHER);
            assertThat(adminDTO.getRole()).isEqualTo(Role.ADMIN);
            assertThat(staffDTO.getRole()).isEqualTo(Role.STAFF);
            assertThat(studentDTO.getRole()).isNotEqualTo(teacherDTO.getRole());
            assertThat(teacherDTO.getRole()).isNotEqualTo(adminDTO.getRole());
            assertThat(adminDTO.getRole()).isNotEqualTo(staffDTO.getRole());
            assertThat(staffDTO.getRole()).isNotEqualTo(studentDTO.getRole());
        }
    }

    private User createUser(Long id, String email, String firstName, String lastName, Role role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(role);
        return user;
    }
}
