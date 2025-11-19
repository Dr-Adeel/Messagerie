package com.eilco.messagerie.services.implementations;


import com.eilco.messagerie.mappers.GroupMapper;
import com.eilco.messagerie.models.request.GroupRequest;
import com.eilco.messagerie.models.response.GroupResponse;
import com.eilco.messagerie.repositories.GroupRepository;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.User;

import java.util.List;

public class GroupService {
    private UserRepository userRepository;
    private GroupRepository groupRepository;
    private GroupMapper groupMapper;


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

}


