package com.eilco.messagerie.services.interfaces;

public interface IGroupService {

    void addMember(Long groupId, Long userId, Long requesterId);

    void removeMember(Long groupId, Long userId, Long requesterId);
}
