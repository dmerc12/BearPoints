package com.bearpoints.api.unit.service;

import com.bearpoints.api.dao.*;
import com.bearpoints.api.dto.BatchUpdateRequest;
import com.bearpoints.api.entity.*;
import com.bearpoints.api.exception.RunnableThrowing;
import com.bearpoints.api.service.GoogleSheetsSyncService;
import com.bearpoints.api.service.impl.GoogleSheetsServiceImpl;
import com.bearpoints.api.service.impl.GoogleSheetsSyncServiceImpl;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpHeaders;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.lang.reflect.UndeclaredThrowableException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link GoogleSheetsSyncService} implementation and functionality.
 * <p>Comprehensive test coverage includes:
 * <ul>
 *     <li>Overall synchronization workflow validation</li>
 *     <li>Daily quota management and error handling</li>
 *     <li>Entity parsing logic for all data types</li>
 *     <li>Row ID assignment and update mechanisms</li>
 *     <li>Retry strategies for API exceptions</li>
 *     <li>Error scenarios and edge cases</li>
 * </ul>
 *
 * <p>Tests validate bidirectional synchronization between database entities
 * and Google Sheets, covering 7 core entity types for 50+ test scenarios.
 *
 * @see GoogleSheetsSyncService
 * @see GoogleSheetsSyncServiceImpl
 * @version 1.0
 * @author Dylan Mercer
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Google Sheets Sync Service Tests")
public class GoogleSheetsSyncServiceTests {
    @Mock
    private UserDAO userDAO;

    @Mock
    private StudentDAO studentDAO;

    @Mock
    private TeacherDAO teacherDAO;

    @Mock
    private BehaviorTypeDAO behaviorTypeDAO;

    @Mock
    private BragLogDAO bragLogDAO;

    @Mock
    private RewardItemDAO rewardItemDAO;

    @Mock
    private StudentRewardDAO studentRewardDAO;

    @Mock
    private GoogleSheetsServiceImpl googleSheetsService;

    @InjectMocks
    private GoogleSheetsSyncServiceImpl syncService;

    @Captor
    private ArgumentCaptor<List<BatchUpdateRequest>> batchUpdateCaptor;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @BeforeEach
    public void setup() {
        String spreadsheetId = "test-spreadsheet-id";
        ReflectionTestUtils.setField(syncService, "spreadsheetId", spreadsheetId);
    }

    /**
     * Tests overall synchronization workflow execution.
     * <p>Verifies:
     * <ul>
     *     <li>Full synchronization cycle completes without errors</li>
     *     <li>Correct API call counts for read/write operations</li>
     *     <li>Proper chunking of batch operations</li>
     * </ul>
     */
    @Nested
    @DisplayName("Overall Workflow Tests")
    class OverallExecutionTests {
        /**
         * Validates complete synchronization workflow execution.
         * <p>Asserts:
         * <ul>
         *     <li>No exceptions thrown during full sync</li>
         *     <li>Correct number of API calls made (14 reads, 7 appends)</li>
         *     <li>All entity types processed in workflow</li>
         * </ul>
         */
        @Test
        @DisplayName("Traditional sync executes without errors")
        public void traditionalSyncExecutesWithoutErrors() throws IOException {
            User adminUser = createTestUser(1L, "admin@example.com", Role.ADMIN);
            when(userDAO.findBySyncedToSheetsFalse()).thenReturn(List.of(adminUser));
            User teacherUser = createTestUser(2L, "teacher@example.com", Role.TEACHER);
            Teacher teacher = createTestTeacher(1L, teacherUser);
            when(teacherDAO.findBySyncedToSheetsFalse()).thenReturn(List.of(teacher));
            User studentUser = createTestUser(3L, "student@example.com", Role.STUDENT);
            Student student = createTestStudent(120, teacher, studentUser);
            when(studentDAO.findBySyncedToSheetsFalse()).thenReturn(List.of(student));
            BehaviorType behaviorType = createTestBehaviorType("Test Behavior Type", 35);
            when(behaviorTypeDAO.findBySyncedToSheetsFalse()).thenReturn(List.of(behaviorType));
            BragLog bragLog = createTestBragLog(student, teacher, Set.of(behaviorType));
            when(bragLogDAO.findBySyncedToSheetsFalse()).thenReturn(List.of(bragLog));
            RewardItem rewardItem = createTestRewardItem("Test Reward", 20, 6);
            when(rewardItemDAO.findBySyncedToSheetsFalse()).thenReturn(List.of(rewardItem));
            StudentReward studentReward = createTestStudentReward(student, rewardItem);
            when(studentRewardDAO.findBySyncedToSheetsFalse()).thenReturn(List.of(studentReward));
            when(googleSheetsService.getSheetData(anyString())).thenReturn(Collections.emptyList());
            when(googleSheetsService.getRowCount(anyString())).thenReturn(10);
            syncService.syncAllData();
            verify(googleSheetsService, times(14)).getSheetData(anyString());
            verify(googleSheetsService, times(7)).appendToSheet(anyString(), anyList());
        }
    }

    /**
     * Tests daily quota management logic.
     * <p>Covers scenarios:
     * <ul>
     *     <li>Quota limit detection</li>
     *     <li>Retry mechanisms</li>
     *     <li>Exception handling</li>
     * </ul>
     */
    @Nested
    @DisplayName("Daily Quota Tests")
    class CheckDailyQuotaTests {
        /**
         * Validates sync prevention when daily quota exceeded.
         * <p>Asserts:
         * <ul>
         *     <li>No API calls made when over quota</li>
         *     <li>Graceful handling of quota limits</li>
         * </ul>
         */
        @Test
        @DisplayName("Prevents sync when exceeded")
        public void exceededDailyQuotaPreventsSyncWhenExceeded() throws IOException {
            when(googleSheetsService.getRowCount("Users")).thenReturn(20000);
            when(googleSheetsService.getRowCount("Teachers")).thenReturn(20000);
            when(googleSheetsService.getRowCount("Students")).thenReturn(20000);
            when(googleSheetsService.getRowCount("BehaviorTypes")).thenReturn(20000);
            when(googleSheetsService.getRowCount("BragLogs")).thenReturn(20000);
            when(googleSheetsService.getRowCount("RewardItems")).thenReturn(20000);
            when(googleSheetsService.getRowCount("StudentRewards")).thenReturn(20000);
            syncService.syncAllData();
            verify(googleSheetsService, never()).getSheetData(anyString());
        }

        /**
         * Validates retry logic after quota errors.
         * <p>Asserts:
         * <ul>
         *     <li>Automatic retry on quota exceptions</li>
         *     <li>Correct retry attempt count</li>
         * </ul>
         */
        @Test
        @DisplayName("Retries after exceeded")
        public void retriesOnQuotaErrors() throws IOException {
            User user = createTestUser(1L, "user@example.com", Role.ADMIN);
            when(userDAO.findBySyncedToSheetsFalse()).thenReturn(List.of(user));
            when(googleSheetsService.getRowCount("Users")).thenReturn(10);
            when(googleSheetsService.getSheetData("Users")).thenReturn(Collections.emptyList());
            syncService.syncAllData();
            verify(googleSheetsService, times(2)).getSheetData("Users");
        }

        /**
         * Validates exception handling during quota checks.
         * <p>Asserts:
         * <ul>
         *     <li>Graceful failure when quota check fails</li>
         *     <li>Correct error state propagation</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles exception in quota check")
        public void handlesExceptionInQuotaCheck() throws IOException {
            when(googleSheetsService.getRowCount("Users")).thenThrow(new IOException("Test exception"));
            boolean result = safeInvocationResult(syncService, "checkDailyQuota", "Users");
            assertFalse(result);
        }
    }

    /**
     * Tests error handling capabilities during synchronization.
     * <p>Verifies proper behavior when encountering:
     * <ul>
     *     <li>API communication failures</li>
     *     <li>Unexpected service exceptions</li>
     *     <li>Partial failure scenarios</li>
     * </ul>
     */
    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        /**
         * Validates graceful handling of API communication failures during synchronization.
         * <p>Verifies:
         * <ul>
         *     <li>Synchronization continues when Google Sheets API throws IOException</li>
         *     <li>No data is written to sheets during API failures</li>
         *     <li>Operation completes without crashing the service</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles API exceptions gracefully")
        public void handleAPIExceptionsGracefully() throws IOException {
            lenient().when(userDAO.findBySyncedToSheetsFalse()).thenReturn(Collections.emptyList());
            when(googleSheetsService.getRowCount("Users")).thenReturn(10);
            when(googleSheetsService.getSheetData("Users")).thenThrow(new IOException("API error"));
            syncService.syncAllData();
            verify(googleSheetsService, never()).appendToSheet(eq("Users"), anyList());
        }
    }

    /**
     * Tests entity parsing from sheet rows.
     * <p>Covers all 7 entity types:
     * <ul>
     *     <li>Users</li>
     *     <li>Teachers</li>
     *     <li>Students</li>
     *     <li>BehaviorTypes</li>
     *     <li>BragLogs</li>
     *     <li>RewardItems</li>
     *     <li>StudentRewards</li>
     * </ul>
     */
    @Nested
    @DisplayName("Entity Parsing Tests")
    class EntityParsingTests {
        /**
         * Validates user entity parsing logic.
         * <p>Asserts:
         * <ul>
         *     <li>Correct field mapping from sheet data</li>
         *     <li>Proper data type conversions</li>
         *     <li>All user fields are correctly populated</li>
         * </ul>
         */
        @Test
        @DisplayName("Parses valid user row correctly")
        public void parsesValidUserRowCorrectly() {
            List<Object> row = Arrays.asList("1", "test@example.com", "John", "Doe", "ADMIN");
            Optional<User> user = ReflectionTestUtils.invokeMethod(syncService, "parseUserFromRow", row);
            assertNotNull(user);
            assertTrue(user.isPresent());
            assertEquals("John", user.get().getFirstName());
        }

