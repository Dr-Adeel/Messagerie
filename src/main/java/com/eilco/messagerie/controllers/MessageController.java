package com.eilco.messagerie.controllers;

import com.eilco.messagerie.exceptions.GroupNotFoundException;
import com.eilco.messagerie.exceptions.UserNotFoundException;
import com.eilco.messagerie.exceptions.UserNotMemberException;
import com.eilco.messagerie.models.request.MessageRequest;
import com.eilco.messagerie.models.response.MessageResponse;
import com.eilco.messagerie.services.interfaces.IMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Messaging", description = "Endpoints for sending and retrieving both direct and group messages.")
public class MessageController {

      private final IMessageService messageService;

      public MessageController(IMessageService messageService) {
            this.messageService = messageService;
      }

      @PostMapping("/send-private")
      @Operation(summary = "Send private Message", description = "Sends a new message from one user to another.")
      @ApiResponses(value = {
              @ApiResponse(responseCode = "200", description = "Message sent successfully and returned",
                      content = @Content(schema = @Schema(implementation = MessageResponse.class)))
      })
      public ResponseEntity<MessageResponse> sendPrivateMessage(@RequestBody MessageRequest request,
                  Authentication authentication) {
          try{
              MessageResponse message = messageService.sendPrivateMessage(request, authentication.getName());
              return ResponseEntity.ok(message);
          }catch (UserNotFoundException e){
              return ResponseEntity.notFound().build();
          }
      }

      @PostMapping("/send-group")
      @Operation(summary = "Send Group Message", description = "Sends a new message to group. Requires the sender to be a group member.")
      @ApiResponses(value = {
              @ApiResponse(responseCode = "200", description = "Message sent successfully and returned",
                      content = @Content(schema = @Schema(implementation = MessageResponse.class)))
      })
      public ResponseEntity<MessageResponse> sendGroupMessage(@Valid @RequestBody MessageRequest request,
                  Authentication authentication) {
          try{
              MessageResponse message = messageService.sendGroupMessage(request, authentication.getName());
              return ResponseEntity.ok(message);
          }catch (UserNotFoundException | GroupNotFoundException e){
              return ResponseEntity.notFound().build();
          }catch (UserNotMemberException e){
              return ResponseEntity.badRequest().build();
          }
      }

      @GetMapping("/private/{username}")
      @Operation(summary = "Get private Conversation", description = "Retrieves the message history between two specific users.")
      @ApiResponses(value = {
              @ApiResponse(responseCode = "200", description = "Conversation history retrieved successfully",
                      content = @Content(schema = @Schema(implementation = MessageResponse.class)))
      })
      public ResponseEntity<List<MessageResponse>> getPrivateConversation(@PathVariable String username,
                  Authentication authentication) {
            List<MessageResponse> messages = messageService.getPrivateConversation(authentication.getName(), username);
            return ResponseEntity.ok(messages);
      }

      @GetMapping("/group/{groupId}")
      @Operation(summary = "Get Group Conversation", description = "Retrieves the message history for a group. Requires the requesting user to be a member.")
      @ApiResponses(value = {
              @ApiResponse(responseCode = "200", description = "Group conversation history retrieved successfully",
                      content = @Content(schema = @Schema(implementation = MessageResponse.class)))
      })
      public ResponseEntity<List<MessageResponse>> getGroupMessages(@PathVariable Long groupId) {
            List<MessageResponse> messages = messageService.getGroupMessages(groupId);
            return ResponseEntity.ok(messages);
      }

    @GetMapping("/unread-count")
    @Operation(summary = "Get Number of unread messages", description = "Count the number of unread messages sent to the requesting user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Number of unread messages retrieved successfully",
                    content = @Content(schema = @Schema(implementation = MessageResponse.class)))
    })
    public ResponseEntity<Long> getUnreadCount(Authentication authentication) {
        return ResponseEntity.ok(messageService.getUnreadCount(authentication.getName()));
    }
}
