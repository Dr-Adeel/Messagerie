package com.eilco.messagerie.controllers;

import com.eilco.messagerie.models.request.MessageRequest;
import com.eilco.messagerie.models.response.MessageResponse;
import com.eilco.messagerie.service.directMessageService;
import com.eilco.messagerie.service.groupMessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/message")
@RestController
public class MessageController {

    private final IGroupMessageService groupMessageService;
    private final IDirectMessageService directMessageService;

    @GetMapping("/direct/{userAId}/{userBId}")
    public List<MessageResponse> getPrivateConversation(
            @PathVariable Long userAId,
            @PathVariable Long userBId){

        return directMessageService.getPrivateConversation(userAId,userBId);
    }

    @PostMapping("/direct")
    public ResponseEntity<MessageResponse> sendDirectMessage(@Valid @RequestBody MessageRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(directMessageService.sendDirectMessage(request));
    }

    @GetMapping("/group/{userId}/{groupId}")
    public List<MessageResponse> getGroupConversation(@PathVariable Long userId, @PathVariable Long groupId){
        return groupMessageService.getGroupMessages(userId, groupId);
    }

    @PostMapping("/group")
    public ResponseEntity<MessageResponse> sendGroupMessage(@Valid @RequestBody MessageRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(directMessageService.sendMessageGroup(request));
    }

}
