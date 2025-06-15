package com.bearpoints.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "brag_log")
public class BragLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    @NotNull(message = "Student is required")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    @NotNull(message = "Teacher is required")
    private Teacher teacher;

    @ManyToMany
    @JoinTable(
            name = "brag_log_behavior",
            joinColumns = @JoinColumn(name = "brag_log_id"),
            inverseJoinColumns = @JoinColumn(name = "behavior_type_id")
    )
    @NotEmpty(message = "At least one behavior is required")
    private Set<BehaviorType> behaviors;

    @NotNull(message = "Points generated is required")
    @Min(value = 1, message = "Minimum points is 1")
    @Column(name = "points_generated", nullable = false)
    private Integer pointsGenerated;

    @Column
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @Column(name = "last_synced")
    private LocalDateTime lastSynced;

    @NotNull(message = "Sync status is required")
    @Column(name = "synced_to_sheets", nullable = false)
    private Boolean syncedToSheets = false;
}
