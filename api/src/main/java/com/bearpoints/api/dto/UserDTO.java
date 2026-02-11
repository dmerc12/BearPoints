package com.bearpoints.api.dto;

import com.bearpoints.api.entity.Role;
import com.bearpoints.api.entity.User;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;

/**
 * Data Transfer Object for {@link User} entities.
 * <p>Provides a simplified view of user data suitable for API responses.
 *
 * <p>Fields:
 * <ul>
 *     <li>{@code id} - Unique user identifier</li>
 *     <li>{@code email} - User's email address</li>
 *     <li>{@code firstName} - User's first name</li>
 *     <li>{@code lastName} - User's last name</li>
 *     <li>{@code role} - User's assigned role (enum name)</li>
 * </ul>
 *
 * @version 1.1
 * @author Dylan Mercer
 */
@Getter
public class UserDTO {
    private final Long id;

    @Pattern(regexp = ".+@okcps\\.org$", message = "Email must be @okcps.org domain")
    private final String email;

    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private final String firstName;

    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private final String lastName;

    @NotNull(message = "Role is required")
    private final Role role;

    /**
     * Constructor for Jackson deserialization
     */
    @JsonCreator
    public UserDTO(@JsonProperty("id") Long id,
                   @JsonProperty("email") String email,
                   @JsonProperty("firstName") String firstName,
                   @JsonProperty("lastName") String lastName,
                   @JsonProperty("role") String role) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = validateAndConvertRole(role);
    }

    /**
     * Constructs a UserDTO from a User entity.
     *
     * @param user Source user entity
     */
    public UserDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.role = user.getRole();
    }

    private Role validateAndConvertRole(String roleString) {
        if (roleString == null) {
            return null;
        }
        String trimmed = roleString.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        String normalized = trimmed.toUpperCase();
        try {
            return Role.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + roleString + ". Valid values are: " +
                    java.util.Arrays.toString(Role.values()));
        }
    }
}
