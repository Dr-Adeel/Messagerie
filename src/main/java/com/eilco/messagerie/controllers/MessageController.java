package com.eilco.messagerie.controllers;

import com.eilco.messagerie.models.request.MessageRequest;
import com.eilco.messagerie.models.response.MessageResponse;
import com.eilco.messagerie.services.interfaces.IMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@Slf4j
public class MessageController {

    private final IMessageService messageService;

    public MessageController(IMessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send-private")
    public ResponseEntity<MessageResponse> sendPrivateMessage(@RequestBody MessageRequest request,
                                                              Authentication authentication) {
        log.info("[MESSAGE CONTROLLER] User '{}' is sending private message to user '{}'", 
                 authentication.getName(), request.getReceiverUserId());
        MessageResponse message = messageService.sendPrivateMessage(request, authentication.getName());
        log.info("[MESSAGE CONTROLLER] Private message sent successfully with ID: {}", message.getId());
        return ResponseEntity.ok(message);
    }

    @PostMapping("/send-group")
    public ResponseEntity<MessageResponse> sendGroupMessage(@RequestBody MessageRequest request,
                                                            Authentication authentication) {
        log.info("[MESSAGE CONTROLLER] User '{}' is sending message to group '{}'", 
                 authentication.getName(), request.getReceiverGroupId());
        MessageResponse message = messageService.sendGroupMessage(request, authentication.getName());
        log.info("[MESSAGE CONTROLLER] Group message sent successfully with ID: {}", message.getId());
        return ResponseEntity.ok(message);
    }

    @GetMapping("/private/{username}")
    public ResponseEntity<List<MessageResponse>> getPrivateConversation(@PathVariable String username,
                                                                        Authentication authentication) {
        log.info("[MESSAGE CONTROLLER] Fetching private conversation between '{}' and '{}'", 
                 authentication.getName(), username);
        List<MessageResponse> messages = messageService.getPrivateConversation(authentication.getName(), username);
        log.info("[MESSAGE CONTROLLER] Retrieved {} messages in private conversation", messages.size());
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<MessageResponse>> getGroupMessages(@PathVariable Long groupId) {
        log.info("[MESSAGE CONTROLLER] Fetching messages for group '{}'", groupId);
        List<MessageResponse> messages = messageService.getGroupMessages(groupId);
        log.info("[MESSAGE CONTROLLER] Retrieved {} messages for group '{}'", messages.size(), groupId);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        log.info("[MESSAGE CONTROLLER] Fetching unread message count for user '{}'", authentication.getName());
        long count = messageService.getUnreadCount(authentication.getName());
        log.info("[MESSAGE CONTROLLER] User '{}' has {} unread messages", authentication.getName(), count);
        return ResponseEntity.ok(count);
    }
}
