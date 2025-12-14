package com.eilco.messagerie.mappers;

import com.eilco.messagerie.models.request.GroupRequest;
import com.eilco.messagerie.models.response.GroupResponse;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class GroupMapperTest {

    private GroupMapper groupMapper;

    @Mock
    private UserMapper userMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        groupMapper = new GroupMapper(userMapper);
    }

    @Test
    void toResponse_ShouldMapGroupToGroupResponse() {
        // Arrange
        User creator = new User();
        creator.setId(5L);
        creator.setUsername("creatorBot");

        Group group = new Group();
        group.setId(100L);
        group.setName("Developers");
        group.setCreator(creator);

        // Act
        GroupResponse response = groupMapper.toResponse(group);

        // Assert
        assertNotNull(response);
        assertEquals(group.getId(), response.getId());
        assertEquals(group.getName(), response.getName());
        assertEquals(creator.getId(), response.getCreatorId());
        assertEquals(creator.getUsername(), response.getCreatorUsername());
    }

    @Test
    void toResponse_ShouldReturnNull_WhenGroupIsNull() {
        assertNull(groupMapper.toResponse(null));
    }

    @Test
    void toEntity_ShouldMapGroupRequestToGroup() {
        // Arrange
        GroupRequest request = new GroupRequest();
        request.setName("New Group");

        // Act
        Group group = groupMapper.toEntity(request);

        // Assert
        assertNotNull(group);
        assertEquals(request.getName(), group.getName());
    }

    @Test
    void toEntity_ShouldReturnNull_WhenRequestIsNull() {
        assertNull(groupMapper.toEntity(null));
    }
}
