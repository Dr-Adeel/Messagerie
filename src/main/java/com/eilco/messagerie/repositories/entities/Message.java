package com.eilco.messagerie.repositories.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "message")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_user_id")
    private User receiverUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_group_id")
    private Group receiverGroup;

    @Entity
    @Table(name = "notifications")
    public static class Notification {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @Column(name = "message_id", nullable = false)
        private Long messageId;

        @Enumerated(EnumType.STRING)
        @Column(name = "type", nullable = false)
        private Group.NotificationType type;

        @Column(name = "sender_id", nullable = false)
        private Long senderId;

        @Column(name = "recipient_id", nullable = false)
        private Long recipientId;


        @Column(name = "grpid")
        private Long groupId;

        @Column(name = "sentAt ")
        private LocalDateTime sentAt ;

        @Column(name = "status", length = 20)
        private Boolean status;



    }
}