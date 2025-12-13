package com.eilco.messagerie.services.interfaces;

import com.eilco.messagerie.models.request.GroupRequest;
import com.eilco.messagerie.models.response.GroupResponse;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.User;

public interface IGroupService {
    Group getById(Long id);

    GroupResponse createGroup(GroupRequest groupRequest, String creatorUsername);

    void addMember(Long groupId, String username);

    void removeMember(Long groupId, String username);

    GroupResponse getUserGroup(String username);

    void deleteGroup(Long groupId);

    public boolean isAdminOfGroup(User user, Group group);
}