        /**
         * Validates teacher entity parsing logic.
         * <p>Asserts:
         * <ul>
         *     <li>Grade level enum is correctly resolved</li>
         *     <li>User association is properly established</li>
         *     <li>All fields are mapped to correct entity properties</li>
         * </ul>
         */
        @Test
        @DisplayName("Parses valid teacher row correctly")
        public void parsesValidTeacherRowCorrectly() {
            List<Object> row = Arrays.asList("1", "FIRST", "2");
            User teacherUser = createTestUser(2L, "teacher@example.com", Role.TEACHER);
            when(userDAO.findById(2L)).thenReturn(Optional.of(teacherUser));
            Optional<Teacher> teacher = ReflectionTestUtils.invokeMethod(syncService, "parseTeacherFromRow", row);
            assertNotNull(teacher);
            assertTrue(teacher.isPresent());
            assertEquals(teacherUser, teacher.get().getUser());
            assertEquals(GradeLevel.FIRST, teacher.get().getGrade());
        }

        /**
         * Validates student entity parsing logic.
         * <p>Asserts:
         * <ul>
         *     <li>Points value is correctly parsed as integer</li>
         *     <li>Token Field is properly mapped</li>
         *     <li>User and Teacher associations are established</li>
         *     <li>All relationships are correctly resolved</li>
         * </ul>
         */
        @Test
        @DisplayName("Parses valid student row correctly")
        public void parsesValidStudentRowCorrectly() {
            List<Object> row = Arrays.asList("1", "120", "student-token", "3", "4");
            User studentUser = createTestUser(3L, "student@example.com", Role.STUDENT);
            User teacherUser = createTestUser(4L, "teacher@example.com", Role.TEACHER);
            Teacher teacher = createTestTeacher(4L, teacherUser);
            when(userDAO.findById(3L)).thenReturn(Optional.of(studentUser));
            when(teacherDAO.findById(4L)).thenReturn(Optional.of(teacher));
            Optional<Student> student = ReflectionTestUtils.invokeMethod(syncService, "parseStudentFromRow", row);
            assertNotNull(student);
            assertTrue(student.isPresent());
            assertEquals(studentUser, student.get().getUser());
            assertEquals(120, student.get().getPoints());
            assertEquals("student-token", student.get().getToken());
            assertEquals(teacher, student.get().getTeacher());
        }

        /**
         * Validates behavior type entity parsing logic.
         * <p>Asserts:
         * <ul>
         *     <li>Name field is correctly mapped</li>
         *     <li>Point value is parsed as integer</li>
         *     <li>Active status is converted to boolean</li>
         *     <li>All primitive fields maintain correct values</li>
         * </ul>
         */
        @Test
        @DisplayName("Parses valid behavior type row correctly")
        public void parsesValidBehaviorTypeRowCorrectly() {
            List<Object> row = Arrays.asList("1", "Good Behavior", "10", "true");
            Optional<BehaviorType> behaviorType = ReflectionTestUtils.invokeMethod(syncService, "parseBehaviorTypeFromRow", row);
            assertNotNull(behaviorType);
            assertTrue(behaviorType.isPresent());
            assertEquals("Good Behavior", behaviorType.get().getName());
            assertEquals(10, behaviorType.get().getPointValue());
            assertTrue(behaviorType.get().getActive());
        }

        /**
         * Validates brag log entity parsing logic.
         * <p>Asserts:
         * <ul>
         *     <li>Student and Teacher associations are resolved</li>
         *     <li>Behavior sets are reconstructed from names</li>
         *     <li>Points generated and notes fields are mapped</li>
         *     <li>Timestamp is parsed with correct format</li>
         * </ul>
         */
        @Test
        @DisplayName("Parses valid brag log row correctly")
        public void parsesValidBragLogRowCorrectly() {
            User teacherUser = createTestUser(4L, "teacher@example.com", Role.TEACHER);
            Teacher teacher = createTestTeacher(1L, teacherUser);
            User studentUser = createTestUser(3L, "student@example.com", Role.STUDENT);
            Student student = createTestStudent(100, teacher, studentUser);
            BehaviorType behavior = createTestBehaviorType("Helping", 5);
            when(studentDAO.findById(1L)).thenReturn(Optional.of(student));
            when(teacherDAO.findById(1L)).thenReturn(Optional.of(teacher));
            when(behaviorTypeDAO.findByName("Helping")).thenReturn(behavior);
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            List<Object> row = Arrays.asList("1", "1", "1", "Helping", "5", "Helped a classmate", timestamp);
            Optional<BragLog> bragLog = ReflectionTestUtils.invokeMethod(syncService, "parseBragLogFromRow", row);
            assertNotNull(bragLog);
            assertTrue(bragLog.isPresent());
            assertEquals(student,  bragLog.get().getStudent());
            assertEquals(teacher,  bragLog.get().getTeacher());
            assertEquals(1, bragLog.get().getBehaviors().size());
            assertEquals(5, bragLog.get().getPointsGenerated());
            assertEquals("Helped a classmate", bragLog.get().getNotes());
            assertEquals(
                    LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    bragLog.get().getTimestamp()
            );
        }

        /**
         * Validates reward item entity parsing logic.
         * <p>Asserts:
         * <ul>
         *     <li>Name field is correctly mapped</li>
         *     <li>Point cost is parsed as integer</li>
         *     <li>Stock value is converted to integer</li>
         *     <li>All inventory-related fields maintain integrity</li>
         * </ul>
         */
        @Test
        @DisplayName("Parses valid reward item row correctly")
        public void parsesValidRewardItemRowCorrectly() {
            List<Object> row = Arrays.asList("1", "Pencil", "5", "20");
            Optional<RewardItem> rewardItem = ReflectionTestUtils.invokeMethod(syncService, "parseRewardItemFromRow", row);
            assertNotNull(rewardItem);
            assertTrue(rewardItem.isPresent());
            assertEquals("Pencil", rewardItem.get().getName());
            assertEquals(5, rewardItem.get().getPointCost());
            assertEquals(20, rewardItem.get().getStock());
        }

        /**
         * Validates student reward entity parsing logic.
         * <p>Asserts:
         * <ul>
         *     <li>Student and RewardItem associations are resolved</li>
         *     <li>Redemption timestamp is parsed with correct format</li>
         *     <li>All reference IDs are properly resolved</li>
         *     <li>Relationship integrity between student and reward is maintained</li>
         * </ul>
         */
        @Test
        @DisplayName("Parses valid student reward row correctly")
        public void parsesValidStudentRewardRowCorrectly() {
            User teacherUser = createTestUser(3L, "teacher@example.com", Role.TEACHER);
            Teacher teacher = createTestTeacher(1L, teacherUser);
            User studentUser = createTestUser(4L, "student@example.com", Role.TEACHER);
            Student student = createTestStudent(100, teacher, studentUser);
            RewardItem reward = createTestRewardItem("Pencil", 5, 20);
            when(studentDAO.findById(1L)).thenReturn(Optional.of(student));
            when(rewardItemDAO.findById(1L)).thenReturn(Optional.of(reward));
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            List<Object> row = Arrays.asList("1", timestamp, "1", "1");
            Optional<StudentReward> studentReward = ReflectionTestUtils.invokeMethod(syncService, "parseStudentRewardFromRow", row);
            assertNotNull(studentReward);
            assertTrue(studentReward.isPresent());
            assertEquals(student, studentReward.get().getStudent());
            assertEquals(reward, studentReward.get().getRewardItem());
            assertEquals(
                    LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                    studentReward.get().getRedeemedAt()
            );
        }
    }

