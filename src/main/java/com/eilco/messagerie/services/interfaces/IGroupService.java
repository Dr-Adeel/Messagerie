package com.eilco.messagerie.services.interfaces;

import com.eilco.messagerie.models.request.GroupRequest;
import com.eilco.messagerie.models.response.GroupResponse;
import com.eilco.messagerie.repositories.entities.Group;

public interface IGroupService {
    // Method to create a group
    GroupResponse createGroup(GroupRequest groupRequest);

    void addMember(Long groupId, Long userId, Long requesterId);

    void removeMember(Long groupId, Long userId, Long requesterId);
    // Method to delete a group by its ID
    void deleteGroup(Long groupId);
}
