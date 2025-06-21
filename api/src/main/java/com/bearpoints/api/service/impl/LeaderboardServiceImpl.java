package com.bearpoints.api.service.impl;

import com.bearpoints.api.dao.BragLogRepository;
import com.bearpoints.api.dto.LeaderboardEntryDTO;
import com.bearpoints.api.entity.BragLog;
import com.bearpoints.api.entity.Student;
import com.bearpoints.api.entity.Timeframe;
import com.bearpoints.api.service.LeaderboardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LeaderboardServiceImpl implements LeaderboardService {
    private final BragLogRepository bragLogRepository;

    public LeaderboardServiceImpl(BragLogRepository bragLogRepository) {
        this.bragLogRepository = bragLogRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeaderboardEntryDTO> getLeaderboard(Timeframe timeframe) {
        LocalDateTime startDate = calculateStartDate(timeframe);
        List<BragLog> logs = bragLogRepository.findByTimestampAfter(startDate);
        return calculateLeaderboardEntries(logs);
    }

    private List<LeaderboardEntryDTO> calculateLeaderboardEntries(List<BragLog> logs) {
        Map<Student, Integer> pointsMap = new HashMap<>();
        for (BragLog log : logs) {
            Student student = log.getStudent();
            pointsMap.merge(student, log.getPointsGenerated(), Integer::sum);
        }
        return pointsMap.entrySet().stream()
                .map(entry ->
                        createEntryDTO(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(LeaderboardEntryDTO::getPoints).reversed())
                .collect(Collectors.toList());
    }

    private LeaderboardEntryDTO createEntryDTO(Student student, int points) {
        return new LeaderboardEntryDTO(
                student.getId(),
                student.getUser().getFirstName() + " " +
                        student.getUser().getLastName(),
                student.getTeacher().getUser().getFirstName() + " " +
                        student.getTeacher().getUser().getLastName(),
                student.getTeacher().getGrade(),
                points
        );
    }

    private LocalDateTime calculateStartDate(Timeframe timeframe) {
        LocalDateTime now = LocalDateTime.now();
        return switch (timeframe) {
            case WEEK -> now.minusWeeks(1);
            case MONTH -> now.minusMonths(1);
            case SEMESTER -> now.minusMonths(6);
            case YEAR -> now.minusYears(1);
        };
    }
}
