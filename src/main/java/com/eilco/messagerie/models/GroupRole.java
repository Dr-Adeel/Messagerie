package com.eilco.messagerie.models;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.Set;
import java.util.HashSet;

@Entity
@Table(name = "app_group")
@Data
@NoArgsConstructor
@AllArgsConstructor

public enum GroupRole {
    CREATOR,
    MEMBER
}
