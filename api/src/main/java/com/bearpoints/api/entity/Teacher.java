package com.bearpoints.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Represents a teacher profile.
 * <p>Implements {@link Syncable} for Google Sheets synchronization
 *
 * @see Role
 * @see Syncable
 * @version 1.0
 * @author Dylan Mercer
 */
@Data
@Entity
@Table(name = "teacher")
@EntityListeners(SyncableEntityListener.class)
public class Teacher implements Syncable {
    /** Unique identifier (auto-generated) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Teacher's currently assigned grade
     * <p>Constraints:
     * <ul>
     *     <li>Non-blank</li>
     *     <li>Must be one of: Pre-K, K, 1, 2, 3, or 4</li>
     * </ul>
     */
    @NotBlank(message = "Grade is required")
    @Pattern(
            regexp = "Pre-K|K|[1-4]",
            message = "Invalid grade level"
    )
    @Column(nullable = false)
    private String grade;

    /** Teacher's personal user information */
    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User reference is required")
    private User user;

    /**
     * Associated students in teacher's class
     * <p>Characteristics:
     * <ul>
     *     <li>Bidirectional relationship</li>
     *     <li>Cascade persist/merge operations</li>
     *     <li>Orphan removal enabled</li>
     * </ul>
     */
    @OneToMany(mappedBy = "teacher", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Student> students;

    /** Associated brag logs from students in teacher's class (optional) */
    @OneToMany(mappedBy = "teacher", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<BragLog> bragLogs;

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
