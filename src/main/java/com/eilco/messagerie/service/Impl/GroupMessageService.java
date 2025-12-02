package com.eilco.messagerie.service.Impl;

import com.eilco.messagerie.repositories.GroupRepository;
import com.eilco.messagerie.repositories.MessageRepository;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.Message;
import com.eilco.messagerie.repositories.entities.User;
import com.eilco.messagerie.service.IGroupMessageService;
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
    public Message sendMessageGroup(Long senderId, Long groupId, String content) {

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Groupe introuvable"));

        // Vérifier que l'utilisateur appartient bien au groupe
//        if (sender.getGroup() == null || !sender.getGroup().getId().equals(groupId)) {
//            throw new IllegalStateException("Vous n’êtes pas membre de ce groupe");
//        }

        Message message = new Message();
        message.setContent(content);
        message.setSender(sender);
        message.setReceiverGroup(group);
        message.setTimestamp(LocalDateTime.now());
        message.setReceiverUser(null); // car message de groupe

        return messageRepository.save(message);
    }

    /**
     * RÉCUPÉRER L'HISTORIQUE DES MESSAGES DU GROUPE
     */
    public List<Message> getGroupMessages(Long groupId, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Vérification d'appartenance au groupe
//        if (user.getGroup() == null || !user.getGroup().getId().equals(groupId)) {
//            throw new IllegalStateException("Accès refusé : Vous ne faites pas partie de ce groupe");
//        }

        return messageRepository.findByReceiverGroupIdOrderByTimestampAsc(groupId);
    }
}
