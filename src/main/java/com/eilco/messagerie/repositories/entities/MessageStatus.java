package com.eilco.messagerie.repositories.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "message_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "message_id", nullable = false)
    private Message message;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User receiver;

    @Column(nullable = false)
    private boolean isRead;
}
