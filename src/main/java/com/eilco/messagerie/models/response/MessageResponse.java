package com.eilco.messagerie.models.response;

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
}