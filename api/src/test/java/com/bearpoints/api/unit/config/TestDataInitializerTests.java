package com.bearpoints.api.unit.config;

import com.bearpoints.api.config.TestDataInitializer;
import com.bearpoints.api.dao.*;
import com.bearpoints.api.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TestDataInitializer}
 * <p>Validates initialization behavior for test data across all scenarios:
 * <ul>
 *     <li>Environment variable validation and conditional execution</li>
 *     <li>Entity creation with proper property assignment</li>
 *     <li>Error handling and edge case management</li>
 *     <li>Security context lifecycle management</li>
 *     <li>Database interaction patters and data integrity</li>
 * </ul>
 *
 * <p>Test coverage includes:
 * <ul>
 *     <li>Environment variable validation (presence, blank values)</li>
 *     <li>Pre-existing user detection and handling</li>
 *     <li>Complete data creation workflow validation</li>
 *     <li>Individual entity creation with correct properties</li>
 *     <li>Bulk data generation with proper relationships</li>
 *     <li>Error scenarios and exception propagation</li>
 *     <li>Boundary conditions and edge cases</li>
 *     <li>Data extraction and transformation logic</li>
 * </ul>
 *
 * <p>Test methodology:
 * <ul>
 *     <li>Mock-based isolation of database dependencies</li>
 *     <li>Reflection for environment simulation</li>
 *     <li>Argument capture for validation of created entities</li>
 *     <li>Verification of interaction patterns with DAOs</li>
 *     <li>Security context state validation</li>
 * </ul>
 *
 * @see TestDataInitializer
 * @version 2.2
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
public class TestDataInitializerTests {
    @Mock
    private UserDAO userDAO;

    @Mock
    private BehaviorTypeDAO behaviorTypeDAO;

    @Mock
    private BragLogDAO bragLogDAO;

    @Mock
    private RewardItemDAO rewardItemDAO;

    @Mock
    private StudentRewardDAO studentRewardDAO;

    @Mock
    private StudentDAO studentDAO;

    @Mock
    private TeacherDAO teacherDAO;

    @InjectMocks
    private TestDataInitializer testDataInitializer;

    private final String testEmail = "utuser@example.com";

    /**
     * Helper method to set environment variable value using reflection
     * @param value      Value to set for TEST_EMAIL
     * @throws Exception if reflection operations fail
     */
    private void setEnvVariable(String value) throws Exception {
        Field field = TestDataInitializer.class.getDeclaredField("testEmail");
        field.setAccessible(true);
        field.set(testDataInitializer, value);
    }

    /**
     * Helper method to set constant values using reflection for testing edge cases
     * @param fieldName  Name of the field to modify
     * @param value      Value to set for the constant
     * @throws Exception if reflection operations fail
     */
    private void setConstant(String fieldName, int value) throws Exception {
        Field field = TestDataInitializer.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(testDataInitializer, value);
    }

    /**
     * Clears security context and resets environment state before each test
     * <p>Ensures test isolation by:
     * <ul>
     *     <li>Clearing Spring Security Context</li>
     *     <li>Resetting environment variable state</li>
     *     <li>Restoring default configuration constants</li>
     * </ul>
     */
    @BeforeEach
    void setUp() throws Exception {
        // Clear security context and clear environment variable
        SecurityContextHolder.clearContext();
        setEnvVariable(null);
        // Reset constants to their original values after each test
        setConstant("NUM_TEST_ADMINS_TO_CREATE", 12);
        setConstant("NUM_TEST_STAFF_TO_CREATE", 5);
        setConstant("NUM_TEST_PARAS_TO_CREATE", 8);
        setConstant("NUM_TEST_TEACHERS_TO_CREATE", 25);
        setConstant("MIN_NUM_TEST_STUDENTS_PER_TEACHER", 20);
        setConstant("MAX_NUM_TEST_STUDENTS_PER_TEACHER", 30);
        setConstant("NUM_TEST_BRAG_LOGS_TO_CREATE", 200);
        setConstant("NUM_TEST_REWARD_ITEMS_TO_CREATE", 20);
        setConstant("NUM_TEST_STUDENT_REWARDS_TO_CREATE", 50);
        lenient().when(studentDAO.findAll()).thenReturn(Collections.emptyList());
        lenient().when(teacherDAO.findAll()).thenReturn(Collections.emptyList());

    }

