package com.eilco.messagerie.services.implementations;

import com.eilco.messagerie.services.interfaces.IUserSessionService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserSessionService implements IUserSessionService {

    // Map pour stocker les utilisateurs connectés
    private final Map<String, String> sessionToUser = new ConcurrentHashMap<>();

    // Map pour stocker les sessions par utilisateur
    private final Map<String, Set<String>> userToSessions = new ConcurrentHashMap<>();

    @Override
    public void addSession(String sessionId, String username) {
        sessionToUser.put(sessionId, username);
        userToSessions.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet()).add(sessionId);
    }

    @Override
    public String removeSession(String sessionId) {
        String username = sessionToUser.remove(sessionId);
        if (username != null) {
            Set<String> sessions = userToSessions.get(username);
            if (sessions != null) {
                sessions.remove(sessionId);
                if (sessions.isEmpty()) {
                    userToSessions.remove(username);
                }
            }
        }
        return username;
    }

    @Override
    public boolean isUserOnline(String username) {
        Set<String> sessions = userToSessions.get(username);
        return sessions != null && !sessions.isEmpty();
    }

    @Override
    public Set<String> getOnlineUsers() {
        return userToSessions.keySet();
    }

    @Override
    public int getOnlineUsersCount() {
        return userToSessions.size();
    }

    @Override
    public int getUserSessionCount(String username) {
        Set<String> sessions = userToSessions.get(username);
        return sessions != null ? sessions.size() : 0;
    }
}