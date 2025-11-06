package com.bearpoints.api.dto;

import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.entity.Teacher;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class TeacherDTO {
    private final Long id;

    private final UserDTO user;

    private final GradeLevel grade;


    /**
     * Constructor for Jackson deserialization
     */
    @JsonCreator
    public TeacherDTO(@JsonProperty("id") Long id,
                      @JsonProperty("user") UserDTO user,
                      @JsonProperty("grade") String grade) {
        this.id = id;
        this.user = user;
        this.grade = validateAndConvertGrade(grade);
    }

    /**
     * Constructs a TeacherDTO from a Teacher entity
     *
     * @param teacher Source teacher entity
     */
    public TeacherDTO(Teacher teacher) {
        this.id = teacher.getId();
        this.user = teacher.getUser() != null ? new UserDTO(teacher.getUser()) : null;
        this.grade = teacher.getGrade();
    }

    private GradeLevel validateAndConvertGrade(String gradeString) {
        if (gradeString == null) {
            return null;
        }
        String trimmed = gradeString.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String normalized = trimmed.toUpperCase().replace("-", "_");
        try {
            return GradeLevel.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid grade level: " + gradeString + ". Valid values are: " +
                    java.util.Arrays.toString(GradeLevel.values()));
        }
    }
}
