package com.bearpoints.api.service;

import com.bearpoints.api.domain.*;
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
import java.util.Arrays;
import java.util.List;
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
        List<User> unsyncedUsers = userRepository.findBySyncedToSheetsFalse();
        if (unsyncedUsers.isEmpty()) return;
        logger.info("Syncing {} users to Google Sheets", unsyncedUsers.size());
        List<List<String>> data = unsyncedUsers.stream()
                .map(user -> Arrays.asList(
                        user.getId().toString(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getRole().name(),
                        formatDateTime(user.getCreatedAt()),
                        formatDateTime(user.getUpdatedAt())
                )).collect(Collectors.toList());
        googleSheetsService.appendToSheet("Users", data);
        markAsSynced(unsyncedUsers);
        userRepository.saveAll(unsyncedUsers);
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

    public interface Syncable {
        void setSyncedToSheets(boolean synced);
        void setLastSynced(LocalDateTime lastSynced);
    }
}
