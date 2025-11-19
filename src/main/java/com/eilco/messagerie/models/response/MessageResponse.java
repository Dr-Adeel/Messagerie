package com.eilco.messagerie.models.response;

import com.eilco.messagerie.repositories.entities.Group;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {

    private Long id;
    private String content;
    private LocalDateTime timestamp;

    private Long senderId;
    private String senderUsername;

    private Long receiverUserId;
    private String receiverUsername;

    private Long receiverGroupId;
    private String receiverGroupName;

    private String messageType;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationResponse {

        private Long id; // ID de la notification

        @NotNull(message = "Message ID is required")
        private Long messageId;

        @NotNull(message = "Notification type is required")
        private Group.NotificationType type;

        @NotNull(message = "Sender ID is required")
        private Long senderId;

        @NotNull(message = "Recipient ID is required")
        private Long recipientId;

        private Long groupId;

        private LocalDateTime sentAt;

        private Boolean status;

    }
}