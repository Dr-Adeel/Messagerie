package com.eilco.messagerie.controller;


import com.eilco.messagerie.service.Impl.GroupMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author akdim
 */

@RestController
@RequestMapping("/api/group-messages")
public class GroupMessageController {

    @Autowired
    private GroupMessageService groupMessageService;

    /**
     * ENVOYER MESSAGE AU GROUPE
     */
    @PostMapping("/{groupId}/send")
    public ResponseEntity<?> sendMessage(@PathVariable Long groupId,
                                         @RequestParam Long senderId,
                                         @RequestParam String content) {

        return ResponseEntity.ok(
                groupMessageService.sendMessage(senderId, groupId, content)
        );
    }

    /**
     * RÉCUPÉRER L'HISTORIQUE
     */
    @GetMapping("/{groupId}/history")
    public ResponseEntity<?> getGroupMessages(@PathVariable Long groupId,
                                              @RequestParam Long userId) {

        return ResponseEntity.ok(
                groupMessageService.getGroupMessages(groupId, userId)
        );
    }
}
