package com.bearpoints.api.unit.dto;

import com.bearpoints.api.dto.TeacherDTO;
import com.bearpoints.api.dto.UserDTO;
import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.Teacher;
import com.bearpoints.api.entity.User;
import com.bearpoints.api.unit.utility.GradeLevelUtilsTests;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TeacherDTO} functionality.
 * <p>Verifies DTO-specific behavior including:
 * <ul>
 *     <li>Correct mapping from Teacher entity to DTO</li>
 *     <li>Proper field population and null handling</li>
 *     <li>JSON deserialization constructor</li>
 *     <li>Edge cases and boundary conditions</li>
 * </ul>
 * <p>Note: Detailed grade level validation logic is tested in {@link GradeLevelUtilsTests}
 *
 * @see TeacherDTO
 * @version 1.1
 * @author Dylan Mercer
 */
@DisplayName("TeacherDTO Tests")
public class TeacherDTOTests {

    @Nested
    @DisplayName("When mapping from Teacher entity")
    class WhenMappingFromTeacherEntity {
        @Test
        @DisplayName("Should correctly map all fields from Teacher entity")
        void shouldCorrectlyMapAllFieldsFromTeacherEntity() {
            Teacher teacher = createTeacher(1L, GradeLevel.FIRST);
            TeacherDTO dto = new TeacherDTO(teacher);
            assertEquals(teacher.getId(), dto.getId());
            assertEquals(teacher.getUser().getId(), dto.getUser().getId());
            assertEquals(teacher.getUser().getEmail(), dto.getUser().getEmail());
            assertEquals(teacher.getUser().getFirstName(), dto.getUser().getFirstName());
            assertEquals(teacher.getUser().getLastName(), dto.getUser().getLastName());
            assertEquals(teacher.getUser().getRole(), dto.getUser().getRole());
            assertEquals(teacher.getGrade(), dto.getGrade());
        }

        @Test
        @DisplayName("Should handle teacher with null ID")
        void shouldHandleTeacherWithNullId() {
            Teacher teacher = createTeacher(null, GradeLevel.FIRST);
            TeacherDTO dto = new TeacherDTO(teacher);
            assertNull(dto.getId());
            assertNotNull(dto.getUser());
            assertEquals(teacher.getGrade(), dto.getGrade());
        }

        @Test
        @DisplayName("Should handle teacher with null grade level")
        void shouldHandleTeacherWithNullGradeLevel() {
            Teacher teacher = createTeacher(1L, null);
            TeacherDTO dto = new TeacherDTO(teacher);
            assertNotNull(dto.getId());
            assertNotNull(dto.getUser());
            assertNull(dto.getGrade());
        }

        @Test
        @DisplayName("Should handle teacher with null user")
        void shouldHandleTeacherWithNullUser() {
            Teacher teacher = new Teacher();
            teacher.setId(1L);
            teacher.setGrade(GradeLevel.FIRST);
            TeacherDTO dto = new TeacherDTO(teacher);
            assertThat(dto.getId()).isEqualTo(teacher.getId());
            assertThat(dto.getGrade()).isEqualTo(teacher.getGrade());
            assertThat(dto.getUser()).isNull();
        }
    }

    @Nested
    @DisplayName("When using JSON creator constructor")
    class WhenUsingJSONCreatorConstructor {
        @Test
        @DisplayName("Should create TeacherDTO with all fields provided")
        void shouldCreateTeacherDTOWithAllFieldsProvided() {
            Long id = 1L;
            UserDTO user = new UserDTO(1L, "test@okcps.org", "John", "Doe", "TEACHER");
            String grade = "FIRST";
            TeacherDTO dto = new TeacherDTO(id, user, grade);
            assertThat(dto.getId()).isEqualTo(id);
            assertThat(dto.getUser()).isEqualTo(user);
            assertThat(dto.getGrade()).isEqualTo(GradeLevel.FIRST);
        }

        @Test
        @DisplayName("Should create TeacherDTO with null ID")
        void shouldCreateTeacherDTOWithNullId() {
            UserDTO user = new UserDTO(1L, "test@okcps.org", "John", "Doe", "TEACHER");
            String grade = "SECOND";
            TeacherDTO dto = new TeacherDTO(null, user, grade);
            assertThat(dto.getId()).isNull();
            assertThat(dto.getUser()).isEqualTo(user);
            assertThat(dto.getGrade()).isEqualTo(GradeLevel.SECOND);
        }

        @Test
        @DisplayName("Should create TeacherDTO with null GradeLevel")
        void shouldCreateTeacherDTOWithNullGradeLevel() {
            Long id = 1L;
            UserDTO user = new UserDTO(1L, "test@okcps.org", "John", "Doe", "TEACHER");
            TeacherDTO dto = new TeacherDTO(id, user, null);
            assertThat(dto.getId()).isEqualTo(id);
            assertThat(dto.getUser()).isEqualTo(user);
            assertThat(dto.getGrade()).isNull();
        }

        @Test
        @DisplayName("Should create TeacherDTO with all null values")
        void shouldCreateTeacherDTOWithAllNullValues() {
            TeacherDTO dto = new TeacherDTO(null, null, null);
            assertThat(dto.getId()).isNull();
            assertThat(dto.getUser()).isNull();
            assertThat(dto.getGrade()).isNull();
        }

