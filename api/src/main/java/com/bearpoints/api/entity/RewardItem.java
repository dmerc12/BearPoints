package com.bearpoints.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Represents a reward item.
 * <p>Implements {@link Syncable} for Google Sheets synchronization
 *
 * @see Syncable
 * @version 1.1
 * @author Dylan Mercer
 */
@Data
@Entity
@EntityListeners(SyncableEntityListener.class)
@Table(name = "reward_item", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name", name = "uk_reward_name")
})
public class RewardItem implements Syncable {
    /** Unique identifier (auto-generated) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Reward Item's name.
     * <p>Constraints:
     * <ul>
     *     <li>Non-blank</li>
     *     <li>Non-null</li>
     *     <li>Between 1 and 50 characters</li>
     * </ul>
     */
    @NotBlank(message = "Item name is required")
    @Size(min = 1, max = 50, message = "Name must be between 1 and 50 characters")
    @Column(nullable = false)
    private String name;

    /**
     * Reward Item's point cost value.
     * <p>Constraints:
     * <ul>
     *     <li>Non-null</li>
     *     <li>Positive or 0</li>
     * </ul>
     */
    @NotNull(message = "Point cost is required")
    @Min(value = 0, message = "Minimum cost is 0 points")
    @Column(name = "point_cost", nullable = false)
    private Integer pointCost;

    /**
     * Reward Item's stock quantity value.
     * <p>Constraints:
     * <ul>
     *     <li>Non-null</li>
     *     <li>Positive or 0</li>
     * </ul>
     */
    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Minimum stock quantity is 0")
    @Column(nullable = false)
    private Integer stock;

    /**
     * Reward Item's active status
     * <p>Constraints:
     * <ul>
     *     <li>Non-null</li>
     * </ul>
     */
    @NotNull(message = "Active status is required")
    @Column(nullable = false)
    private Boolean active = true;

    /** Associated student rewards used with reward item */
    @OneToMany(mappedBy = "rewardItem", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<StudentReward> studentRewards;

    /**
     * Version field for JPA optimistic locking.
     * <p>Automatically managed by JPA to prevent concurrent modifications
     */
    @Version
    @Column(nullable = false)
    private Long version;

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
