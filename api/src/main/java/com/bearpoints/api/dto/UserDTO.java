package com.bearpoints.api.dto;

import com.bearpoints.api.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
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
 * @version 1.0
 * @author Dylan Mercer
 */
@Getter
public class UserDTO {
    private final Long id;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Pattern(regexp = ".+@okcps\\.org$", message = "Email must be @okcps.org domain")
    private final String email;

    @NotBlank(message = "First name is required")
    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private final String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private final String lastName;

    private final String role;

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
        this.role = user.getRole().name();
    }
}
