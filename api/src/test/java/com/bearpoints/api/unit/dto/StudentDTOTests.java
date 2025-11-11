package com.bearpoints.api.unit.dto;

import com.bearpoints.api.dto.StudentDTO;
import com.bearpoints.api.dto.TeacherDTO;
import com.bearpoints.api.dto.UserDTO;
import com.bearpoints.api.entity.*;
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
 * Unit tests for {@link StudentDTO} functionality.
 * <p>Verifies DTO-specific behavior including:
 * <ul>
 *     <li>Correct mapping from Student entity to DTO</li>
 *     <li>Proper field population and null handling</li>
 *     <li>JSON deserialization constructor</li>
 *     <li>Validation constraints</li>
 *     <li>Edge cases and boundary conditions</li>
 * </ul>
 *
 * @see StudentDTO
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("StudentDTO Tests")
public class StudentDTOTests {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("When mapping from Student entity")
    class WhenMappingFromStudentEntity {
        @Test
        @DisplayName("Should correctly map all fields from Student entity")
        void shouldCorrectlyMapAllFieldsFromStudentEntity() {
            Student student = createStudent(1L, "student@okcps.org", 60);
            StudentDTO dto = new StudentDTO(student);
            assertEquals(student.getId(), dto.getId());
            assertEquals(student.getPoints(), dto.getPoints());
            assertEquals(student.getToken(), dto.getToken());
            assertNotNull(dto.getUser());
            assertEquals(student.getUser().getId(), dto.getUser().getId());
            assertEquals(student.getUser().getEmail(), dto.getUser().getEmail());
            assertEquals(student.getUser().getFirstName(), dto.getUser().getFirstName());
            assertEquals(student.getUser().getLastName(), dto.getUser().getLastName());
            assertEquals(student.getUser().getRole(), dto.getUser().getRole());
            assertNotNull(dto.getTeacher());
            assertEquals(student.getTeacher().getId(), dto.getTeacher().getId());
            assertEquals(student.getTeacher().getGrade(), dto.getTeacher().getGrade());
        }

        @Test
        @DisplayName("Should handle student student with null ID")
        void shouldHandleStudentWithNullId() {
            Student student = createStudent(null, "student@okcps.org", 50);
            StudentDTO dto = new StudentDTO(student);
            assertNull(dto.getId());
            assertEquals(student.getPoints(), dto.getPoints());
            assertEquals(student.getToken(), dto.getToken());
            assertNotNull(dto.getUser());
            assertNotNull(dto.getTeacher());
        }

        @Test
        @DisplayName("Should handle student with zero points")
        void shouldHandleStudentWithZeroPoints() {
            Student student = createStudent(1L, "student@okcps.org", 0);
            StudentDTO dto = new StudentDTO(student);
            assertEquals(0, dto.getPoints());
            assertNotNull(dto.getUser());
            assertNotNull(dto.getTeacher());
        }

        @Test
        @DisplayName("Should handle student with null user")
        void shouldHandleStudentWithNullUser() {
            Student student = createStudent(1L, "test@okcps.org", 100);
            student.setUser(null);
            StudentDTO dto = new StudentDTO(student);
            assertEquals(student.getId(), dto.getId());
            assertEquals(student.getPoints(), dto.getPoints());
            assertEquals(student.getToken(), dto.getToken());
            assertNull(dto.getUser());
            assertNotNull(dto.getTeacher());
        }

        @Test
        @DisplayName("Should handle student with null teacher")
        void shouldHandleStudentWithNullTeacher() {
            Student student = createStudent(1L, "test@okcps.org", 100);
            student.setTeacher(null);
            StudentDTO dto = new StudentDTO(student);
            assertEquals(student.getId(), dto.getId());
            assertEquals(student.getPoints(), dto.getPoints());
            assertEquals(student.getToken(), dto.getToken());
            assertNotNull(dto.getUser());
            assertNull(dto.getTeacher());
        }

