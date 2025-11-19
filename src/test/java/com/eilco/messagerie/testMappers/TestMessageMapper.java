package com.eilco.messagerie.testMappers;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eilco.messagerie.mappers.MessageMapper;
import com.eilco.messagerie.models.response.MessageResponse;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.Message;
import com.eilco.messagerie.repositories.entities.User;

public class TestMessageMapper {

	private MessageMapper mapper;

	@BeforeEach
	void setUp() {
		mapper = new MessageMapper();
	}

	@Test
	void testToResponseNullMessageReturnsNull() {
		Assertions.assertNull(mapper.toResponse(null));
	}

	@Test
	void testToResponseDirectMessageMapping() {
		User sender = new User();
		sender.setId(1L);
		sender.setUsername("alice");
		sender.setPassword("hashed");

		User receiver = new User();
		receiver.setId(2L);
		receiver.setUsername("bob");
		receiver.setPassword("hashed");

		Message message = new Message();
		message.setId(10L);
		message.setContent("Hello Bob");
		message.setTimestamp(LocalDateTime.now());
		message.setSender(sender);
		message.setReceiverUser(receiver);

		MessageResponse response = mapper.toResponse(message);

		Assertions.assertNotNull(response);
		Assertions.assertEquals(Long.valueOf(10L), response.getId());
		Assertions.assertEquals("Hello Bob", response.getContent());
		Assertions.assertEquals("alice", response.getSenderUsername());
		Assertions.assertEquals(Long.valueOf(1L), response.getSenderId());
		Assertions.assertEquals(Long.valueOf(2L), response.getReceiverUserId());
		Assertions.assertEquals("bob", response.getReceiverUsername());
		Assertions.assertEquals("DM", response.getMessageType());
		Assertions.assertNull(response.getReceiverGroupId());
	}

	@Test
	void testToResponseGroupMessageMapping() {
		User sender = new User();
		sender.setId(3L);
		sender.setUsername("charlie");
		sender.setPassword("hashed");

		User creator = new User();
		creator.setId(4L);
		creator.setUsername("david");
		creator.setPassword("hashed");

		Group group = new Group();
		group.setId(20L);
		group.setName("GroupeTest");
		group.setCreator(creator);

		Message message = new Message();
		message.setId(11L);
		message.setContent("Message au groupe");
		message.setTimestamp(LocalDateTime.now());
		message.setSender(sender);
		message.setReceiverGroup(group);

		MessageResponse response = mapper.toResponse(message);

		Assertions.assertNotNull(response);
		Assertions.assertEquals("GROUP", response.getMessageType());
		Assertions.assertEquals(Long.valueOf(20L), response.getReceiverGroupId());
		Assertions.assertEquals("GroupeTest", response.getReceiverGroupName());
		Assertions.assertNull(response.getReceiverUserId());
	}

	@Test
	void testToResponseInvalidMessageMapping() {
		User sender = new User();
		sender.setId(5L);
		sender.setUsername("eve");
		sender.setPassword("hashed");

		Message message = new Message();
		message.setId(12L);
		message.setContent("Sans destinataire");
		message.setTimestamp(LocalDateTime.now());
		message.setSender(sender);
		// ni receiverUser ni receiverGroup

		MessageResponse response = mapper.toResponse(message);
		Assertions.assertNotNull(response);
		Assertions.assertEquals("INVALID", response.getMessageType());
		Assertions.assertNull(response.getReceiverUserId());
		Assertions.assertNull(response.getReceiverGroupId());
	}
}
