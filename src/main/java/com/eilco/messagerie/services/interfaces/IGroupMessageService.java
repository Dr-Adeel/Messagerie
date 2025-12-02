package com.eilco.messagerie.services.interfaces;


import com.eilco.messagerie.repositories.entities.Message;

import java.util.List;

public interface IGroupMessageService {
    Message sendMessageGroup(Long senderId, Long groupId, String content);

    List<Message> getGroupMessages(Long groupId, Long userId);
}