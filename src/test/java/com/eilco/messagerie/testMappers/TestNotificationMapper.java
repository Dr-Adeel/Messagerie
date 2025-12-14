package com.eilco.messagerie.testMappers;


import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eilco.messagerie.mappers.NotificationMapper;
import com.eilco.messagerie.models.request.NotificationRequest;
import com.eilco.messagerie.models.response.NotificationResponse;
import com.eilco.messagerie.repositories.entities.Notification;
import com.eilco.messagerie.repositories.entities.NotificationType;

public class TestNotificationMapper {

    private NotificationMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new NotificationMapper();
    }

    @Test
    void testToEntityNullRequestReturnsNull() {
        Assertions.assertNull(mapper.toEntity(null));
    }

    @Test
    void testToEntityMapping() {
        NotificationRequest request = NotificationRequest.builder()
                .messageId(100L)
                .type(NotificationType.PRIVATE_MESSAGE)
                .senderId(1L)
                .recipientId(2L)
                .groupId(null)
                .status(false)
                .sentAt(LocalDateTime.now())
                .build();

        Notification entity = mapper.toEntity(request);

        Assertions.assertNotNull(entity);
        Assertions.assertEquals(Long.valueOf(100L), entity.getMessageId());
        Assertions.assertEquals(NotificationType.PRIVATE_MESSAGE, entity.getType());
        Assertions.assertEquals(Long.valueOf(1L), entity.getSenderId());
        Assertions.assertEquals(Long.valueOf(2L), entity.getRecipientId());
        Assertions.assertNull(entity.getGroupId());
        Assertions.assertFalse(entity.getStatus());
        Assertions.assertNotNull(entity.getSentAt());
    }

    @Test
    void testToResponseMapping() {
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

        NotificationResponse response = mapper.toResponse(entity);

        Assertions.assertNotNull(response);
        Assertions.assertEquals(Long.valueOf(200L), response.getId());
        Assertions.assertEquals(Long.valueOf(101L), response.getMessageId());
        Assertions.assertEquals(NotificationType.GROUP_MESSAGE, response.getType());
        Assertions.assertEquals(Long.valueOf(1L), response.getSenderId());
        Assertions.assertEquals(Long.valueOf(3L), response.getRecipientId());
        Assertions.assertEquals(Long.valueOf(10L), response.getGroupId());
        Assertions.assertFalse(response.getStatus());
        Assertions.assertNotNull(response.getSentAt());
    }

    @Test
    void testToResponseNullEntityReturnsNull() {
        Assertions.assertNull(mapper.toResponse(null));
    }
}
