package com.eilco.messagerie.models.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageRequest {

    @NotBlank(message = "Le contenu du message ne peut pas être vide.")
    private String content;

    @NotNull(message = "L'ID de l'expéditeur est requis.")
    private Long senderId;

    private Long receiverUserId;

    private Long receiverGroupId;
}
