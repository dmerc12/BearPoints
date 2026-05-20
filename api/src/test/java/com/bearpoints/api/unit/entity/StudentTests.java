package com.bearpoints.api.unit.entity;

import com.bearpoints.api.entity.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link Student} entity validation and functionality.
 * <p>Tests include:
 * <ul>
 *     <li>Field validation constraints</li>
 *     <li>{@link Syncable} interface implementation</li>
 *     <li>User assignment validation</li>
 *     <li>Teacher assignment validation</li>
 *     <li>Automatic token generation</li>
 * </ul>
 * <p>Validation tests cover:
 * <ul>
 *   <li>User (null)</li>
 *   <li>Teacher (null)</li>
 *   <li>Points (default, positive, negative, zero)</li>
 *   <li>Token (generate, preserve)</li>
 *  </ul>
 *
 * @see Student
 * @version 1.1
 * @author Dylan Mercer
 */
public class StudentTests {
    private Validator validator;
    private Student validStudent;

    @BeforeEach
    public void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        validStudent = createValidStudent();
    }

    private Student createValidStudent() {
        User teacherUser = new User();
        teacherUser.setEmail("valid.teacher@okcps.org");
        teacherUser.setFirstName("ValidFirstName");
        teacherUser.setLastName("ValidLastName");
        teacherUser.setRole(Role.TEACHER);
        Teacher teacher = new Teacher();
        teacher.setUser(teacherUser);
        teacher.setGrade(GradeLevel.PRE_K);
        User studentUser = new User();
        studentUser.setEmail("valid.student@okcps.org");
        studentUser.setFirstName("ValidFirstName");
        studentUser.setLastName("ValidLastName");
        studentUser.setRole(Role.STUDENT);
        Student student = new Student();
        student.setUser(studentUser);
        student.setTeacher(teacher);
        student.generateToken();
        return student;
    }

    /** Tests valid teacher creation */
    @Test
    @DisplayName("Valid student passes all constraints")
    public void testStudentValid() {
        Set<ConstraintViolation<Student>> violations = validator.validate(validStudent);
        assertThat(violations).isEmpty();
    }

    /** Tests null user validation */
    @Test
    @DisplayName("Null user fails validation")
    public void testStudentUserNull() {
        validStudent.setUser(null);
        Set<ConstraintViolation<Student>> violations = validator.validate(validStudent);
        assertThat(violations)
                .filteredOn(v -> v.getPropertyPath().toString().equals("user"))
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("User reference is required");
    }

    /** Tests null teacher validation */
    @Test
    @DisplayName("Null teacher fails validation")
    public void testStudentTeacherNull() {
        validStudent.setTeacher(null);
        Set<ConstraintViolation<Student>> violations = validator.validate(validStudent);
        assertThat(violations)
                .filteredOn(v -> v.getPropertyPath().toString().equals("teacher"))
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("Teacher is required");
    }

    @Nested
    @DisplayName("Points validation tests")
    class PointsValidation {
        /** Tests points default value */
        @Test
        @DisplayName("Points default to 0")
        void pointsDefaultValue() {
            Student student = new Student();
            assertThat(student.getPoints()).isEqualTo(0);
        }

        /** Tests valid points values */
        @Test
        @DisplayName("Positive points pass validation")
        void pointsPositive() {
            validStudent.setPoints(10);
            Set<ConstraintViolation<Student>> violations = validator.validate(validStudent);
            assertThat(violations).isEmpty();
        }

        /** Tests zero points validation */
        @Test
        @DisplayName("Zero points pass validation")
        void pointsZero() {
            validStudent.setPoints(0);
            Set<ConstraintViolation<Student>> violations = validator.validate(validStudent);
            assertThat(violations).isEmpty();
        }

        /** Tests negative points validation */
        @Test
        @DisplayName("Negative points fail validation")
        void pointsNegative() {
            validStudent.setPoints(-5);
            Set<ConstraintViolation<Student>> violations = validator.validate(validStudent);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("points"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsAnyOf("Points cannot be negative");
        }
    }

    @Nested
    @DisplayName("Token generation tests")
    class TokenGeneration {
        /** Tests token generation when token is null */
        @Test
        @DisplayName("Token generated when null")
        void generateTokenWhenNull() {
            Student student = new Student();
            student.generateToken();
            assertThat(student.getToken())
                    .isNotNull();
        }

        /** Tests token not regenerated when exists */
        @Test
        @DisplayName("Token preserved when exists")
        void preserveTokenWhenExists() {
            Student student = new Student();
            String existingToken = "existing-token-123";
            student.setToken(existingToken);
            student.generateToken();
            assertThat(student.getToken()).isEqualTo(existingToken);
        }
    }

    /** Tests null active status validation */
    @Test
    @DisplayName("Null active status fails validation")
    public void studentActiveStatusNull() {
        validStudent.setActive(null);
        Set<ConstraintViolation<Student>> violations = validator.validate(validStudent);
        assertThat(violations)
                .filteredOn(v -> v.getPropertyPath().toString().equals("active"))
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("Active status is required");
    }

    @Nested
    @DisplayName("Role validation tests")
    class RoleValidationTests {
        private Student createStudentWithUserRole(Role role) {
            User user = new User();
            user.setRole(role);
            user.setEmail("test@okcps.org");
            user.setFirstName("test");
            user.setLastName("test");
            Teacher teacher = new Teacher();
            teacher.setUser(user);
            Student student = new Student();
            student.setTeacher(teacher);
            student.setUser(user);
            return student;
        }

        @Test
        @DisplayName("validateRole does nothing when user is null")
        void validateRoleWithNullUser() {
            Student student = new Student();
            student.setUser(null);
            student.validateRole();
        }

        @Test
        @DisplayName("validateRole does nothing when user role is STUDENT")
        void validateRoleWithStudentRole() {
            Student student = new Student();
            student.setUser(null);
            student.validateRole();
        }

        @Test
        @DisplayName(" validateRole throws IllegalStateException when user role is TEACHER")
        void validateRoleWithTeacherRole() {
            Student student = createStudentWithUserRole(Role.TEACHER);
            assertThatThrownBy(student::validateRole)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Student can only be linked to user with role STUDENT")
                    .hasMessageContaining(Role.TEACHER.name());
        }

        @Test
        @DisplayName(" validateRole throws IllegalStateException when user role is ADMIN")
        void validateRoleWithAdminRole() {
            Student student = createStudentWithUserRole(Role.ADMIN);
            assertThatThrownBy(student::validateRole)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Student can only be linked to user with role STUDENT")
                    .hasMessageContaining(Role.ADMIN.name());
        }

        @Test
        @DisplayName(" validateRole throws IllegalStateException when user role is STAFF")
        void validateRoleWithStaffRole() {
            Student student = createStudentWithUserRole(Role.STAFF);
            assertThatThrownBy(student::validateRole)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Student can only be linked to user with role STUDENT")
                    .hasMessageContaining(Role.STAFF.name());
        }

        @Test
        @DisplayName(" validateRole throws IllegalStateException when user role is PARA")
        void validateRoleWithParaRole() {
            Student student = createStudentWithUserRole(Role.PARA);
            assertThatThrownBy(student::validateRole)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Student can only be linked to user with role STUDENT")
                    .hasMessageContaining(Role.PARA.name());
        }

        @Test
        @DisplayName("prePersist calls validateRole and throws on invalid role")
        void prePersistThrowsWhenRoleInvalid() {
            Student student = createStudentWithUserRole(Role.TEACHER);
            assertThatThrownBy(student::prePersist)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("STUDENT");
        }

        @Test
        @DisplayName("preUpdate calls validateRole and throws on invalid role")
        void preUpdateThrowsWhenRoleInvalid() {
            Student student = createStudentWithUserRole(Role.ADMIN);
            assertThatThrownBy(student::preUpdate)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("STUDENT");
        }
    }

    /** Version field tests */
    @Nested
    @DisplayName("Version field tests")
    class VersionTests {/** Tests version field setter functionality */
        @Test
        @DisplayName("Version field can be set and retrieved")
        public void versionFieldCanBeSetAndRetrieved() {
            validStudent.setVersion(5L);
            assertThat(validStudent.getVersion()).isEqualTo(5L);
        }

        /** Tests that version field doesn't affect validation */
        @Test
        @DisplayName("Version field changes don't affect validation")
        public void versionFieldChangesDontAffectValidation() {
            validStudent.setVersion(10L);
            Set<ConstraintViolation<Student>> violations = validator.validate(validStudent);
            assertThat(violations).isEmpty();
        }
    }

    /** Tests for {@link Syncable} interface methods implemented in {@link User}. */
    @Nested
    @DisplayName("Syncable interface implementation tests")
    class SyncableTests {
        /**
         * Verifies that setting and getting the sync status works correctly.
         * <p>
         * Sets the sync status to true and false, then verifies the values
         * through getter the getter method.
         */
        @Test
        @DisplayName("Set and get synced status")
        void testSyncedStatus() {
            validStudent.setSyncedToSheets(true);
            assertThat(validStudent.getSyncedToSheets()).isTrue();
            validStudent.setSyncedToSheets(false);
            assertThat(validStudent.getSyncedToSheets()).isFalse();
        }

        /**
         * Verifies that setting and getting the last sync timestamp works correctly.
         * <p>
         * Sets the last synced timestamp to the current time, then verifies the value matches
         * through the getter method.
         */
        @Test
        @DisplayName("Set and get last synced timestamp")
        void testLastSynced() {
            LocalDateTime now = LocalDateTime.now();
            validStudent.setLastSynced(now);
            assertThat(validStudent.getLastSynced()).isEqualTo(now);
        }

        /**
         * Verifies that setting and getting the sheet row ID works correctly.
         * <p>
         * Sets the sheet row ID to a non-null value and null, then verifies the value
         * through the getter method
         */
        @Test
        @DisplayName("Set and get sheet row ID")
        void testSheetRowId() {
            validStudent.setSheetRowId(42);
            assertThat(validStudent.getSheetRowId()).isEqualTo(42);
            validStudent.setSheetRowId(null);
            assertThat(validStudent.getSheetRowId()).isNull();
        }
    }
}
