package com.eilco.messagerie.websocket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusNotification {
    private String username;
    private boolean online;
    private LocalDateTime timestamp;
    private String type; // "CONNECTED" ou "DISCONNECTED"

    public UserStatusNotification(String username, boolean online) {
        this.username = username;
        this.online = online;
        this.timestamp = LocalDateTime.now();
        this.type = online ? "CONNECTED" : "DISCONNECTED";
    }
}