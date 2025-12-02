package com.eilco.messagerie.services.implementations;

import java.util.List;

import org.springframework.stereotype.Service;

import com.eilco.messagerie.mappers.GroupMapper;
import com.eilco.messagerie.models.request.GroupRequest;
import com.eilco.messagerie.models.response.GroupResponse;
import com.eilco.messagerie.repositories.GroupRepository;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.User;
import com.eilco.messagerie.services.interfaces.IGroupService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GroupService implements IGroupService {

    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMapper groupMapper;

    @Override
    public GroupResponse createGroup(GroupRequest groupRequest) {
        Long creatorId = groupRequest.getCreatorId();

        User dbCreator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Creator user not found"));

        if (dbCreator.getGroup() != null) {
            throw new IllegalStateException("User is already assigned to a group");
        }

        Group groupToSave = groupMapper.toEntity(groupRequest);
        groupToSave.setCreator(dbCreator);

        Group savedGroup = groupRepository.save(groupToSave);
        dbCreator.setGroup(savedGroup);
        userRepository.save(dbCreator);

        return groupMapper.toResponse(savedGroup);
    }


    @Override
    public void deleteGroup(Long groupId){
        // Fetch the group
        Group group = getById(groupId);


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
    public void addMember(Long groupId, Long userId, Long requesterId) {

        // Récupération des entités
        Group group = getById(groupId);

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

        Group group = getById(groupId);

        User requester = userRepository.findById(requesterId)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        User member = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 1. Vérifier que le requester est le créateur du groupe
        if (!isAdminOfGroup(requester, group)) {
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

    @Override
    public Group getById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
    }

    @Override
    public boolean isAdminOfGroup(User user, Group group) {
        if (user == null || group == null || group.getCreator() == null) {
            return false;
        }
        return group.getCreator().getId().equals(user.getId());
    }
}
