package com.bearpoints.api.service.impl;

import com.bearpoints.api.entity.*;
import com.bearpoints.api.dto.BatchUpdateRequest;
import com.bearpoints.api.entity.Syncable;
import com.bearpoints.api.exception.RunnableThrowing;
import com.bearpoints.api.dao.*;
import com.bearpoints.api.service.GoogleSheetsSyncService;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.common.collect.Lists;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Service for synchronizing application data with Google Sheets.
 *
 * <p>Performs bidirectional synchronization between database entities and Google Sheets.
 * Scheduled to run twice daily (8AM and 8PM) via cron jobs.
 * Handles synchronization for:
 * <ul>
 *     <li>Users</li>
 *     <li>Teachers</li>
 *     <li>Students</li>
 *     <li>BehaviorTypes</li>
 *     <li>BragLogs</li>
 *     <li>RewardItems</li>
 *     <li>StudentRewards</li>
 * </ul>
 *
 * <p>Features
 * <ul>
 *     <li>Batch processing with chunking (100 rows/chunk)</li>
 *     <li>Retry mechanism with exponential backoff</li>
 *     <li>Daily quota management</li>
 *     <li>Bidirectional sync (DB -> Sheets and Sheets -> DB)</li>
 *     <li>Row-level tracking using sheetRowId</li>
 * </ul>
 *
 * @see GoogleSheetsSyncService
 * @version 1.1
 * @author Dylan Mercer
 */
