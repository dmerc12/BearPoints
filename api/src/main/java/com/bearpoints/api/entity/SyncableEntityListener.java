package com.bearpoints.api.entity;

import com.bearpoints.api.dto.Syncable;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class SyncableEntityListener {
    @PrePersist
    public void prePersist(Syncable entity) {
        entity.setSyncedToSheets(false);
    }

    @PreUpdate
    public void preUpdate(Syncable entity) {
        if (entity.getSheetRowId() != null) {
            entity.setSyncedToSheets(false);
        }
    }
}
