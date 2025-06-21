package com.bearpoints.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LeaderboardEntryDTO {
    private Long studentId;
    private String studentName;
    private String teacherName;
    private String grade;
    private Integer points;
}
