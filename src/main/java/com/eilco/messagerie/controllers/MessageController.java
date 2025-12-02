package com.eilco.messagerie.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.eilco.messagerie.mappers.MessageMapper;
import com.eilco.messagerie.models.request.MessageRequest;
import com.eilco.messagerie.models.response.MessageResponse;
import com.eilco.messagerie.service.IGroupMessageService;
import com.eilco.messagerie.services.interfaces.IDirectMessageService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/api/message")
@RestController
public class MessageController {

    private final IGroupMessageService groupMessageService;
    private final IDirectMessageService directMessageService;
    private final MessageMapper messageMapper;

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
        return groupMessageService.getGroupMessages(groupId, userId)
                .stream()
                .map(messageMapper::toResponse)
                .toList();
    }

    @PostMapping("/group")
    public ResponseEntity<MessageResponse> sendGroupMessage(@Valid @RequestBody MessageRequest request) {
        if (request.getReceiverGroupId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "receiverGroupId est obligatoire pour un message de groupe.");
        }

        var message = groupMessageService.sendMessageGroup(
                request.getSenderId(),
                request.getReceiverGroupId(),
                request.getContent());

        return ResponseEntity.status(HttpStatus.OK)
                .body(messageMapper.toResponse(message));
    }

}
