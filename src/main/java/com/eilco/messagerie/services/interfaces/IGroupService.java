package com.eilco.messagerie.services.interfaces;

import com.eilco.messagerie.models.request.GroupRequest;
import com.eilco.messagerie.models.response.GroupResponse;

public interface IGroupService {
    GroupResponse createGroup(GroupRequest groupRequest, String creatorUsername);

    void addMember(Long groupId, String username);

    void removeMember(Long groupId, String username);

    GroupResponse getUserGroup(String username);
}
