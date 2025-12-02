package com.eilco.messagerie.services.interfaces;

import java.util.Set;

public interface IUserSessionService {

    void addSession(String sessionId, String username);

    String removeSession(String sessionId);

    boolean isUserOnline(String username);

    Set<String> getOnlineUsers();

    int getOnlineUsersCount();

    int getUserSessionCount(String username);
}