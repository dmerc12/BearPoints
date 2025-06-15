package com.bearpoints.api.dto;

import java.time.LocalDateTime;

public interface Syncable {
    void setSyncedToSheets(boolean synced);
    void setLastSynced(LocalDateTime lastSynced);
    Integer getSheetRowId();
    void setSheetRowId(Integer rowId);
}
