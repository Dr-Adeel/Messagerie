package com.eilco.messagerie.service.interfaces;

import com.eilco.messagerie.models.request.GroupRequest;
import com.eilco.messagerie.repositories.entities.Group;

public interface IGroupService {
    // Method to create a group
    Group createGroup(GroupRequest groupRequest);

    // Method to delete a group by its ID
    void deleteGroup(Long groupId);
}
