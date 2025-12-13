package com.eilco.messagerie.controllers;

import com.eilco.messagerie.models.request.UserRequest;
import com.eilco.messagerie.models.response.UserResponse;
import com.eilco.messagerie.services.interfaces.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@Tag(name = "Users Management", description = "Endpoints for viewing, updating, deleting, and searching user profiles.")
public class UserController {

      private final IUserService userService;

      public UserController(IUserService userService) {
            this.userService = userService;
      }

    @GetMapping("/all")
    @Operation(summary = "Get All Users", description = "Retrieves list of all registered users in the system.")
    @ApiResponse(responseCode = "200", description = "List of users retrieved",
            content = @Content(schema = @Schema(implementation = UserResponse.class)))
      public ResponseEntity<List<UserResponse>> getAllUsers() {
            return ResponseEntity.ok(userService.findAllUsers());
      }

    @GetMapping("/{id}")
    @Operation(summary = "Get User by ID", description = "Retrieves the detailed information for a specific user using their unique ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found successfully",
                    content = @Content(schema = @Schema(implementation = UserResponse.class)))
    })
    public UserResponse getUser(
            @Parameter(description = "Unique ID of the user to retrieve")
            @PathVariable Long id) {
        return userService.getById(id);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a User Profile", description = "Updates an existing user's information.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User profile updated successfully",
                         content = @Content(schema = @Schema(implementation = UserResponse.class)))
    })
    public ResponseEntity<String> update(
            @Parameter(description = "Unique ID of the user to update")
            @PathVariable Long id,
            @Valid @RequestBody UserRequest request) {
          try {
              return ResponseEntity.ok("user :" + userService.update(id, request).getUsername() + " information updated successfully");
          } catch (EntityNotFoundException e) {
              return ResponseEntity.badRequest().body(e.getMessage());
          }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a User", description = "Deletes a specific user from the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User successfully deleted (No Content)")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Unique ID of the user to delete")
            @PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping(value = "/search", params = "name")
    @Operation(summary = "Search by First Name", description = "Searches for users whose first name contains the provided string.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users matching the first name query",
                    content = @Content(schema = @Schema(implementation = UserResponse.class)))
    })
    public ResponseEntity<List<UserResponse>> searchByFirstName(
            @Parameter(description = "The first name to search for")
            @RequestParam String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search name cannot be empty");
        }
        return ResponseEntity.ok(userService.findByFirstName(name));
    }

    @GetMapping(value = "/search", params = "username")
    @Operation(summary = "Search by Username", description = "Searches for users whose username contains the provided string.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of users matching the username query",
                    content = @Content(schema = @Schema(implementation = UserResponse.class)))
    })
    public ResponseEntity<List<UserResponse>> searchByUsername(
            @Parameter(description = "The username to search for")
            @RequestParam String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search name cannot be empty");
        }
        return ResponseEntity.ok(userService.findByUserName(username));
    }
}
