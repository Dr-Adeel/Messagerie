package com.eilco.messagerie.service.implementations;


import com.eilco.messagerie.repositories.GroupRepository;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.User;
import java.util.List;

public class GroupService {
    private GroupRepository groupRepository;
    private UserRepository userRepository;
    public void deleteGroup(Long groupId){
    // Fetch the group
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new IllegalArgumentException("Group not found"));


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


