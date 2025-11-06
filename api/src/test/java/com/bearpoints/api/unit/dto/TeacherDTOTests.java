package com.bearpoints.api.unit.dto;

import com.bearpoints.api.dto.TeacherDTO;
import com.bearpoints.api.dto.UserDTO;
import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.Teacher;
import com.bearpoints.api.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link TeacherDTO} functionality.
 * <p>Verifies:
 * <ul>
 *     <li>Correct mapping from Teacher entity to DTO</li>
 *     <li>All fields are properly populated</li>
 *     <li>Edge cases and different grade level mapping</li>
 *     <li>JSON deserialization constructor</li>
 *     <li>GradeLevel validation and conversion logic</li>
 * </ul>
 *
 * @see TeacherDTO
 * @version 1.0
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
        @DisplayName("Should correctly map PRE_K grade level")
        void shouldCorrectlyMapPRE_KGradeLevel() {
            Teacher teacher = createTeacher(1L, GradeLevel.PRE_K);
            TeacherDTO dto = new TeacherDTO(teacher);
            assertEquals(teacher.getGrade(), dto.getGrade());
        }

        @Test
        @DisplayName("Should correctly map K grade level")
        void shouldCorrectlyMapKGradeLevel() {
            Teacher teacher = createTeacher(1L, GradeLevel.K);
            TeacherDTO dto = new TeacherDTO(teacher);
            assertEquals(teacher.getGrade(), dto.getGrade());
        }

        @Test
        @DisplayName("Should correctly map FIRST grade level")
        void shouldCorrectlyMapFIRSTGradeLevel() {
            Teacher teacher = createTeacher(1L, GradeLevel.FIRST);
            TeacherDTO dto = new TeacherDTO(teacher);
            assertEquals(teacher.getGrade(), dto.getGrade());
        }

        @Test
        @DisplayName("Should correctly map SECOND grade level")
        void shouldCorrectlyMapSECONDGradeLevel() {
            Teacher teacher = createTeacher(1L, GradeLevel.SECOND);
            TeacherDTO dto = new TeacherDTO(teacher);
            assertEquals(teacher.getGrade(), dto.getGrade());
        }

        @Test
        @DisplayName("Should correctly map THIRD grade level")
        void shouldCorrectlyMapTHIRDGradeLevel() {
            Teacher teacher = createTeacher(1L, GradeLevel.THIRD);
            TeacherDTO dto = new TeacherDTO(teacher);
            assertEquals(teacher.getGrade(), dto.getGrade());
        }

        @Test
        @DisplayName("Should correctly map FOURTH grade level")
        void shouldCorrectlyMapFOURTHGradeLevel() {
            Teacher teacher = createTeacher(1L, GradeLevel.FOURTH);
            TeacherDTO dto = new TeacherDTO(teacher);
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
        @DisplayName("Should handle empty string for grade level field")
        void shouldHandleEmptyStringForGradeLevelField() {
            UserDTO user = new UserDTO(1L, "test@okcps.org", "John", "Doe", "TEACHER");
            TeacherDTO dto = new TeacherDTO(1L, user, "");
            assertThat(dto.getGrade()).isNull();
        }

        @Test
        @DisplayName("Should handle different grade level string cases")
        void shouldHandleDifferentGradeLevelStringCases() {
            UserDTO user = new UserDTO(1L, "test@okcps.org", "John", "Doe", "TEACHER");
            TeacherDTO dto1 = new TeacherDTO(1L, user, "pre_k");
            TeacherDTO dto2 = new TeacherDTO(2L, user, "first");
            TeacherDTO dto3 = new TeacherDTO(3L, user, "FOURTH");
            assertThat(dto1.getGrade()).isEqualTo(GradeLevel.PRE_K);
            assertThat(dto2.getGrade()).isEqualTo(GradeLevel.FIRST);
            assertThat(dto3.getGrade()).isEqualTo(GradeLevel.FOURTH);
        }

        @Test
        @DisplayName("Should handle whitespace grade level string as null")
        void shouldHandleWhitespaceGradLevelStringAsNull() {
            UserDTO user = new UserDTO(1L, "test@okcps.org", "John", "Doe", "TEACHER");
            TeacherDTO dto1 = new TeacherDTO(1L, user, "    ");
            TeacherDTO dto2 = new TeacherDTO(2L, user, "\t");
            TeacherDTO dto3 = new TeacherDTO(3L, user, "\n");
            assertThat(dto1.getGrade()).isNull();
            assertThat(dto2.getGrade()).isNull();
            assertThat(dto3.getGrade()).isNull();
        }

        @Test
        @DisplayName("Should handle hyphen to underscore conversion in grade levels")
        void shouldHandleHyphenToUnderscoreConversionInGradeLevels() {
            UserDTO user = new UserDTO(1L, "test@okcps.org", "John", "Doe", "TEACHER");
            TeacherDTO dto1 = new TeacherDTO(1L, user, "pre-k");
            TeacherDTO dto2 = new TeacherDTO(2L, user, "PRE-K");
            assertThat(dto1.getGrade()).isEqualTo(GradeLevel.PRE_K);
            assertThat(dto2.getGrade()).isEqualTo(GradeLevel.PRE_K);
        }

        @Test
        @DisplayName("Should throw exception for invalid grade level string")
        void shouldThrowExceptionForInvalidGradeLevelString() {
            UserDTO user = new UserDTO(1L, "test@okcps.org", "John", "Doe", "TEACHER");
            String invalidGrade = "INVALID_GRADE";
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new TeacherDTO(1L, user, invalidGrade)
            );
            assertThat(exception.getMessage()).contains("Invalid grade level: " + invalidGrade);
            assertThat(exception.getMessage()).contains("Valid values are: ");
            for (GradeLevel grade : GradeLevel.values()) {
                assertThat(exception.getMessage()).contains(grade.name());
            }
        }

        @Test
        @DisplayName("Should throw exception for malformed grade level string")
        void shouldThrowExceptionForMalformedGradeLevelString() {
            UserDTO user = new UserDTO(1L, "test@okcps.org", "John", "Doe", "TEACHER");
            String malformedGrade = "FIRST_SECOND";
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new TeacherDTO(1L, user, malformedGrade)
            );
            assertThat(exception.getMessage()).contains("Invalid grade level: " + malformedGrade);
        }

        @Test
        @DisplayName("Should trim whitespace from role string before validation")
        void shouldTrimWhitespaceFromRoleStringBeforeValidation() {
            UserDTO user = new UserDTO(1L, "test@okcps.org", "John", "Doe", "TEACHER");
            TeacherDTO dto1 = new TeacherDTO(1L, user, "   first  ");
            TeacherDTO dto2 = new TeacherDTO(2L, user, "\tsecond\t");
            TeacherDTO dto3 = new TeacherDTO(3L, user, "\nthird\n");
            assertThat(dto1.getGrade()).isEqualTo(GradeLevel.FIRST);
            assertThat(dto2.getGrade()).isEqualTo(GradeLevel.SECOND);
            assertThat(dto3.getGrade()).isEqualTo(GradeLevel.THIRD);
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

        @Test
        @DisplayName("TeacherDTOs with different grade levels should have different grade level values")
        void teacherDTOsWithDifferentGradLevelsShouldHaveDifferentGradLevelValues() {
            UserDTO user = new UserDTO(1L, "test@okcps.org", "John", "Doe", "TEACHER");
            TeacherDTO preKDTO = new TeacherDTO(1L, user, "PRE_K");
            TeacherDTO firstDTO = new TeacherDTO(2L, user, "FIRST");
            TeacherDTO fourthDTO = new TeacherDTO(3L, user, "FOURTH");
            assertThat(preKDTO.getGrade()).isEqualTo(GradeLevel.PRE_K);
            assertThat(firstDTO.getGrade()).isEqualTo(GradeLevel.FIRST);
            assertThat(fourthDTO.getGrade()).isEqualTo(GradeLevel.FOURTH);
            assertThat(preKDTO.getGrade()).isNotEqualTo(firstDTO.getGrade());
            assertThat(firstDTO.getGrade()).isNotEqualTo(fourthDTO.getGrade());
            assertThat(fourthDTO.getGrade()).isNotEqualTo(preKDTO.getGrade());
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
