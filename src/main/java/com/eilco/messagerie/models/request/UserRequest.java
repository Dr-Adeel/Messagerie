package com.eilco.messagerie.models.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank(message = "Le nom d'utilisateur est requis.")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères.")
    private String username;

    // Pour l'enregistrement, on a besoin du mot de passe en clair (sera haché par le service)
    @NotBlank(message = "Le mot de passe est requis.")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères.")
    private String password;

    @NotBlank(message = "Le prénom est requis.")
    private String firstName;

    @NotBlank(message = "Le nom de famille est requis.")
    private String lastName;

    // L'ID du groupe si l'utilisateur est ajouté à un groupe lors de sa création.
    // Peut être null.
    private Long groupId;
}