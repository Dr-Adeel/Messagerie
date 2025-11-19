package com.eilco.messagerie.services.implementations;

import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.User;
import com.eilco.messagerie.repositories.GroupRepository;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.services.interfaces.IGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GroupService implements IGroupService {

    @Autowired
    private GroupRepository groupRepo;

    @Autowired
    private UserRepository userRepo;

    @Override
    public void addMember(Long groupId, Long userId, Long requesterId) {

        // Récupération des entités
        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User requester = userRepo.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        User newMember = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1️ Vérifier que le requester est bien le créateur du groupe
        if (!group.getCreator().getId().equals(requester.getId())) {
            throw new RuntimeException("Not authorized: only creator can add members");
        }

        // 2️ Vérifier que l'utilisateur n'est pas déjà dans un autre groupe
        if (newMember.getGroup() != null) {
            throw new RuntimeException("User already belongs to another group");
        }

        // 3️ Ajouter l'utilisateur au groupe
        newMember.setGroup(group);

        userRepo.save(newMember);
    }
    @Override
    public void removeMember(Long groupId, Long userId, Long requesterId) {

        Group group = groupRepo.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User requester = userRepo.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        User member = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Vérifier que le requester est le créateur du groupe
        if (!group.getCreator().getId().equals(requester.getId())) {
            throw new RuntimeException("Not authorized");
        }

        // 2. Vérifier que l'utilisateur appartient bien au groupe
        if (member.getGroup() == null || !member.getGroup().getId().equals(groupId)) {
            throw new RuntimeException("This user is not in the group");
        }

        // 3. Empêcher de supprimer le créateur lui-même
        if (member.getId().equals(group.getCreator().getId())) {
            throw new RuntimeException("Group creator cannot be removed");
        }

        // 4. Retirer l’utilisateur du groupe
        member.setGroup(null);

        userRepo.save(member);  // persister la suppression du lien
    }

}
