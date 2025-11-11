package com.bearpoints.api.dto;

import com.bearpoints.api.entity.Student;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class StudentDTO {
    private final Long id;

    @Valid
    private final UserDTO user;

    @Min(value = 0, message = "Points cannot be negative")
    private final Integer points;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private final String token;

    @Valid
    private final TeacherDTO teacher;

    /**
     * Constructor for Jackson deserialization
     */
    @JsonCreator
    public StudentDTO(@JsonProperty("id") Long id,
                      @JsonProperty("user") UserDTO user,
                      @JsonProperty("points") Integer points,
                      @JsonProperty("token") String token,
                      @JsonProperty("teacher") TeacherDTO teacher
    ) {
        this.id = id;
        this.user = user;
        this.points = points;
        this.token = token;
        this.teacher = teacher;
    }

    /**
     * Constructs a StudentDTO from a Student entity
     *
     * @param student Source student entity
     */
    public StudentDTO(Student student) {
        this.id = student.getId();
        this.user = student.getUser() != null ? new UserDTO(student.getUser()) : null;
        this.points = student.getPoints();
        this.token = student.getToken();
        this.teacher = student.getTeacher() != null ? new TeacherDTO(student.getTeacher()) : null;
    }
}
