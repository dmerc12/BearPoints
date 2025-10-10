package com.bearpoints.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Represents a brag log.
 * <p>Implements {@link Syncable} for Google Sheets synchronization
 *
 * @see Student
 * @see Teacher
 * @see Syncable
 * @see BehaviorType
 * @version 1.0
 * @author Dylan Mercer
 */
@Data
@Entity
@Table(name = "brag_log")
@EntityListeners(SyncableEntityListener.class)
public class BragLog implements Syncable {
    /** Unique identifier (auto-generated) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * BragLog's assigned student
     * <p>Constraints:
     * <ul>
     *     <li>Non-null</li>
     * </ul>
     */
    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    @NotNull(message = "Student is required")
    private Student student;

    /**
     * BearBrag's Students' assigned teacher
     * <p>Constraints:
     * <ul>
     *     <li>Non-null</li>
     * </ul>
     */
    @ManyToOne
    @JoinColumn(name = "teacher_id", nullable = false)
    @NotNull(message = "Teacher is required")
    private Teacher teacher;

    /**
     * BearBrag's associated behaviors
     * <p>Table associating behaviors with brag logs:
     * <ul>
     *     <li>brag_log_id - Associated brag log</li>
     *     <li>behavior_type_id - Associated behavior type</li>
     * </ul>
     * <p>Constraints:
     * <ul>
     *     <li>Non-Empty</li>
     * </ul>
     */
    @ManyToMany
    @JoinTable(
            name = "brag_log_behavior",
            joinColumns = @JoinColumn(name = "brag_log_id"),
            inverseJoinColumns = @JoinColumn(name = "behavior_type_id")
    )
    @NotEmpty(message = "At least one behavior is required")
    private Set<BehaviorType> behaviors;

    /**
     * BearBrag's points generated
     * <p>Constraints:
     * <ul>
     *     <li>Non-null</li>
     *     <li>Minimum value</li>
     * </ul>
     */
    @NotNull(message = "Points generated is required")
    @Min(value = 1, message = "Minimum points is 1")
    @Column(name = "points_generated", nullable = false)
    private Integer pointsGenerated;

    /**
     * BearBrag's notes
     * <p>Constraints:
     * <ul>
     *     <li>Maximum value</li>
     * </ul>
     */
    @Column
    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    /**
     * Version field for JPA optimistic locking.
     * <p>Automatically managed by JPA to prevent concurrent modifications
     */
    @Version
    @Column(nullable = false)
    private Long version;

    /**
     * Timestamp (auto-generated).
     * <p>Constraints:
     * <ul>
     *     <li>Non-updatable</li>
     *     <li>Non-null</li>
     * </ul>
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

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
}
