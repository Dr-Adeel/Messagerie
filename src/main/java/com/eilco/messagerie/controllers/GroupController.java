package com.eilco.messagerie.controllers;

import com.eilco.messagerie.models.request.GroupRequest;
import com.eilco.messagerie.models.response.GroupResponse;
import com.eilco.messagerie.services.interfaces.IGroupService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
public class GroupController {

    private final IGroupService groupService;

    public GroupController(IGroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/create")
    public ResponseEntity<GroupResponse> createGroup(@RequestBody GroupRequest groupRequest,
                                                     Authentication authentication) {
        GroupResponse group = groupService.createGroup(groupRequest, authentication.getName());
        return ResponseEntity.ok(group);
    }

    @PostMapping("/{id}/add-member")
    public ResponseEntity<String> addMember(@PathVariable Long id, @RequestParam String username) {
        groupService.addMember(id, username);
        return ResponseEntity.ok("Member added successfully");
    }

    @PostMapping("/{id}/remove-member")
    public ResponseEntity<String> removeMember(@PathVariable Long id, @RequestParam String username) {
        groupService.removeMember(id, username);
        return ResponseEntity.ok("Member removed successfully");
    }

    @GetMapping("/my")
    public ResponseEntity<GroupResponse> getMyGroup(Authentication authentication) {
        return ResponseEntity.ok(groupService.getUserGroup(authentication.getName()));
    }
}
