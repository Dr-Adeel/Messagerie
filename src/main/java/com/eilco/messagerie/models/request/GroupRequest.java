package com.eilco.messagerie.models.request;

import com.eilco.messagerie.repositories.entities.Group;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
public class GroupRequest {

    @NotBlank(message = "Le nom du groupe est requis.")
    @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères.")
    private String name;

    @NotNull(message = "L'ID du créateur est requis.")
    private Long creatorId;

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class NotificationRequest {
        private Long messageId;
        private Group.NotificationType type;
        private Long senderId;
        private Long recipientId;
        private Long groupId;
        private LocalDateTime sentAt;
        private Boolean status;

    }
}