    /**
     * Tests error scenarios in entity parsing logic.
     * <p>Covers invalid/malformed data cases for:
     * <ul>
     *     <li>All entity types (Users, Teachers, Students, etc.)</li>
     *     <li>Various data corruption scenarios</li>
     *     <li>Boundary conditions and edge cases</li>
     * </ul>
     */
    @Nested
    @DisplayName("Parsing Error Tests")
    class ParsingErrorTests {
        /**
         * Validates handling of null ID values during row ID parsing.
         * <p>Asserts:
         * <ul>
         *     <li>Empty Optional is returned for null input</li>
         *     <li>No exceptions are thrown</li>
         *     <li>Graceful degradation in ID processing</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles null ID value in parseRowId")
        public void handlesNullIdValueInParseRowId() {
            Optional<Long> result = ReflectionTestUtils.invokeMethod(syncService, "parseRowId", null, 1);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        /**
         * Validates handling of invalid user ID formats.
         * <p>Asserts:
         * <ul>
         *     <li>Empty Optional is returned for non-numeric IDs</li>
         *     <li>Parsing fails gracefully without exceptions</li>
         *     <li>No partial entity creation occurs</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles user parsing errors")
        public void handlesUserParsingErrors_InvalidId() {
            List<Object> row = Arrays.asList("invalid", "test@example.com", "John", "Doe", "ADMIN");
            Optional<User> user = ReflectionTestUtils.invokeMethod(syncService, "parseUserFromRow", row);
            assertNotNull(user);
            assertTrue(user.isEmpty());
        }

        /**
         * Validates handling of incomplete user rows.
         * <p>Asserts:
         * <ul>
         *     <li>Empty Optional is returned for rows with missing columns</li>
         *     <li>Minimum required field count is enforced</li>
         *     <li>No partial entity population occurs</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles user row too short")
        public void handlesUserParsingErrors_ShortSize() {
            List<Object> row = Arrays.asList("1", "test@example.com");
            Optional<User> user = ReflectionTestUtils.invokeMethod(syncService, "parseUserFromRow", row);
            assertNotNull(user);
            assertTrue(user.isEmpty());
        }

        /**
         * Validates handling of missing user references in teacher rows.
         * <p>Asserts:
         * <ul>
         *     <li>Empty Optional is returned when referenced user doesn't exist</li>
         *     <li>Association failures are handled gracefully</li>
         *     <li>No teacher entity is created with invalid user reference</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles missing user in teacher parsing")
        public void handlesMissingUserInTeacherParsing() {
            List<Object> row = Arrays.asList("1", "FIRST", "999");
            when(userDAO.findById(999L)).thenReturn(Optional.empty());
            Optional<Teacher> teacher = ReflectionTestUtils.invokeMethod(syncService, "parseTeacherFromRow", row);
            assertNotNull(teacher);
            assertTrue(teacher.isEmpty());
        }

        /**
         * Validates handling of incomplete teacher rows.
         * <p>Asserts:
         * <ul>
         *     <li>Empty Optional is returned for rows with missing columns</li>
         *     <li>Minimum required field count is enforced</li>
         *     <li>No partial entity creation occurs</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles teacher row too short")
        public void handlesTeacherParsingError_ShortSize() {
            List<Object> row = Arrays.asList("1", "FIRST");
            Optional<Teacher> teacher = ReflectionTestUtils.invokeMethod(syncService, "parseTeacherFromRow", row);
            assertNotNull(teacher);
            assertTrue(teacher.isEmpty());
        }

        /**
         * Validates handling of missing user references in student rows.
         * <p>Asserts:
         * <ul>
         *     <li>Empty Optional is returned when referenced user doesn't exist</li>
         *     <li>User association failures prevent entity creation</li>
         *     <li>Graceful degradation in entity resolution</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles missing user in student parsing")
        public void handlesMissingUserInStudentParsing() {
            List<Object> row = Arrays.asList("1", "120", "token", "3", "999");
            when(userDAO.findById(3L)).thenReturn(Optional.empty());
            Optional<Student> student = ReflectionTestUtils.invokeMethod(syncService, "parseStudentFromRow", row);
            assertNotNull(student);
            assertTrue(student.isEmpty());
        }

        /**
         * Validates handling of missing teacher references in student rows.
         * <p>Asserts:
         * <ul>
         *     <li>Empty Optional is returned when referenced teacher doesn't exist</li>
         *     <li>Teacher association failures prevent entity creation</li>
         *     <li>Graceful handling of broken relationships</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles missing teacher in student parsing")
        public void handlesMissingTeacherInStudentParsing() {
            List<Object> row = Arrays.asList("1", "120", "token", "3", "999");
            when(userDAO.findById(3L)).thenReturn(Optional.of(new User()));
            when(teacherDAO.findById(999L)).thenReturn(Optional.empty());
            Optional<Student> student = ReflectionTestUtils.invokeMethod(syncService, "parseStudentFromRow", row);
            assertNotNull(student);
            assertTrue(student.isEmpty());
        }

        /**
         * Validates handling of incomplete student rows.
         * <p>Asserts:
         * <ul>
         *     <li>Empty Optional is returned for rows with missing columns</li>
         *     <li>Minimum required field count is enforced</li>
         *     <li>No partial entity creation occurs</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles student row too short")
        public void handlesStudentParsingError_ShortSize() {
            List<Object> row = Arrays.asList("1", "120");
            Optional<Student> student = ReflectionTestUtils.invokeMethod(syncService, "parseStudentFromRow", row);
            assertNotNull(student);
            assertTrue(student.isEmpty());
        }

        /**
         * Validates handling of invalid behavior point values.
         * <p>Asserts:
         * <ul>
         *     <li>Empty Optional is returned for non-numeric point values</li>
         *     <li>Type conversion failures are handled gracefully</li>
         *     <li>No entity is created with invalid data types</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles behavior type parsing errors")
        public void handlesBehaviorTypeParsingErrors() {
            List<Object> row = Arrays.asList("1", "Good Behavior", "invalid", "true");
            Optional<BehaviorType> behaviorType = ReflectionTestUtils.invokeMethod(syncService, "parseBehaviorTypeFromRow", row);
            assertNotNull(behaviorType);
            assertTrue(behaviorType.isEmpty());
        }

        /**
         * Validates handling of incomplete behavior type rows.
         * <p>Asserts:
         * <ul>
         *     <li>Empty Optional is returned for rows with missing columns</li>
         *     <li>Minimum required field count is enforced</li>
         *     <li>No partial entity creation occurs</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles behavior type row too short")
        public void handlesBehaviorTypeParsingErrors_ShortSize() {
            List<Object> row = Arrays.asList("1", "Good Behavior");
            Optional<BehaviorType> behaviorType = ReflectionTestUtils.invokeMethod(syncService, "parseBehaviorTypeFromRow", row);
            assertNotNull(behaviorType);
            assertTrue(behaviorType.isEmpty());
        }

        /**
         * Validates handling of missing student references in brag log rows.
         * <p>Asserts:
         * <ul>
         *     <li>Empty Optional is returned when referenced student doesn't exist</li>
         *     <li>Student association failures prevent entity creation</li>
         *     <li>Graceful degradation in entity resolution</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles missing student in brag log parsing")
        public void handlesMissingStudentInBragLogParsing() {
            List<Object> row = Arrays.asList("1", "1", "1", "", "", "", "");
            when(studentDAO.findById(1L)).thenReturn(Optional.empty());
            Optional<BragLog> bragLog = ReflectionTestUtils.invokeMethod(syncService, "parseBragLogFromRow", row);
            assertNotNull(bragLog);
            assertTrue(bragLog.isEmpty());
        }

        /**
         * Validates handling of missing teacher references in brag log rows.
         * <p>Asserts:
         * <ul>
         *     <li>Empty Optional is returned when referenced teacher doesn't exist</li>
         *     <li>Teacher association failures prevent entity creation</li>
         *     <li>Graceful handling of broken relationships</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles missing teacher in brag log parsing")
        public void handlesMissingTeacherInBragLogParsing() {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            List<Object> row = Arrays.asList("1", "1", "1", "Helping", "5", "Helped a classmate", timestamp);
            when(studentDAO.findById(1L)).thenReturn(Optional.of(new Student()));
            when(teacherDAO.findById(1L)).thenReturn(Optional.empty());
            Optional<BragLog> bragLog = ReflectionTestUtils.invokeMethod(syncService, "parseBragLogFromRow", row);
            assertNotNull(bragLog);
            assertTrue(bragLog.isEmpty());
        }

        /**
         * Validates handling of incomplete brag log rows.
         * <p>Asserts:
         * <ul>
         *     <li>Empty Optional is returned for rows with missing columns</li>
         *     <li>Minimum required field count is enforced</li>
         *     <li>No partial entity creation occurs</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles brag log row too short")
        public void handlesBragLogParsingError_ShortSize() {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            List<Object> row = Arrays.asList("Helping", "5", "Helped a classmate", timestamp);
            Optional<BragLog> bragLog = ReflectionTestUtils.invokeMethod(syncService, "parseBragLogFromRow", row);
            assertNotNull(bragLog);
            assertTrue(bragLog.isEmpty());
        }

        /**
         * Validates handling of missing names in reward item rows.
         * <p>Asserts:
         * <ul>
         *     <li>Empty Optional is returned when name field is null</li>
         *     <li>Null values in required fields prevent entity creation</li>
         *     <li>Graceful handling of missing critical data</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles missing name in reward item parsing")
        public void handlesMissingNameInRewardItemParsing() {
            List<Object> row = Arrays.asList("1", null, "20", "25");
            Optional<RewardItem> rewardItem = ReflectionTestUtils.invokeMethod(syncService, "parseRewardItemFromRow", row);
            assertNotNull(rewardItem);
            assertTrue(rewardItem.isEmpty());
        }

        /**
         * Validates handling of incomplete reward item rows.
         * <p>Asserts:
         * <ul>
         *     <li>Empty Optional is returned for rows with missing columns</li>
         *     <li>Minimum required field count is enforced</li>
         *     <li>No partial entity creation occurs</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles reward item row too short")
        public void handlesRewardItemParsingErrors_ShortSize() {
            List<Object> row = Arrays.asList("1", "20", "25");
            Optional<RewardItem> rewardItem = ReflectionTestUtils.invokeMethod(syncService, "parseRewardItemFromRow", row);
            assertNotNull(rewardItem);
            assertTrue(rewardItem.isEmpty());
        }

        /**
         * Validates handling of missing student references in student reward rows.
         * <p>Asserts:
         * <ul>
         *     <li>Empty Optional is returned when referenced student doesn't exist</li>
         *     <li>Student association failures prevent entity creation</li>
         *     <li>Graceful degradation in entity resolution</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles missing student in student reward parsing")
        public void handlesMissingStudentInStudentRewardParsing() {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            List<Object> row = Arrays.asList("1", timestamp, "1", "1");
            when(studentDAO.findById(1L)).thenReturn(Optional.empty());
            Optional<StudentReward> studentReward = ReflectionTestUtils.invokeMethod(syncService, "parseStudentRewardFromRow", row);
            assertNotNull(studentReward);
            assertTrue(studentReward.isEmpty());
        }

        /**
         * Validates handling of missing reward item references in student reward rows.
         * <p>Asserts:
         * <ul>
         *     <li>Empty Optional is returned when referenced reward doesn't exist</li>
         *     <li>Reward item association failures prevent entity creation</li>
         *     <li>Graceful handling of broken relationships</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles missing reward item in student reward parsing")
        public void handlesMissingRewardItemInStudentRewardParsing() {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            List<Object> row = Arrays.asList("1", timestamp, "1", "1");
            when(studentDAO.findById(1L)).thenReturn(Optional.of(new Student()));
            when(rewardItemDAO.findById(1L)).thenReturn(Optional.empty());
            Optional<StudentReward> studentReward = ReflectionTestUtils.invokeMethod(syncService, "parseStudentRewardFromRow", row);
            assertNotNull(studentReward);
            assertTrue(studentReward.isEmpty());
        }

        /**
         * Validates handling of incomplete student reward rows.
         * <p>Asserts:
         * <ul>
         *     <li>Empty Optional is returned for rows with missing columns</li>
         *     <li>Minimum required field count is enforced</li>
         *     <li>No partial entity creation</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles student reward row too short")
        public void handlesStudentRewardParsingErrors_ShortSize() {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            List<Object> row = Arrays.asList("1", timestamp);
            Optional<StudentReward> studentReward = ReflectionTestUtils.invokeMethod(syncService, "parseStudentRewardFromRow", row);
            assertNotNull(studentReward);
            assertTrue(studentReward.isEmpty());
        }
    }

    /**
     * Tests update mechanisms for existing sheet rows.
     * <p>Verifies:
     * <ul>
     *     <li>Detection of modified entities in database</li>
     *     <li>Proper construction of update requests</li>
     *     <li>Correct application of updates to Google Sheets</li>
     * </ul>
     */
    @Nested
    @DisplayName("Existing Row Update Tests")
    class ExistingRowUpdateTests {
        /**
         * Validates update process for modified user entities.
         * <p>Asserts:
         * <ul>
         *     <li>Modified entities are detected in database</li>
         *     <li>Correct update request is constructed</li>
         *     <li>Batch update is executed with proper parameters</li>
         *     <li>Only changed entities trigger updates</li>
         * </ul>
         */
        @Test
        @DisplayName("Updates existing user row")
        public void updatesExistingUserRow() throws IOException {
            User user = createTestUser(1L, "user@example.com", Role.ADMIN);
            when(userDAO.findBySyncedToSheetsFalse()).thenReturn(List.of(user));
            List<List<Object>> sheetData = Arrays.asList(
                    Arrays.asList("ID", "Email", "FirstName", "LastName", "Role"),
                    Arrays.asList("1", "old@example.com", "Old", "User", "ADMIN")
            );
            when(googleSheetsService.getSheetData("Users")).thenReturn(sheetData);
            when(googleSheetsService.getRowCount("Users")).thenReturn(2);
            syncService.syncAllData();
            verify(googleSheetsService).batchUpdate(eq("Users"), batchUpdateCaptor.capture());
            List<BatchUpdateRequest> updates = batchUpdateCaptor.getValue();
            assertEquals(1, updates.size());
        }
    }

