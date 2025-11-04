package com.bearpoints.api.unit.entity;

import com.bearpoints.api.entity.Syncable;
import com.bearpoints.api.entity.SyncableEntityListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link SyncableEntityListener} functionality.
 * <p>Tests include:
 * <ul>
 *     <li>{@code @PrePersist} callback behavior</li>
 *     <li>{@code @PreUpdate} callback scenarios</li>
 * </ul>
 * <p>Test scenarios cover:
 * <ul>
 *     <li>New entity persistence</li>
 *     <li>Entity update with existing sheet row ID</li>
 *     <li>Entity update without sheet row ID</li>
 * </ul>
 *
 * @see SyncableEntityListener
 * @see Syncable
 * @version 1.0
 * @author Dylan Mercer
 */
public class SyncableEntityListenerTests {
    private SyncableEntityListener listener;
    private TestSyncableEntity entity;

    @BeforeEach
    public void setUp() {
        listener = new SyncableEntityListener();
        entity = new TestSyncableEntity();
    }

    /**
     * Mock implementation of {@link Syncable} for testing.
     */
    private static class TestSyncableEntity implements Syncable {
        private boolean syncedToSheets;
        private Integer sheetRowId;

        @Override
        public void setSyncedToSheets(boolean synced) {
            this.syncedToSheets = synced;
        }

        @Override
        public void setLastSynced(LocalDateTime lastSynced) {
        }

        @Override
        public Integer getSheetRowId() {
            return sheetRowId;
        }

        @Override
        public void setSheetRowId(Integer rowId) {
            this.sheetRowId = rowId;
        }

        @Override
        public Long getId() {
            return 1L;
        }
    }

    /** Tests pre-persist callback */
    @Nested
    @DisplayName("PrePersist callback tests")
    class PrePersistTests {
        /** Tests synced flag reset on new entity */
        @Test
        @DisplayName("New entity sets syncedToSheets to false")
        void newEntitySetsSyncedToFalse() {
            entity.setSyncedToSheets(true);
            listener.prePersist(entity);
            assertThat(entity.syncedToSheets).isFalse();
        }
    }

    /** Tests pre-update callback */
    @Nested
    @DisplayName("PreUpdate callback tests")
    class PreUpdateTests {
        /** Tests update with existing row ID resets flag */
        @Test
        @DisplayName("Update with sheet row ID sets syncedToSheets to false")
        void updateWithRowIdResetsSyncFlag() {
            entity.setSheetRowId(42);
            entity.setSyncedToSheets(true);
            listener.preUpdate(entity);
            assertThat(entity.syncedToSheets).isFalse();
        }

        /** Tests update without row ID preserves flag */
        @Test
        @DisplayName("Update without sheet row ID preserves synced state")
        void updateWithoutRowIdPreservesSyncFlag() {
            entity.setSheetRowId(null);
            entity.setSyncedToSheets(true);
            listener.preUpdate(entity);
            assertThat(entity.syncedToSheets).isTrue();
        }
    }
}
