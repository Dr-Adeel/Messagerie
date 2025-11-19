package com.eilco.messagerie.models.response;

import com.eilco.messagerie.repositories.entities.NotificationType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id; // ID de la notification

    @NotNull(message = "Message ID is required")
    private Long messageId;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    @NotNull(message = "Sender ID is required")
    private Long senderId;

    @NotNull(message = "Recipient ID is required")
    private Long recipientId;

    private Long groupId;

    private LocalDateTime sentAt;

    private Boolean status;

}
