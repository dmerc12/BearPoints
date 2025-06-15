package com.bearpoints.api.domain;

import com.bearpoints.api.dto.Syncable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "student_reward")
public class StudentReward implements Syncable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "redeemed_at", nullable = false, updatable = false)
    private LocalDateTime redeemedAt;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    @NotNull(message = "Student is required")
    private Student student;

    @ManyToOne
    @JoinColumn(name = "reward_item_id", nullable = false)
    @NotNull(message = "Reward item is required")
    private RewardItem rewardItem;

    @Column(name = "last_synced")
    private LocalDateTime lastSynced;

    @NotNull(message = "Sync status is required")
    @Column(name = "synced_to_sheets", nullable = false)
    private Boolean syncedToSheets = false;

    @Column(name = "sheet_row_id")
    private Integer sheetRowId;

    @Override
    public void setSyncedToSheets(boolean synced) {
        this.syncedToSheets = synced;
    }

    @Override
    public void setLastSynced(LocalDateTime lastSynced) {
        this.lastSynced = lastSynced;
    }

    @Override
    public Integer getSheetRowId() {
        return this.sheetRowId;
    }

    @Override
    public void setSheetRowId(Integer rowId) {
        this.sheetRowId = rowId;
    }
}
