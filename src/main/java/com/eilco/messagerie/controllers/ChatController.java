package com.eilco.messagerie.controllers;

import com.eilco.messagerie.models.request.MessageRequest;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.User;
import com.eilco.messagerie.services.interfaces.IChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.Optional;

@Controller
public class ChatController {

    private final IChatService chatService;
    private final UserRepository userRepository;

    public ChatController(IChatService chatService,
                          UserRepository userRepository) {
        this.chatService = chatService;
        this.userRepository = userRepository;
    }

    @MessageMapping("/chat.group")
    @SendTo("/topic/group")
    public Map<String, Object> sendGroupMessage(@Payload Map<String, Object> payload) {
        MessageRequest request = convertToMessageRequest(payload, true);

        return chatService.sendGroupMessage(request);
    }

    @MessageMapping("/user.connect")
    public void registerUser(@Payload Map<String, Object> payload,
                             StompHeaderAccessor headerAccessor) {
        String username = (String) payload.get("username");
        String sessionId = headerAccessor.getSessionId();

        try {
            chatService.registerUser(username, sessionId);
        } catch (IllegalArgumentException e) {
            chatService.sendErrorToUser(sessionId, e.getMessage());
        } catch (RuntimeException e) {
            chatService.sendErrorToUser(sessionId, e.getMessage());
            System.out.println("Tentative de connexion avec un utilisateur inexistant: " + username);
        } catch (Exception e) {
            chatService.sendErrorToUser(sessionId, "Une erreur est survenue lors de la connexion: " + e.getMessage());
            System.out.println("Erreur lors de l'enregistrement de l'utilisateur: " + e.getMessage());
        }
    }

    @MessageMapping("/chat.direct")
    public void sendDirectMessage(@Payload Map<String, Object> payload) {
        MessageRequest request = convertToMessageRequest(payload, false);
        chatService.sendDirectMessage(request);
    }

    private MessageRequest convertToMessageRequest(Map<String, Object> payload, boolean isGroupMessage) {
        MessageRequest request = new MessageRequest();
        request.setContent((String) payload.get("content"));
        String senderUsername = (String) payload.get("sender");
        if (senderUsername != null) {
            Optional<User> sender = userRepository.findByUsername(senderUsername);
            if (sender.isPresent()) {
                request.setSenderId(sender.get().getId());
            } else {
                throw new RuntimeException("Utilisateur expéditeur introuvable: " + senderUsername);
            }
        }

        if (isGroupMessage) {
            if (payload.containsKey("receiverGroupId")) {
                request.setReceiverGroupId(((Number) payload.get("receiverGroupId")).longValue());
            } else {
                Optional<User> sender = userRepository.findByUsername(senderUsername);
                if (sender.isPresent() && sender.get().getGroup() != null) {
                    request.setReceiverGroupId(sender.get().getGroup().getId());
                } else {
                    throw new RuntimeException("Aucun groupe trouvé pour l'utilisateur: " + senderUsername);
                }
            }
        } else {
            String receiverUsername = (String) payload.get("receiver");
            if (receiverUsername != null) {
                Optional<User> receiver = userRepository.findByUsername(receiverUsername);
                if (receiver.isPresent()) {
                    request.setReceiverUserId(receiver.get().getId());
                } else {
                    throw new RuntimeException("Utilisateur destinataire introuvable: " + receiverUsername);
                }
            }
        }
        return request;
    }
}