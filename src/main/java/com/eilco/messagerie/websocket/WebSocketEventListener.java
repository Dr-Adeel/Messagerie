package com.eilco.messagerie.websocket;

import com.eilco.messagerie.services.interfaces.IUserSessionService;
import com.eilco.messagerie.websocket.dto.UserStatusNotification;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final IUserSessionService userSessionService;

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate,
                                  IUserSessionService userSessionService) {
        this.messagingTemplate = messagingTemplate;
        this.userSessionService = userSessionService;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        System.out.println("Nouvelle connexion WebSocket: " + sessionId);
        // Le username sera enregistré via l'endpoint /app/user.connect
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();

        // Récupérer le username depuis la session
        String username = userSessionService.removeSession(sessionId);

        if (username != null) {
            // Vérifier si l'utilisateur a encore d'autres sessions actives
            boolean isStillOnline = userSessionService.isUserOnline(username);

            // Si l'utilisateur n'a plus de sessions, notifier qu'il est hors ligne
            if (!isStillOnline) {
                UserStatusNotification notification = new UserStatusNotification(username, false);
                messagingTemplate.convertAndSend("/topic/user.status", notification);
                System.out.println("Utilisateur déconnecté: " + username + " (Session: " + sessionId + ")");
            } else {
                System.out.println("Session fermée pour " + username + " mais toujours en ligne (Session: " + sessionId + ")");
            }
        }
    }
}