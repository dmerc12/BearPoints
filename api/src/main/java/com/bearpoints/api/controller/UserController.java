package com.bearpoints.api.controller;

import com.bearpoints.api.dto.UserDTO;
import com.bearpoints.api.entity.User;
import com.bearpoints.api.security.FirebaseUserDetails;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user-related operations.
 * <p>Provides endpoints for:
 * <ul>
 *     <li>Retrieving authenticated user details</li>
 * </ul>
 *
 * <p>Endpoints:
 * <ul>
 *     <li>{@code GET /api/users/me} - Returns current authenticated user's details</li>
 * </ul>
 *
 * @see FirebaseUserDetails
 * @see UserDTO
 * @version 1.1
 * @author Dylan Mercer
 */
@CrossOrigin
@RestController
@RequestMapping("/api/users")
public class UserController {
    /**
     * Retrieves details of the currently authenticated user.
     * <p>Requires valid authentication token. Returns user information in simplified DTO format.
     *
     * @param userDetails Authenticated user's security context (required)
     * @return HTTP 200 OK with user details
     * @throws IllegalStateException if user details are invalid
     */
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(
            @NotNull @AuthenticationPrincipal FirebaseUserDetails userDetails) {
        User user = userDetails.getUser();
        if (user == null) {
            throw new IllegalStateException("Invalid user details");
        }
        return ResponseEntity.ok(new UserDTO(user));
    }
}
