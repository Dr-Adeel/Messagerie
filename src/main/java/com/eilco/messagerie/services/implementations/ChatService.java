package com.eilco.messagerie.services.implementations;

import com.eilco.messagerie.mappers.MessageMapper;
import com.eilco.messagerie.models.request.MessageRequest;
import com.eilco.messagerie.models.response.MessageResponse;
import com.eilco.messagerie.repositories.GroupRepository;
import com.eilco.messagerie.repositories.MessageRepository;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.Message;
import com.eilco.messagerie.repositories.entities.User;
import com.eilco.messagerie.services.interfaces.IChatService;
import com.eilco.messagerie.services.interfaces.IUserSessionService;
import com.eilco.messagerie.websocket.dto.UserStatusNotification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class ChatService implements IChatService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final MessageMapper messageMapper;
    private final IUserSessionService userSessionService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatService(
            MessageRepository messageRepository,
            UserRepository userRepository,
            GroupRepository groupRepository,
            MessageMapper messageMapper,
            IUserSessionService userSessionService,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.messageMapper = messageMapper;
        this.userSessionService = userSessionService;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Transactional
    public Map<String, Object> sendGroupMessage(MessageRequest request) {
        if (request.getReceiverGroupId() == null) {
            throw new IllegalArgumentException("L'ID du groupe destinataire est requis pour un message de groupe");
        }
        if (request.getReceiverUserId() != null) {
            throw new IllegalArgumentException("Un message de groupe ne peut pas avoir de destinataire utilisateur");
        }

        // Récupérer l'expéditeur
        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new RuntimeException("Utilisateur expéditeur introuvable avec l'ID: " + request.getSenderId()));

        // Récupérer le groupe destinataire
        Group receiverGroup = groupRepository.findById(request.getReceiverGroupId())
                .orElseThrow(() -> new RuntimeException("Groupe introuvable avec l'ID: " + request.getReceiverGroupId()));

        // Créer et sauvegarder le message
        Message message = new Message();
        message.setContent(request.getContent());
        message.setSender(sender);
        message.setReceiverGroup(receiverGroup);
        message.setReceiverUser(null);
        message.setTimestamp(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);

        // Convertir en MessageResponse puis en format frontend
        MessageResponse response = messageMapper.toResponse(savedMessage);
        return convertToFrontFormat(response);
    }

    @Override
    @Transactional
    public void sendDirectMessage(MessageRequest request) {
        if (request.getReceiverUserId() == null) {
            throw new IllegalArgumentException("L'ID du destinataire est requis pour un message direct");
        }
        if (request.getReceiverGroupId() != null) {
            throw new IllegalArgumentException("Un message direct ne peut pas avoir de groupe destinataire");
        }

        // Récupérer l'expéditeur
        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new RuntimeException("Utilisateur expéditeur introuvable avec l'ID: " + request.getSenderId()));

        // Récupérer le destinataire
        User receiver = userRepository.findById(request.getReceiverUserId())
                .orElseThrow(() -> new RuntimeException("Utilisateur destinataire introuvable avec l'ID: " + request.getReceiverUserId()));

        // Créer et sauvegarder le message
        Message message = new Message();
        message.setContent(request.getContent());
        message.setSender(sender);
        message.setReceiverUser(receiver);
        message.setReceiverGroup(null);
        message.setTimestamp(LocalDateTime.now());

        Message savedMessage = messageRepository.save(message);

        // Convertir en MessageResponse puis en format frontend
        MessageResponse response = messageMapper.toResponse(savedMessage);
        Map<String, Object> frontMessage = convertToFrontFormat(response);

        // Envoyer au destinataire via le topic
        if (response.getReceiverUsername() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/user/" + response.getReceiverUsername(),
                    frontMessage
            );
        }

        if (response.getSenderUsername() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/user/" + response.getSenderUsername(),
                    frontMessage
            );
        }
    }

    @Override
    public void registerUser(String username, String sessionId) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom d'utilisateur est requis");
        }

        // Vérifier que la session existe
        if (sessionId == null) {
            throw new IllegalArgumentException("Session invalide");
        }

        // Vérifier que l'utilisateur existe dans la base de données
        User user = userRepository.findByUsername(username.trim())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable: " + username + ". Veuillez vérifier votre nom d'utilisateur."));

        String validUsername = user.getUsername();

        // Enregistrer la session
        userSessionService.addSession(sessionId, validUsername);

        // Récupérer la liste des utilisateurs en ligne
        Set<String> onlineUsers = userSessionService.getOnlineUsers();

        Map<String, Object> onlineUsersMessage = new HashMap<>();
        onlineUsersMessage.put("type", "ONLINE_USERS");
        onlineUsersMessage.put("users", new ArrayList<>(onlineUsers));
        messagingTemplate.convertAndSend("/topic/user/" + validUsername + "/online.users", onlineUsersMessage);

        // Notifier tous les utilisateurs qu'un utilisateur est en ligne
        UserStatusNotification notification = new UserStatusNotification(validUsername, true);
        messagingTemplate.convertAndSend("/topic/user.status", notification);

        System.out.println("Utilisateur enregistré: " + validUsername + " (Session: " + sessionId + "). Utilisateurs en ligne: " + onlineUsers.size());
    }

    @Override
    public void sendErrorToUser(String sessionId, String errorMessage) {
        if (sessionId == null) {
            return;
        }

        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("type", "ERROR");
        errorResponse.put("message", errorMessage);
        errorResponse.put("sessionId", sessionId);
        errorResponse.put("timestamp", LocalDateTime.now());

        // Envoyer l'erreur via un topic global pour les erreurs de connexion
        messagingTemplate.convertAndSend("/topic/connection.error", errorResponse);

        System.out.println("Erreur de connexion pour la session " + sessionId + ": " + errorMessage);
    }

    private Map<String, Object> convertToFrontFormat(MessageResponse response) {
        Map<String, Object> frontMessage = new HashMap<>();
        frontMessage.put("content", response.getContent());
        frontMessage.put("sender", response.getSenderUsername());
        frontMessage.put("timestamp", response.getTimestamp());
        frontMessage.put("type", response.getMessageType());

        if ("DM".equals(response.getMessageType()) && response.getReceiverUsername() != null) {
            frontMessage.put("receiver", response.getReceiverUsername());
        } else {
            frontMessage.put("receiver", null);
        }

        return frontMessage;
    }
}
