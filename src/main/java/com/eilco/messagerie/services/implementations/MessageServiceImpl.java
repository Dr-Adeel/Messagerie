package com.eilco.messagerie.services.implementations;

import com.eilco.messagerie.exceptions.GroupNotFoundException;
import com.eilco.messagerie.exceptions.UserNotFoundException;
import com.eilco.messagerie.exceptions.UserNotMemberException;
import com.eilco.messagerie.mappers.MessageMapper;
import com.eilco.messagerie.models.request.MessageRequest;
import com.eilco.messagerie.models.request.NotificationRequest;
import com.eilco.messagerie.models.response.MessageResponse;
import com.eilco.messagerie.repositories.GroupRepository;
import com.eilco.messagerie.repositories.MessageRepository;
import com.eilco.messagerie.repositories.MessageStatusRepository;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.Message;
import com.eilco.messagerie.repositories.entities.MessageStatus;
import com.eilco.messagerie.repositories.entities.User;
import com.eilco.messagerie.repositories.entities.NotificationType;
import com.eilco.messagerie.services.interfaces.IMessageService;
import com.eilco.messagerie.services.interfaces.INotificationService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class MessageServiceImpl implements IMessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final MessageStatusRepository messageStatusRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageMapper messageMapper;
    private final INotificationService notificationService;

    @Override
    @Transactional
    public MessageResponse sendPrivateMessage(MessageRequest request, String senderUsername) {
        log.info("Sending private message from {} to user ID {}", senderUsername, request.getReceiverUserId());

        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new UserNotFoundException("Sender not found"));
        User receiver = userRepository.findById(request.getReceiverUserId())
                .orElseThrow(() -> new UserNotFoundException("Receiver not found"));

        // Création du message
        Message message = new Message();
        message.setSender(sender);
        message.setReceiverUser(receiver);
        message.setContent(request.getContent());

        Message savedMessage = messageRepository.save(message);

        // Création du MessageStatus pour le destinataire
        MessageStatus status = MessageStatus.builder()
                .message(savedMessage)
                .receiver(receiver)
                .isRead(false)
                .build();
        messageStatusRepository.save(status);

        // 🔔 Création automatique de la notification
        NotificationRequest notificationRequest = NotificationRequest.builder()
                .messageId(savedMessage.getId())
                .type(NotificationType.PRIVATE_MESSAGE)
                .senderId(sender.getId())
                .recipientId(receiver.getId())
                .status(false)
                .sentAt(LocalDateTime.now())
                .build();
        notificationService.createNotification(notificationRequest);

        // Conversion en réponse
        MessageResponse response = messageMapper.toResponse(savedMessage);

        // Push WebSocket
        messagingTemplate.convertAndSendToUser(receiver.getUsername(), "/queue/messages", response);

        return response;
    }

    @Override
    @Transactional
    public MessageResponse sendGroupMessage(MessageRequest request, String senderUsername) {
        log.info("Sending group message from {} to group ID {}", senderUsername, request.getReceiverGroupId());

        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new UserNotFoundException("Sender not found"));
        Group group = groupRepository.findById(request.getReceiverGroupId())
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));

        if (sender.getGroup() == null || !sender.getGroup().getId().equals(group.getId())) {
            log.error("User {} is not a member of group {}", senderUsername, group.getId());
            throw new UserNotMemberException("User is not a member of this group");
        }

        // Création du message
        Message message = new Message();
        message.setSender(sender);
        message.setReceiverGroup(group);
        message.setContent(request.getContent());

        Message savedMessage = messageRepository.save(message);

        // ✅ CORRECTION : Utiliser userRepository au lieu de group.getMembers()
        List<User> groupMembers = userRepository.findByGroupId(group.getId());
        log.info("Found {} members in group {}", groupMembers.size(), group.getId());

        // Création du MessageStatus et notification pour chaque membre sauf l'expéditeur
        groupMembers.stream()
                .filter(member -> !member.getId().equals(sender.getId()))
                .forEach(member -> {
                    MessageStatus status = MessageStatus.builder()
                            .message(savedMessage)
                            .receiver(member)
                            .isRead(false)
                            .build();
                    messageStatusRepository.save(status);

                    // 🔔 Création de notification pour chaque membre
                    NotificationRequest notificationRequest = NotificationRequest.builder()
                            .messageId(savedMessage.getId())
                            .type(NotificationType.GROUP_MESSAGE)
                            .senderId(sender.getId())
                            .recipientId(member.getId())
                            .groupId(group.getId())
                            .status(false)
                            .sentAt(LocalDateTime.now())
                            .build();
                    notificationService.createNotification(notificationRequest);
                });

        MessageResponse response = messageMapper.toResponse(savedMessage);

        // Push WebSocket pour le groupe
        messagingTemplate.convertAndSend("/topic/group/" + group.getId(), response);

        return response;
    }

    @Override
    public List<MessageResponse> getPrivateConversation(String username1, String username2) {
        User u1 = userRepository.findByUsername(username1)
                .orElseThrow(() -> new UserNotFoundException("User 1 not found"));
        User u2 = userRepository.findByUsername(username2)
                .orElseThrow(() -> new UserNotFoundException("User 2 not found"));
        return messageRepository.findConversation(u1.getId(), u2.getId()).stream()
                .map(messageMapper::toResponse)
                .toList();
    }

    @Override
    public List<MessageResponse> getGroupMessages(Long groupId) {
        return messageRepository.findByReceiverGroupIdOrderByTimestampAsc(groupId).stream()
                .map(messageMapper::toResponse)
                .toList();
    }

    @Override
    public void markAsRead(Long messageId) {
        MessageStatus status = messageStatusRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message Status not found"));
        status.setRead(true);
        messageStatusRepository.save(status);
    }

    @Override
    public long getUnreadCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return messageStatusRepository.countByReceiverIdAndIsReadFalse(user.getId());
    }
}
