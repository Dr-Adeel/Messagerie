package com.eilco.messagerie.services;


import com.eilco.messagerie.models.response.MessageResponse;
import com.eilco.messagerie.repositories.entities.Message;

import java.util.List;

public interface IGroupMessageService {
    MessageResponse sendMessageGroup(Long senderId, Long groupId, String content);

    List<MessageResponse> getGroupMessages(Long groupId, Long userId);
}