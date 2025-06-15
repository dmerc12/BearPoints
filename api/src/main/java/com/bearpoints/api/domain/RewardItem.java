package com.bearpoints.api.domain;

import com.bearpoints.api.service.GoogleSheetsSyncService;
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

@Data
@Entity
@Table(name = "reward_item", uniqueConstraints = {
        @UniqueConstraint(columnNames = "name", name = "uk_reward_name")
})
public class RewardItem implements GoogleSheetsSyncService.Syncable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Item name is required")
    @Size(min = 3, max = 50, message = "Name must be 3-50 characters")
    @Column(nullable = false)
    private String name;

    @NotNull(message = "Point cost is required")
    @Min(value = 1, message = "Minimum cost is 1 point")
    @Column(name = "point_cost", nullable = false)
    private Integer pointCost;

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock cannot be negative")
    @Column(nullable = false)
    private Integer stock;

    @OneToMany(mappedBy = "rewardItem", cascade = CascadeType.ALL)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<StudentReward> studentRewards;

    @Column(name = "last_synced")
    private LocalDateTime lastSynced;

    @NotNull(message = "Sync status is required")
    @Column(name = "synced_to_sheets", nullable = false)
    private Boolean syncedToSheets = false;

    @Override
    public void setSyncedToSheets(boolean synced) {
        this.syncedToSheets = synced;
    }

    @Override
    public void setLastSynced(LocalDateTime lastSynced) {
        this.lastSynced = lastSynced;
    }
}
