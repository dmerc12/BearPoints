package com.bearpoints.api.entity;

import com.bearpoints.api.dto.Syncable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Entity
@Table(name = "student", indexes = {
        @Index(name = "idx_student_token", columnList = "token")
})
@EntityListeners(SyncableEntityListener.class)
public class Student implements Syncable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Points cannot be negative")
    @Column(nullable = false)
    private Integer points = 0;

    @NotBlank(message = "Token is required")
    @Column(nullable = false, unique = true, length = 36)
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User reference is required")
    private User user;

    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    @NotNull(message = "Teacher is required")
    private Teacher teacher;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<BragLog> bragLogs;

    @Column(name = "last_synced")
    private LocalDateTime lastSynced;

    @NotNull(message = "Sync status is required")
    @Column(name = "synced_to_sheets", nullable = false)
    private Boolean syncedToSheets = false;

    @Column(name = "sheet_row_id")
    private Integer sheetRowId;

    @Override
    public void setSyncedToSheets(boolean synced) {
        this.syncedToSheets = synced;
    }

    @Override
    public void setLastSynced(LocalDateTime lastSynced) {
        this.lastSynced = lastSynced;
    }

    @Override
    public Integer getSheetRowId() {
        return this.sheetRowId;
    }

    @Override
    public void setSheetRowId(Integer rowId) {
        this.sheetRowId = rowId;
    }

    @PrePersist
    public void generateToken() {
        if (token == null) {
            token = UUID.randomUUID().toString();
        }
    }
}