        @Test
        @DisplayName("Should handle student with null token")
        void shouldHandleStudentWithNullToken() {
            Student student = createStudent(1L, "test@okcps.org", 100);
            student.setToken(null);
            StudentDTO dto = new StudentDTO(student);
            assertEquals(student.getId(), dto.getId());
            assertEquals(student.getPoints(), dto.getPoints());
            assertNull(dto.getToken());
            assertNotNull(dto.getUser());
            assertNotNull(dto.getTeacher());
        }

        @Test
        @DisplayName("Should handle student with all null optional fields")
        void shouldHandleStudentWithAllNullOptionalFields() {
            Student student = new Student();
            student.setId(null);
            student.setPoints(0);
            student.setToken(null);
            student.setUser(null);
            student.setTeacher(null);
            StudentDTO dto = new StudentDTO(student);
            assertNull(dto.getId());
            assertEquals(0, dto.getPoints());
            assertNull(dto.getToken());
            assertNull(dto.getUser());
            assertNull(dto.getTeacher());
        }
    }

    @Nested
    @DisplayName("When using JSON creator constructor")
    class WhenUsingJSONCreatorConstructor {
        @Test
        @DisplayName("Should create StudentDTO with all fields provided")
        void shouldCreateStudentDTOWithAllFieldsProvided() {
            Long id = 1L;
            UserDTO user = new UserDTO(1L, "student@okcps.org", "John", "Doe", "STUDENT");
            Integer points = 150;
            String token = "json-token-123";
            TeacherDTO teacher = new TeacherDTO(1L,
                    new UserDTO(2L, "teacher@okcps.org", "Jane", "Smith", "TEACHER"),
                    "FIRST");
            StudentDTO dto = new StudentDTO(id, user, points, token, teacher);
            assertThat(dto.getId()).isEqualTo(id);
            assertThat(dto.getUser()).isEqualTo(user);
            assertThat(dto.getPoints()).isEqualTo(points);
            assertThat(dto.getToken()).isEqualTo(token);
            assertThat(dto.getTeacher()).isEqualTo(teacher);
        }

        @Test
        @DisplayName("Should create StudentDTO with null ID")
        void shouldCreateStudentDTOWithNullId() {
            UserDTO user = new UserDTO(1L, "student@okcps.org", "John", "Doe", "STUDENT");
            Integer points = 150;
            String token = "json-token-123";
            TeacherDTO teacher = new TeacherDTO(1L,
                    new UserDTO(2L, "teacher@okcps.org", "Jane", "Smith", "TEACHER"),
                    "FIRST");
            StudentDTO dto = new StudentDTO(null, user, points, token, teacher);
            assertThat(dto.getId()).isNull();
            assertThat(dto.getUser()).isEqualTo(user);
            assertThat(dto.getPoints()).isEqualTo(points);
            assertThat(dto.getToken()).isEqualTo(token);
            assertThat(dto.getTeacher()).isEqualTo(teacher);
        }

        @Test
        @DisplayName("Should create StudentDTO with null user")
        void shouldCreateStudentDTOWithNullUser() {
            Long id = 1L;
            Integer points = 150;
            String token = "json-token-123";
            TeacherDTO teacher = new TeacherDTO(1L,
                    new UserDTO(2L, "teacher@okcps.org", "Jane", "Smith", "TEACHER"),
                    "FIRST");
            StudentDTO dto = new StudentDTO(id, null, points, token, teacher);
            assertThat(dto.getId()).isEqualTo(id);
            assertThat(dto.getUser()).isNull();
            assertThat(dto.getPoints()).isEqualTo(points);
            assertThat(dto.getToken()).isEqualTo(token);
            assertThat(dto.getTeacher()).isEqualTo(teacher);
        }