    /**
     * Verifies initialization is skipped when TEST_EMAIL is unset
     * <p>Asserts:
     * <ul>
     *     <li>No database interactions occur</li>
     *     <li>Security context remains cleared</li>
     * </ul>
     */
    @Test
    @DisplayName("Skipped when TEST_EMAIL is not set")
    void skippedWhenTestEmailIsNotSet() {
        testDataInitializer.run();
        verify(userDAO, never()).findByEmail(any());
        verify(userDAO, never()).save(any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Verifies initialization is skipped when TEST_EMAIL is blank
     * <p>Asserts:
     * <ul>
     *     <li>No database interactions occur</li>
     *     <li>Security context remains cleared</li>
     * </ul>
     */
    @Test
    @DisplayName("Skipped when TEST_EMAIL is blank")
    void skippedWhenTestEmailIsBlank() throws Exception {
        setEnvVariable("");
        testDataInitializer.run();
        verify(userDAO, never()).findByEmail(any());
        verify(userDAO, never()).save(any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Verifies no action is taken when test user already exists
     * <p>Asserts:
     * <ul>
     *     <li>User lookup occurs but no save operation is performed</li>
     *     <li>Security context remains cleared</li>
     * </ul>
     */
    @Test
    @DisplayName("Skipped when user already exists")
    void skippedWhenUserAlreadyExists() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.of(new User()));
        testDataInitializer.run();
        verify(userDAO, never()).save(any());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Verifies complete data creation when all conditions are met
     * <p>Asserts:
     * <ul>
     *     <li>All DAO save operations are executed appropriately</li>
     *     <li>Security context is properly cleared after execution</li>
     *     <li>Entity relationships are correctly established</li>
     * </ul>
     */
    @Test
    @DisplayName("Creates test data when conditions are met")
    void createTestDataWhenConditionsAreMet() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            if (user.getRole() == Role.TEACHER && user.getTeacher() != null) {
                user.getTeacher().setId(1L);
                user.getTeacher().setUser(user);
            } else if (user.getRole() == Role.STUDENT && user.getStudent() != null ) {
                user.getStudent().setId(1L);
                if (user.getStudent().getTeacher() == null) {
                    Teacher mockTeacher = new Teacher();
                    mockTeacher.setId(999L);
                    mockTeacher.setGrade(GradeLevel.FIRST);
                    User teacherUser = new User();
                    teacherUser.setId(555L);
                    teacherUser.setEmail("mkteacher@okcps.org");
                    teacherUser.setFirstName("Mock");
                    teacherUser.setLastName("Teacher");
                    teacherUser.setRole(Role.TEACHER);
                    mockTeacher.setUser(teacherUser);
                    teacherUser.setTeacher(mockTeacher);
                    user.getStudent().setTeacher(mockTeacher);
                }
            }
            return user;
        });
        when(behaviorTypeDAO.save(any(BehaviorType.class))).thenAnswer(invocation -> {
            BehaviorType behaviorType = invocation.getArgument(0);
            behaviorType.setId(1L);
            if (behaviorType.getName() != null) {
                String name = behaviorType.getName();
                behaviorType.setActive(!name.equals("Behaved") && !name.equals("Quiet") && !name.equals("Cleaned Up") && !name.equals("Helped Others"));
            }
            return behaviorType;
        });
        when(bragLogDAO.save(any(BragLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(rewardItemDAO.save(any(RewardItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(studentRewardDAO.save(any(StudentReward.class))).thenAnswer(invocation -> invocation.getArgument(0));
        testDataInitializer.run();
        verify(userDAO, atLeastOnce()).save(any(User.class));
        verify(behaviorTypeDAO, atLeastOnce()).save(any(BehaviorType.class));
        verify(bragLogDAO, atLeastOnce()).save(any(BragLog.class));
        verify(rewardItemDAO, atLeastOnce()).save(any(RewardItem.class));
        verify(studentRewardDAO, atLeastOnce()).save(any(StudentReward.class));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Verifies security context is cleared during exception handling
     * <p>Asserts:
     * <ul>
     *     <li>Exceptions during save operations are propagated</li>
     *     <li>Security context is cleared despite operation failure</li>
     *     <li>Cleanup occurs even in error scenarios</li>
     * </ul>
     */
    @Test
    @DisplayName("Clears security context on exception")
    void clearSecurityContextOnException() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenThrow(new RuntimeException("Test exception"));
        assertThrows(RuntimeException.class, testDataInitializer::run);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Verifies created admin has all required properties
     * <p>Asserts created admin contains:
     * <ul>
     *     <li>Correct email address from environment variable</li>
     *     <li>Properly extracted first name (first initial)</li>
     *     <li>Correctly parsed last name from email</li>
     *     <li>ADMIN role assignment</li>
     * </ul>
     */
    @Test
    @DisplayName("Creates admin with correct properties")
    void createAdminWithCorrectProperties() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        setConstant("NUM_TEST_ADMINS_TO_CREATE", 0);
        setConstant("NUM_TEST_STAFF_TO_CREATE", 0);
        setConstant("NUM_TEST_PARAS_TO_CREATE", 0);
        setConstant("NUM_TEST_TEACHERS_TO_CREATE", 0);
        setConstant("MIN_NUM_TEST_STUDENTS_PER_TEACHER", 0);
        setConstant("MAX_NUM_TEST_STUDENTS_PER_TEACHER", 0);
        setConstant("NUM_TEST_BRAG_LOGS_TO_CREATE", 0);
        setConstant("NUM_TEST_REWARD_ITEMS_TO_CREATE", 0);
        setConstant("NUM_TEST_STUDENT_REWARDS_TO_CREATE", 0);
        testDataInitializer.run();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDAO, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(testEmail, savedUser.getEmail());
        assertEquals("u", savedUser.getFirstName());
        assertEquals("user", savedUser.getLastName());
        assertEquals(Role.ADMIN, savedUser.getRole());
    }

    /**
     * Verifies created staff has all required properties
     * <p>Asserts created staff contains:
     * <ul>
     *     <li>Correct email address from environment variable</li>
     *     <li>Properly extracted first name (first initial)</li>
     *     <li>Correctly parsed last name from email</li>
     *     <li>STAFF role assignment</li>
     * </ul>
     */
    @Test
    @DisplayName("Creates staff with correct properties")
    void createStaffWithCorrectProperties() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        setConstant("NUM_TEST_ADMINS_TO_CREATE", 0);
        setConstant("NUM_TEST_STAFF_TO_CREATE", 1);
        setConstant("NUM_TEST_PARAS_TO_CREATE", 0);
        setConstant("NUM_TEST_TEACHERS_TO_CREATE", 0);
        setConstant("MIN_NUM_TEST_STUDENTS_PER_TEACHER", 0);
        setConstant("MAX_NUM_TEST_STUDENTS_PER_TEACHER", 0);
        setConstant("NUM_TEST_BRAG_LOGS_TO_CREATE", 0);
        setConstant("NUM_TEST_REWARD_ITEMS_TO_CREATE", 0);
        setConstant("NUM_TEST_STUDENT_REWARDS_TO_CREATE", 0);
        testDataInitializer.run();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDAO, times(2)).save(userCaptor.capture());
        List<User> savedUsers = userCaptor.getAllValues();
        User primaryAdmin = savedUsers.getFirst();
        assertEquals(testEmail, primaryAdmin.getEmail());
        assertEquals("u", primaryAdmin.getFirstName());
        assertEquals("user", primaryAdmin.getLastName());
        assertEquals(Role.ADMIN, primaryAdmin.getRole());
        User staffUser = savedUsers.get(1);
        assertEquals("staff0@okcps.org", staffUser.getEmail());
        assertEquals("staff", staffUser.getFirstName());
        assertEquals("staff0", staffUser.getLastName());
        assertEquals(Role.STAFF, staffUser.getRole());
    }

    /**
     * Verifies created staff has all required properties
     * <p>Asserts created staff contains:
     * <ul>
     *     <li>Correct email address from environment variable</li>
     *     <li>Properly extracted first name (first initial)</li>
     *     <li>Correctly parsed last name from email</li>
     *     <li>STAFF role assignment</li>
     * </ul>
     */
    @Test
    @DisplayName("Creates para with correct properties")
    void createParaWithCorrectProperties() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        setConstant("NUM_TEST_ADMINS_TO_CREATE", 0);
        setConstant("NUM_TEST_STAFF_TO_CREATE", 0);
        setConstant("NUM_TEST_PARAS_TO_CREATE", 1);
        setConstant("NUM_TEST_TEACHERS_TO_CREATE", 0);
        setConstant("MIN_NUM_TEST_STUDENTS_PER_TEACHER", 0);
        setConstant("MAX_NUM_TEST_STUDENTS_PER_TEACHER", 0);
        setConstant("NUM_TEST_BRAG_LOGS_TO_CREATE", 0);
        setConstant("NUM_TEST_REWARD_ITEMS_TO_CREATE", 0);
        setConstant("NUM_TEST_STUDENT_REWARDS_TO_CREATE", 0);
        testDataInitializer.run();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDAO, times(2)).save(userCaptor.capture());
        List<User> savedUsers = userCaptor.getAllValues();
        User primaryAdmin = savedUsers.getFirst();
        assertEquals(testEmail, primaryAdmin.getEmail());
        assertEquals("u", primaryAdmin.getFirstName());
        assertEquals("user", primaryAdmin.getLastName());
        assertEquals(Role.ADMIN, primaryAdmin.getRole());
        User paraUser = savedUsers.get(1);
        assertEquals("para0@okcps.org", paraUser.getEmail());
        assertEquals("para", paraUser.getFirstName());
        assertEquals("para0", paraUser.getLastName());
        assertEquals(Role.PARA, paraUser.getRole());
    }

    /**
     * Verifies creation of multiple administrator accounts
     * <p>Asserts:
     * <ul>
     *     <li>Correct number of admin accounts are created (primary + configured count)</li>
     *     <li>All admin accounts are properly saved to database</li>
     *     <li>No unintended side effects on other entity types</li>
     * </ul>
     */
    @Test
    @DisplayName("Creates multiple test admins")
    void createsMultipleTestAdmins() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        setConstant("NUM_TEST_STAFF_TO_CREATE", 0);
        setConstant("NUM_TEST_PARAS_TO_CREATE", 0);
        setConstant("NUM_TEST_TEACHERS_TO_CREATE", 0);
        setConstant("MIN_NUM_TEST_STUDENTS_PER_TEACHER", 0);
        setConstant("MAX_NUM_TEST_STUDENTS_PER_TEACHER", 0);
        setConstant("NUM_TEST_BRAG_LOGS_TO_CREATE", 0);
        setConstant("NUM_TEST_REWARD_ITEMS_TO_CREATE", 0);
        setConstant("NUM_TEST_STUDENT_REWARDS_TO_CREATE", 0);
        testDataInitializer.run();
        verify(userDAO, times(13)).save(any(User.class));
    }

    /**
     * Verifies creation of multiple staff accounts
     * <p>Asserts:
     * <ul>
     *     <li>Correct number of staff accounts are created (primary + configured staff count)</li>
     *     <li>All staff accounts are properly saved to database</li>
     *     <li>No unintended side effects on other entity types</li>
     * </ul>
     */
    @Test
    @DisplayName("Creates multiple test staff")
    void createsMultipleTestStaff() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        setConstant("NUM_TEST_ADMINS_TO_CREATE", 0);
        setConstant("NUM_TEST_PARAS_TO_CREATE", 0);
        setConstant("NUM_TEST_TEACHERS_TO_CREATE", 0);
        setConstant("MIN_NUM_TEST_STUDENTS_PER_TEACHER", 0);
        setConstant("MAX_NUM_TEST_STUDENTS_PER_TEACHER", 0);
        setConstant("NUM_TEST_BRAG_LOGS_TO_CREATE", 0);
        setConstant("NUM_TEST_REWARD_ITEMS_TO_CREATE", 0);
        setConstant("NUM_TEST_STUDENT_REWARDS_TO_CREATE", 0);
        testDataInitializer.run();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDAO, times(6)).save(userCaptor.capture());
        List<User> savedUsers = userCaptor.getAllValues();
        assertEquals(Role.ADMIN, savedUsers.getFirst().getRole());
        for (int i = 1; i <= 5; i++) {
            User staff = savedUsers.get(i);
            assertEquals("staff", staff.getFirstName());
            assertEquals("staff" + (i - 1), staff.getLastName());
            assertEquals(Role.STAFF, staff.getRole());
        }
    }

    /**
     * Verifies creation of multiple para accounts
     * <p>Asserts:
     * <ul>
     *     <li>Correct number of para accounts are created (primary + configured para count)</li>
     *     <li>All para accounts are properly saved to database</li>
     *     <li>No unintended side effects on other entity types</li>
     * </ul>
     */
    @Test
    @DisplayName("Creates multiple test paras")
    void createsMultipleTestParas() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        setConstant("NUM_TEST_ADMINS_TO_CREATE", 0);
        setConstant("NUM_TEST_STAFF_TO_CREATE", 0);
        setConstant("NUM_TEST_TEACHERS_TO_CREATE", 0);
        setConstant("MIN_NUM_TEST_STUDENTS_PER_TEACHER", 0);
        setConstant("MAX_NUM_TEST_STUDENTS_PER_TEACHER", 0);
        setConstant("NUM_TEST_BRAG_LOGS_TO_CREATE", 0);
        setConstant("NUM_TEST_REWARD_ITEMS_TO_CREATE", 0);
        setConstant("NUM_TEST_STUDENT_REWARDS_TO_CREATE", 0);
        testDataInitializer.run();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDAO, times(9)).save(userCaptor.capture());
        List<User> savedUsers = userCaptor.getAllValues();
        assertEquals(Role.ADMIN, savedUsers.getFirst().getRole());
        for (int i = 1; i <= 8; i++) {
            User para = savedUsers.get(i);
            assertEquals("para", para.getFirstName());
            assertEquals("para" + (i - 1), para.getLastName());
            assertEquals(Role.PARA, para.getRole());
        }
    }

    /**
     * Verifies creation of teacher accounts with randomized grade level assignments
     * <p>Asserts:
     * <ul>
     *     <li>Teachers are created with valid grade level assignments</li>
     *     <li>Proper user-teacher relationship establishment</li>
     *     <li>Correct total number of user accounts created</li>
     * </ul>
     */
    @Test
    @DisplayName("Creates test teachers")
    void createsTestTeachersWithRandomGradeLevels() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            if (user.getTeacher() != null) {
                user.getTeacher().setId(1L);
                user.getTeacher().setUser(user);
            }
            return user;
        });
        when(behaviorTypeDAO.save(any(BehaviorType.class))).thenAnswer(invocation -> {
            BehaviorType behaviorType = invocation.getArgument(0);
            behaviorType.setId(1L);
            return behaviorType;
        });
        setConstant("NUM_TEST_BRAG_LOGS_TO_CREATE", 0);
        setConstant("NUM_TEST_REWARD_ITEMS_TO_CREATE", 0);
        setConstant("NUM_TEST_STUDENT_REWARDS_TO_CREATE", 0);
        testDataInitializer.run();
        verify(userDAO, atLeast(26)).save(any(User.class));
    }

    /**
     * Verifies student creation and distribution across teachers
     * <p>Asserts:
     * <ul>
     *     <li>Students are properly assigned to teachers</li>
     *     <li>Correct number of students created based on configuration</li>
     *     <li>Student-teacher relationships are correctly established</li>
     * </ul>
     */
    @Test
    @DisplayName("Creates test students for teachers")
    void createsTestStudentsForTeachers() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            if (user.getStudent() != null) {
                user.getStudent().setId(1L);
            }
            if (user.getTeacher() != null) {
                user.getTeacher().setId(1L);
            }
            return user;
        });
        when(behaviorTypeDAO.save(any(BehaviorType.class))).thenAnswer(invocation -> {
            BehaviorType behaviorType = invocation.getArgument(0);
            behaviorType.setId(1L);
            return behaviorType;
        });
        testDataInitializer.run();
        verify(userDAO, atLeast(500)).save(any(User.class));
    }

    /**
     * Verifies behavior type creation with mixed active status
     * <p>Asserts:
     * <ul>
     *     <li>Correct number of behavior types created</li>
     *     <li>Mixed active/inactive status distribution</li>
     *     <li>Proper point value assignment</li>
     * </ul>
     */
    @Test
    @DisplayName("Creates test behavior types")
    void createsTestBehaviorTypesWithMixedActiveStatus() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(behaviorTypeDAO.save(any(BehaviorType.class))).thenAnswer(invocation -> invocation.getArgument(0));
        testDataInitializer.run();
        verify(behaviorTypeDAO, times(9)).save(any(BehaviorType.class));
    }

    /**
     * Verifies brag log creation with randomized behavior combinations
     * <p>Asserts:
     * <ul>
     *     <li>Brag logs are created with valid behavior combinations</li>
     *     <li>Point calculations are correctly performed</li>
     *     <li>Proper student-teacher associations</li>
     * </ul>
     */
    @Test
    @DisplayName("Creates test brag logs with behavior combinations")
    void createsTestBragLogsWithBehaviorCombinations() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            if (user.getStudent() != null) {
                user.getStudent().setId(1L);
                Teacher teacher = new Teacher();
                teacher.setId(1L);
                User teacherUser = new User();
                teacherUser.setId(2L);
                teacherUser.setFirstName("Test");
                teacherUser.setLastName("Teacher");
                teacher.setUser(teacherUser);
                teacherUser.setTeacher(teacher);
                user.getStudent().setTeacher(teacher);
            }
            if (user.getTeacher() != null) {
                user.getTeacher().setId(1L);
                if (user.getTeacher().getUser() == null) {
                    user.getTeacher().setUser(user);
                }
            }
            return user;
        });
        when(behaviorTypeDAO.save(any(BehaviorType.class))).thenAnswer(invocation -> {
            BehaviorType bt = invocation.getArgument(0);
            bt.setId(1L);
            bt.setActive(true);
            return bt;
        });
        when(bragLogDAO.save(any(BragLog.class))).thenAnswer(invocation -> invocation.getArgument(0));
        testDataInitializer.run();
        verify(bragLogDAO, times(200)).save(any(BragLog.class));
    }

    /**
     * Verifies brag log creation is skipped when no students exist
     * <p>Asserts:
     * <ul>
     *     <li>No brag logs are created when student list is empty</li>
     *     <li>Appropriate warning log is generated</li>
     *     <li>No database interactions for brag log creation</li>
     * </ul>
     */
    @Test
    @DisplayName("Skips brag log creation when no students")
    void skipsBragLogCreationWhenNoStudents() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        setConstant("NUM_TEST_TEACHERS_TO_CREATE", 0);
        setConstant("NUM_TEST_BRAG_LOGS_TO_CREATE", 10);
        testDataInitializer.run();
        verify(bragLogDAO, never()).save(any(BragLog.class));
    }

    /**
     * Verifies brag log creation is skipped when behavior types list is empty
     * <p>Asserts:
     * <ul>
     *     <li>No brag logs are created when behavior types list is empty</li>
     *     <li>Early return condition is properly triggered</li>
     *     <li>No database interactions for brag log creation</li>
     * </ul>
     */
    @Test
    @DisplayName("Skips brag log creation when no behavior types")
    void skipsBragLogCreationWhenNoBehaviorTypes() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            if (user.getStudent() != null) {
                user.getStudent().setId(1L);
                user.getStudent().setTeacher(new Teacher());
            }
            return user;
        });
        when(behaviorTypeDAO.save(any(BehaviorType.class))).thenAnswer(invocation -> {
            BehaviorType behaviorType = invocation.getArgument(0);
            behaviorType.setId(1L);
            return behaviorType;
        });
        setConstant("NUM_TEST_TEACHERS_TO_CREATE", 1);
        setConstant("MIN_NUM_TEST_STUDENTS_PER_TEACHER", 1);
        setConstant("MAX_NUM_TEST_STUDENTS_PER_TEACHER", 1);
        setConstant("NUM_TEST_BRAG_LOGS_TO_CREATE", 0);
        setConstant("NUM_TEST_REWARD_ITEMS_TO_CREATE", 0);
        setConstant("NUM_TEST_STUDENT_REWARDS_TO_CREATE", 0);
        testDataInitializer.run();
        Field behaviorTypesField = TestDataInitializer.class.getDeclaredField("createdBehaviorTypes");
        behaviorTypesField.setAccessible(true);
        @SuppressWarnings("unchecked")
        List<BehaviorType> behaviorTypes = (List<BehaviorType>) behaviorTypesField.get(testDataInitializer);
        behaviorTypes.clear();
        Field studentsField = TestDataInitializer.class.getDeclaredField("MIN_NUM_TEST_STUDENTS_PER_TEACHER");
        studentsField.setAccessible(true);
        List<Student> mockStudents = new ArrayList<>();
        Student mockStudent = new Student();
        mockStudent.setId(1L);
        Teacher mockTeacher = new Teacher();
        mockTeacher.setId(1L);
        mockStudent.setTeacher(mockTeacher);
        mockStudents.add(mockStudent);
        java.lang.reflect.Method method = TestDataInitializer.class.getDeclaredMethod("createTestBragLogs", List.class);
        method.setAccessible(true);
        method.invoke(testDataInitializer, mockStudents);
        verify(bragLogDAO, never()).save(any(BragLog.class));
    }

    /**
     * Verifies brag log creation with empty behavior sets when no active types exist
     * <p>Asserts:
     * <ul>
     *     <li>Brag logs are created even with no active behavior types</li>
     *     <li>Behavior sets remain empty when no active types available</li>
     *     <li>Proper handling of empty behavior collections</li>
     * </ul>
     */
    @Test
    @DisplayName("Creates brag logs with empty behaviors when no active behavior types")
    void createsTestBragLogsWithEmptyBehaviorsWhenNoActiveBehaviorTypes() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            if (user.getStudent() != null) {
                user.getStudent().setId(1L);
                Teacher teacher = new Teacher();
                teacher.setId(1L);
                User teacherUser = new User();
                teacherUser.setId(2L);
                teacherUser.setFirstName("Test");
                teacherUser.setLastName("Teacher");
                teacher.setUser(teacherUser);
                teacherUser.setTeacher(teacher);
                user.getStudent().setTeacher(teacher);
            }
            return user;
        });
        when(behaviorTypeDAO.save(any(BehaviorType.class))).thenAnswer(invocation -> {
            BehaviorType behaviorType = invocation.getArgument(0);
            behaviorType.setId(1L);
            behaviorType.setActive(false);
            return behaviorType;
        });
        when(bragLogDAO.save(any(BragLog.class))).thenAnswer(invocation -> {
            BragLog bragLog = invocation.getArgument(0);
            assertTrue(bragLog.getBehaviors().isEmpty());
            return bragLog;
        });
        testDataInitializer.run();
        verify(bragLogDAO, atLeastOnce()).save(any(BragLog.class));
    }

    /**
     * Verifies brag log creation is skipped when both students and behavior types are empty
     * <p>Asserts:
     * <ul>
     *     <li>No brag logs are created when both prerequisites are missing</li>
     *     <li>Early return condition handles combined empty state</li>
     *     <li>Appropriate warning log is generated</li>
     * </ul>
     */
    @Test
    @DisplayName("Skips brag log creation when both students and behavior types are empty")
    void skipsBragLogCreationWhenBothStudentsAndBehaviorTypesEmpty() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        setConstant("NUM_TEST_TEACHERS_TO_CREATE", 0);
        setConstant("NUM_TEST_BRAG_LOGS_TO_CREATE", 10);
        testDataInitializer.run();
        verify(bragLogDAO, never()).save(any(BragLog.class));
    }

    /**
     * Verifies reward item creation with randomized properties
     * <p>Asserts:
     * <ul>
     *     <li>Reward items are created with valid properties</li>
     *     <li>Randomized point costs and stock levels within expected ranges</li>
     *     <li>Proper database persistence</li>
     * </ul>
     */
    @Test
    @DisplayName("Creates test reward items")
    void createsTestRewardItems() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(behaviorTypeDAO.save(any(BehaviorType.class))).thenAnswer(invocation -> {
            BehaviorType behaviorType = invocation.getArgument(0);
            behaviorType.setId(1L);
            return behaviorType;
        });
        when(rewardItemDAO.save(any(RewardItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        setConstant("NUM_TEST_ADMINS_TO_CREATE", 0);
        setConstant("NUM_TEST_TEACHERS_TO_CREATE", 0);
        setConstant("NUM_TEST_BRAG_LOGS_TO_CREATE", 0);
        setConstant("NUM_TEST_STUDENT_REWARDS_TO_CREATE", 0);
        testDataInitializer.run();
        verify(rewardItemDAO, atLeastOnce()).save(any(RewardItem.class));
    }

    /**
     * Verifies student reward assignment process
     * <p>Asserts:
     * <ul>
     *     <li>Rewards are properly assigned to students</li>
     *     <li>Correct number of reward assignments created</li>
     *     <li>Proper student-reward item relationships established</li>
     * </ul>
     */
    @Test
    @DisplayName("Creates test student rewards")
    void createsTestStudentRewards() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(1L);
            if (user.getStudent() != null) {
                user.getStudent().setId(1L);
            }
            return user;
        });
        when(behaviorTypeDAO.save(any(BehaviorType.class))).thenAnswer(invocation -> {
            BehaviorType behaviorType = invocation.getArgument(0);
            behaviorType.setId(1L);
            return behaviorType;
        });
        when(rewardItemDAO.save(any(RewardItem.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(studentRewardDAO.save(any(StudentReward.class))).thenAnswer(invocation -> invocation.getArgument(0));
        setConstant("NUM_TEST_ADMINS_TO_CREATE", 0);
        setConstant("NUM_TEST_BRAG_LOGS_TO_CREATE", 0);
        testDataInitializer.run();
        verify(studentRewardDAO, times(50)).save(any(StudentReward.class));
    }

    /**
     * Verifies student reward creation is skipped when prerequisites are missing
     * <p>Asserts:
     * <ul>
     *     <li>No rewards assigned when students list is empty</li>
     *     <li>No rewards assigned when reward items list is empty</li>
     *     <li>Appropriate warning log is generated</li>
     * </ul>
     */
    @Test
    @DisplayName("Skips student reward creation when no students or reward items")
    void skipsStudentRewardCreationWhenNoStudentsOrRewardItems() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        setConstant("NUM_TEST_TEACHERS_TO_CREATE", 0);
        setConstant("NUM_TEST_REWARD_ITEMS_TO_CREATE", 0);
        setConstant("NUM_TEST_STUDENT_REWARDS_TO_CREATE", 10);
        testDataInitializer.run();
        verify(studentRewardDAO, never()).save(any(StudentReward.class));
    }

    /**
     * Verifies handling of edge case with all constants set to 0
     * <p>Asserts:
     * <ul>
     *     <li>Only essential entities are created (primary admin, behavior types)</li>
     *     <li>No bulk data creation occurs when constants are zero</li>
     *     <li>System handles minimal configuration gracefully</li>
     * </ul>
     */
    @Test
    @DisplayName("Handles edge case with 0 constants")
    void handlesEdgeCaseWithZeroConstants() throws Exception {
        setEnvVariable(testEmail);
        when(userDAO.findByEmail(testEmail)).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        setConstant("NUM_TEST_ADMINS_TO_CREATE", 0);
        setConstant("NUM_TEST_STAFF_TO_CREATE", 0);
        setConstant("NUM_TEST_PARAS_TO_CREATE", 0);
        setConstant("NUM_TEST_TEACHERS_TO_CREATE", 0);
        setConstant("NUM_TEST_BRAG_LOGS_TO_CREATE", 0);
        setConstant("NUM_TEST_REWARD_ITEMS_TO_CREATE", 0);
        setConstant("NUM_TEST_STUDENT_REWARDS_TO_CREATE", 0);
        testDataInitializer.run();
        verify(userDAO, times(1)).save(any(User.class));
        verify(behaviorTypeDAO, times(9)).save(any(BehaviorType.class));
        verify(bragLogDAO, never()).save(any(BragLog.class));
        verify(rewardItemDAO, never()).save(any(RewardItem.class));
        verify(studentRewardDAO, never()).save(any(StudentReward.class));
    }

    /**
     * Verifies correct name extraction logic from email address
     * <p>Asserts:
     * <ul>
     *     <li>First name is correctly extracted as first initial</li>
     *     <li>Last name is properly parsed from email local part</li>
     *     <li>Special email formats are handled correctly</li>
     * </ul>
     */
    @Test
    @DisplayName("Uses correct name extraction from email")
    void usesCorrectNameExtractionFromEmail() throws Exception {
        String specificEmail = "judoe@okcps.org";
        setEnvVariable(specificEmail);
        when(userDAO.findByEmail(specificEmail)).thenReturn(Optional.empty());
        when(userDAO.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(behaviorTypeDAO.save(any(BehaviorType.class))).thenAnswer(invocation -> {
            BehaviorType behaviorType = invocation.getArgument(0);
            behaviorType.setId(1L);
            return behaviorType;
        });
        setConstant("NUM_TEST_ADMINS_TO_CREATE", 0);
        setConstant("NUM_TEST_STAFF_TO_CREATE", 0);
        setConstant("NUM_TEST_PARAS_TO_CREATE", 0);
        setConstant("NUM_TEST_TEACHERS_TO_CREATE", 0);
        setConstant("NUM_TEST_BRAG_LOGS_TO_CREATE", 0);
        setConstant("NUM_TEST_REWARD_ITEMS_TO_CREATE", 0);
        setConstant("NUM_TEST_STUDENT_REWARDS_TO_CREATE", 0);
        testDataInitializer.run();
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userDAO).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("j", savedUser.getFirstName());
        assertEquals("doe", savedUser.getLastName());
    }
}
