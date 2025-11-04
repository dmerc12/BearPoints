package com.bearpoints.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Data Transfer Object for person information.
 * <p>Represents basic identity information for students and teachers.
 *
 * <p>Behavior notes:
 * <ul>
 *     <li>Accepts null values for name fields</li>
 *     <li>Accepts empty strings for name fields</li>
 *     <li>ID must be non-negative (0 and positive values accepted)</li>
 * </ul>
 *
 * <p>Fields:
 * <ul>
 *     <li>{@code id} - Unique identifier</li>
 *     <li>{@code firstName} - First name</li>
 *     <li>{@code lastName} - Last name</li>
 * </ul>
 *
 * @version 1.1
 * @author Dylan Mercer
 */
@Getter
@AllArgsConstructor
public class PersonDTO {
    private Long id;
    private String firstName;
    private String lastName;
}
