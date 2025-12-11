package com.eilco.messagerie.mappers;

import com.eilco.messagerie.models.request.UserRequest;
import com.eilco.messagerie.models.response.UserResponse;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapper();
    }

    @Test
    void toResponse_ShouldMapUserToUserResponse() {
        // Arrange
        Group group = new Group();
        group.setId(10L);
        group.setName("Test Group");

        User user = new User();
        user.setId(1L);
        user.setUsername("john_doe");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setGroup(group);

        // Act
        UserResponse response = userMapper.toResponse(user);

        // Assert
        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getUsername());
        assertEquals(user.getFirstName(), response.getFirstName());
        assertEquals(user.getLastName(), response.getLastName());
        assertEquals(group.getId(), response.getGroupId());
        assertEquals(group.getName(), response.getGroupName());
    }

    @Test
    void toResponse_ShouldReturnNull_WhenUserIsNull() {
        assertNull(userMapper.toResponse(null));
    }

    @Test
    void toEntity_ShouldMapUserRequestToUser() {
        // Arrange
        UserRequest request = UserRequest.builder()
                .username("jane_doe")
                .firstName("Jane")
                .lastName("Doe")
                .password("secret")
                .build();

        // Act
        User user = userMapper.toEntity(request);

        // Assert
        assertNotNull(user);
        assertEquals(request.getUsername(), user.getUsername());
        assertEquals(request.getFirstName(), user.getFirstName());
        assertEquals(request.getLastName(), user.getLastName());
        // Password is explicitly NOT mapped in mapper
        assertNull(user.getPassword()); 
    }

    @Test
    void toEntity_ShouldReturnNull_WhenRequestIsNull() {
        assertNull(userMapper.toEntity(null));
    }
}