        @Test
        @DisplayName("Should delegate grade level validation to GradeLevelUtils")
        void shouldDelegateGradeLevelValidationToGradeLevelUtils() {
            UserDTO user = new UserDTO(1L, "test@okcps.org", "John", "Doe", "TEACHER");
            TeacherDTO dto1 = new TeacherDTO(1L, user, "pre-k");
            assertThat(dto1.getGrade()).isEqualTo(GradeLevel.PRE_K);
            TeacherDTO dto2 = new TeacherDTO(2L, user, "  first ");
            assertThat(dto2.getGrade()).isEqualTo(GradeLevel.FIRST);
            TeacherDTO dto3 = new TeacherDTO(3L, user, "");
            assertThat(dto3.getGrade()).isNull();
        }

        @Test
        @DisplayName("Should propagate grade level validation exceptions from GradeLevelUtils")
        void shouldPropagateGradeLevelValidationExceptions() {
            UserDTO user = new UserDTO(1L, "test@okcps.org", "John", "Doe", "TEACHER");
            String invalidGrade = "INVALID_GRADE";
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new TeacherDTO(1L, user, invalidGrade)
            );
            assertThat(exception.getMessage()).contains("Invalid grade level: " + invalidGrade);
        }
    }

    @Nested
    @DisplayName("Object Equality and Comparison")
    class ObjectEqualityAndComparisonTests {
        @Test
        @DisplayName("Two TeacherDTOs with same field values should have equal field values")
        void twoTeacherDTOsWithSameFieldValuesShouldHaveEqualFieldValues() {
            UserDTO user1 = new UserDTO(1L, "test@okcps.org", "John", "Doe", "TEACHER");
            UserDTO user2 = new UserDTO(1L, "test@okcps.org", "John", "Doe", "TEACHER");
            TeacherDTO dto1 = new TeacherDTO(1L, user1, "FIRST");
            TeacherDTO dto2 = new TeacherDTO(1L, user2, "FIRST");
            assertThat(dto1.getId()).isEqualTo(dto2.getId());
            assertThat(dto1.getUser().getId()).isEqualTo(dto2.getUser().getId());
            assertThat(dto1.getUser().getEmail()).isEqualTo(dto2.getUser().getEmail());
            assertThat(dto1.getUser().getFirstName()).isEqualTo(dto2.getUser().getFirstName());
            assertThat(dto1.getUser().getLastName()).isEqualTo(dto2.getUser().getLastName());
            assertThat(dto1.getUser().getRole()).isEqualTo(dto2.getUser().getRole());
            assertThat(dto1.getGrade()).isEqualTo(dto2.getGrade());
        }

        @Test
        @DisplayName("TeacherDTO from entity constructor should match JSON constructor")
        void teacherDTOFromEntityConstructorShouldMatchJSONConstructor() {
            Teacher teacher = createTeacher(1L, GradeLevel.FIRST);
            TeacherDTO fromEntity = new TeacherDTO(teacher);
            UserDTO userDTO = new UserDTO(teacher.getUser().getId(), teacher.getUser().getEmail(),
                    teacher.getUser().getFirstName(), teacher.getUser().getLastName(),
                    teacher.getUser().getRole().name());
            TeacherDTO fromJSON = new TeacherDTO(teacher.getId(), userDTO, teacher.getGrade().name());
            assertThat(fromEntity.getId()).isEqualTo(fromJSON.getId());
            assertThat(fromEntity.getUser().getId()).isEqualTo(fromJSON.getUser().getId());
            assertThat(fromEntity.getUser().getEmail()).isEqualTo(fromJSON.getUser().getEmail());
            assertThat(fromEntity.getUser().getFirstName()).isEqualTo(fromJSON.getUser().getFirstName());
            assertThat(fromEntity.getUser().getLastName()).isEqualTo(fromJSON.getUser().getLastName());
            assertThat(fromEntity.getUser().getRole()).isEqualTo(fromJSON.getUser().getRole());
            assertThat(fromEntity.getGrade()).isEqualTo(fromJSON.getGrade());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    class EdgeCasesAndBoundaryConditionsTests {
        @Test
        @DisplayName("Should handle teacher with null user in entity constructor")
        void shouldHandleTeacherWithNullUserInEntityConstructor() {
            Teacher teacher = new Teacher();
            teacher.setId(1L);
            teacher.setGrade(GradeLevel.FIRST);
            // user is null
            TeacherDTO dto = new TeacherDTO(teacher);
            assertThat(dto.getId()).isEqualTo(teacher.getId());
            assertThat(dto.getGrade()).isEqualTo(GradeLevel.FIRST);
            assertThat(dto.getUser()).isNull();
        }

        @Test
        @DisplayName("Should handle all GradeLevel enum values")
        void shouldHandleAllGradeLevelEnumValues() {
            UserDTO user = new UserDTO(1L, "test@okcps.org", "John", "Doe", "TEACHER");
            for (GradeLevel gradeLevel : GradeLevel.values()) {
                TeacherDTO dto = new TeacherDTO(1L, user, gradeLevel.name());
                assertThat(dto.getGrade()).isEqualTo(gradeLevel);
            }
        }
    }

    private Teacher createTeacher(Long id, GradeLevel gradeLevel) {
        Teacher teacher = new Teacher();
        teacher.setId(id);
        User user = new User();
        user.setId(1L);
        user.setEmail("test@okcps.org");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.TEACHER);
        teacher.setUser(user);
        teacher.setGrade(gradeLevel);
        return teacher;
    }
}
