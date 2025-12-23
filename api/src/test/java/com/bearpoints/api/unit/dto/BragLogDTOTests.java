package com.bearpoints.api.unit.dto;

import com.bearpoints.api.dto.BehaviorTypeDTO;
import com.bearpoints.api.dto.BragLogDTO;
import com.bearpoints.api.entity.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link BragLogDTO} functionality.
 * <p>Tests include:
 * <ul>
 *     <li>Validation constraints for request scenarios</li>
 *     <li>Entity constructor for response scenarios</li>
 *     <li>JSON deserialization and constructor</li>
 *     <li>Edge cases and null handling</li>
 * </ul>
 *
 * @see BragLogDTO
 * @version 1.0
 * @author Dylan Mercer
 */
@DisplayName("BragLogDTO Tests")
public class BragLogDTOTests {
    private static Validator validator;

    private static User createUser(String firstName, String lastName) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return user;
    }

    private static BehaviorType createBehaviorType(Long id, String name, Integer pointValue) {
        BehaviorType behaviorType = new BehaviorType();
        behaviorType.setId(id);
        behaviorType.setName(name);
        behaviorType.setPointValue(pointValue);
        behaviorType.setActive(true);
        return behaviorType;
    }

    @BeforeAll
    public static void setup() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("JSON Creator Constructor Tests")
    class JSONConstructorTests {
        @Test
        @DisplayName("Should create BragLogDTO with all fields via JSON creator")
        void shouldCreateBragLogDTOWithAllFieldsViaJSONCreator() {
            Long id = 1L;
            Long studentId = 10L;
            Long teacherId = 20L;
            Set<Long> behaviorIds = Set.of(101L, 102L);
            String notes = "Good behavior";
            String studentName = "John Doe";
            String teacherName = "Jane Doe";
            GradeLevel grade = GradeLevel.FIRST;
            Set<BehaviorTypeDTO> behaviors = Set.of(
                    new BehaviorTypeDTO(101L, "Helped Others", 3, true),
                    new BehaviorTypeDTO(101L, "Stayed on Task", 2, true)
            );
            Integer pointsGenerated = 5;
            LocalDateTime timestamp = LocalDateTime.now();
            BragLogDTO dto = new BragLogDTO(id, studentId, teacherId, behaviorIds, notes,
                    studentName, teacherName, grade, behaviors, pointsGenerated, timestamp);
            assertThat(dto.getId()).isEqualTo(id);
            assertThat(dto.getStudentId()).isEqualTo(studentId);
            assertThat(dto.getTeacherId()).isEqualTo(teacherId);
            assertThat(dto.getBehaviorIds()).isEqualTo(behaviorIds);
            assertThat(dto.getNotes()).isEqualTo(notes);
            assertThat(dto.getStudentName()).isEqualTo(studentName);
            assertThat(dto.getTeacherName()).isEqualTo(teacherName);
            assertThat(dto.getGrade()).isEqualTo(grade);
            assertThat(dto.getBehaviors()).isEqualTo(behaviors);
            assertThat(dto.getPointsGenerated()).isEqualTo(pointsGenerated);
            assertThat(dto.getTimestamp()).isEqualTo(timestamp);
        }

        @Test
        @DisplayName("Should create BragLogDTO with minimal fields fields for creation request")
        void shouldCreateBragLogDTOWithMinimalFieldsForCreationRequest() {
            Long studentId = 10L;
            Long teacherId = 20L;
            Set<Long> behaviorIds = Set.of(101L);
            String notes = "Good behavior";
            BragLogDTO dto = new BragLogDTO(null, studentId, teacherId, behaviorIds, notes,
                    null, null, null, null, null, null);
            assertThat(dto.getId()).isNull();
            assertThat(dto.getStudentId()).isEqualTo(studentId);
            assertThat(dto.getTeacherId()).isEqualTo(teacherId);
            assertThat(dto.getBehaviorIds()).isEqualTo(behaviorIds);
            assertThat(dto.getNotes()).isEqualTo(notes);
            assertThat(dto.getStudentName()).isNull();
            assertThat(dto.getTeacherName()).isNull();
            assertThat(dto.getGrade()).isNull();
            assertThat(dto.getBehaviors()).isNull();
            assertThat(dto.getPointsGenerated()).isNull();
            assertThat(dto.getTimestamp()).isNull();
        }
    }

    @Nested
    @DisplayName("Entity Constructor Tests")
    class EntityConstructorTests {
        @Test
        @DisplayName("Should correctly map all fields from BragLog entity")
        void shouldCorrectlyMapAllFieldsFromBragLogEntity() {
            BragLog bragLog = new BragLog();
            bragLog.setId(1L);
            Student student = new Student();
            student.setId(10L);
            User studentUser = createUser("John", "Doe");
            student.setUser(studentUser);
            bragLog.setStudent(student);
            Teacher teacher = new Teacher();
            teacher.setId(20L);
            User teacherUser = createUser("Jane", "Smith");
            teacher.setUser(teacherUser);
            bragLog.setTeacher(teacher);
            BehaviorType behavior1 = createBehaviorType(101L, "Helped Others", 3);
            BehaviorType behavior2 = createBehaviorType(102L, "Stayed on Task", 2);
            bragLog.setBehaviors(Set.of(behavior1, behavior2));
            bragLog.setGrade(GradeLevel.FIRST);
            bragLog.setPointsGenerated(5);
            bragLog.setNotes("Excellent work today!");
            bragLog.setTimestamp(LocalDateTime.now());
            BragLogDTO dto = new BragLogDTO(bragLog);
            assertThat(dto.getId()).isEqualTo(bragLog.getId());
            assertThat(dto.getStudentId()).isEqualTo(student.getId());
            assertThat(dto.getStudentName())
                    .isEqualTo(studentUser.getFirstName() + " " + studentUser.getLastName());
            assertThat(dto.getTeacherId()).isEqualTo(teacher.getId());
            assertThat(dto.getTeacherName())
                    .isEqualTo(teacherUser.getFirstName() + " " + teacherUser.getLastName());
            assertThat(dto.getGrade()).isEqualTo(bragLog.getGrade());
            assertThat(dto.getPointsGenerated()).isEqualTo(bragLog.getPointsGenerated());
            assertThat(dto.getNotes()).isEqualTo(bragLog.getNotes());
            assertThat(dto.getTimestamp()).isEqualTo(bragLog.getTimestamp());
            assertThat(dto.getBehaviorIds()).containsExactlyInAnyOrder(behavior1.getId(), behavior2.getId());
            assertThat(dto.getBehaviors()).hasSize(bragLog.getBehaviors().size());
            BehaviorTypeDTO behavior1DTO = dto.getBehaviors().stream()
                    .filter(b -> b.getId().equals(behavior1.getId())).findFirst().orElse(null);
            assertThat(behavior1DTO).isNotNull();
            assertThat(behavior1DTO.getId()).isEqualTo(behavior1.getId());
            assertThat(behavior1DTO.getName()).isEqualTo(behavior1.getName());
            assertThat(behavior1DTO.getPointValue()).isEqualTo(behavior1.getPointValue());
            assertThat(behavior1DTO.getActive()).isEqualTo(behavior1.getActive());
            BehaviorTypeDTO behavior2DTO = dto.getBehaviors().stream()
                    .filter(b -> b.getId().equals(behavior2.getId())).findFirst().orElse(null);
            assertThat(behavior2DTO).isNotNull();
            assertThat(behavior2DTO.getId()).isEqualTo(behavior2.getId());
            assertThat(behavior2DTO.getName()).isEqualTo(behavior2.getName());
            assertThat(behavior2DTO.getPointValue()).isEqualTo(behavior2.getPointValue());
            assertThat(behavior2DTO.getActive()).isEqualTo(behavior2.getActive());
        }

        @Test
        @DisplayName("Should handle null student and teacher relationships")
        void shouldHandleNullStudentAndTeacherRelationships() {
            BragLog bragLog = new BragLog();
            bragLog.setId(1L);
            bragLog.setStudent(null);
            bragLog.setTeacher(null);
            bragLog.setBehaviors(Set.of());
            bragLog.setGrade(GradeLevel.FIRST);
            bragLog.setPointsGenerated(3);
            bragLog.setNotes("Test notes");
            bragLog.setTimestamp(LocalDateTime.now());
            BragLogDTO dto = new BragLogDTO(bragLog);
            assertThat(dto.getStudentId()).isNull();
            assertThat(dto.getStudentName()).isNull();
            assertThat(dto.getTeacherId()).isNull();
            assertThat(dto.getTeacherName()).isNull();
            assertThat(dto.getBehaviorIds()).isEmpty();
            assertThat(dto.getBehaviors()).isEmpty();
        }

        @Test
        @DisplayName("Should handle null behaviors set")
        void shouldHandleNullBehaviorsSet() {
            BragLog bragLog = new BragLog();
            bragLog.setId(1L);
            Student student = new Student();
            student.setId(10L);
            bragLog.setStudent(student);
            Teacher teacher = new Teacher();
            teacher.setId(20L);
            bragLog.setTeacher(teacher);
            bragLog.setBehaviors(null);
            bragLog.setGrade(GradeLevel.FIRST);
            bragLog.setPointsGenerated(3);
            bragLog.setNotes("Test notes");
            bragLog.setTimestamp(LocalDateTime.now());
            BragLogDTO dto = new BragLogDTO(bragLog);
            assertThat(dto.getBehaviorIds()).isNull();
            assertThat(dto.getBehaviors()).isNull();
        }

        @Test
        @DisplayName("Should handle null user in student and teacher")
        void shouldHandleNullUserInStudentAndTeacher() {
            BragLog bragLog = new BragLog();
            bragLog.setId(1L);
            Student student = new Student();
            student.setId(10L);
            student.setUser(null);
            bragLog.setStudent(student);
            Teacher teacher = new Teacher();
            teacher.setId(20L);
            teacher.setUser(null);
            bragLog.setTeacher(teacher);
            bragLog.setBehaviors(Set.of());
            bragLog.setGrade(GradeLevel.FIRST);
            bragLog.setPointsGenerated(3);
            bragLog.setNotes("Test notes");
            bragLog.setTimestamp(LocalDateTime.now());
            BragLogDTO dto = new BragLogDTO(bragLog);
            assertThat(dto.getStudentId()).isEqualTo(student.getId());
            assertThat(dto.getStudentName()).isNull();
            assertThat(dto.getTeacherId()).isEqualTo(teacher.getId());
            assertThat(dto.getTeacherName()).isNull();
        }
    }

    @Nested
    @DisplayName("Validation Tests for Request Scenarios")
    class ValidationTests {
        @Test
        @DisplayName("Valid creation request passes all constraints")
        void validCreationRequestPassesAllConstraints() {
            BragLogDTO dto = new BragLogDTO(null, 1L, 2L, Set.of(101L), "Good job!",
                    null, null, null, null, null, null);
            Set<ConstraintViolation<BragLogDTO>> violations = validator.validate(dto);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Null studentId violates @NotNull constraint")
        void nullStudentIdViolatesNotNullConstraint() {
            BragLogDTO dto = new BragLogDTO(null, null, 2L, Set.of(101L), "Notes",
                    null, null, null, null, null, null);
            Set<ConstraintViolation<BragLogDTO>> violations = validator.validate(dto);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage()).isEqualTo("Student ID is required");
        }

        @Test
        @DisplayName("Empty behaviorIds violates @NotEmpty constraint")
        void emptyBehaviorIdsViolatesNotEmptyConstraint() {
            BragLogDTO dto = new BragLogDTO(null, 1L, 2L, Set.of(), "Notes",
                    null, null, null, null, null, null);
            Set<ConstraintViolation<BragLogDTO>> violations = validator.validate(dto);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("At least one behavior is required");
        }

        @Test
        @DisplayName("Null behaviorIds violates @NotEmpty constraint")
        void nullBehaviorIdViolatesNotEmptyConstraint() {
            BragLogDTO dto = new BragLogDTO(null, 1L, 2L, null, "Notes",
                    null, null, null, null, null, null);
            Set<ConstraintViolation<BragLogDTO>> violations = validator.validate(dto);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("At least one behavior is required");
        }

        @ParameterizedTest
        @ValueSource(ints = {501, 600, 1000})
        @DisplayName("Notes exceeding 500 characters violates @Size constraint")
        void notesExceeding500CharactersViolatesSizeConstraint(int length) {
            String longNotes = "A".repeat(length);
            BragLogDTO dto = new BragLogDTO(
                    null, 1L, 2L, Set.of(101L), longNotes,
                    null, null, null, null, null, null
            );
            Set<ConstraintViolation<BragLogDTO>> violations = validator.validate(dto);
            assertThat(violations).hasSize(1);
            assertThat(violations.iterator().next().getMessage())
                    .isEqualTo("Notes cannot exceed 500 characters");
        }

        @Test
        @DisplayName("Notes at exactly 500 characters passes validation")
        void notesAtExactly500CharactersPassesValidation() {
            String notes = "A".repeat(500);
            BragLogDTO dto = new BragLogDTO(
                    null, 1L, 2L, Set.of(101L), notes, null,
                    null, null, null, null, null
            );
            Set<ConstraintViolation<BragLogDTO>> violations = validator.validate(dto);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Null notes at passes validation (optional field)")
        void nullNotesPassesValidation() {
            BragLogDTO dto = new BragLogDTO(
                    null, 1L, 2L, Set.of(101L), null, null,
                    null, null, null, null, null
            );
            Set<ConstraintViolation<BragLogDTO>> violations = validator.validate(dto);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Empty notes at passes validation (optional field)")
        void emptyNotesPassesValidation() {
            BragLogDTO dto = new BragLogDTO(
                    null, 1L, 2L, Set.of(101L), "", null,
                    null, null, null, null, null
            );
            Set<ConstraintViolation<BragLogDTO>> violations = validator.validate(dto);
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("Edge Cases and Boundary Conditions")
    class EdgeCasesAndBoundaryConditions {
        @Test
        @DisplayName("Update request with ID passes validation")
        void updateRequestWithIDPassesValidation() {
            BragLogDTO dto = new BragLogDTO(
                    1L, 10L, 20L, Set.of(101L), "Updated notes", null,
                    null, null, null, null, null
            );
            Set<ConstraintViolation<BragLogDTO>> violations = validator.validate(dto);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Multiple behavior IDs are handled correctly")
        void multipleBehaviorIDsAreHandledCorrectly() {
            BragLogDTO dto = new BragLogDTO(
                    1L, 10L, 20L, Set.of(101L, 102L, 103L), "Multiple behaviors",
                    null, null, null, null, null, null
            );
            Set<ConstraintViolation<BragLogDTO>> violations = validator.validate(dto);
            assertThat(violations).isEmpty();
            assertThat(dto.getBehaviorIds()).hasSize(3);
        }

        @Test
        @DisplayName("Response fields do not affect validation for requests")
        void responseFieldsDoNotAffectValidationForRequests() {
            BragLogDTO dto = new BragLogDTO(
                    1L, 10L, 20L, Set.of(101L), "Updated notes", "John Doe",
                    "Jane Smith", GradeLevel.FIRST, Set.of(new BehaviorTypeDTO(101L, "Test",
                    3, true)), 5, LocalDateTime.now()
            );
            Set<ConstraintViolation<BragLogDTO>> violations = validator.validate(dto);
            assertThat(violations).isEmpty();
        }
    }
}
