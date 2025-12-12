package com.eilco.messagerie.mappers;

import com.eilco.messagerie.models.response.MessageResponse;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.Message;
import com.eilco.messagerie.repositories.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MessageMapperTest {

    private MessageMapper messageMapper;

    @BeforeEach
    void setUp() {
        messageMapper = new MessageMapper();
    }

    @Test
    void toResponse_ShouldMapPrivateMessage() {
        // Arrange
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        User receiver = new User();
        receiver.setId(2L);
        receiver.setUsername("receiver");

        Message message = new Message();
        message.setId(500L);
        message.setContent("Hello World");
        message.setTimestamp(LocalDateTime.now());
        message.setSender(sender);
        message.setReceiverUser(receiver);

        // Act
        MessageResponse response = messageMapper.toResponse(message);

        // Assert
        assertNotNull(response);
        assertEquals(message.getId(), response.getId());
        assertEquals(message.getContent(), response.getContent());
        assertEquals(message.getTimestamp(), response.getTimestamp());
        assertEquals(sender.getId(), response.getSenderId());
        assertEquals(sender.getUsername(), response.getSenderUsername());
        assertEquals(receiver.getId(), response.getReceiverUserId());
        assertEquals(receiver.getUsername(), response.getReceiverUsername());
        assertEquals("DM", response.getMessageType());
    }

    @Test
    void toResponse_ShouldMapGroupMessage() {
        // Arrange
        User sender = new User();
        sender.setId(1L);
        sender.setUsername("sender");

        Group group = new Group();
        group.setId(99L);
        group.setName("Chat Room");

        Message message = new Message();
        message.setId(501L);
        message.setContent("Hello Group");
        message.setTimestamp(LocalDateTime.now());
        message.setSender(sender);
        message.setReceiverGroup(group);

        // Act
        MessageResponse response = messageMapper.toResponse(message);

        // Assert
        assertNotNull(response);
        assertEquals("GROUP", response.getMessageType());
        assertEquals(group.getId(), response.getReceiverGroupId());
        assertEquals(group.getName(), response.getReceiverGroupName());
        assertNull(response.getReceiverUserId());
    }

    @Test
    void toResponse_ShouldReturnNull_WhenMessageIsNull() {
        assertNull(messageMapper.toResponse(null));
    }
}
