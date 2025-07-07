package com.bearpoints.api.entity;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

/**
 * Entity listener for {@link Syncable} entities to manage synchronization state.
 * <p>Automatically manages the {@code syncedToSheets} flag during persistence events:
 * <ul>
 *     <li>Sets flag to false on initial persistence</li>
 *     <li>Resets flag on update if sheet row ID exists</li>
 * </ul>
 * <p>Usage:
 * <pre>
 *     {@code
 *     @Entity
 *     @EntityListeners(SyncableEntityListener.class)
 *     public class YourEntity implements Syncable {...}
 *     }
 * </pre>
 *
 * @see Syncable
 * @version 1.0
 * @author Dylan Mercer
 */
public class SyncableEntityListener {
    /**
     * Pre-persist callback for synchronization state.
     * <p>Sets {@code syncedToSheets} to false for new entities before they are persisted.
     *
     * @param entity The entity being persisted
     */
    @PrePersist
    public void prePersist(Syncable entity) {
        entity.setSyncedToSheets(false);
    }

    /**
     * Pre-update callback for synchronization state.
     * <p>Resets {@code syncedToSheets} to false if:
     * <ul>
     *     <li>Entity has been previously synced ({@code sheetRowId != null})</li>
     *     <li>Entity is being modified</li>
     * </ul>
     *
     * @param entity The entity being updated
     */
    @PreUpdate
    public void preUpdate(Syncable entity) {
        if (entity.getSheetRowId() != null) {
            entity.setSyncedToSheets(false);
        }
    }
}
