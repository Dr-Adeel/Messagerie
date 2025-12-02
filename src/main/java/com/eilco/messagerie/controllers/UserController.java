package com.eilco.messagerie.controllers;

import com.eilco.messagerie.models.request.UserRequest;
import com.eilco.messagerie.models.response.UserResponse;
import com.eilco.messagerie.services.interfaces.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/user")
@RestController
public class UserController {

    private final IUserService userService;

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        return userService.getById(id);
    }

    @GetMapping("/all")
    public List<UserResponse> getUsers() {
        return userService.getAll();
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return ResponseEntity.ok(userService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/search", params = "name")
    public ResponseEntity<List<UserResponse>> searchByFirstName(@RequestParam String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search name cannot be empty");
        }
        return ResponseEntity.ok(userService.searchByFirstName(name));
    }

    @GetMapping(value = "/search", params = "username")
    public ResponseEntity<List<UserResponse>> searchByUsername(@RequestParam String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search name cannot be empty");
        }
        return ResponseEntity.ok(userService.searchByUsername(username));
    }
}