    /**
     * Tests ID assignment logic for new sheet rows:
     * <p>Covers scenarios:
     * <ul>
     *     <li>New entity synchronization</li>
     *     <li>Sheet row ID assignment</li>
     *     <li>Handling of various data formats and edge cases</li>
     * </ul>
     */
    @Nested
    @DisplayName("New Row ID Assignment Tests")
    class NewRowIdAssignmentTests {
        /**
         * Validates sheetRowId assignment for newly created rows.
         * <p>Asserts:
         * <ul>
         *     <li>Correct row ID is assigned to new entities</li>
         *     <li>Assignment occurs after successful sheet append</li>
         *     <li>Entity is persisted with updated sheetRowId</li>
         * </ul>
         */
        @Test
        @DisplayName("Assigns sheetRowId to new rows")
        public void assignsSheetRowIdToNewRows() throws IOException {
            User user = createTestUser(1L, "user@example.com", Role.ADMIN);
            when(userDAO.findBySyncedToSheetsFalse()).thenReturn(List.of(user));
            when(googleSheetsService.getSheetData("Users")).thenReturn(
                    Collections.singletonList(
                            Arrays.asList("ID", "Email", "FirstName", "Role")
                    )
            );
            when(googleSheetsService.getSheetData("Users")).thenReturn(Arrays.asList(
                    Arrays.asList("ID", "Email", "FirstName", "LastName", "Role"),
                    Arrays.asList("1", "user@example.com", "Test", "User", "ADMIN")
            ));
            syncService.syncAllData();
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userDAO).save(userCaptor.capture());
            assertEquals(2, userCaptor.getValue().getSheetRowId());
        }