        @Test
        @DisplayName("Should create StudentDTO with null points")
        void shouldCreateStudentDTOWithNullPoints() {
            Long id = 1L;
            UserDTO user = new UserDTO(1L, "student@okcps.org", "John", "Doe", "STUDENT");
            String token = "json-token-123";
            TeacherDTO teacher = new TeacherDTO(1L,
                    new UserDTO(2L, "teacher@okcps.org", "Jane", "Smith", "TEACHER"),
                    "FIRST");
            StudentDTO dto = new StudentDTO(id, user, null, token, teacher);
            assertThat(dto.getId()).isEqualTo(id);
            assertThat(dto.getUser()).isEqualTo(user);
            assertThat(dto.getPoints()).isNull();
            assertThat(dto.getToken()).isEqualTo(token);
            assertThat(dto.getTeacher()).isEqualTo(teacher);
        }

        @Test
        @DisplayName("Should create StudentDTO with null token")
        void shouldCreateStudentDTOWithNullToken() {
            Long id = 1L;
            UserDTO user = new UserDTO(1L, "student@okcps.org", "John", "Doe", "STUDENT");
            Integer points = 150;
            TeacherDTO teacher = new TeacherDTO(1L,
                    new UserDTO(2L, "teacher@okcps.org", "Jane", "Smith", "TEACHER"),
                    "FIRST");
            StudentDTO dto = new StudentDTO(id, user, points, null, teacher);
            assertThat(dto.getId()).isEqualTo(id);
            assertThat(dto.getUser()).isEqualTo(user);
            assertThat(dto.getPoints()).isEqualTo(points);
            assertThat(dto.getToken()).isNull();
            assertThat(dto.getTeacher()).isEqualTo(teacher);
        }

        @Test
        @DisplayName("Should create StudentDTO with null teacher")
        void shouldCreateStudentDTOWithNullTeacher() {
            Long id = 1L;
            UserDTO user = new UserDTO(1L, "student@okcps.org", "John", "Doe", "STUDENT");
            Integer points = 150;
            String token = "json-token-123";
            StudentDTO dto = new StudentDTO(id, user, points, token, null);
            assertThat(dto.getId()).isEqualTo(id);
            assertThat(dto.getUser()).isEqualTo(user);
            assertThat(dto.getPoints()).isEqualTo(points);
            assertThat(dto.getToken()).isEqualTo(token);
            assertThat(dto.getTeacher()).isNull();
        }

        @Test
        @DisplayName("Should create StudentDTO with all null values")
        void shouldCreateStudentDTOWithAllNullValues() {
            StudentDTO dto = new StudentDTO(null, null, null, null, null);
            assertThat(dto.getId()).isNull();
            assertThat(dto.getUser()).isNull();
            assertThat(dto.getPoints()).isNull();
            assertThat(dto.getToken()).isNull();
            assertThat(dto.getTeacher()).isNull();
        }
    }

    @Nested
    @DisplayName("Validation Constraints")
    class ValidationConstraintsTests {
        @Test
        @DisplayName("Should validate points cannot be negative")
        void shouldValidatePointsCannotBeNegative() {
            UserDTO user = new UserDTO(1L, "student@okcps.org", "John", "Doe", "STUDENT");
            TeacherDTO teacher = new TeacherDTO(1L,
                    new UserDTO(2L, "teacher@okcps.org", "Jane", "Smith", "TEACHER"),
                    "FIRST");
            StudentDTO dto = new StudentDTO(1L, user, -10, "token-negative", teacher);
            Set<ConstraintViolation<StudentDTO>> violations = validator.validate(dto);
            assertThat(violations).isNotNull();
            assertThat(violations.size()).isGreaterThan(0);
            assertThat(violations.toString()).contains("Points cannot be negative");
        }

        @Test
        @DisplayName("Should accept zero points")
        void shouldAcceptZeroPoints() {
            UserDTO user = new UserDTO(1L, "student@okcps.org", "John", "Doe", "STUDENT");
            TeacherDTO teacher = new TeacherDTO(1L,
                    new UserDTO(2L, "teacher@okcps.org", "Jane", "Smith", "TEACHER"),
                    "FIRST");
            StudentDTO dto = new StudentDTO(1L, user, 0, "token-zero", teacher);
            Set<ConstraintViolation<StudentDTO>> violations = validator.validate(dto);
            boolean hasPointsViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("points"));
            assertThat(hasPointsViolation).isFalse();
        }

