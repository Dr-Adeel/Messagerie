package com.eilco.messagerie.mappers;


import org.springframework.stereotype.Component;

import com.eilco.messagerie.models.request.UserRequest;
import com.eilco.messagerie.models.response.UserResponse;
import com.eilco.messagerie.repositories.entities.User;

@Component
public class UserMapper {

    /**
     * Convertit l'Entité User en DTO de Réponse.
     * @param user L'entité User à mapper.
     * @return UserResponse DTO.
     */
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }

        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());

        // Mapping de la relation de groupe
        if (user.getGroup() != null) {
            response.setGroupId(user.getGroup().getId());
            response.setGroupName(user.getGroup().getName());
        }

        return response;
        
    }


    public User toEntity(UserRequest request) {
        if (request == null) {
            return null;
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        // Ne pas mapper le mot de passe ici pour des raisons de sécurité.
        // Il sera setté et haché dans le service.
        // user.setPassword(request.getPassword());

        // Le champ 'group' (Entité) doit être géré dans le Service après avoir
        // récupéré l'objet Group par son ID (request.getGroupId()).

        return user;
    }
}