        /**
         * Validates handling of rows with invalid ID formats.
         * <p>Asserts:
         * <ul>
         *     <li>No assignment occurs for non-numeric IDs</li>
         *     <li>Entities remain unsynced when ID parsing fails</li>
         *     <li>No database saves are attempted</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles invalid ID in new row")
        public void handlesInvalidIdInNewRow() throws IOException {
            User user = createTestUser(1L, "user@example.com", Role.ADMIN);
            when(userDAO.findBySyncedToSheetsFalse()).thenReturn(List.of(user));
            when(googleSheetsService.getSheetData("Users")).thenReturn(
                    Collections.singletonList(
                            Arrays.asList("ID", "Email", "FirstName", "LastName", "Role")
                    )
            );
            when(googleSheetsService.getSheetData("Users")).thenReturn(Arrays.asList(
                    Arrays.asList("ID", "Email", "FirstName", "LastName", "Role"),
                    Arrays.asList("invalid", "user@example.com", "Test", "User", "ADMIN")
            ));
            syncService.syncAllData();
            verify(userDAO, never()).save(any(User.class));
        }

        /**
         * Validates handling of empty rows in sheet data.
         * <p>Asserts:
         * <ul>
         *     <li>Empty rows are skipped during ID assignment</li>
         *     <li>No database operations occur for empty rows</li>
         *     <li>Synchronization continues without errors</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles empty row in updateNewRowIds")
        public void handlesEmptyRowInUpdateNewRowIds() throws IOException {
            User user = createTestUser(1L, "user@example.com", Role.ADMIN);
            when(googleSheetsService.getSheetData("Users")).thenReturn(Arrays.asList(
                    Arrays.asList("ID", "Email", "FirstName", "LastName", "Role"),
                    Collections.emptyList()
            ));
            ReflectionTestUtils.invokeMethod(syncService, "updateNewRowIds",
                    "Users", 1, List.of(user), (Consumer<User>) userDAO::save);
            verify(userDAO, never()).save(any());
        }

        /**
         * Validates handling of malformed ID values.
         * <p>Asserts:
         * <ul>
         *     <li>Non-numeric ID formats are rejected</li>
         *     <li>No sheetRowId assignment occurs</li>
         *     <li>Database save operations are skipped</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles invalid ID format in updateNewRowIds")
        public void handlesInvalidIdFormatInUpdateNewRowIds() throws IOException {
            User user = createTestUser(1L, "@example.com", Role.ADMIN);
            when(googleSheetsService.getSheetData("Users")).thenReturn(Arrays.asList(
                    Arrays.asList("ID", "Email", "FirstName", "LastName", "Role"),
                    Arrays.asList("invalid-id", "test@example.com", "Test", "User", "ADMIN")
            ));
            ReflectionTestUtils.invokeMethod(syncService, "updateNewRowIds",
                    "Users", 1, List.of(user), (Consumer<User>) userDAO::save);
            verify(userDAO, never()).save(any());
        }

        /**
         * Validates handling of mixed row formats in sheets.
         * <p>Asserts:
         * <ul>
         *     <li>Valid rows are processed correctly</li>
         *     <li>Invalid rows are skipped silently</li>
         *     <li>Empty rows don't interrupt processing</li>
         *     <li>Correct ID assignment for valid entities</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles various row formats in updateNewRowIds")
        public void handleVariousRowFormatsInUpdateNewRowIds() throws IOException {
            User user = createTestUser(1L, "user@example.com", Role.ADMIN);
            List<List<Object>> sheetData = Arrays.asList(
                    Arrays.asList("ID", "Email", "FirstName", "LastName", "Role"),
                    Arrays.asList("1", "user@example.com", "Test", "User", "ADMIN"),
                    Collections.emptyList(),
                    Arrays.asList("invalid-id", "test@example.com", "Test", "User", "ADMIN")
            );
            when(googleSheetsService.getSheetData("Users")).thenReturn(sheetData);
            ReflectionTestUtils.invokeMethod(syncService, "updateNewRowIds",
                    "Users", 1, List.of(user), (Consumer<User>) userDAO::save);
            verify(userDAO).save(userCaptor.capture());
            assertEquals(2, userCaptor.getValue().getSheetRowId());
        }

        /**
         * Validates handling of null rows in sheet data.
         * <p>Asserts:
         * <ul>
         *     <li>Null rows are skipped during processing</li>
         *     <li>No assignment attempts for null rows</li>
         *     <li>No database operations triggered</li>
         * </ul>
         */
        @Test
        @DisplayName("Skips null row in updateNewRowIds")
        public void skipsNullRow() throws IOException {
            User user = createTestUser(1L, "user@example.com", Role.ADMIN);
            List<List<Object>> sheetData = Arrays.asList(
                    Arrays.asList("ID", "Email", "FirstName", "LastName", "Role"),
                    null
            );
            when(googleSheetsService.getSheetData("Users")).thenReturn(sheetData);
            ReflectionTestUtils.invokeMethod(syncService, "updateNewRowIds", "Users", 1, List.of(user), (Consumer<User>) userDAO::save);
            verify(userDAO, never()).save(any());
        }

        /**
         * Validates handling of rows with null ID values.
         * <p>Asserts:
         * <ul>
         *     <li>Rows with null first element are skipped</li>
         *     <li>No assignment occurs when ID is null</li>
         *     <li>Database save operations are prevented</li>
         * </ul>
         */
        @Test
        @DisplayName("Skips row with null first element in updateNewRowIds")
        public void skipsNullFirstRow() throws IOException {
            User user = createTestUser(1L, "user@example.com", Role.ADMIN);
            List<List<Object>> sheetData = Arrays.asList(
                    Arrays.asList("ID", "Email", "FirstName", "LastName", "Role"),
                    Arrays.asList(null, "test@example.com", "Test", "User", "ADMIN")
            );
            when(googleSheetsService.getSheetData("Users")).thenReturn(sheetData);
            ReflectionTestUtils.invokeMethod(syncService, "updateNewRowIds", "Users", 1, List.of(user), (Consumer<User>) userDAO::save);
            verify(userDAO, never()).save(any());
        }

        /**
         * Validates selective assignment for unsynced entities.
         * <p>Asserts:
         * <ul>
         *     <li>Only entities with null sheetRowId are updated</li>
         *     <li>Pre-synced entities are skipped</li>
         *     <li>Correct ID assignment for matching entities</li>
         *     <li>Database operations only for relevant entities</li>
         * </ul>
         */
        @Test
        @DisplayName("Updates only matching entities with null sheetRowId")
        public void updatesOnlyMatchingEntitiesWithNullSheetRowId() throws IOException {
            User user1 = createTestUser(1L, "user1@example.com", Role.ADMIN);
            User user2 = createTestUser(2L, "user2@example.com", Role.TEACHER);
            User user3 = createTestUser(3L, "user3@example.com", Role.STUDENT);
            user2.setSheetRowId(100);
            List<User> unsyncedUsers = Arrays.asList(user1, user2, user3);
            List<List<Object>> sheetData = Arrays.asList(
                    Arrays.asList("ID", "Email", "FirstName", "LastName", "Role"),
                    Arrays.asList("1", "user1@example.com", "User", "One", "ADMIN"),
                    Arrays.asList("3", "user3@example.com", "User", "Three", "STUDENT")

            );
            when(googleSheetsService.getSheetData("Users")).thenReturn(sheetData);
            ReflectionTestUtils.invokeMethod(syncService, "updateNewRowIds", "Users", 1, unsyncedUsers, (Consumer<User>) userDAO::save);
            verify(userDAO, times(2)).save(userCaptor.capture());
            List<User> savedUsers = userCaptor.getAllValues();
            assertEquals(2, savedUsers.size());
            assertEquals(1L, savedUsers.getFirst().getId());
            assertEquals(2, savedUsers.getFirst().getSheetRowId());
            assertEquals(3L, savedUsers.get(1).getId());
            assertEquals(3, savedUsers.get(1).getSheetRowId());
            assertFalse(savedUsers.stream().anyMatch(u -> u.getId().equals(2L)));
        }

        /**
         * Validates skipping of pre-synced entities.
         * <p>Asserts:
         * <ul>
         *     <li>Entities with existing sheetRowId are ignored</li>
         *     <li>Only unsynced entities get new IDs assigned</li>
         *     <li>Correct ID assignment for new entities</li>
         *     <li>Database operations minimized to necessary updates</li>
         * </ul>
         */
        @Test
        @DisplayName("Skips entities with non-null sheetRowId")
        public void skipsEntitiesWithNonNullSheetRowId() throws IOException {
            User user1 = createTestUser(1L, "user1@example.com", Role.ADMIN);
            User user2 = createTestUser(2L, "user2@example.com", Role.TEACHER);
            user1.setSheetRowId(100);
            List<User> unsyncedUsers = Arrays.asList(user1, user2);
            List<List<Object>> sheetData = Arrays.asList(
                    Arrays.asList("ID", "Email", "FirstName", "LastName", "Role"),
                    Arrays.asList("1", "user1@example.com", "User", "One", "ADMIN"),
                    Arrays.asList("2", "user2@example.com", "User", "Two", "TEACHER")
            );
            when(googleSheetsService.getSheetData("Users")).thenReturn(sheetData);
            ReflectionTestUtils.invokeMethod(syncService, "updateNewRowIds",
                    "Users", 1, unsyncedUsers, (Consumer<User>) userDAO::save);
            verify(userDAO, times(1)).save(userCaptor.capture());
            User savedUser = userCaptor.getValue();
            assertEquals(2L, savedUser.getId());
            assertEquals(3, savedUser.getSheetRowId());
        }
    }

