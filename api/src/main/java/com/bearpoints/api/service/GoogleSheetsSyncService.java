package com.bearpoints.api.service;

import com.bearpoints.api.domain.*;
import com.bearpoints.api.dto.BatchUpdateRequest;
import com.bearpoints.api.dto.Syncable;
import com.bearpoints.api.repository.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class GoogleSheetsSyncService {
    private static final Logger logger = LoggerFactory.getLogger(GoogleSheetsSyncService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final BragLogRepository bragLogRepository;
    private final RewardItemRepository rewardItemRepository;
    private final StudentRewardRepository studentRewardRepository;
    private final BehaviorTypeRepository behaviorTypeRepository;
    private final GoogleSheetsService googleSheetsService;

    @Value("${google.sheets.spreadsheet.id}")
    private String spreadsheetId;

    public GoogleSheetsSyncService(
            UserRepository theUserRepository,
            StudentRepository theStudentRepository,
            TeacherRepository theTeacherRepository,
            BragLogRepository theBragLogRepository,
            RewardItemRepository theRewardItemRepository,
            StudentRewardRepository theStudentRewardRepository,
            BehaviorTypeRepository theBehaviorTypeRepository,
            GoogleSheetsService theGoogleSheetsService) {
        this.userRepository = theUserRepository;
        this.studentRepository = theStudentRepository;
        this.teacherRepository = theTeacherRepository;
        this.bragLogRepository = theBragLogRepository;
        this.rewardItemRepository = theRewardItemRepository;
        this.studentRewardRepository = theStudentRewardRepository;
        this.behaviorTypeRepository = theBehaviorTypeRepository;
        this.googleSheetsService = theGoogleSheetsService;
    }

    @Scheduled(fixedRate = 300000)
    public void syncAllData() {
        try {
            logger.info("Starting Google Sheets sync process");
            syncUsers();
            syncTeachers();
            syncStudents();
            syncBehaviorTypes();
            syncBragLogs();
            syncRewardItems();
            syncStudentRewards();
            logger.info("Google Sheets sync completed successfully");
        } catch (Exception e) {
            logger.error("Google Sheets sync failed", e);
        }
    }

    private void syncUsers() throws IOException {
        // 1. Get existing sheet data
        List<List<Object>> sheetData = googleSheetsService.getSheetData("Users");
        Map<Long, Integer> sheetRowMap = new HashMap<>();
        // Create ID -> row number mapping (skip header row)
        for (int i = 1; i < sheetData.size(); i++) {
            List<Object> row = sheetData.get(i);
            if (!row.isEmpty()) {
                Long id = Long.parseLong(row.getFirst().toString());
                // +1 for 1-based indexing
                sheetRowMap.put(id, i + 1);
            }
        }
        // 2. Process database entities
        List<User> allUsers = userRepository.findAll();
        List<List<String>> newData = new ArrayList<>();
        List<BatchUpdateRequest> updates = new ArrayList<>();
        for (User user : allUsers) {
            List<String> rowData = Arrays.asList(
                    user.getId().toString(),
                    user.getEmail(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole().name(),
                    formatDateTime(user.getCreatedAt()),
                    formatDateTime(user.getUpdatedAt())
            );
            if (sheetRowMap.containsKey(user.getId())) {
                // Existing row - prepare update
                int rowNum = sheetRowMap.get(user.getId());
                updates.add(new BatchUpdateRequest(rowNum, rowData));
                user.setSheetRowId(rowNum);
            } else {
                // New row - prepare for append
                newData.add(rowData);
            }
        }
        // 3. Execute batch updates
        if (!updates.isEmpty()) {
            googleSheetsService.batchUpdate("Users", updates);
        }
        // 4. Append new rows
        if (!newData.isEmpty()) {
            googleSheetsService.appendToSheet("Users", newData);
            // Update row IDs for new entries
            List<List<Object>> updatedSheet = googleSheetsService.getSheetData("Users");
            for (int i = sheetData.size() + 1; i <= updatedSheet.size(); i++) {
                List<Object> row = updatedSheet.get(i - 1);
                if (!row.isEmpty()) {
                    Long id = Long.parseLong(row.getFirst().toString());
                    int finalI = i;
                    userRepository.findById(id).ifPresent(user -> user.setSheetRowId(finalI));
                }
            }
        }
        // 5. Sync from Sheets to DB (handle new sheet entries)
        for (int i = 1; i < sheetData.size(); i++) {
            List<Object> row = sheetData.get(i);
            if (row.size() >= 5 && !row.getFirst().toString().isEmpty()) {
                Long id = Long.parseLong(row.getFirst().toString());
                if (!userRepository.existsById(id)) {
                    User newUser = new User();
                    newUser.setId(id);
                    newUser.setEmail(row.get(1).toString());
                    newUser.setFirstName(row.get(2).toString());
                    newUser.setLastName(row.get(3).toString());
                    newUser.setRole(Role.valueOf(row.get(4).toString()));
                    newUser.setCreatedAt((LocalDateTime) row.get(5));
                    newUser.setUpdatedAt((LocalDateTime) row.get(6));
                    newUser.setTeacher((Teacher) row.get(7));
                    newUser.setStudent((Student) row.get(8));
                    newUser.setLastSynced((LocalDateTime) row.get(9));
                    newUser.setSyncedToSheets((Boolean) row.get(10));
                    newUser.setSheetRowId(i + 1);
                    userRepository.save(newUser);
                }
            }
        }
        // 6. Mark all as synced
        markAsSynced(allUsers);
        userRepository.saveAll(allUsers);
    }

    private void syncTeachers() throws IOException {
        List<Teacher> unsyncedTeachers = teacherRepository.findBySyncedToSheetsFalse();
        if (unsyncedTeachers.isEmpty()) return;
        logger.info("Syncing {} teachers to Google Sheets", unsyncedTeachers.size());
        List<List<String>> data = unsyncedTeachers.stream()
                .map(teacher -> Arrays.asList(
                        teacher.getId().toString(),
                        teacher.getGrade(),
                        teacher.getUser().getId().toString()
                )).collect(Collectors.toList());
        googleSheetsService.appendToSheet("Teachers", data);
        markAsSynced(unsyncedTeachers);
        teacherRepository.saveAll(unsyncedTeachers);
    }

    private void syncStudents() throws IOException {
        List<Student> unsyncedStudents = studentRepository.findBySyncedToSheetsFalse();
        if (unsyncedStudents.isEmpty()) return;
        logger.info("Syncing {} students to Google Sheets", unsyncedStudents.size());
        List<List<String>> data = unsyncedStudents.stream()
                .map(student -> Arrays.asList(
                        student.getId().toString(),
                        student.getPoints().toString(),
                        student.getToken(),
                        student.getUser().getId().toString(),
                        student.getTeacher().getId().toString()
                )).collect(Collectors.toList()).reversed();
        googleSheetsService.appendToSheet("Students", data);
        markAsSynced(unsyncedStudents);
        studentRepository.saveAll(unsyncedStudents);
    }

    private void syncBehaviorTypes() throws IOException {
        List<BehaviorType> unsyncedBehaviorTypes = behaviorTypeRepository.findBySyncedToSheetsFalse();
        if (unsyncedBehaviorTypes.isEmpty()) return;
        logger.info("Syncing {} behaviorTypes to Google Sheets", unsyncedBehaviorTypes.size());
        List<List<String>> data = unsyncedBehaviorTypes.stream()
                .map(behaviorType -> Arrays.asList(
                        behaviorType.getId().toString(),
                        behaviorType.getName(),
                        behaviorType.getPointValue().toString(),
                        behaviorType.getActive().toString(),
                        formatDateTime(behaviorType.getCreatedAt()),
                        formatDateTime(behaviorType.getUpdatedAt())
                )).collect(Collectors.toList());
        googleSheetsService.appendToSheet("BehaviorTypes", data);
        markAsSynced(unsyncedBehaviorTypes);
        behaviorTypeRepository.saveAll(unsyncedBehaviorTypes);
    }

    private void syncBragLogs() throws IOException {
        List<BragLog> unsyncedBragLogs = bragLogRepository.findBySyncedToSheetsFalse();
        if (unsyncedBragLogs.isEmpty()) return;
        logger.info("Syncing {} bragLogs to Google Sheets", unsyncedBragLogs.size());
        List<List<String>> data = unsyncedBragLogs.stream()
                .map(bragLog -> {
                    String behaviors = bragLog.getBehaviors().stream()
                            .map(BehaviorType::getName)
                            .collect(Collectors.joining(", "));
                    return Arrays.asList(
                            bragLog.getId().toString(),
                            bragLog.getStudent().getId().toString(),
                            bragLog.getTeacher().getId().toString(),
                            behaviors,
                            bragLog.getPointsGenerated().toString(),
                            bragLog.getNotes(),
                            formatDateTime(bragLog.getTimestamp())
                    );
                }).collect(Collectors.toList());
        googleSheetsService.appendToSheet("BragLogs", data);
        markAsSynced(unsyncedBragLogs);
        bragLogRepository.saveAll(unsyncedBragLogs);
    }

    private void syncRewardItems() throws IOException {
        List<RewardItem> unsyncedRewardItems = rewardItemRepository.findBySyncedToSheetsFalse();
        if (unsyncedRewardItems.isEmpty()) return;
        logger.info("Syncing {} rewardItems to Google Sheets", unsyncedRewardItems.size());
        List<List<String>> data = unsyncedRewardItems.stream()
                .map(rewardItem -> Arrays.asList(
                        rewardItem.getId().toString(),
                        rewardItem.getName(),
                        rewardItem.getPointCost().toString(),
                        rewardItem.getStock().toString()
                )).collect(Collectors.toList());
        googleSheetsService.appendToSheet("RewardItems", data);
        markAsSynced(unsyncedRewardItems);
        rewardItemRepository.saveAll(unsyncedRewardItems);
    }

    private void syncStudentRewards() throws IOException {
        List<StudentReward> unsyncedStudentRewards = studentRewardRepository.findBySyncedToSheetsFalse();
        if (unsyncedStudentRewards.isEmpty()) return;
        logger.info("Syncing {} studentRewards to Google Sheets", unsyncedStudentRewards.size());
        List<List<String>> data = unsyncedStudentRewards.stream()
                .map(studentReward -> Arrays.asList(
                        studentReward.getId().toString(),
                        studentReward.getStudent().getId().toString(),
                        studentReward.getRewardItem().getId().toString(),
                        formatDateTime(studentReward.getRedeemedAt())
                )).collect(Collectors.toList());
        googleSheetsService.appendToSheet("StudentRewards", data);
        markAsSynced(unsyncedStudentRewards);
        studentRewardRepository.saveAll(unsyncedStudentRewards);
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
}
