package com.eilco.messagerie.service.Impl;

import com.eilco.messagerie.repositories.GroupRepository;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author akdim
 */

@Service
public class GroupService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroupRepository groupRepository;

    public void addUserToGroup(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Groupe introuvable"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Ajouter l'utilisateur à la liste des membres
        List<User> members = group.getMembers();
        if (!members.contains(user)) {
            members.add(user);
            group.setMembers(members);
        }

        // Sauvegarder le groupe pour persister le changement
        groupRepository.save(group);
    }
}
