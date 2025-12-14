package com.eilco.messagerie.repositories.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;


@Entity
@Table(name = "app_group")
@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_user_id")
    private User creator;



    public enum NotificationType {
        PRIVATE_MESSAGE,
        GROUP_MESSAGE,
        MENTION,
        GROUP_INVITE
    }

    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private List<User> members;

}
