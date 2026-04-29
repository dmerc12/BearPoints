package com.bearpoints.api.unit.entity;

import com.bearpoints.api.entity.User;
import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.Syncable;
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

/**
* Unit tests for {@link User} entity validation and functionality.
* <p>Tests include:
* <ul>
*     <li>Field validation constraints</li>
*     <li>{@link Syncable} interface implementation</li>
*     <li>Role assignment validation</li>
* </ul>
* <p>Validation tests cover:
* <ul>
*   <li>Email (blank, null, invalid format/domain)</li>
*   <li>First name (blank, null, length boundaries)</li>
*   <li>Last name (blank, null, length boundaries)</li>
*   <li>Role (null)</li>
*  </ul>
 *
* @see User
* @version 1.3
* @author Dylan Mercer
*/
public class UserTests {
    private Validator validator;
    private User validUser;

    @BeforeEach
    public void setup() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
        validUser = createValidUser();
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("valid.user@okcps.org");
        user.setFirstName("ValidFirstName");
        user.setLastName("ValidLastName");
        user.setRole(Role.ADMIN);
        return user;
    }

    /** Tests valid user creation */
    @Test
    @DisplayName("Valid user passes all constraints")
    public void userValid() {
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertThat(violations).isEmpty();
    }

    /** Tests email validation */
    @Nested
    @DisplayName("Email validation tests")
    class EmailValidation {
        /** Tests blank email validation */
        @Test
        @DisplayName("Blank email fails validation")
        public void userEmailBlank() {
            validUser.setEmail("");
            Set<ConstraintViolation<User>> violations = validator.validate(validUser);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("email"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsAnyOf("Email is required");
        }

        /** Tests null email validation */
        @Test
        @DisplayName("Null email fails validation")
        public void userEmailNull() {
            validUser.setEmail(null);
            Set<ConstraintViolation<User>> violations = validator.validate(validUser);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("email"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Email is required");
        }

        /** Tests invalid email format validation */
        @Test
        @DisplayName("Invalid email format fails validation")
        public void userEmailInvalidFormat() {
            validUser.setEmail("invalid-email");
            Set<ConstraintViolation<User>> violations = validator.validate(validUser);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("email"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsAnyOf("Invalid email format");
        }

        /** Tests invalid email domain validation */
        @Test
        @DisplayName("Invalid domain fails validation")
        public void userEmailInvalidDomain() {
            validUser.setEmail("invalid@gmail.com");
            Set<ConstraintViolation<User>> violations = validator.validate(validUser);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("email"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Email must be @okcps.org domain");
        }
    }

    @Nested
    @DisplayName("First name validation tests")
    class FirstNameValidation {
        /** Tests blank first name validation */
        @Test
        @DisplayName("Blank first name fails validation")
        public void userFirstNameBlank() {
            validUser.setFirstName("");
            Set<ConstraintViolation<User>> violations = validator.validate(validUser);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("firstName"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsAnyOf("First name is required");
        }

        /** Tests null first name validation */
        @Test
        @DisplayName("Null first name fails validation")
        public void userFirstNameNull() {
            validUser.setFirstName(null);
            Set<ConstraintViolation<User>> violations = validator.validate(validUser);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("firstName"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("First name is required");
        }

        /** Tests first name length boundary validation */
        @Test
        @DisplayName("1-character first name passes validation")
        public void userFirstNameMinLength() {
            validUser.setFirstName("A");
            Set<ConstraintViolation<User>> violations = validator.validate(validUser);
            assertThat(violations).isEmpty();
        }

        /** Tests first name length boundary validation */
        @Test
        @DisplayName("100-character first name passes validation")
        public void userFirstNameMaxLength() {
            validUser.setFirstName("A".repeat(100));
            Set<ConstraintViolation<User>> violations = validator.validate(validUser);
            assertThat(violations).isEmpty();
        }

        /** Tests first name length boundary validation */
        @Test
        @DisplayName("101-character and over first name fails validation")
        public void userFirstNameTooLong() {
            validUser.setFirstName("A".repeat(101));
            Set<ConstraintViolation<User>> violations = validator.validate(validUser);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("firstName"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("First name must be between 1 and 100 characters");
        }
    }

    @Nested
    @DisplayName("Last name validation tests")
    class LastNameValidation {
        /** Tests blank last name validation */
        @Test
        @DisplayName("Blank last name fails validation")
        public void userLastNameBlank() {
            validUser.setLastName("");
            Set<ConstraintViolation<User>> violations = validator.validate(validUser);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("lastName"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsAnyOf("Last name is required");
        }

        /** Tests null last name validation */
        @Test
        @DisplayName("Null last name fails validation")
        public void userLastNameNull() {
            validUser.setLastName(null);
            Set<ConstraintViolation<User>> violations = validator.validate(validUser);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("lastName"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Last name is required");
        }

        /** Tests last name length boundary validation */
        @Test
        @DisplayName("1-character last name passes validation")
        public void userLastNameMinLength() {
            validUser.setLastName("A");
            Set<ConstraintViolation<User>> violations = validator.validate(validUser);
            assertThat(violations).isEmpty();
        }

        /** Tests last name length boundary validation */
        @Test
        @DisplayName("100-character last name passes validation")
        public void userLastNameMaxLength() {
            validUser.setLastName("A".repeat(100));
            Set<ConstraintViolation<User>> violations = validator.validate(validUser);
            assertThat(violations).isEmpty();
        }

        /** Tests last name length boundary validation */
        @Test
        @DisplayName("101-character and over last name fails validation")
        public void userLastNameTooLong() {
            validUser.setLastName("A".repeat(101));
            Set<ConstraintViolation<User>> violations = validator.validate(validUser);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("lastName"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Last name must be between 1 and 100 characters");
        }
    }

    /** Tests null role validation */
    @Nested
    @DisplayName("Role validation tests")
    class RoleValidationTests {
        @Test
        @DisplayName("User with ADMIN role passes validation")
        public void userWithAdminRoleValid() {
            validUser.setRole(Role.ADMIN);
            Set<ConstraintViolation<User>> violations = validator.validate(validUser);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("User with STAFF role passes validation")
        public void userWithStaffRoleValid() {
            validUser.setRole(Role.STAFF);
            Set<ConstraintViolation<User>> violations = validator.validate(validUser);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("User with PARA role passes validation")
        public void userWithParaRoleValid() {
            validUser.setRole(Role.PARA);
            Set<ConstraintViolation<User>> violations = validator.validate(validUser);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("User with TEACHER role passes validation")
        public void userWithTeacherRoleValid() {
            validUser.setRole(Role.TEACHER);
            Set<ConstraintViolation<User>> violations = validator.validate(validUser);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("User with STUDENT role passes validation")
        public void userWithStudentRoleValid() {
            validUser.setRole(Role.STUDENT);
            Set<ConstraintViolation<User>> violations = validator.validate(validUser);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Null role fails validation")
        public void userRoleNull() {
            validUser.setRole(null);
            Set<ConstraintViolation<User>> violations = validator.validate(validUser);
            assertThat(violations)
                    .filteredOn(v -> v.getPropertyPath().toString().equals("role"))
                    .extracting(ConstraintViolation::getMessage)
                    .containsExactly("Role is required");
        }
    }

    /** Tests null active status validation */
    @Test
    @DisplayName("Null active status fails validation")
    public void userActiveStatusNull() {
        validUser.setActive(null);
        Set<ConstraintViolation<User>> violations = validator.validate(validUser);
        assertThat(violations)
                .filteredOn(v -> v.getPropertyPath().toString().equals("active"))
                .extracting(ConstraintViolation::getMessage)
                .containsExactly("Active status is required");
    }

    /** Version field tests */
    @Nested
    @DisplayName("Version field tests")
    class VersionTests {
        /** Tests version field setter functionality */
        @Test
        @DisplayName("Version field can be set and retrieved")
        public void versionFieldCanBeSetAndRetrieved() {
            validUser.setVersion(5L);
            assertThat(validUser.getVersion()).isEqualTo(5L);
        }

        /** Tests that version field doesn't affect validation */
        @Test
        @DisplayName("Version field changes don't affect validation")
        public void versionChangesDontAffectValidation() {
            validUser.setVersion(10L);
            Set<ConstraintViolation<User>> violations = validator.validate(validUser);
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
            validUser.setSyncedToSheets(true);
            assertThat(validUser.getSyncedToSheets()).isTrue();
            validUser.setSyncedToSheets(false);
            assertThat(validUser.getSyncedToSheets()).isFalse();
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
            validUser.setLastSynced(now);
            assertThat(validUser.getLastSynced()).isEqualTo(now);
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
            validUser.setSheetRowId(42);
            assertThat(validUser.getSheetRowId()).isEqualTo(42);
            validUser.setSheetRowId(null);
            assertThat(validUser.getSheetRowId()).isNull();
        }
    }
}
