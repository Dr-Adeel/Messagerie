package com.eilco.messagerie.entitty;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
/**
 * @author akdim
 */

public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

//
//    @ManyToOne
//    @JoinColumn(name = "sender_id", nullable = false)
//    private User sender;

//    @ManyToOne
//    @JoinColumn(name = "receiver_group_id")
//    private Group receiverGroup;

//    @ManyToOne
//    @JoinColumn(name = "receiver_user_id")
//    private User receiverUser; // null pour les messages de groupe

}

