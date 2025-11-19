
package com.eilco.messagerie.testMappers;

import com.eilco.messagerie.mappers.GroupMapper;
import com.eilco.messagerie.mappers.UserMapper;
import com.eilco.messagerie.models.request.GroupRequest;
import com.eilco.messagerie.models.response.GroupResponse;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TestGroupMapper {
	private GroupMapper groupMapper;
	private UserMapper userMapper;

	@BeforeEach
	void setUp() {
		userMapper = new UserMapper();
		groupMapper = new GroupMapper(userMapper);
	}

	@Test
	void testToResponse_NullGroup() {
		GroupResponse response = groupMapper.toResponse(null);
		assertNull(response);
	}

	@Test
	void testToResponse_GroupWithoutCreator() {
		Group group = new Group();
		group.setId(1L);
		group.setName("Test Group");
		group.setCreator(null);

		GroupResponse response = groupMapper.toResponse(group);
		assertNotNull(response);
		assertEquals(1L, response.getId());
		assertEquals("Test Group", response.getName());
		assertNull(response.getCreatorId());
		assertNull(response.getCreatorUsername());
	}

	@Test
	void testToResponse_GroupWithCreator() {
		User creator = new User();
		creator.setId(2L);
		creator.setUsername("creatorUser");

		Group group = new Group();
		group.setId(1L);
		group.setName("Test Group");
		group.setCreator(creator);

		GroupResponse response = groupMapper.toResponse(group);
		assertNotNull(response);
		assertEquals(1L, response.getId());
		assertEquals("Test Group", response.getName());
		assertEquals(2L, response.getCreatorId());
		assertEquals("creatorUser", response.getCreatorUsername());
	}

	@Test
	void testToEntity_NullRequest() {
		Group group = groupMapper.toEntity(null);
		assertNull(group);
	}

	@Test
	void testToEntity_ValidRequest() {
		GroupRequest request = new GroupRequest();
		request.setName("New Group");

		Group group = groupMapper.toEntity(request);
		assertNotNull(group);
		assertEquals("New Group", group.getName());
	}
}
