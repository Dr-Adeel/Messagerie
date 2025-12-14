package com.eilco.messagerie.services.implementations;

import com.eilco.messagerie.exceptions.GroupNotFoundException;
import com.eilco.messagerie.exceptions.UserAlreadyInGroupException;
import com.eilco.messagerie.exceptions.UserNotFoundException;
import com.eilco.messagerie.exceptions.UserNotMemberException;
import com.eilco.messagerie.mappers.GroupMapper;
import com.eilco.messagerie.models.request.GroupRequest;
import com.eilco.messagerie.models.response.GroupResponse;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.User;
import com.eilco.messagerie.repositories.GroupRepository;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.services.interfaces.IGroupService;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class GroupServiceImpl implements IGroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final GroupMapper groupMapper;

    @Override
    public Group getById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found with ID: " + id));
    }

    @Override
    @Transactional
    public GroupResponse createGroup(GroupRequest groupRequest, String creatorUsername) {
        User creator = userRepository.findByUsername(creatorUsername)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (creator.getGroup() != null) {
            throw new UserAlreadyInGroupException("User is already in a group");
        }

        Group group = new Group();
        group.setName(groupRequest.getName());
        group.setCreator(creator);

        group = groupRepository.save(group);

        creator.setGroup(group);
        userRepository.save(creator);

        return groupMapper.toResponse(group);
    }

    @Override
    @Transactional
    public void addMember(Long groupId, String username) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException("Group not found"));
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getGroup() != null) {
            throw new UserAlreadyInGroupException("User is already in a group");
        }

        user.setGroup(group);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void removeMember(Long groupId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getGroup() == null || !user.getGroup().getId().equals(groupId)) {
            throw new UserNotMemberException("User is not in this group");
        }

        user.setGroup(null);
        userRepository.save(user);
    }

    @Override
    public GroupResponse getUserGroup(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getGroup() == null) {
            return null;
        }

        return groupMapper.toResponse(user.getGroup());
    }

    @Override
    public void deleteGroup(Long groupId) {
        Group group = getById(groupId);
         String username = SecurityContextHolder.getContext().getAuthentication().getName();
         if (!group.getCreator().getUsername().equals(username)) return;
        List<User> usersInGroup = userRepository.findAllByGroup(group);
        for (User user : usersInGroup) {
            user.setGroup(null);
        }
        userRepository.saveAll(usersInGroup); // Update all users in DB

        // Delete the group
        groupRepository.delete(group);
    }

    @Override
    public boolean isAdminOfGroup(User user, Group group) {
        if (user == null || group == null || group.getCreator() == null) {
            return false;
        }
        return group.getCreator().getId().equals(user.getId());
    }
}
