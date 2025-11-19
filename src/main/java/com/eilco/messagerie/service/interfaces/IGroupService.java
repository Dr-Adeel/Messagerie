package com.eilco.messagerie.service.interfaces;

import com.eilco.messagerie.repositories.entities.Group;

public interface IGroupService {
    // Method to create a group
    Group createGroup(Group group);

    // Method to delete a group by its ID
    void deleteGroup(Long groupId);
}
