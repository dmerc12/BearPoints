package com.bearpoints.api.dto;

import com.bearpoints.api.domain.User;
import lombok.Getter;

@Getter
public class UserDTO {
    private final Long id;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final String role;

    public UserDTO(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.role = user.getRole().name();
    }
}
