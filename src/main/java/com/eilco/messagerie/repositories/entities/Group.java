package com.eilco.messagerie.repositories.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "app_group")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

//    @JsonIgnoreProperties({"group"})
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "creator_user_id", nullable = false)
//    private User creator;

//    @OneToMany(fetch = FetchType.LAZY)
//    private List<User> members;
}