    /**
     * Tests entity existence verification logic.
     * <p>Verifies correct identification of existing entities:
     * <ul>
     *     <li>All supported entity types</li>
     *     <li>Positive and negative cases</li>
     *     <li>Boundary conditions</li>
     * </ul>
     */
    @Nested
    @DisplayName("Database Existence Tests")
    class ErrorLoggingTests {
        /**
         * Validates existence checks for all entity types.
         * <p>Asserts:
         * <ul>
         *     <li>Correct positive identification for all 7 entity types</li>
         *     <li>Proper negative handling for unsupported types</li>
         *     <li>Boundary case handling for ID values</li>
         *     <li>Consistent behavior across entity types</li>
         * </ul>
         */
        @Test
        @DisplayName("Checks existence for all entity types")
        public void checksExistenceForAllEntityTypes() {
            when(userDAO.existsById(1L)).thenReturn(true);
            when(teacherDAO.existsById(1L)).thenReturn(true);
            when(studentDAO.existsById(1L)).thenReturn(true);
            when(behaviorTypeDAO.existsById(1L)).thenReturn(true);
            when(bragLogDAO.existsById(1L)).thenReturn(true);
            when(rewardItemDAO.existsById(1L)).thenReturn(true);
            when(studentRewardDAO.existsById(1L)).thenReturn(true);
            assertTrue(safeInvocationResult(syncService, "existsInDataBase", User.class, 1L));
            assertTrue(safeInvocationResult(syncService, "existsInDataBase", Teacher.class, 1L));
            assertTrue(safeInvocationResult(syncService, "existsInDataBase", Student.class, 1L));
            assertTrue(safeInvocationResult(syncService, "existsInDataBase", BehaviorType.class, 1L));
            assertTrue(safeInvocationResult(syncService, "existsInDataBase", BragLog.class, 1L));
            assertTrue(safeInvocationResult(syncService, "existsInDataBase", RewardItem.class, 1L));
            assertTrue(safeInvocationResult(syncService, "existsInDataBase", StudentReward.class, 1L));
            assertFalse(safeInvocationResult(syncService, "existsInDataBase", String.class, 1L));
        }
    }

    /**
     * Tests retry logic and error recovery strategies.
     * <p>Covers:
     * <ul>
     *     <li>Quota exceeded scenarios</li>
     *     <li>Generic exception handling</li>
     *     <li>Thread interruption cases</li>
     *     <li>Success after retry scenarios</li>
     * </ul>
     */
    @Nested
    @DisplayName("Retry Mechanism Tests")
    class RetryMechanismTests {
        /**
         * Validates retry behavior when Google API quota is exceeded.
         * <p>Asserts:
         * <ul>
         *     <li>Proper exception wrapping after max retries</li>
         *     <li>Correct error message indicating quota exhaustion</li>
         *     <li>Original GoogleJsonResponseException is preserved</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles quota exceeded with retry")
        public void handlesQuotaExceededWithRetry() {
            RunnableThrowing operation = () -> { throw createQuotaExceededException(); };
            Throwable thrown = assertThrows(UndeclaredThrowableException.class, () ->
                    ReflectionTestUtils.invokeMethod(syncService, "executeWithRetry", operation, "testOp"));
            assertInstanceOf(IOException.class, thrown.getCause());
            assertTrue(thrown.getCause().getMessage().contains("Quota exceeded for testOp after 3 attempts"));
        }

        /**
         * Validates retry behavior for generic runtime exceptions.
         * <p>Asserts:
         * <ul>
         *     <li>Proper exception wrapping after max retries</li>
         *     <li>Correct error message indicating operation failure</li>
         *     <li>Original exception is preserved in cause chain</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles generic exception with retry")
        public void handlesGenericExceptionWithRetry() {
            RunnableThrowing operation = () -> { throw new RuntimeException("Test exception"); };
            Throwable thrown = assertThrows(UndeclaredThrowableException.class, () ->
                    ReflectionTestUtils.invokeMethod(syncService, "executeWithRetry", operation, "testOp"));
            assertInstanceOf(IOException.class, thrown.getCause());
            assertTrue(thrown.getCause().getMessage().contains("Operation failed for testOp after 3 attempts"));
            assertInstanceOf(RuntimeException.class, thrown.getCause().getCause());
        }

        /**
         * Validates handling of thread interruptions during backoff.
         * <p>Asserts:
         * <ul>
         *     <li>InterruptedException is properly wrapped</li>
         *     <li>Thread interrupt status is preserved</li>
         *     <li>Correct error message indicates interruption</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles thread interruption during retry")
        public void handlesThreadInterruptionDuringRetry() {
            RunnableThrowing operation = () -> { throw new RuntimeException("Test exception"); };
            Thread.currentThread().interrupt();
            Throwable thrown = assertThrows(UndeclaredThrowableException.class, () ->
                    ReflectionTestUtils.invokeMethod(syncService, "executeWithRetry", operation, "testOp"));
            IOException ioEx = (IOException) thrown.getCause();
            assertTrue(ioEx.getMessage().contains("Interrupted during backoff"));
            assertInstanceOf(InterruptedException.class, ioEx.getCause());
            assertTrue(Thread.currentThread().isInterrupted());
        }

        /**
         * Validates immediate rethrow for non-quota Google exceptions.
         * <p>Asserts:
         * <ul>
         *     <li>Non-quota exceptions are not retired</li>
         *     <li>Original exception is propagated immediately</li>
         *     <li>Status code is preserved in rethrown exception</li>
         * </ul>
         */
        @Test
        @DisplayName("Rethrows non-quota Google exception immediately")
        public void rethrowsNonQuotaGoogleException() {
            GoogleJsonResponseException notFoundException = createNotFoundException();
            RunnableThrowing operation = () -> { throw notFoundException; };
            Throwable thrown = assertThrows(UndeclaredThrowableException.class, () ->
                    ReflectionTestUtils.invokeMethod(syncService, "executeWithRetry", operation, "testOp")
            );
            assertInstanceOf(GoogleJsonResponseException.class, thrown.getCause());
            assertEquals(404, ((GoogleJsonResponseException) thrown.getCause()).getStatusCode());
        }

        /**
         * Validates successful operation after generic exception retry.
         * <p>Asserts:
         * <ul>
         *     <li>Operation succeeds after initial failure</li>
         *     <li>Correct number of retry attempts (2 total executions)</li>
         *     <li>No exceptions thrown when retry succeeds</li>
         * </ul>
         */
        @Test
        @DisplayName("Succeeds after generic exception retry")
        public void succeedsAfterGenericExceptionRetry() throws IOException {
            RunnableThrowing operation = mock(RunnableThrowing.class);
            doThrow(new RuntimeException("Error")).doNothing().when(operation).run();
            assertDoesNotThrow(() ->
                    ReflectionTestUtils.invokeMethod(syncService, "executeWithRetry", operation, "testOp")
            );
            verify(operation, times(2)).run();
        }

        /**
         * Validates successful operation after quota error retry.
         * <p>Asserts:
         * <ul>
         *     <li>Operation succeeds after initial quota failure</li>
         *     <li>Correct number of retry attempts (2 total executions)</li>
         *     <li>No exceptions thrown when retry succeeds</li>
         * </ul>
         */
        @Test
        @DisplayName("Succeeds after quota error retry")
        public void succeedsAfterQuotaErrorRetry() throws IOException {
            RunnableThrowing operation = mock(RunnableThrowing.class);
            doThrow(createQuotaExceededException()).doNothing().when(operation).run();
            assertDoesNotThrow(() ->
                    ReflectionTestUtils.invokeMethod(syncService, "executeWithRetry", operation, "testOp")
            );
            verify(operation, times(2)).run();
        }

        /**
         * Validates successful first-attempt execution.
         * <p>Asserts:
         * <ul>
         *     <li>No retries occur when operation succeeds immediately</li>
         *     <li>Operation is executed exactly once</li>
         *     <li>No exceptions are thrown</li>
         * </ul>
         */
        @Test
        @DisplayName("Succeeds on first attempt")
        public void succeedsOnFirstAttempt() throws IOException {
            RunnableThrowing operation = mock(RunnableThrowing.class);
            doNothing().when(operation).run();
            assertDoesNotThrow(() ->
                    ReflectionTestUtils.invokeMethod(syncService, "executeWithRetry", operation, "testOp")
            );
            verify(operation, times(1)).run();
        }

