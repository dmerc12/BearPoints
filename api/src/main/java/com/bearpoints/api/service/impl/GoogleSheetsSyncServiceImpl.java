package com.bearpoints.api.service.impl;

import com.bearpoints.api.entity.*;
import com.bearpoints.api.dto.BatchUpdateRequest;
import com.bearpoints.api.dto.Syncable;
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

@Service
@Transactional
public class GoogleSheetsSyncServiceImpl implements GoogleSheetsSyncService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleSheetsSyncServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final BragLogRepository bragLogRepository;
    private final RewardItemRepository rewardItemRepository;
    private final StudentRewardRepository studentRewardRepository;
    private final BehaviorTypeRepository behaviorTypeRepository;
    private final GoogleSheetsServiceImpl googleSheetsService;

    @Getter
    @Setter
    @Value("${google.sheets.spreadsheet.id}")
    private String spreadsheetId;

    public GoogleSheetsSyncServiceImpl(
            UserRepository theUserRepository,
            StudentRepository theStudentRepository,
            TeacherRepository theTeacherRepository,
            BragLogRepository theBragLogRepository,
            RewardItemRepository theRewardItemRepository,
            StudentRewardRepository theStudentRewardRepository,
            BehaviorTypeRepository theBehaviorTypeRepository,
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
                        teacher.getGrade(),
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
        syncEntity(
                "BragLogs",
                bragLogRepository::findBySyncedToSheetsFalse,
                bragLog -> {
                    String behaviors = bragLog.getBehaviors().stream()
                            .map(BehaviorType::getName)
                            .collect(Collectors.joining(", "));
                    return Arrays.asList(
                            bragLog.getId().toString(),
                            bragLog.getStudent().getId().toString(),
                            bragLog.getTeacher().getId().toString(),
                            behaviors,
                            String.valueOf(bragLog.getPointsGenerated()),
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

    private <T extends Syncable> void syncEntity(
            String sheetName,
            Supplier<List<T>> repositoryFindUnsynced,
            Function<T, List<String>> toRowConverter,
            Function<List<Object>, Optional<T>> fromRowConverter,
            Consumer<T> repositorySave,
            Consumer<List<T>> markAndSave) throws IOException {
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
        int newRowIndex = originalSize + 1;
        for (int i = originalSize; i < updatedSheet.size(); i++) {
            List<Object> row = updatedSheet.get(i);
            if (row == null || row.isEmpty() || row.getFirst() == null) continue;
            try {
                Long id = Long.parseLong(row.getFirst().toString());
                unsyncedEntities.stream()
                        .filter(e -> e.getId().equals(id) && e.getSheetRowId() == null)
                        .findFirst().ifPresent(entity -> {
                            entity.setSheetRowId(newRowIndex + 1);
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
            if (row.size() < 5) return Optional.empty();
            Teacher teacher = new Teacher();
            teacher.setId(Long.parseLong(row.getFirst().toString()));
            teacher.setGrade(String.valueOf(row.get(1)));
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
            if (row.size() < 5) return Optional.empty();
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
            if (row.size() < 7) return Optional.empty();
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
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            bragLog.setBehaviors(behaviors);
            bragLog.setPointsGenerated(Integer.parseInt(row.get(4).toString()));
            bragLog.setNotes(row.get(5).toString());
            bragLog.setTimestamp(LocalDateTime.parse(row.get(6).toString(), DATE_FORMATTER));
            return Optional.of(bragLog);
        } catch (Exception e) {
            logger.error("Error parsing BragLog from row: {}", row, e);
            return Optional.empty();
        }
    }

    private Optional<RewardItem> parseRewardItemFromRow(List<Object> row) {
        try {
            if (row.size() < 5) return Optional.empty();
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
            if (row.size() < 5) return Optional.empty();
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
            parseRowId(row.getFirst(), i).ifPresent(id ->
                    fromRowConverter.apply(row).ifPresent(newEntity -> {
                if (!existsInDataBase(newEntity.getClass(), id)) {
                    newEntity.setSheetRowId(finalI + 1);
                    repositorySave.accept(newEntity);
                }
            }));
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
            return operations < (dailyQuota * 0.8);
        } catch (IOException e) {
            logger.error("Failed to get row count for {}: {}", sheetName, e.getMessage());
            return false;
        }
    }

    private void executeWithRetry(RunnableThrowing operation, String operationName) throws IOException {
        int maxAttempts = 3;
        for (int i = 0; i < maxAttempts; i++) {
            try {
                operation.run();
                return;
            } catch (GoogleJsonResponseException e) {
                if (e.getStatusCode() == 429) {
                    long waitTime = (long) Math.pow(2, i) * 1000;
                    logger.warn("Quota exceeded for {}, retrying in {} ms", operationName, waitTime);
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Interrupted during backoff", ex);
                    }
                } else {
                    throw e;
                }
            } catch (Exception e) {
                if (i == maxAttempts - 1) {
                    if (e instanceof IOException) throw (IOException) e;
                    throw new IOException("Operation failed:", e);
                }
                long waitTime = (long) Math.pow(2, i) * 1000;
                logger.warn("Error for {}, retrying in {} ms: {}", operationName, waitTime, e.getMessage());
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted during backoff", ex);
                }
            }
        }
    }
}
