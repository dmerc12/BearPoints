package com.bearpoints.api.service.impl;

import com.bearpoints.api.dao.BragLogDAO;
import com.bearpoints.api.dto.LeaderboardEntryDTO;
import com.bearpoints.api.dto.PersonDTO;
import com.bearpoints.api.entity.BragLog;
import com.bearpoints.api.entity.Student;
import com.bearpoints.api.entity.Timeframe;
import com.bearpoints.api.service.LeaderboardService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Represents service responsible for retrieving and calculating brag log leaderboard.
 * <p>Implements {@link LeaderboardService}
 *
 * @see LeaderboardEntryDTO
 * @see Timeframe
 * @see Student
 * @see BragLog
 * @see BragLogDAO
 *
 * @version 2.0
 * @author Dylan Mercer
 */
@Service
public class LeaderboardServiceImpl implements LeaderboardService {
    private final BragLogDAO bragLogRepository;

    public LeaderboardServiceImpl(BragLogDAO bragLogRepository) {
        this.bragLogRepository = bragLogRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<LeaderboardEntryDTO> getLeaderboard(Timeframe timeframe, Pageable pageable) {
        LocalDateTime startDate = calculateStartDate(timeframe);
        List<BragLog> logs = bragLogRepository.findByTimestampAfter(startDate);
        List<LeaderboardEntryDTO> allEntries = calculateLeaderboardEntries(logs);
        return getPaginatedResults(allEntries, pageable);
    }

    private Page<LeaderboardEntryDTO> getPaginatedResults(List<LeaderboardEntryDTO> allEntries, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allEntries.size());
        if (start > allEntries.size()) {
            return new PageImpl<>(List.of(), pageable, allEntries.size());
        }
        List<LeaderboardEntryDTO> pageContent = allEntries.subList(start, end);
        return new PageImpl<>(pageContent, pageable, allEntries.size());
    }

    private List<LeaderboardEntryDTO> calculateLeaderboardEntries(List<BragLog> logs) {
        Map<Student, Integer> pointsMap = new HashMap<>();
        for (BragLog log : logs) {
            Student student = log.getStudent();
            pointsMap.merge(student, log.getPointsGenerated(), Integer::sum);
        }
        AtomicInteger rank = new AtomicInteger(1);
        return pointsMap.entrySet().stream()
                .map(entry -> createUnrankedEntry(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingInt(LeaderboardEntryDTO::getPoints).reversed())
                .map(unrankedEntry -> new LeaderboardEntryDTO(
                        rank.getAndIncrement(),
                        unrankedEntry.getStudent(),
                        unrankedEntry.getTeacher(),
                        unrankedEntry.getGrade(),
                        unrankedEntry.getPoints()
                ))
                .collect(Collectors.toList());
    }

    private LeaderboardEntryDTO createUnrankedEntry(Student student, int points) {
        PersonDTO studentDTO = new PersonDTO(
                student.getId(),
                student.getUser().getFirstName(),
                student.getUser().getLastName()
        );
        PersonDTO teacherDTO = new PersonDTO(
                student.getTeacher().getId(),
                student.getTeacher().getUser().getFirstName(),
                student.getTeacher().getUser().getLastName()
        );
        String grade = student.getTeacher().getGrade().name();
        return new LeaderboardEntryDTO(0, studentDTO, teacherDTO, grade, points);
    }

    private LocalDateTime calculateStartDate(Timeframe timeframe) {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        return switch (timeframe) {
            case WEEK -> now.minusWeeks(1);
            case MONTH -> now.minusMonths(1);
            case SEMESTER -> now.minusMonths(6);
            case YEAR -> now.minusYears(1);
        };
    }
}
