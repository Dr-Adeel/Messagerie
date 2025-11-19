package com.eilco.messagerie.mappers;

import com.eilco.messagerie.models.response.MessageResponse;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.Message;
import com.eilco.messagerie.repositories.entities.User;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {


    public MessageResponse toResponse(Message message) {
        if (message == null) {
            return null;
        }

        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setContent(message.getContent());
        response.setTimestamp(message.getTimestamp());

        User sender = message.getSender();
        if (sender != null) {
            response.setSenderId(sender.getId());
            response.setSenderUsername(sender.getUsername());
        }

        if (message.getReceiverUser() != null) {
            // C'est un Message Direct (DM)
            User receiver = message.getReceiverUser();
            response.setReceiverUserId(receiver.getId());
            response.setReceiverUsername(receiver.getUsername());
            response.setMessageType("DM");
        } else if (message.getReceiverGroup() != null) {
            // C'est un Message de Groupe
            Group receiver = message.getReceiverGroup();
            response.setReceiverGroupId(receiver.getId());
            response.setReceiverGroupName(receiver.getName());
            response.setMessageType("GROUP");
        } else {
            response.setMessageType("INVALID");
        }

        return response;
    }



}
