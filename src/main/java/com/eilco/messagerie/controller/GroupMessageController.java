package com.eilco.messagerie.controller;


import com.eilco.messagerie.models.request.MessageRequest;
import com.eilco.messagerie.service.Impl.GroupMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author akdim
 */

@RestController
@RequestMapping("/group-messages")
public class GroupMessageController {

    @Autowired
    private GroupMessageService groupMessageService;


    @PostMapping("/{groupId}/send")
    public ResponseEntity<?> sendMessageGroup(@PathVariable Long groupId, @RequestBody MessageRequest messageRequest) {


        return ResponseEntity.ok(
                groupMessageService.sendMessageGroup(messageRequest.getSenderId(), groupId, messageRequest.getContent())
        );
    }


    @GetMapping("/{groupId}/history")
    public ResponseEntity<?> getGroupMessages(@PathVariable Long groupId,
                                              @RequestParam Long userId) {

        return ResponseEntity.ok(
                groupMessageService.getGroupMessages(groupId, userId)
        );
    }
}
