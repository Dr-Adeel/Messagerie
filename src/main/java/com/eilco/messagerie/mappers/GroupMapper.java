package com.eilco.messagerie.mappers;


import com.eilco.messagerie.models.request.GroupRequest;
import com.eilco.messagerie.models.response.GroupResponse;
import com.eilco.messagerie.models.response.UserResponse;
import com.eilco.messagerie.repositories.entities.Group;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GroupMapper {

    private final UserMapper userMapper;


    public GroupMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }


    public GroupResponse toResponse(Group group) {
        if (group == null) {
            return null;
        }

        GroupResponse response = new GroupResponse();
        response.setId(group.getId());
        response.setName(group.getName());

        if (group.getCreator() != null) {
            response.setCreatorId(group.getCreator().getId());
            response.setCreatorUsername(group.getCreator().getUsername());
        }


        return response;
    }


    public Group toEntity(GroupRequest request) {
        if (request == null) {
            return null;
        }

        Group group = new Group();
        group.setName(request.getName());

        return group;
    }

}



