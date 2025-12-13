package com.eilco.messagerie.models.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class GroupRequest {

    @NotBlank(message = "Le nom du groupe est requis.")
    @Size(min = 3, max = 100, message = "Le nom doit contenir entre 3 et 100 caractères.")
    private String name;

    @NotNull(message = "L'ID du créateur est requis.")
    private Long creatorId;

}