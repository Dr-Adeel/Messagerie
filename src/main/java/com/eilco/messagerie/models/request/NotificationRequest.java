package com.eilco.messagerie.models.request;



import com.eilco.messagerie.repositories.entities.NotificationType;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    private Long messageId;
    private NotificationType type;
    private Long senderId;
    private Long recipientId;
    private Long groupId;
    private LocalDateTime sentAt;
    private Boolean status;

}