package com.eilco.messagerie.testMappers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.eilco.messagerie.mappers.UserMapper;
import com.eilco.messagerie.models.request.UserRequest;
import com.eilco.messagerie.models.response.UserResponse;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.User;

class TestUserMapper {

    private final UserMapper userMapper = new UserMapper();

    @Test
    void toResponseReturnsNullWhenUserIsNull() {
        assertNull(userMapper.toResponse(null));
    }

    @Test
    void toResponseMapsEntityToDtoIncludingGroupData() {
        Group group = new Group();
        group.setId(42L);
        group.setName("Admins");

        User user = new User();
        user.setId(7L);
        user.setUsername("john.doe");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setGroup(group);

        UserResponse response = userMapper.toResponse(user);

        assertNotNull(response);
        assertEquals(7L, response.getId());
        assertEquals("john.doe", response.getUsername());
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals(42L, response.getGroupId());
        assertEquals("Admins", response.getGroupName());
    }

    @Test
    void toEntityReturnsNullWhenRequestIsNull() {
        assertNull(userMapper.toEntity(null));
    }

    @Test
    void toEntityMapsRequestToEntityWithoutSensitiveFields() {
        UserRequest request = new UserRequest();
        request.setUsername("jane.doe");
        request.setPassword("secretPassword!");
        request.setFirstName("Jane");
        request.setLastName("Doe");
        request.setGroupId(11L);

        User entity = userMapper.toEntity(request);

        assertNotNull(entity);
        assertEquals("jane.doe", entity.getUsername());
        assertEquals("Jane", entity.getFirstName());
        assertEquals("Doe", entity.getLastName());
        assertNull(entity.getPassword());
        assertNull(entity.getGroup());
    }
}