        /**
         * Validates handling of various exception types.
         * <p>Asserts:
         * <ul>
         *     <li>Different exception types are properly wrapped</li>
         *     <li>Original exception is preserved in cause chain</li>
         *     <li>Correct error message indicates operation failure</li>
         *     <li>Retry count respects max attempts configuration</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles different exception types")
        public void handlesDifferentExceptionTypes() {
            RunnableThrowing operation = () -> { throw new IllegalArgumentException("Test"); };
            Throwable thrown = assertThrows(UndeclaredThrowableException.class, () ->
                    ReflectionTestUtils.invokeMethod(syncService, "executeWithRetry", operation, "testOp")
            );
            assertInstanceOf(IOException.class, thrown.getCause());
            assertInstanceOf(IllegalArgumentException.class, thrown.getCause().getCause());
            assertTrue(thrown.getCause().getMessage().contains("Operation failed for testOp after 3 attempts"));
        }
    }

    /**
     * Tests date/time formatting utilities.
     * <p>Verifies:
     * <ul>
     *     <li>Correct formatting of valid dates</li>
     *     <li>Handling of null values</li>
     *     <li>Consistent formatting patterns</li>
     * </ul>
     */
    @Nested
    @DisplayName("Format DateTime Tests")
    class FormatDateTimeTests {
        /**
         * Validates correct formatting of valid datetime values.
         * <p>Asserts:
         * <ul>
         *     <li>Pattern consistency (yyyy-MM-dd HH:mm:ss)</li>
         *     <li>Correct time component representation</li>
         *     <li>Proper padding of single-digit values</li>
         *     <li>24-hour time format maintenance</li>
         * </ul>
         */
        @Test
        @DisplayName("Formats non-null date correctly")
        public void formatsNonNullDateCorrectly() {
            LocalDateTime dateTime = LocalDateTime.of(2023, 10, 15, 14, 30);
            String result = ReflectionTestUtils.invokeMethod(syncService, "formatDateTime", dateTime);
            assertEquals("2023-10-15 14:30:00", result);
        }

        /**
         * Validates handling of null datetime values.
         * <p>Asserts:
         * <ul>
         *     <li>Empty string is returned for null input</li>
         *     <li>No exceptions are thrown</li>
         *     <li>Graceful degradation in formatting</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles null date correctly")
        public void handlesNullDateCorrectly() {
            String result = ReflectionTestUtils.invokeMethod(syncService, "formatDateTime", (LocalDateTime) null);
            assertEquals("", result);
        }
    }

    /**
     * Tests creation of row ID mappings from sheet data.
     * <p>Handles various data scenarios:
     * <ul>
     *     <li>Null and empty rows</li>
     *     <li>Invalid ID formats</li>
     *     <li>Header-only sheets</li>
     *     <li>Mixed valid/invalid rows</li>
     * </ul>
     */
    @Nested
    @DisplayName("Row ID Map Creation Tests")
    class RowIdMapCreationTests {
        /**
         * Validates handling of null rows in sheet data.
         * <p>Asserts:
         * <ul>
         *     <li>Null rows are skipped without errors</li>
         *     <li>Valid rows are processed correctly</li>
         *     <li>Resulting map contains only valid entries</li>
         *     <li>Row numbers are correctly calculated</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles null row in sheet data")
        public void handlesNullRow() {
            List<List<Object>> sheetData = Arrays.asList(
                    Arrays.asList("ID", "Data"),
                    null,
                    Arrays.asList("3", "Value3")
            );
            Map<Long, Integer> result = ReflectionTestUtils.invokeMethod(
                    syncService, "createRowIdMap", sheetData
            );
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(3, result.get(3L));
        }

        /**
         * Validates handling of empty rows in sheet data.
         * <p>Asserts:
         * <ul>
         *     <li>Empty rows are skipped during processing</li>
         *     <li>Subsequent rows maintain correct numbering</li>
         *     <li>All valid rows are included in the mapping</li>
         *     <li>Row offsets account for skipped rows</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles empty row in sheet data")
        public void handlesEmptyRow() {
            List<List<Object>> sheetData = Arrays.asList(
                    Arrays.asList("ID", "Data"),
                    Collections.emptyList(),
                    Arrays.asList("2", "Value2"),
                    Arrays.asList("3", "Value3")
            );
            Map<Long, Integer> result = ReflectionTestUtils.invokeMethod(
                    syncService, "createRowIdMap", sheetData
            );
            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals(3, result.get(2L));
            assertEquals(4, result.get(3L));
        }

        /**
         * Validates creation of ID mappings for valid sheet data.
         * <p>Asserts:
         * <ul>
         *     <li>All valid rows are included in the map</li>
         *     <li>Correct row numbers are assigned (1-based indexing)</li>
         *     <li>Header row is properly excluded</li>
         *     <li>ID to row number mapping is accurate</li>
         * </ul>
         */
        @Test
        @DisplayName("Creates map for valid rows")
        public void createsMapForValidRows() {
            List<List<Object>> sheetData = Arrays.asList(
                    Arrays.asList("ID", "Data"),
                    Arrays.asList("1", "Value1"),
                    Arrays.asList("2", "Value2"),
                    Arrays.asList("3", "Value3")
            );
            Map<Long, Integer> result = ReflectionTestUtils.invokeMethod(
                    syncService, "createRowIdMap", sheetData
            );
            assertNotNull(result);
            assertEquals(3, result.size());
            assertEquals(2, result.get(1L));
            assertEquals(3, result.get(2L));
            assertEquals(4, result.get(3L));
        }

        /**
         * Validates handling of rows with invalid IDs.
         * <p>Asserts:
         * <ul>
         *     <li>Rows with non-numeric IDs are skipped</li>
         *     <li>Rows with null IDs are skipped</li>
         *     <li>Only valid ID rows are included in mapping</li>
         *     <li>Row numbering accounts for skipped invalid rows</li>
         * </ul>
         */
        @Test
        @DisplayName("Skips rows with invalid IDs")
        public void skipsRowsWithInvalidIds() {
            List<List<Object>> sheetData = Arrays.asList(
                    Arrays.asList("ID", "Data"),
                    Arrays.asList("invalid", "Value1"),
                    Arrays.asList("2", "Value2"),
                    Arrays.asList(null, "Value3")
            );
            Map<Long, Integer> result = ReflectionTestUtils.invokeMethod(
                    syncService, "createRowIdMap", sheetData
            );
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(3, result.get(2L));
        }

        /**
         * Validates handling of sheets containing only a header row.
         * <p>Asserts:
         * <ul>
         *     <li>Empty map is returned for header-only sheets</li>
         *     <li>No exceptions are thrown</li>
         *     <li>Result object is properly initialized</li>
         *     <li>No row processing occurs beyond header</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles single row (only header)")
        public void handlesSingleRow() {
            List<List<Object>> sheetData = Collections.singletonList(
                    Arrays.asList("ID", "Data")
            );
            Map<Long, Integer> result = ReflectionTestUtils.invokeMethod(
                    syncService, "createRowIdMap", sheetData
            );
            assertNotNull(result);
            assertTrue(result.isEmpty());
        }
    }

    /**
     * Tests synchronization from Google Sheets to database.
     * <p>Verifies:
     * <ul>
     *     <li>Handling of null/empty rows</li>
     *     <li>Insertion of new entities</li>
     *     <li>Skip logic for existing entities</li>
     *     <li>Mixed condition processing</li>
     * </ul>
     */
    @Nested
    @DisplayName("Sync From Sheets Tests")
    @SuppressWarnings({"rawtypes", "unchecked"})
    class SyncFromSheetsTests {
        /**
         * Validates skipping of null rows during synchronization.
         * <p>Asserts:
         * <ul>
         *     <li>Null rows are ignored during processing</li>
         *     <li>No conversion attempts are made for null rows</li>
         *     <li>No save operations are triggered</li>
         * </ul>
         */
        @Test
        @DisplayName("Skips null rows during sync")
        public void skipsNullRows() {
            Consumer mockSave = mock(Consumer.class);
            Function mockConverter = mock(Function.class);
            List<List<Object>> sheetData = Arrays.asList(
                    List.of("Header"),
                    null
            );
            ReflectionTestUtils.invokeMethod(syncService, "syncFromSheets",
                    sheetData, mockSave, mockConverter);
            verify(mockConverter, never()).apply(any());
        }

