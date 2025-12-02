package com.eilco.messagerie.services.interfaces;

import com.eilco.messagerie.models.request.MessageRequest;

import java.util.Map;

public interface IChatService {

    Map<String, Object> sendGroupMessage(MessageRequest request);

    void sendDirectMessage(MessageRequest request);

    void registerUser(String username, String sessionId);

    void sendErrorToUser(String sessionId, String errorMessage);
}
