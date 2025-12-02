package com.eilco.messagerie.services.implementations;

import com.eilco.messagerie.mappers.GroupMapper;
import com.eilco.messagerie.models.request.GroupRequest;
import com.eilco.messagerie.models.response.GroupResponse;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.User;
import com.eilco.messagerie.repositories.GroupRepository;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.services.interfaces.IGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GroupService implements IGroupService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private GroupRepository groupRepository;

    @Override
    public GroupResponse createGroup(GroupRequest groupRequest) {
        Long creatorId = groupRequest.getCreatorId();

        User dbCreator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Creator user not found"));

        if (dbCreator.getGroup() != null) {
            throw new IllegalStateException("User is already assigned to a group");
        }

        GroupResponse savedGroup = groupMapper.toResponse(
                groupRepository.save(
                        groupMapper.toEntity(groupRequest)
                )
        );

        // Assign the new group to the creator
        dbCreator.setGroup(groupMapper.toEntity(groupRequest));
        userRepository.save(dbCreator);

        return savedGroup;
    }


    public void deleteGroup(Long groupId){
        // Fetch the group
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));


        // Find all users assigned to this group and set their group field to null
        List<User> usersInGroup = userRepository.findAllByGroup(group);
        for (User user : usersInGroup) {
            user.setGroup(null);
        }
        userRepository.saveAll(usersInGroup); // Update all users in DB

        // Delete the group
        groupRepository.delete(group);
    }

    @Override
    public Group getById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found with ID: " + id));
    }



    @Override
    public boolean isAdminOfGroup(User user, Group group) {
        if (user == null || group == null) {
            return false;
        }
        // Ajoutez votre logique d'autorisation ici. Exemple :
        return group.getCreator() != null && group.getCreator().getId().equals(user.getId());
    }

    @Override
    public Group getById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found with ID: " + id));
    }
    @Override
    public void addMember(Long groupId, Long userId, Long requesterId) {

        // Récupération des entités
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        User newMember = userRepository.findById(userId)
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

        userRepository.save(newMember);
    }
    @Override
    public void removeMember(Long groupId, Long userId, Long requesterId) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        User member = userRepository.findById(userId)
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

        userRepository.save(member);  // persister la suppression du lien
    }

}
