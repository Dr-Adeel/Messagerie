package com.eilco.messagerie.services.interfaces;

import com.eilco.messagerie.models.request.MessageRequest;
import com.eilco.messagerie.models.response.MessageResponse;

import java.util.List;

public interface IDirectMessageService {

	MessageResponse sendDirectMessage(MessageRequest request);

	List<MessageResponse> getPrivateConversation(Long userAId, Long userBId);
}
