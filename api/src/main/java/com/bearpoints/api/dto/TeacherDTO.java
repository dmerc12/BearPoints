package com.bearpoints.api.dto;

import com.bearpoints.api.entity.GradeLevel;
import com.bearpoints.api.entity.Teacher;
import com.bearpoints.api.utility.GradeLevelUtils;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class TeacherDTO {
    private final Long id;

    @Valid
    private final UserDTO user;

    @NotNull(message = "Grade is required")
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
        this.grade = GradeLevelUtils.validateAndConvertGrade(grade);
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
}
