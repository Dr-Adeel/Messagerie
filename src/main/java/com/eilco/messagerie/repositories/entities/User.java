package com.eilco.messagerie.repositories.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.Objects;

@Entity
@Table(name = "app_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // Doit être le mot de passe HACHÉ (utilisez Spring Security)

    private String firstName;
    private String lastName;

    // Relation ManyToOne: Un utilisateur appartient à UN SEUL groupe.
    // Cette colonne 'group_id' sera créée dans la table 'app_user'.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

}


