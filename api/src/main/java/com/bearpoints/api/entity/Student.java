package com.bearpoints.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a student profile.
 * <p>Implements {@link Syncable} for Google Sheets synchronization
 *
 * @see User
 * @see Role
 * @see Syncable
 * @version 1.0
 * @author Dylan Mercer
 */
@Data
@Entity
@Table(name = "student", indexes = {
        @Index(name = "idx_student_token", columnList = "token")
})
@EntityListeners(SyncableEntityListener.class)
public class Student implements Syncable {
    /** Unique identifier (auto-generated) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Student's points total
     * <p>Constraints:
     * <ul>
     *     <li>Non-null</li>
     *     <li>Non-negative</li>
     * </ul>
     * */
    @Min(value = 0, message = "Points cannot be negative")
    @Column(nullable = false)
    private Integer points = 0;

    /** Student's unique identifying token */
    @NotBlank(message = "Token is required")
    @Column(nullable = false, unique = true, length = 36)
    private String token;

    /** Student's personal user information */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User reference is required")
    private User user;

    /** Teacher's assigned teacher */
    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    @NotNull(message = "Teacher is required")
    private Teacher teacher;

    /** Associated brag logs with student */
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<BragLog> bragLogs;

    /**
     * Version field for JPA optimistic locking.
     * <p>Automatically managed by JPA to prevent concurrent modifications
     */
    @Version
    @Column(nullable = false)
    private Long version = 0L;

    /**
     * Timestamp of last successful sync with Google Sheets.
     * <p>Null indicates the entity has never been synced.
     */
    @Column(name = "last_synced")
    private LocalDateTime lastSynced;

    /**
     * Synchronization status flag for Google Sheets.
     * <p>Constraints:
     * <ul>
     *     <li>Non-null</li>
     *     <li>True indicates successful sync</li>
     *     <li>False indicates pending sync</li>
     * </ul>
     */
    @NotNull(message = "Sync status is required")
    @Column(name = "synced_to_sheets", nullable = false)
    private Boolean syncedToSheets = false;

    /**
     * Corresponding row identifier in Google Sheets.
     * <p>Null indicates the row ID hasn't been assigned or synced.
     */
    @Column(name = "sheet_row_id")
    private Integer sheetRowId;

    /**
     * Sets the sync completion status for Google Sheets.
     * @param synced true indicates successful sync completion,
     *               false indicates pending sync
     */
    @Override
    public void setSyncedToSheets(boolean synced) {
        this.syncedToSheets = synced;
    }

    /**
     * Sets the timestamp of the last successful sync with Google Sheets.
     * @param lastSynced timestamp of last sync operation completion
     */
    @Override
    public void setLastSynced(LocalDateTime lastSynced) {
        this.lastSynced = lastSynced;
    }

    /**
     * Retrieves the row ID location in Google Sheets.
     * @return integer row number in Google Sheets, or null if not set
     */
    @Override
    public Integer getSheetRowId() {
        return this.sheetRowId;
    }

    /**
     * Sets the row ID location in Google Sheets.
     * @param rowId integer row number in Google Sheets
     */
    @Override
    public void setSheetRowId(Integer rowId) {
        this.sheetRowId = rowId;
    }

    /**
     * Generates and sets a new UUID token if it is null on initial save
     */
    @PrePersist
    public void generateToken() {
        if (token == null) {
            token = UUID.randomUUID().toString();
        }
    }
}