        @Test
        @DisplayName("Should accept positive points")
        void shouldAcceptPositivePoints() {
            UserDTO user = new UserDTO(1L, "student@okcps.org", "John", "Doe", "STUDENT");
            TeacherDTO teacher = new TeacherDTO(1L,
                    new UserDTO(2L, "teacher@okcps.org", "Jane", "Smith", "TEACHER"),
                    "FIRST");
            StudentDTO dto = new StudentDTO(1L, user, 100, "token-positive", teacher);
            Set<ConstraintViolation<StudentDTO>> violations = validator.validate(dto);
            boolean hasPointsViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("points"));
            assertThat(hasPointsViolation).isFalse();
        }

        @Test
        @DisplayName("Should cascade validation to user field")
        void shouldCascadeValidationToUserField() {
            UserDTO invalidUser = new UserDTO(1L, "invalid-email@gmail.com", "John", "Doe", "STUDENT");
            TeacherDTO teacher = new TeacherDTO(1L,
                    new UserDTO(2L, "teacher@okcps.org", "Jane", "Smith", "TEACHER"),
                    "FIRST");
            StudentDTO dto = new StudentDTO(1L, invalidUser, 100, "token-cascade", teacher);
            Set<ConstraintViolation<StudentDTO>> violations = validator.validate(dto);
            assertThat(violations).isNotNull();
            assertThat(violations.size()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should cascade validation to teacher field")
        void shouldCascadeValidationToTeacherField() {
            UserDTO user = new UserDTO(1L, "student@okcps.org", "John", "Doe", "STUDENT");
            TeacherDTO invalidTeacher = new TeacherDTO(1L,
                    new UserDTO(2L, "teacher@okcps.org", "Jane", "Smith", "TEACHER"),
                    null);
            StudentDTO dto = new StudentDTO(1L, user, 100, "token-cascade", invalidTeacher);
            Set<ConstraintViolation<StudentDTO>> violations = validator.validate(dto);
            assertThat(violations).isNotNull();
            assertThat(violations.size()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should handle null points in validation")
        void shouldHandleNullPointsInValidation() {
            UserDTO user = new UserDTO(1L, "student@okcps.org", "John", "Doe", "STUDENT");
            TeacherDTO teacher = new TeacherDTO(1L,
                    new UserDTO(2L, "teacher@okcps.org", "Jane", "Smith", "TEACHER"),
                    "FIRST");
            StudentDTO dto = new StudentDTO(1L, user, null, "token-null-points", teacher);
            Set<ConstraintViolation<StudentDTO>> violations = validator.validate(dto);
            boolean hasPointsViolation = violations.stream()
                    .anyMatch(v -> v.getPropertyPath().toString().equals("points"));
            assertThat(hasPointsViolation).isFalse();
        }
    }

    @Nested
    @DisplayName("Object Equality and Comparison")
    class ObjectEqualityAndComparisonTests {
        @Test
        @DisplayName("Two StudentDTOs with same field values should have equal field values")
        void twoStudentDTOsWithSameFieldValuesShouldHaveEqualFieldValues() {
            UserDTO user1 = new UserDTO(1L, "student@okcps.org", "John", "Doe", "STUDENT");
            UserDTO user2 = new UserDTO(1L, "student@okcps.org", "John", "Doe", "STUDENT");
            TeacherDTO teacher1 = new TeacherDTO(1L,
                    new UserDTO(2L, "teacher@okcps.org", "Jane", "Smith", "TEACHER"),
                    "FIRST");
            TeacherDTO teacher2 = new TeacherDTO(1L,
                    new UserDTO(2L, "teacher@okcps.org", "Jane", "Smith", "TEACHER"),
                    "FIRST");
            StudentDTO dto1 = new StudentDTO(1L, user1, 100, "token-same", teacher1);
            StudentDTO dto2 = new StudentDTO(1L, user2, 100, "token-same", teacher2);
            assertThat(dto1.getId()).isEqualTo(dto2.getId());
            assertThat(dto1.getPoints()).isEqualTo(dto2.getPoints());
            assertThat(dto1.getToken()).isEqualTo(dto2.getToken());
            assertThat(dto1.getUser().getId()).isEqualTo(dto2.getUser().getId());
            assertThat(dto1.getTeacher().getId()).isEqualTo(dto2.getTeacher().getId());
        }

        @Test
        @DisplayName("StudentDTO from entity constructor should match JSON constructor")
        void studentDTOFromEntityConstructorShouldMatchJSONConstructor() {
            Student student = createStudent(1L, "student@okcps.org", 150);
            StudentDTO fromEntity = new StudentDTO(student);
            StudentDTO fromJSON = createStudentDTOFromJSON(student);
            assertThat(fromEntity.getId()).isEqualTo(fromJSON.getId());
            assertThat(fromEntity.getPoints()).isEqualTo(fromJSON.getPoints());
            assertThat(fromEntity.getToken()).isEqualTo(fromJSON.getToken());
            assertThat(fromEntity.getUser().getId()).isEqualTo(fromJSON.getUser().getId());
            assertThat(fromEntity.getTeacher().getId()).isEqualTo(fromJSON.getTeacher().getId());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    class EdgeCasesAndBoundaryConditionsTests {
        @Test
        @DisplayName("Should handle very high points value")
        void shouldHandleVeryHighPointsValue() {
            Student student = createStudent(1L, "student@okcps.org", Integer.MAX_VALUE);
            StudentDTO dto = new StudentDTO(student);
            assertEquals(Integer.MAX_VALUE, dto.getPoints());
            assertNotNull(dto.getUser());
            assertNotNull(dto.getTeacher());
        }

        @Test
        @DisplayName("Should handle empty token string")
        void shouldHandleEmptyTokenString() {
            Student student = createStudent(1L, "student@okcps.org", 100);
            student.setToken("");
            StudentDTO dto = new StudentDTO(student);
            assertEquals("", dto.getToken());
            assertNotNull(dto.getUser());
            assertNotNull(dto.getTeacher());
        }

        @Test
        @DisplayName("Should handle very long token string")
        void shouldHandleVeryLongTokenString() {
            String longToken = "a".repeat(1000);
            Student student = createStudent(1L, "student@okcps.org", 100);
            student.setToken(longToken);
            StudentDTO dto = new StudentDTO(student);
            assertEquals(longToken, dto.getToken());
            assertNotNull(dto.getUser());
            assertNotNull(dto.getTeacher());
        }
    }

    /**
     * Helper method to create a Student entity with all required fields
     */
    private Student createStudent(Long id, String email, Integer points) {
        Student student = new Student();
        student.setId(id);
        student.setPoints(points);
        student.generateToken();
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.STUDENT);
        student.setUser(user);
        Teacher teacher = new Teacher();
        teacher.setId(id);
        User teacherUser = new User();
        teacherUser.setId(id);
        teacherUser.setEmail("teacher@okcps.org");
        teacherUser.setFirstName("Jane");
        teacherUser.setLastName("Smith");
        teacherUser.setRole(Role.TEACHER);
        teacher.setUser(teacherUser);
        teacher.setGrade(GradeLevel.FIRST);
        student.setTeacher(teacher);
        return student;
    }

    private StudentDTO createStudentDTOFromJSON(Student student) {
        UserDTO userDTO = new UserDTO(student.getUser().getId(), student.getUser().getEmail(),
                student.getUser().getFirstName(), student.getUser().getLastName(),
                student.getUser().getRole().name());
        TeacherDTO teacherDTO = new TeacherDTO(student.getTeacher().getId(),
                new UserDTO(student.getTeacher().getUser().getId(),
                        student.getTeacher().getUser().getEmail(), student.getTeacher().getUser().getFirstName(),
                        student.getTeacher().getUser().getLastName(), student.getTeacher().getUser().getRole().name()),
                student.getTeacher().getGrade().name());
        return new StudentDTO(student.getId(), userDTO, student.getPoints(),
                student.getToken(), teacherDTO);
    }
}
