package com.eilco.messagerie.services.interfaces;

import com.eilco.messagerie.models.request.MessageRequest;
import com.eilco.messagerie.models.response.MessageResponse;

import java.util.List;

public interface IMessageService {
    MessageResponse sendPrivateMessage(MessageRequest request, String senderUsername);

    MessageResponse sendGroupMessage(MessageRequest request, String senderUsername);

    List<MessageResponse> getPrivateConversation(String username1, String username2);

    List<MessageResponse> getGroupMessages(Long groupId);

    void markAsRead(Long messageId);

    long getUnreadCount(String username);
}
