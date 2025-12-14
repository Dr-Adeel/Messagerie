package com.eilco.messagerie.controllers;

import com.eilco.messagerie.models.request.GroupRequest;
import com.eilco.messagerie.models.response.GroupResponse;
import com.eilco.messagerie.services.interfaces.IGroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
@Slf4j
public class GroupController {

    private final IGroupService groupService;

    public GroupController(IGroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/create")
    public ResponseEntity<GroupResponse> createGroup(@RequestBody GroupRequest groupRequest,
                                                     Authentication authentication) {
        log.info("[GROUP CONTROLLER] User '{}' is creating a new group '{}'", authentication.getName(), groupRequest.getName());
        GroupResponse group = groupService.createGroup(groupRequest, authentication.getName());
        log.info("[GROUP CONTROLLER] Group '{}' created successfully with ID: {}", group.getName(), group.getId());
        return ResponseEntity.ok(group);
    }

    @PostMapping("/{id}/add-member")
    public ResponseEntity<String> addMember(@PathVariable Long id, @RequestParam String username) {
        log.info("[GROUP CONTROLLER] Adding member '{}' to group ID '{}'", username, id);
        groupService.addMember(id, username);
        log.info("[GROUP CONTROLLER] Member '{}' added successfully to group ID '{}'", username, id);
        return ResponseEntity.ok("Member added successfully");
    }

    @PostMapping("/{id}/remove-member")
    public ResponseEntity<String> removeMember(@PathVariable Long id, @RequestParam String username) {
        log.info("[GROUP CONTROLLER] Removing member '{}' from group ID '{}'", username, id);
        groupService.removeMember(id, username);
        log.info("[GROUP CONTROLLER] Member '{}' removed successfully from group ID '{}'", username, id);
        return ResponseEntity.ok("Member removed successfully");
    }

    @GetMapping("/my")
    public ResponseEntity<GroupResponse> getMyGroup(Authentication authentication) {
        log.info("[GROUP CONTROLLER] Fetching group for user '{}'", authentication.getName());
        GroupResponse group = groupService.getUserGroup(authentication.getName());
        log.info("[GROUP CONTROLLER] Retrieved group '{}' for user '{}'", group.getName(), authentication.getName());
        return ResponseEntity.ok(group);
    }
}
