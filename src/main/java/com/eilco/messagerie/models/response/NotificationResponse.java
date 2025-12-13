package com.eilco.messagerie.models.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationResponse {
    private String type; // "MESSAGE", "STATUS", "COUNT"
    private String content;
    private Long count;
    private String sender;
}
