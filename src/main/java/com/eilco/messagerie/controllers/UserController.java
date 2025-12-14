package com.eilco.messagerie.controllers;

import com.eilco.messagerie.models.response.UserResponse;
import com.eilco.messagerie.services.interfaces.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Slf4j
public class UserController {

    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("[USER CONTROLLER] GET /api/users - Fetching all users");

        List<UserResponse> users = userService.findAllUsers();

        log.info("[USER CONTROLLER] Retrieved {} users", users.size());

        return ResponseEntity.ok(users);
    }
}
