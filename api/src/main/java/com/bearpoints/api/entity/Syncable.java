package com.bearpoints.api.entity;

import java.time.LocalDateTime;

/**
 * Defines synchronization capabilities for entities with Google Sheets.
 * <p>Entities implementing this interface can be tracked for synchronization status,
 * last sync timestamp, and their corresponding row location in Google Sheets.
 *
 * <p><b>Usage:</b>
 * <pre>
 *     {@code
 *     @Entity
 *     @EntityListeners(SyncableEntityListeners.class)
 *     public class YourEntity implements Syncable {
 *         // Implement interface methods
 *     }
 *     }
 * </pre>
 *
 * <p><b>Lifecycle Management:</b>
 * The {@link SyncableEntityListener} automatically manages the {@code syncedToSheets}
 * flag during persistence operations.
 *
 * @see SyncableEntityListener
 * @version 1.0
 * @author Dylan Mercer
 */
public interface Syncable {

    /**
     * Sets the synchronization status with Google Sheets.
     * <p>Should be implemented to update the entity's sync status flag.
     *
     * @param synced true indicates successful synchronization,
     *               false indicates pending synchronization
     */
    void setSyncedToSheets(boolean synced);

    /**
     * Sets the timestamp of the last successful synchronization.
     * <p>Should be implemented to record when the entity was last synced.
     *
     * @param lastSynced timestamp of the last successful sync operation,
     *                   null if never synced
     */
    void setLastSynced(LocalDateTime lastSynced);

    /**
     * Gets the corresponding row identifier in Google Sheets.
     * <p>Should be implemented to return the row number where this entity
     * is stored in Google Sheets.
     *
     * @return integer row number in Google Sheets, or null if not synced
     */
    Integer getSheetRowId();

    /**
     * Sets the corresponding row identifier in Google Sheets.
     * <p>Should be implemented to store the row number where this entity
     * is located in Google Sheets.
     *
     * @param rowId integer row number in Google Sheets
     */
    void setSheetRowId(Integer rowId);

    /**
     * Gets the unique identifier of the entity.
     * <p>Should be implemented to return the entity's unique ID (typically database primary key).
     *
     * @return unique identifier of the entity
     */
    Long getId();
}