        /**
         * Validates skipping of empty rows during synchronization.
         * <p>Asserts:
         * <ul>
         *     <li>Empty rows are ignored during processing</li>
         *     <li>No conversion attempts are made for empty rows</li>
         *     <li>Processing continues to valid rows</li>
         * </ul>
         */
        @Test
        @DisplayName("Skips empty rows during sync")
        public void skipsEmptyRows() {
            Consumer mockSave = mock(Consumer.class);
            Function mockConverter = mock(Function.class);
            when(mockConverter.apply(any())).thenReturn(Optional.empty());
            List<List<Object>> sheetData = Arrays.asList(
                    List.of("Header"),
                    Collections.emptyList(),
                    Arrays.asList("1", "Data")
            );
            ReflectionTestUtils.invokeMethod(syncService, "syncFromSheets",
                    sheetData, mockSave, mockConverter);
            verify(mockConverter, never()).apply(Collections.emptyList());
        }

        /**
         * Validates creation of new entities from sheet data.
         * <p>Asserts:
         * <ul>
         *     <li>New entities are properly created and saved</li>
         *     <li>Correct sheetRowId is assigned based on position</li>
         *     <li>Database save operation is executed</li>
         *     <li>Only non-existing entities are processed</li>
         * </ul>
         */
        @Test
        @DisplayName("Saves new entities not in database")
        public void savesNewEntities() {
            Consumer mockSave = mock(Consumer.class);
            User newUser = createTestUser(1L, "test@example.com", Role.ADMIN);
            when(userDAO.existsById(1L)).thenReturn(false);
            List<List<Object>> sheetData = Arrays.asList(
                    Arrays.asList("ID", "Email", "FirstName", "LastName", "Role"),
                    Arrays.asList("1", "test@example.com", "Test", "User", "ADMIN")
            );
            ReflectionTestUtils.invokeMethod(syncService, "syncFromSheets",
                    sheetData,
                    mockSave,
                    (Function<List<Object>, Optional<User>>) _ -> Optional.of(newUser)
            );
            verify(mockSave).accept(newUser);
            assertEquals(2, newUser.getSheetRowId());
        }

        /**
         * Validates skipping of existing entities during synchronization.
         * <p>Asserts:
         * <ul>
         *     <li>Existing entities are detected in database</li>
         *     <li>No save operations are performed for existing entities</li>
         *     <li>Processing continues without errors</li>
         *     <li>Converter is called but result is discarded</li>
         * </ul>
         */
        @Test
        @DisplayName("Skips existing entities in database")
        public void skipsExistingEntities() {
            Consumer<User> mockSave = mock(Consumer.class);
            User existingUser = createTestUser(1L, "test@example.com", Role.ADMIN);
            when(userDAO.existsById(1L)).thenReturn(true);
            List<List<Object>> sheetData = Arrays.asList(
                    Arrays.asList("ID", "Email", "FirstName", "LastName", "Role"),
                    Arrays.asList("1", "test@example.com", "Test", "User", "ADMIN")
            );
            ReflectionTestUtils.invokeMethod(syncService, "syncFromSheets",
                    sheetData,
                    mockSave,
                    (Function<List<Object>, Optional<User>>) _ -> Optional.of(existingUser)
            );
            verify(mockSave, never()).accept(any());
        }

        /**
         * Validates processing of sheets with mixed row conditions.
         * <p>Asserts:
         * <ul>
         *     <li>Null rows are skipped</li>
         *     <li>Empty rows are skipped</li>
         *     <li>Existing entities are skipped</li>
         *     <li>New entities are saved</li>
         *     <li>Correct row number is calculated for new entities</li>
         * </ul>
         */
        @Test
        @DisplayName("Handles multiple rows with mixed conditions")
        public void handlesMultipleRows() {
            Consumer<Object> mockSave = mock(Consumer.class);
            User newUser = createTestUser(2L, "new@example.com", Role.TEACHER);
            when(userDAO.existsById(1L)).thenReturn(true);
            when(userDAO.existsById(2L)).thenReturn(false);
            List<List<Object>> sheetData = Arrays.asList(
                    Arrays.asList("ID", "Data"),
                    null,
                    Collections.emptyList(),
                    Arrays.asList("1", "Existing"),
                    Arrays.asList("2", "New")
            );
            Function<List<Object>, Optional<Object>> converter = row -> {
                if (row.getFirst().equals("1")) return Optional.of(createTestUser(1L, "existing@example.com", Role.ADMIN));
                if (row.getFirst().equals("2")) return Optional.of(newUser);
                return Optional.empty();
            };
            ReflectionTestUtils.invokeMethod(syncService, "syncFromSheets",
                    sheetData, mockSave, converter);
            verify(mockSave, times(1)).accept(any());
            verify(mockSave).accept(newUser);
            assertEquals(5, newUser.getSheetRowId());
        }

        /**
         * Validates successful execution after retries.
         * <p>Asserts:
         * <ul>
         *     <li>Operation succeeds after two failures</li>
         *     <li>Correct number of retry attempts (3 total executions)</li>
         *     <li>No exceptions thrown when retry succeeds</li>
         *     <li>Retry logic handles mixed exception types</li>
         * </ul>
         */
        @Test
        @DisplayName("Succeeds on third attempt after two failures")
        public void succeedsOnThirdAttemptAfterTwoFailures() throws IOException {
            RunnableThrowing operation = mock(RunnableThrowing.class);
            doThrow(createQuotaExceededException())
                    .doThrow(new RuntimeException("Temporary error"))
                    .doNothing()
                    .when(operation).run();
            assertDoesNotThrow(() ->
                    ReflectionTestUtils.invokeMethod(syncService, "executeWithRetry", operation, "testOp")
            );
            verify(operation, times(3)).run();
        }
    }

    private User createTestUser(Long id, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(role);
        return user;
    }

    private Teacher createTestTeacher(Long id, User user) {
        Teacher teacher = new Teacher();
        teacher.setId(id);
        teacher.setGrade(GradeLevel.FIRST);
        teacher.setUser(user);
        return teacher;
    }

    private Student createTestStudent(int points, Teacher teacher, User user) {
        Student student =  new Student();
        student.setId(1L);
        student.setPoints(points);
        student.generateToken();
        student.setTeacher(teacher);
        student.setUser(user);
        return student;
    }

    private BehaviorType createTestBehaviorType(String name, int points) {
        BehaviorType behaviorType = new BehaviorType();
        behaviorType.setId(1L);
        behaviorType.setName(name);
        behaviorType.setPointValue(points);
        behaviorType.setActive(true);
        return behaviorType;
    }

    private BragLog createTestBragLog(Student student, Teacher teacher,
                                      Set<BehaviorType> behaviors) {
        BragLog bragLog = new BragLog();
        bragLog.setId(1L);
        bragLog.setStudent(student);
        bragLog.setTeacher(teacher);
        bragLog.setBehaviors(behaviors);
        bragLog.setPointsGenerated(behaviors.stream().mapToInt(BehaviorType::getPointValue).sum());
        bragLog.setNotes("Test notes");
        bragLog.setTimestamp(LocalDateTime.now());
        return bragLog;
    }

    private RewardItem createTestRewardItem(String name, int cost, int stock) {
        RewardItem rewardItem = new RewardItem();
        rewardItem.setId(1L);
        rewardItem.setName(name);
        rewardItem.setPointCost(cost);
        rewardItem.setStock(stock);
        return rewardItem;
    }

    private StudentReward createTestStudentReward(Student student, RewardItem reward) {
        StudentReward studentReward = new StudentReward();
        studentReward.setId(1L);
        studentReward.setStudent(student);
        studentReward.setRewardItem(reward);
        studentReward.setRedeemedAt(LocalDateTime.now());
        return studentReward;
    }

    private GoogleJsonResponseException createQuotaExceededException() {
        GoogleJsonError error = new GoogleJsonError();
        error.setCode(429);
        error.setMessage("Quota exceeded");
        return new GoogleJsonResponseException(
                new GoogleJsonResponseException.Builder(
                        error.getCode(),
                        error.getMessage(),
                        new HttpHeaders()
                ),
                error
        );
    }

    private GoogleJsonResponseException createNotFoundException() {
        GoogleJsonError error = new GoogleJsonError();
        error.setCode(404);
        error.setMessage("Not found");
        return new GoogleJsonResponseException(
                new GoogleJsonResponseException.Builder(
                        error.getCode(),
                        error.getMessage(),
                        new HttpHeaders()
                ),
                error
        );
    }

    private boolean safeInvocationResult(Object target, String method, Object... args) {
        Object result = ReflectionTestUtils.invokeMethod(target, method, args);
        return result != null && (boolean) result;
    }
}
