package com.eilco.messagerie.mappers;
import com.eilco.messagerie.models.request.NotificationRequest;
import com.eilco.messagerie.models.response.NotificationResponse;
import com.eilco.messagerie.repositories.entities.Notification;
import org.springframework.stereotype.Component;

@Component
public class NotificationMapper {

    /**
     * Convert NotificationRequest DTO to Notification entity
     */
    public Notification toEntity(NotificationRequest request) {
        if (request == null) {
            return null;
        }

        return Notification.builder()
                .messageId(request.getMessageId())
                .type(request.getType())
                .senderId(request.getSenderId())
                .recipientId(request.getRecipientId())
                .groupId(request.getGroupId())
                .sentAt(request.getSentAt())
                .status(request.getStatus())
                .build();
    }

    /**
     * Convert Notification entity to NotificationResponse DTO
     */
    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }

        return NotificationResponse.builder()
                .id(notification.getId())
                .messageId(notification.getMessageId())
                .type(notification.getType())
                .senderId(notification.getSenderId())
                .recipientId(notification.getRecipientId())
                .groupId(notification.getGroupId())
                .sentAt(notification.getSentAt())
                .status(notification.getStatus())
                .build();
}
}

