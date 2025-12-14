package com.eilco.messagerie.mappers;

import com.eilco.messagerie.models.request.NotificationRequest;
import com.eilco.messagerie.models.response.NotificationResponse;
import com.eilco.messagerie.repositories.entities.Notification;
import com.eilco.messagerie.repositories.entities.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class NotificationMapperTest {

    private NotificationMapper notificationMapper;

    @BeforeEach
    void setUp() {
        notificationMapper = new NotificationMapper();
    }

    @Test
    void toEntity_ShouldMapNotificationRequest() {
        // Arrange
        NotificationRequest request = NotificationRequest.builder()
                .messageId(100L)
                .type(NotificationType.PRIVATE_MESSAGE)
                .senderId(1L)
                .recipientId(2L)
                .groupId(null)
                .status(false)
                .sentAt(LocalDateTime.now())
                .build();

        // Act
        Notification entity = notificationMapper.toEntity(request);

        // Assert
        assertNotNull(entity);
        assertEquals(request.getMessageId(), entity.getMessageId());
        assertEquals(request.getType(), entity.getType());
        assertEquals(request.getSenderId(), entity.getSenderId());
        assertEquals(request.getRecipientId(), entity.getRecipientId());
        assertEquals(request.getGroupId(), entity.getGroupId());
        assertEquals(request.getStatus(), entity.getStatus());
        assertEquals(request.getSentAt(), entity.getSentAt());
    }

    @Test
    void toResponse_ShouldMapNotificationEntity() {
        // Arrange
        Notification entity = Notification.builder()
                .id(200L)
                .messageId(101L)
                .type(NotificationType.GROUP_MESSAGE)
                .senderId(1L)
                .recipientId(3L)
                .groupId(10L)
                .status(false)
                .sentAt(LocalDateTime.now())
                .build();

        // Act
        NotificationResponse response = notificationMapper.toResponse(entity);

        // Assert
        assertNotNull(response);
        assertEquals(entity.getId(), response.getId());
        assertEquals(entity.getMessageId(), response.getMessageId());
        assertEquals(entity.getType(), response.getType());
        assertEquals(entity.getSenderId(), response.getSenderId());
        assertEquals(entity.getRecipientId(), response.getRecipientId());
        assertEquals(entity.getGroupId(), response.getGroupId());
        assertEquals(entity.getStatus(), response.getStatus());
        assertEquals(entity.getSentAt(), response.getSentAt());
    }

    @Test
    void toEntity_ShouldReturnNull_WhenRequestIsNull() {
        assertNull(notificationMapper.toEntity(null));
    }

    @Test
    void toResponse_ShouldReturnNull_WhenEntityIsNull() {
        assertNull(notificationMapper.toResponse(null));
    }
}
