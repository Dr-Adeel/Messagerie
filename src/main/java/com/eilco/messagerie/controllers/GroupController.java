package com.eilco.messagerie.controllers;

import com.eilco.messagerie.models.request.GroupRequest;
import com.eilco.messagerie.models.response.GroupResponse;
import com.eilco.messagerie.repositories.entities.User;
import com.eilco.messagerie.services.interfaces.IGroupService;
import com.eilco.messagerie.services.implementations.CurrentUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/group")
public class GroupController {

    private final IGroupService groupService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody GroupRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(groupService.createGroup(request));
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        try {
            groupService.deleteGroup(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/{groupId}/member/{userId}")
    public ResponseEntity<String> addMember(@PathVariable Long groupId, @PathVariable Long userId) {
        User currentUser = currentUserService.getCurrentUser();
        try {
            groupService.addMember(groupId, userId, currentUser.getId());
            return ResponseEntity.ok("Member added successfully!");
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{groupId}/member/{userId}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long groupId, @PathVariable Long userId) {
        User currentUser = currentUserService.getCurrentUser();
        try {
            groupService.removeMember(groupId, userId, currentUser.getId());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
