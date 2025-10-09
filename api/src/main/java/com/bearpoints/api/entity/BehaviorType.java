package com.bearpoints.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents a behavior type.
 * <p>Implements {@link Syncable} for Google Sheets synchronization
 *
 * @see Syncable
 * @version 1.0
 * @author Dylan Mercer
 */
@Data
@Entity
@EntityListeners(SyncableEntityListener.class)
@Table(name = "behavior_type", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name", name = "uk_behavior_name")
})
public class BehaviorType implements Syncable {
    /** Unique identifier (auto-generated) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Behavior Type's name.
     * <p>Constraints:
     * <ul>
     *     <li>Non-blank</li>
     *     <li>Between 1 and 50 characters</li>
     * </ul>
     */
    @NotBlank(message = "Behavior name is required")
    @Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters")
    @Column(nullable = false)
    private String name;

    /**
     * Behavior Type's point value.
     * <p>Constraints:
     * <ul>
     *     <li>Non-null</li>
     *     <li>Between 1 and 5</li>
     * </ul>
     */
    @NotNull(message = "Point value is required")
    @Min(value = 1, message = "Minimum point value is 1")
    @Max(value = 5, message = "Maximum point value is 5")
    @Column(name = "point_value", nullable = false)
    private Integer pointValue = 1;

    /**
     * Behavior Type's active status.
     * <p>Constraints:
     * <ul>
     *     <li>Non-null</li>
     * </ul>
     */
    @NotNull(message = "Active status is required")
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * Version field for JPA optimistic locking.
     * <p>Automatically managed by JPA to prevent concurrent modifications
     */
    @Version
    @Column(nullable = false)
    private Long version = 0L;

    /**
     * Creation timestamp (auto-generated).
     * <p>Constraints:
     * <ul>
     *     <li>Non-updatable</li>
     *     <li>Non-null</li>
     * </ul>
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** Update timestamp (auto-generated) */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

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
