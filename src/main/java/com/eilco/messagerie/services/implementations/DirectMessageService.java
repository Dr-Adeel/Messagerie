package com.eilco.messagerie.services.implementations;

import com.eilco.messagerie.mappers.MessageMapper;
import com.eilco.messagerie.models.request.MessageRequest;
import com.eilco.messagerie.models.response.MessageResponse;
import com.eilco.messagerie.repositories.MessageRepository;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.Message;
import com.eilco.messagerie.repositories.entities.User;
import com.eilco.messagerie.services.interfaces.IDirectMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectMessageService implements IDirectMessageService {

	private final MessageRepository messageRepository;
	private final UserRepository userRepository;
	private final MessageMapper messageMapper;


	@Override
	public List<MessageResponse> getPrivateConversation(Long userAId, Long userBId) {
		log.info("DirectMessageService.getPrivateConversation - users: {} & {}", userAId, userBId);

		// Récupération des entités Message via la requête JPQL déjà définie
		List<Message> messages = messageRepository.findConversationBetweenUsers(userAId, userBId);

		// Mapping vers MessageResponse
		return messages.stream()
			.map(messageMapper::toResponse)
			.toList();
	}
}