@Service
@Transactional
public class GoogleSheetsSyncServiceImpl implements GoogleSheetsSyncService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleSheetsSyncServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Map<String, List<String>> SHEET_HEADERS = Map.of(
            "Users", Arrays.asList("ID", "Email", "First Name", "Last Name", "Role"),
            "Teachers", Arrays.asList("ID", "Grade", "User ID"),
            "Students", Arrays.asList("ID", "Points", "Token", "User ID", "Teacher ID"),
            "BehaviorTypes", Arrays.asList("ID", "Name", "Point Value", "Active"),
            "BragLogs", Arrays.asList("ID", "Student ID", "Teacher ID", "Behaviors", "Points Generated", "Submitter Name", "Submitter User ID", "Notes", "Timestamp"),
            "RewardItems", Arrays.asList("ID", "Name", "Point Cost", "Stock"),
            "StudentRewards", Arrays.asList("ID", "Redeemed At", "Student ID", "Reward Item ID")
    );

    private final UserDAO userRepository;
    private final StudentDAO studentRepository;
    private final TeacherDAO teacherRepository;
    private final BragLogDAO bragLogRepository;
    private final RewardItemDAO rewardItemRepository;
    private final StudentRewardDAO studentRewardRepository;
    private final BehaviorTypeDAO behaviorTypeRepository;
    private final GoogleSheetsServiceImpl googleSheetsService;

    @Getter
    @Setter
    @Value("${google.sheets.spreadsheet.id}")
    private String spreadsheetId;

    public GoogleSheetsSyncServiceImpl(
            UserDAO theUserRepository,
            StudentDAO theStudentRepository,
            TeacherDAO theTeacherRepository,
            BragLogDAO theBragLogRepository,
            RewardItemDAO theRewardItemRepository,
            StudentRewardDAO theStudentRewardRepository,
            BehaviorTypeDAO theBehaviorTypeRepository,
            GoogleSheetsServiceImpl theGoogleSheetsService) {
        this.userRepository = theUserRepository;
        this.studentRepository = theStudentRepository;
        this.teacherRepository = theTeacherRepository;
        this.bragLogRepository = theBragLogRepository;
        this.rewardItemRepository = theRewardItemRepository;
        this.studentRewardRepository = theStudentRewardRepository;
        this.behaviorTypeRepository = theBehaviorTypeRepository;
        this.googleSheetsService = theGoogleSheetsService;
    }

    /**
     * Main synchronization method executed on schedule.
     * Orchestrates synchronization of all entity types with quota checks.
     */
    @Override
    @Scheduled(cron = "0 0 8,20 * * *")
    public void syncAllData() {
        try {
            logger.info("Starting Google Sheets sync process");
            if (checkDailyQuota("Users")) syncUsers();
            if (checkDailyQuota("Teachers")) syncTeachers();
            if (checkDailyQuota("Students")) syncStudents();
            if (checkDailyQuota("BehaviorTypes")) syncBehaviorTypes();
            if (checkDailyQuota("BragLogs")) syncBragLogs();
            if (checkDailyQuota("RewardItems")) syncRewardItems();
            if (checkDailyQuota("StudentRewards")) syncStudentRewards();
            logger.info("Google Sheets sync completed successfully");
        } catch (Exception e) {
            logger.error("Google Sheets sync failed", e);
        }
    }

    private void syncUsers() throws IOException {
        syncEntity(
                "Users",
                userRepository::findBySyncedToSheetsFalse,
                user -> Arrays.asList(
                        user.getId().toString(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getRole().name()
                ),
                this::parseUserFromRow,
                userRepository::save,
                users -> {
                        markAsSynced(users);
                        userRepository.saveAll(users);
                }
        );
    }

    private void syncTeachers() throws IOException {
        syncEntity(
                "Teachers",
                teacherRepository::findBySyncedToSheetsFalse,
                teacher -> Arrays.asList(
                        teacher.getId().toString(),
                        teacher.getGrade().name(),
                        teacher.getUser().getId().toString()
                ),
                this::parseTeacherFromRow,
                teacherRepository::save,
                teachers -> {
                    markAsSynced(teachers);
                    teacherRepository.saveAll(teachers);
                }
        );
    }

    private void syncStudents() throws IOException {
        syncEntity(
                "Students",
                studentRepository::findBySyncedToSheetsFalse,
                student -> Arrays.asList(
                        student.getId().toString(),
                        student.getPoints().toString(),
                        student.getToken(),
                        student.getUser().getId().toString(),
                        student.getTeacher().getId().toString()
                ),
                this::parseStudentFromRow,
                studentRepository::save,
                students -> {
                    markAsSynced(students);
                    studentRepository.saveAll(students);
                }
        );
    }

    private void syncBehaviorTypes() throws IOException {
        syncEntity(
                "BehaviorTypes",
                behaviorTypeRepository::findBySyncedToSheetsFalse,
                behaviorType -> Arrays.asList(
                        behaviorType.getId().toString(),
                        behaviorType.getName(),
                        behaviorType.getPointValue().toString(),
                        behaviorType.getActive().toString()
                ),
                this::parseBehaviorTypeFromRow,
                behaviorTypeRepository::save,
                behaviorTypes -> {
                    markAsSynced(behaviorTypes);
                    behaviorTypeRepository.saveAll(behaviorTypes);
                }
        );
    }

    private void syncBragLogs() throws IOException {
        List<BragLog> unsynced = bragLogRepository.findBySyncedToSheetsFalse();
        logger.info("Found {} unsynced BragLogs", unsynced.size());
        syncEntity(
                "BragLogs",
                bragLogRepository::findBySyncedToSheetsFalse,
                bragLog -> {
                    logger.debug("Converting BragLog {} to row", bragLog.getId());
                    String behaviors = bragLog.getBehaviors().stream()
                            .map(BehaviorType::getName)
                            .collect(Collectors.joining(", "));
                    return Arrays.asList(
                            bragLog.getId().toString(),
                            bragLog.getStudent().getId().toString(),
                            bragLog.getTeacher().getId().toString(),
                            behaviors,
                            String.valueOf(bragLog.getPointsGenerated()),
                            bragLog.getSubmitterName(),
                            bragLog.getSubmitterUser().getId().toString(),
                            bragLog.getNotes(),
                            formatDateTime(bragLog.getTimestamp())
                    );
                },
                this::parseBragLogFromRow,
                bragLogRepository::save,
                logs -> {
                    markAsSynced(logs);
                    bragLogRepository.saveAll(logs);
                }
        );
    }

    private void syncRewardItems() throws IOException {
        syncEntity(
                "RewardItems",
                rewardItemRepository::findBySyncedToSheetsFalse,
                rewardItem -> Arrays.asList(
                        rewardItem.getId().toString(),
                        rewardItem.getName(),
                        rewardItem.getPointCost().toString(),
                        rewardItem.getStock().toString()
                ),
                this::parseRewardItemFromRow,
                rewardItemRepository::save,
                rewardItems -> {
                    markAsSynced(rewardItems);
                    rewardItemRepository.saveAll(rewardItems);
                }
        );
    }

    private void syncStudentRewards() throws IOException {
        syncEntity(
                "StudentRewards",
                studentRewardRepository::findBySyncedToSheetsFalse,
                studentReward -> Arrays.asList(
                        studentReward.getId().toString(),
                        formatDateTime(studentReward.getRedeemedAt()),
                        studentReward.getStudent().getId().toString(),
                        studentReward.getRewardItem().getId().toString()
                ),
                this::parseStudentRewardFromRow,
                studentRewardRepository::save,
                studentRewards -> {
                    markAsSynced(studentRewards);
                    studentRewardRepository.saveAll(studentRewards);
                }
        );
    }

    /**
     * Ensures that the given sheet has a header row. If the sheet is empty or has no headers, the header row is appended.
     *
     * @param sheetName Name of the sheet to check
     * @throws IOException If the operation fails
     */
    private void ensureHeaders(String sheetName) throws IOException {
        logger.info("Ensuring headers for sheet {}", sheetName);
        List<List<Object>> existingData = googleSheetsService.getSheetData(sheetName);
        if (existingData == null || existingData.isEmpty()) {
            // Sheet is completely empty - add header row
            List<String> headers = SHEET_HEADERS.get(sheetName);
            if (headers == null) {
                logger.warn("No header definition for sheet: {}", sheetName);
                return;
            }
            logger.info("Sheet '{}' is empty - adding header row", sheetName);
            googleSheetsService.appendToSheet(sheetName, Collections.singletonList(headers));
        } else if (existingData.size() == 1 && existingData.getFirst().size() < SHEET_HEADERS.get(sheetName).size()) {
            // Only one row exists, but it's incomplete - replace with proper header
            logger.warn("Sheet '{}' has invalid headers, overwriting", sheetName);
            googleSheetsService.clearSheet(sheetName);
            List<String> headers = SHEET_HEADERS.get(sheetName);
            googleSheetsService.appendToSheet(sheetName, Collections.singletonList(headers));
        }
    }

    /**
     * Generic synchronization method for entities.
     *
     * @param <T> Entity type implementing Syncable
     * @param sheetName Target Google Sheet name
     * @param repositoryFindUnsynced Supplier for unsynced entities
     * @param toRowConverter Converts entity to sheet row
     * @param fromRowConverter Parses sheet row to entity
     * @param repositorySave Consumer to save entities
     * @param markAndSave Consumer to mark entities as synced
     */
    private <T extends Syncable> void syncEntity(
            String sheetName,
            Supplier<List<T>> repositoryFindUnsynced,
            Function<T, List<String>> toRowConverter,
            Function<List<Object>, Optional<T>> fromRowConverter,
            Consumer<T> repositorySave,
            Consumer<List<T>> markAndSave) throws IOException {
        // Ensure sheet headers exist and are valid
        ensureHeaders(sheetName);
        // 1. Get sheet data
        List<List<Object>> sheetData = googleSheetsService.getSheetData(sheetName);
        Map<Long, Integer> sheetRowMap = createRowIdMap(sheetData);
        // 2. Process database entities
        List<T> unsyncedEntities = repositoryFindUnsynced.get();
        List<List<String>> newData = new ArrayList<>();
        List<BatchUpdateRequest> updates = new ArrayList<>();
        for (T entity : unsyncedEntities) {
            List<String> rowData = toRowConverter.apply(entity);
            Long id = Long.parseLong(rowData.getFirst());
            if (sheetRowMap.containsKey(id)) {
                // Existing row - prepare update
                int rowNum = sheetRowMap.get(id);
                updates.add(new BatchUpdateRequest(rowNum, rowData));
                entity.setSheetRowId(rowNum);
            } else {
                // New row - prepare for append
                newData.add(rowData);
            }
        }
        // 3. Execute batch updates
        executeWithRetry(() -> executeBatchOperations(sheetName, updates, newData), "batchUpdate");
        // 4. Append new rows
        executeWithRetry(() -> updateNewRowIds(sheetName, sheetData.size(), unsyncedEntities, repositorySave), "updateRowIds");
        // 5. Sync from Sheets to DB (handle new sheet entries)
        executeWithRetry(() -> syncFromSheets(sheetData, repositorySave, fromRowConverter), "syncFromSheets");
        // 6. Mark as synced
        markAndSave.accept(unsyncedEntities);
    }

    private <T extends Syncable> void updateNewRowIds(
            String sheetName,
            int originalSize,
            List<T> unsyncedEntities,
            Consumer<T> repositorySave) throws IOException {
        List<List<Object>> updatedSheet = googleSheetsService.getSheetData(sheetName);
        for (int i = originalSize; i < updatedSheet.size(); i++) {
            List<Object> row = updatedSheet.get(i);
            if (row == null || row.isEmpty() || row.getFirst() == null) continue;
            try {
                Long id = Long.parseLong(row.getFirst().toString());
                int finalI = i;
                unsyncedEntities.stream()
                        .filter(e -> e.getId().equals(id) && e.getSheetRowId() == null)
                        .findFirst().ifPresent(entity -> {
                            entity.setSheetRowId(finalI + 1);
                            repositorySave.accept(entity);
                        });
            } catch (NumberFormatException e) {
                logger.warn("Invalid ID in row: {}", row.getFirst());
            }
        }
    }

    private Optional<User> parseUserFromRow(List<Object> row) {
        try {
            if (row.size() < 5) return Optional.empty();
            User user = new User();
            user.setId(Long.parseLong(row.getFirst().toString()));
            user.setEmail(row.get(1).toString());
            user.setFirstName(row.get(2).toString());
            user.setLastName(row.get(3).toString());
            user.setRole(Role.valueOf(row.get(4).toString()));
            return Optional.of(user);
        } catch (Exception e) {
            logger.error("Error parsing User from row: {}", row, e);
            return Optional.empty();
        }
    }

    private Optional<Teacher> parseTeacherFromRow(List<Object> row) {
        try {
            if (row.size() < 3) return Optional.empty();
            Teacher teacher = new Teacher();
            teacher.setId(Long.parseLong(row.getFirst().toString()));
            teacher.setGrade(GradeLevel.valueOf(row.get(1).toString()));
            Long userId = Long.parseLong(row.get(2).toString());
            User user = userRepository.findById(userId).orElseThrow(() ->
                    new EntityNotFoundException("User not found: " + userId));
            teacher.setUser(user);
            return Optional.of(teacher);
        } catch (Exception e) {
            logger.error("Error parsing Teacher from row: {}", row, e);
            return Optional.empty();
        }
    }

    private Optional<Student> parseStudentFromRow(List<Object> row) {
        try {
            if (row.size() < 5) return Optional.empty();
            Student student = new Student();
            student.setId(Long.parseLong(row.getFirst().toString()));
            student.setPoints(Integer.parseInt(row.get(1).toString()));
            student.setToken(row.get(2).toString());
            Long userId = Long.parseLong(row.get(3).toString());
            User user = userRepository.findById(userId).orElseThrow(() ->
                    new EntityNotFoundException("User not found: " + userId));
            student.setUser(user);
            Long teacherId = Long.parseLong(row.get(4).toString());
            Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(() ->
                    new EntityNotFoundException("Teacher not found: " + teacherId));
            student.setTeacher(teacher);
            return Optional.of(student);
        } catch (Exception e) {
            logger.error("Error parsing Student from row: {}", row, e);
            return Optional.empty();
        }
    }

    private Optional<BehaviorType> parseBehaviorTypeFromRow(List<Object> row) {
        try {
            if (row.size() < 4) return Optional.empty();
            BehaviorType behaviorType = new BehaviorType();
            behaviorType.setId(Long.parseLong(row.getFirst().toString()));
            behaviorType.setName(row.get(1).toString());
            behaviorType.setPointValue(Integer.parseInt(row.get(2).toString()));
            behaviorType.setActive(Boolean.parseBoolean(row.get(3).toString()));
            return Optional.of(behaviorType);
        } catch (Exception e) {
            logger.error("Error parsing BehaviorType from row: {}", row, e);
            return Optional.empty();
        }
    }

    private Optional<BragLog> parseBragLogFromRow(List<Object> row) {
        try {
            if (row.size() < 9) return Optional.empty();
            BragLog bragLog = new BragLog();
            bragLog.setId(Long.parseLong(row.getFirst().toString()));
            Long studentId = Long.parseLong(row.get(1).toString());
            Student student = studentRepository.findById(studentId).orElseThrow(() ->
                    new EntityNotFoundException("Student not found: " + studentId));
            bragLog.setStudent(student);
            Long teacherId = Long.parseLong(row.get(2).toString());
            Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(() ->
                    new EntityNotFoundException("Teacher not found: " + teacherId));
            bragLog.setTeacher(teacher);
            String[] behaviorNames = row.get(3).toString().split(", ");
            Set<BehaviorType> behaviors = Arrays.stream(behaviorNames)
                    .map(behaviorTypeRepository::findByName)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toSet());
            bragLog.setBehaviors(behaviors);
            bragLog.setPointsGenerated(Integer.parseInt(row.get(4).toString()));
            bragLog.setSubmitterName(row.get(5).toString());
            Long submitterUserId = Long.parseLong(row.get(6).toString());
            User submitter = userRepository.findById(submitterUserId).orElseThrow(() ->
                    new EntityNotFoundException("User not found: " + studentId));
            bragLog.setSubmitterUser(submitter);
            bragLog.setNotes(row.get(7).toString());
            bragLog.setTimestamp(LocalDateTime.parse(row.get(8).toString(), DATE_FORMATTER));
            return Optional.of(bragLog);
        } catch (Exception e) {
            logger.error("Error parsing BragLog from row: {}", row, e);
            return Optional.empty();
        }
    }

    private Optional<RewardItem> parseRewardItemFromRow(List<Object> row) {
        try {
            if (row.size() < 4) return Optional.empty();
            RewardItem rewardItem = new RewardItem();
            rewardItem.setId(Long.parseLong(row.getFirst().toString()));
            rewardItem.setName(row.get(1).toString());
            rewardItem.setPointCost(Integer.parseInt(row.get(2).toString()));
            rewardItem.setStock(Integer.parseInt(row.get(3).toString()));
            return Optional.of(rewardItem);
        } catch (Exception e) {
            logger.error("Error parsing RewardItem from row: {}", row, e);
            return Optional.empty();
        }
    }

    private Optional<StudentReward> parseStudentRewardFromRow(List<Object> row) {
        try {
            if (row.size() < 4) return Optional.empty();
            StudentReward studentReward = new StudentReward();
            studentReward.setId(Long.parseLong(row.getFirst().toString()));
            studentReward.setRedeemedAt(LocalDateTime.parse(row.get(1).toString(), DATE_FORMATTER));
            Long studentId = Long.parseLong(row.get(2).toString());
            Student student = studentRepository.findById(studentId).orElseThrow(() ->
                    new EntityNotFoundException("Student not found: " + studentId));
            studentReward.setStudent(student);
            Long rewardItemId = Long.parseLong(row.get(3).toString());
            RewardItem rewardItem = rewardItemRepository.findById(rewardItemId).orElseThrow(() ->
                    new EntityNotFoundException("RewardItem not found: " + rewardItemId));
            studentReward.setRewardItem(rewardItem);
            return Optional.of(studentReward);
        } catch (Exception e) {
            logger.error("Error parsing StudentReward from row: {}", row, e);
            return Optional.empty();
        }
    }

    private Map<Long, Integer> createRowIdMap(List<List<Object>> sheetData) {
        Map<Long, Integer> sheetRowMap = new HashMap<>();
        if (sheetData == null || sheetData.size() <= 1) return sheetRowMap;
        // Create ID -> row number mapping (skip header row)
        for (int i = 1; i < sheetData.size(); i++) {
            List<Object> row = sheetData.get(i);
            if (row == null || row.isEmpty()) continue;
            int finalI = i;
            parseRowId(row.getFirst(), i).ifPresent(id ->
                    sheetRowMap.put(id, finalI + 1)
            );
        }
        return sheetRowMap;
    }

    private <T extends Syncable> void syncFromSheets(
            List<List<Object>> sheetData,
            Consumer<T> repositorySave,
            Function<List<Object>, Optional<T>> fromRowConverter) {
        for (int i = 1; i < sheetData.size(); i++) {
            List<Object> row = sheetData.get(i);
            if (row == null || row.isEmpty()) continue;
            int finalI = i;
            parseRowId(row.getFirst(), i).ifPresent(id -> {
                Optional<T> entityOpt = fromRowConverter.apply(row);
                if (entityOpt.isPresent()) {
                    entityOpt.ifPresent(newEntity -> {
                        if (!existsInDataBase(newEntity.getClass(), id)) {
                            newEntity.setSheetRowId(finalI + 1);
                            repositorySave.accept(newEntity);
                        }
                    });
                }
            });
        }
    }

    private void executeBatchOperations(
            String sheetName,
            List<BatchUpdateRequest> updates,
            List<List<String>> newData ) throws IOException {
        if (!updates.isEmpty()) {
            List<List<BatchUpdateRequest>> updateChunks = Lists.partition(updates, 100);
            for (List<BatchUpdateRequest> chunk : updateChunks) {
                googleSheetsService.batchUpdate(sheetName, chunk);
            }
        }
        if (!newData.isEmpty()) {
            List<List<List<String>>> dataChunks = Lists.partition(newData, 100);
            for (List<List<String>> chunk : dataChunks) {
                googleSheetsService.appendToSheet(sheetName, chunk);
            }
        }
    }

    private <T extends Syncable> void markAsSynced(List<T> entities) {
        LocalDateTime now = LocalDateTime.now();
        entities.forEach(entity -> {
            entity.setSyncedToSheets(true);
            entity.setLastSynced(now);
        });
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATE_FORMATTER) : "";
    }

    private Optional<Long> parseRowId(Object idValue, int rowIndex) {
        if (idValue == null) return Optional.empty();
        try {
            return Optional.of(Long.parseLong(idValue.toString()));
        } catch (NumberFormatException e) {
            logger.warn("Invalid ID in row {}: {}", rowIndex, idValue);
            return Optional.empty();
        }
    }

    private boolean existsInDataBase(Class<?> entityClass, Long id) {
        if (entityClass == User.class) return userRepository.existsById(id);
        if (entityClass == Teacher.class) return teacherRepository.existsById(id);
        if (entityClass == Student.class) return studentRepository.existsById(id);
        if (entityClass == BehaviorType.class) return behaviorTypeRepository.existsById(id);
        if (entityClass == BragLog.class) return bragLogRepository.existsById(id);
        if (entityClass == RewardItem.class) return rewardItemRepository.existsById(id);
        if (entityClass == StudentReward.class) return studentRewardRepository.existsById(id);
        return false;
    }

    private boolean checkDailyQuota(String sheetName) {
        try {
            int rowCount = googleSheetsService.getRowCount(sheetName);
            int operations = rowCount * 2;
            int dailyQuota = 50000;
            boolean result =  operations < (dailyQuota * 0.8);
            logger.debug("Quota check for {}: rowCount: {}, operations={}, within quota={}", sheetName, rowCount, operations, result);
            return result;
        } catch (IOException e) {
            logger.error("Failed to get row count for {}: {}", sheetName, e.getMessage());
            return false;
        }
    }

    private void executeWithRetry(RunnableThrowing operation, String operationName) throws IOException {
        executeWithRetryInternal(operation, operationName, 0, 3);
    }

    private void executeWithRetryInternal(RunnableThrowing operation, String operationName, int attempt, int maxAttempts) throws IOException {
        try {
            operation.run();
        } catch (GoogleJsonResponseException e) {
            handleGoogleException(operation, operationName, attempt, maxAttempts, e);
        } catch (Exception e) {
            handleGenericException(operation, operationName, attempt, maxAttempts, e);
        }
    }

    private void handleGoogleException(RunnableThrowing operation, String operationName, int attempt,
                                       int maxAttempts, GoogleJsonResponseException e) throws IOException {
        if (e.getStatusCode() == 429) {
            handleQuotaExceeded(operation, operationName, attempt, maxAttempts, e);
        } else {
            throw e;
        }
    }

    private void handleQuotaExceeded(RunnableThrowing operation, String operationName, int attempt,
                                     int maxAttempts, GoogleJsonResponseException e) throws IOException {
        if (attempt == maxAttempts - 1) {
            throw new IOException("Quota exceeded for " + operationName + " after " + maxAttempts + " attempts", e);
        }
        long waitTime = (long) Math.pow(2, attempt) * 1000;
        logger.warn("Quota exceeded for {}, retrying in {} ms: {}", operationName, waitTime, e.getMessage());
        sleep(waitTime);
        executeWithRetryInternal(operation, operationName, attempt + 1, maxAttempts);
    }

    private void handleGenericException(RunnableThrowing operation, String operationName, int attempt,
                                     int maxAttempts, Exception e) throws IOException {
        if (attempt == maxAttempts - 1) {
            throw new IOException("Operation failed for " + operationName + " after " + maxAttempts + " attempts", e);
        }
        long waitTime = (long) Math.pow(2, attempt) * 1000;
        logger.warn("Error for {}, retrying in {} ms: {}", operationName, waitTime, e.getMessage());
        sleep(waitTime);
        executeWithRetryInternal(operation, operationName, attempt + 1, maxAttempts);
    }

    private void sleep(long waitTime) throws IOException {
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted during backoff", ex);
        }
    }
}
