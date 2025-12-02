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

	public MessageResponse sendDirectMessage(MessageRequest request) {
		log.info("DirectMessageService.sendDirectMessage - senderId={}, receiverUserId={}",
				request.getSenderId(), request.getReceiverUserId());

		// Vérifier que receiverUserId est bien fourni
		if (request.getReceiverUserId() == null) {
			throw new IllegalArgumentException("receiverUserId est obligatoire pour un message direct (DM).");
		}

		// Récupérer l'expéditeur
		User sender = userRepository.findById(request.getSenderId())
				.orElseThrow(() -> new IllegalArgumentException("Sender not found with id: " + request.getSenderId()));

		// Récupérer le destinataire
		User receiver = userRepository.findById(request.getReceiverUserId())
				.orElseThrow(() -> new IllegalArgumentException("Receiver not found with id: " + request.getReceiverUserId()));

		// Construire l'entité Message pour un DM
		Message message = new Message();
		message.setContent(request.getContent());
		message.setTimestamp(LocalDateTime.now());   // tu peux aussi laisser le default de l'entité
		message.setSender(sender);
		message.setReceiverUser(receiver);
		message.setReceiverGroup(null);              //C'est un DM, pas un message de groupe!!!

		// Sauvegarder en base
		Message saved = messageRepository.save(message);

		// Convertir en MessageResponse via le mapper
		return messageMapper.toResponse(saved);
	}

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
