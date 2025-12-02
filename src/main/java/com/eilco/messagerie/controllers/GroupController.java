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

@RequiredArgsConstructor
@RequestMapping("/api/group")
@RestController
public class GroupController {

    private final IGroupService groupService;
    private final CurrentUserService currentUserService;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody GroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(groupService.createGroup(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGroup(@PathVariable Long id) {
        groupService.deleteGroup(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{groupId}/member/{userId}")
    public ResponseEntity<String> addMember(@PathVariable Long groupId, @PathVariable Long userId) {
        User currentUser = currentUserService.getCurrentUser();
        if(currentUser != null) {
            groupService.addMember(groupId, userId, currentUser.getId());
            return ResponseEntity.status(HttpStatus.OK)
                    .body("Member added successfully !");
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body("A problem encountered while adding a member !");

    }

    @DeleteMapping("/{groupId}/member/{userId}")
    public ResponseEntity<Void> deleteMember(@PathVariable Long groupId, @PathVariable Long userId) {
        User currentUser = currentUserService.getCurrentUser();
        groupService.removeMember(groupId,userId, currentUser.getId());
        return ResponseEntity.noContent().build();
    }

}
