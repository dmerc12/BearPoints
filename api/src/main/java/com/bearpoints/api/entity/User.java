package com.bearpoints.api.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Represents an application user with authentication and role-based attributes.
 * <p>Implements {@link Syncable} for Google Sheets synchronization
 *
 * @see Role
 * @see Syncable
 * @author Dylan Mercer
 */
@Data
@Entity
@EntityListeners(SyncableEntityListener.class)
@Table(name = "app_user", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email", name = "uk_user_email")
})
public class User implements Syncable {
    /** Unique identifier (auto-generated) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * School-provided email address.
     * <p>Constraints:
     * <ul>
     *     <li>Non-blank</li>
     *     <li>Valid email format</li>
     *     <li>@okcps.org domain</li>
     * </ul>
     */
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Pattern(
            regexp = ".+@okcps\\.org$",
            message = "Email must be @okcps.org domain"
    )
    @Column(nullable = false)
    private String email;

    /**
     * User's first name.
     * <p>Constraints:
     * <ul>
     *     <li>Non-blank</li>
     *     <li>Between 1 and 100 characters</li>
     * </ul>
     */
    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1-100 characters")
    @Column(name = "first_name", nullable = false)
    private String firstName;

    /**
     * User's last name.
     * <p>Constraints:
     * <ul>
     *     <li>Non-blank</li>
     *     <li>Between 1 and 100 characters</li>
     * </ul>
     */
    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1-100 characters")
    @Column(name = "last_name", nullable = false)
    private String lastName;

    /**
     * User's assigned role.
     * <p>Constraints:
     * <ul>
     *     <li>Non-null</li>
     * </ul>
     */
    @Enumerated(EnumType.STRING)
    @NotNull(message = "Role is required")
    @Column(nullable = false)
    private Role role;

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
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Associated teacher profile (optional) */
    @OneToOne(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Teacher teacher;

    /** Associated student profile (optional) */
    @OneToOne(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH}, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Student student;

    /** Timestamp when last synced */
    @Column(name = "last_synced")
    private LocalDateTime lastSynced;

    /** Boolean indicating if synced */
    @NotNull(message = "Sync status is required")
    @Column(name = "synced_to_sheets", nullable = false)
    private Boolean syncedToSheets = false;

    /** Integer row id location for syncing */
    @Column(name = "sheet_row_id")
    private Integer sheetRowId;

    /**
     * Set sync status
     * @param synced true if synced to Google Sheets
     */
    @Override
    public void setSyncedToSheets(boolean synced) {
        this.syncedToSheets = synced;
    }

    /**
     * Set last synced timestamp
     * @param lastSynced timestamp of last sync
     */
    @Override
    public void setLastSynced(LocalDateTime lastSynced) {
        this.lastSynced = lastSynced;
    }

    /**
     * Get row id location
     * @return integer row id location in Google Sheets
     */
    @Override
    public Integer getSheetRowId() {
        return this.sheetRowId;
    }

    /**
     * Set row id location
     * @param rowId integer row for id location in Google Sheets
     */
    @Override
    public void setSheetRowId(Integer rowId) {
        this.sheetRowId = rowId;
    }
}
