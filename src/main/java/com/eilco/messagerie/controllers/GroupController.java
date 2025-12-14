package com.eilco.messagerie.controllers;

import com.eilco.messagerie.models.request.GroupRequest;
import com.eilco.messagerie.models.response.GroupResponse;
import com.eilco.messagerie.services.interfaces.IGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
@Tag(name = "Group Management", description = "Endpoints for  viewing, updating, deleting, ...,  and managing groups and members of these groups.")
public class GroupController {

    private final IGroupService groupService;

    public GroupController(IGroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/create")
    @Operation(summary = "Create a new group", description = "Creates a new communication group. The authenticated user will be automatically set as the group owner.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Group created successfully",
                    content = @Content(schema = @Schema(implementation = GroupResponse.class)))
    })
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody GroupRequest groupRequest,
                                                     Authentication authentication) {
        GroupResponse group = groupService.createGroup(groupRequest, authentication.getName());
        return ResponseEntity.ok(group);
    }

    @PostMapping("/{id}/add-member")
    @Operation(summary = "Add a member to a group", description = "Adds a specified user to a group. Requires administrative privileges or group ownership.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Operation processed. The message body indicates success or a problem encountered.",
                    content = @Content(schema = @Schema(type = "string", example = "Member added successfully !")))
    })
    public ResponseEntity<String> addMember(
            @Parameter(description = "id of the group to modify")
            @PathVariable Long id,
            @Parameter(description = "username of the user to be added")
            @RequestParam String username) {
        groupService.addMember(id, username);
        return ResponseEntity.ok("Member added successfully");
    }

    @DeleteMapping("/{id}/remove-member")
    @Operation(summary = "Remove a member from a group", description = "Removes a specified user from a group. Can be performed by an administrator, the group owner, or the user themselves.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Member successfully removed (No Content)")
    })
    public ResponseEntity<String> removeMember(
            @Parameter(description = "id of the group to modify")
            @PathVariable Long id,
            @Parameter(description = "username of the user to be removed")
            @RequestParam String username) {
        groupService.removeMember(id, username);
        return ResponseEntity.ok("Member removed successfully");
    }

    @GetMapping("/my")
    @Operation(summary = "Get user's groups", description = "Get all user groups.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "202", description = "Found user groups successfully")
    })
    public ResponseEntity<GroupResponse> getMyGroup(Authentication authentication) {
        return ResponseEntity.ok(groupService.getUserGroup(authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a group", description = "Removes a specified group. Can be performed by the group owner.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Member successfully removed (No Content)")
    })
    public ResponseEntity<Void> DeleteGroup(
            @Parameter(description = "id of the group to delete")
            @PathVariable Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

}
