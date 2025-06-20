package com.bearpoints.api.controller;

import com.bearpoints.api.dto.UserDTO;
import com.bearpoints.api.security.FirebaseUserDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin
@RestController
@RequestMapping("/api/users")
public class UserController {
    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(
            @AuthenticationPrincipal FirebaseUserDetails userDetails) {
        return ResponseEntity.ok(new UserDTO(userDetails.getUser()));
    }
}
