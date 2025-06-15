package com.bearpoints.api.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "behavior_type", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name", name = "uk_behavior_name")
})
public class BehaviorType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Behavior name is required")
    @Size(min = 5, max = 50, message = "Name must be 5-50 characters")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Point value is required")
    @Min(value = 1, message = "Minimum point value is 1")
    @Max(value = 5, message = "Maximum point value is 5")
    @Column(name = "point_value", nullable = false)
    private Integer pointValue = 1;

    @NotNull(message = "Active status is required")
    @Column(nullable = false)
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "last_synced")
    private LocalDateTime lastSynced;

    @NotNull(message = "Sync status is required")
    @Column(name = "synced_to_sheets", nullable = false)
    private Boolean syncedToSheets = false;
}
