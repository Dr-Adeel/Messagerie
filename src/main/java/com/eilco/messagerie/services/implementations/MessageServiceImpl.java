package com.eilco.messagerie.services.implementations;

import com.eilco.messagerie.exceptions.GroupNotFoundException;
import com.eilco.messagerie.exceptions.UserNotFoundException;
import com.eilco.messagerie.exceptions.UserNotMemberException;
import com.eilco.messagerie.mappers.MessageMapper;
import com.eilco.messagerie.models.request.MessageRequest;
import com.eilco.messagerie.models.response.MessageResponse;
import com.eilco.messagerie.repositories.GroupRepository;
import com.eilco.messagerie.repositories.MessageRepository;
import com.eilco.messagerie.repositories.MessageStatusRepository;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.Message;
import com.eilco.messagerie.repositories.entities.MessageStatus;
import com.eilco.messagerie.repositories.entities.User;
import com.eilco.messagerie.services.interfaces.IMessageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public MessageResponse sendPrivateMessage(MessageRequest request, String senderUsername) {
        log.info("Sending private message from {} to user ID {}", senderUsername, request.getReceiverUserId());
        User sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new UserNotFoundException("Sender not found"));
        User receiver = userRepository.findById(request.getReceiverUserId())
                .orElseThrow(() -> new UserNotFoundException("Receiver not found"));

        Message message = new Message();
        message.setSender(sender);
        message.setReceiverUser(receiver);
        message.setContent(request.getContent());

        Message savedMessage = messageRepository.save(message);

        // Create MessageStatus for receiver
        MessageStatus status = MessageStatus.builder()
                .message(savedMessage)
                .receiver(receiver)
                .isRead(false)
                .build();
        messageStatusRepository.save(status);

        MessageResponse response = messageMapper.toResponse(savedMessage);
        
        // Push to WebSocket
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

        Message message = new Message();
        message.setSender(sender);
        message.setReceiverGroup(group);
        message.setContent(request.getContent());

        Message savedMessage = messageRepository.save(message);

        // Create MessageStatus for all other group members
        if (group.getMembers() != null) {
            group.getMembers().stream()
                .filter(member -> !member.getId().equals(sender.getId()))
                .forEach(member -> {
                    MessageStatus status = MessageStatus.builder()
                        .message(savedMessage)
                        .receiver(member)
                        .isRead(false)
                        .build();
                    messageStatusRepository.save(status);
                });
        }

        MessageResponse response = messageMapper.toResponse(savedMessage);
        
        // Push to WebSocket
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
