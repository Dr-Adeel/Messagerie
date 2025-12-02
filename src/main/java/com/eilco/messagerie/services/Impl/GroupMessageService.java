package com.eilco.messagerie.services.Impl;

import com.eilco.messagerie.mappers.MessageMapper;
import com.eilco.messagerie.models.response.MessageResponse;
import com.eilco.messagerie.repositories.GroupRepository;
import com.eilco.messagerie.repositories.MessageRepository;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.Message;
import com.eilco.messagerie.repositories.entities.User;
import com.eilco.messagerie.services.IGroupMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class GroupMessageService implements IGroupMessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * ENVOYER UN MESSAGE DANS UN GROUPE
     */
    @Override
    public MessageResponse sendMessageGroup(Long senderId, Long groupId, String content) {

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Groupe introuvable"));

        // Vérifier que l'utilisateur appartient bien au groupe
        if (sender.getGroup() == null || !sender.getGroup().getId().equals(groupId)) {
            throw new IllegalStateException("Vous n’êtes pas membre de ce groupe");
        }

        // Créer et sauvegarder le message
        Message message = new Message();
        message.setContent(content);
        message.setSender(sender);
        message.setReceiverGroup(group);
        message.setReceiverUser(null); // car message de groupe
        message.setTimestamp(LocalDateTime.now());

        return MessageMapper.toResponse(messageRepository.save(message));
    }

    /**
     * RÉCUPÉRER L'HISTORIQUE DES MESSAGES DU GROUPE
     */
    @Override
    public List<MessageResponse> getGroupMessages(Long groupId, Long userId) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Groupe introuvable"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Vérifier que l'utilisateur appartient bien au groupe
        if (user.getGroup() == null || !user.getGroup().getId().equals(groupId)) {
            throw new IllegalStateException("Accès refusé : Vous ne faites pas partie de ce groupe");
        }

        // Retourner les messages du groupe triés par date croissante
        return MessageMapper.toResponse(messageRepository.findByReceiverGroupIdOrderByTimestampAsc(groupId));

    }
}
