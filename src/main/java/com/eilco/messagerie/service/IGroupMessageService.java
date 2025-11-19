package com.eilco.messagerie.service;

import com.eilco.messagerie.entitty.Message;

import java.util.List;

public interface IGroupMessageService {
    public Message sendMessage(Long senderId, Long groupId, String content);

    public List<Message> getGroupMessages(Long groupId, Long userId);
}