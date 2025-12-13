package com.eilco.messagerie.controllers;

import com.eilco.messagerie.models.request.MessageRequest;
import com.eilco.messagerie.models.response.MessageResponse;
import com.eilco.messagerie.services.interfaces.IMessageService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

      private final IMessageService messageService;

      public MessageController(IMessageService messageService) {
            this.messageService = messageService;
      }

      @PostMapping("/send-private")
      public ResponseEntity<MessageResponse> sendPrivateMessage(@RequestBody MessageRequest request,
                  Authentication authentication) {
            MessageResponse message = messageService.sendPrivateMessage(request, authentication.getName());
            return ResponseEntity.ok(message);
      }

      @PostMapping("/send-group")
      public ResponseEntity<MessageResponse> sendGroupMessage(@RequestBody MessageRequest request,
                  Authentication authentication) {
            MessageResponse message = messageService.sendGroupMessage(request, authentication.getName());
            return ResponseEntity.ok(message);
      }

      @GetMapping("/private/{username}")
      public ResponseEntity<List<MessageResponse>> getPrivateConversation(@PathVariable String username,
                  Authentication authentication) {
            List<MessageResponse> messages = messageService.getPrivateConversation(authentication.getName(), username);
            return ResponseEntity.ok(messages);
      }

      @GetMapping("/group/{groupId}")
      public ResponseEntity<List<MessageResponse>> getGroupMessages(@PathVariable Long groupId) {
            List<MessageResponse> messages = messageService.getGroupMessages(groupId);
            return ResponseEntity.ok(messages);
      }

      @GetMapping("/unread-count")
      public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
            return ResponseEntity.ok(messageService.getUnreadCount(authentication.getName()));
      }

      @PutMapping("/read/{messageId}")
      public ResponseEntity<Void> markAsRead(@PathVariable Long messageId) {
            messageService.markAsRead(messageId);
            return ResponseEntity.ok().build();
      }
}
