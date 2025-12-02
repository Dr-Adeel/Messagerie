package com.eilco.messagerie.services.implementations;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.eilco.messagerie.mappers.MessageMapper;
import com.eilco.messagerie.models.request.MessageRequest;
import com.eilco.messagerie.models.response.MessageResponse;
import com.eilco.messagerie.repositories.MessageRepository;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.Message;
import com.eilco.messagerie.repositories.entities.User;

@ExtendWith(MockitoExtension.class)
class DirectMessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageMapper messageMapper;

    private DirectMessageService service;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        service = new DirectMessageService(messageRepository, userRepository, messageMapper);
    }

    @Test
    void sendDirectMessage_persistsMessageWithUsers() {
        MessageRequest request = new MessageRequest();
        request.setContent("Hello");
        request.setSenderId(1L);
        request.setReceiverUserId(2L);

        User sender = new User();
        sender.setId(1L);
        User receiver = new User();
        receiver.setId(2L);
        given(userRepository.findById(1L)).willReturn(Optional.of(sender));
        given(userRepository.findById(2L)).willReturn(Optional.of(receiver));

        Message saved = new Message();
        saved.setId(99L);
        given(messageRepository.save(any(Message.class))).willReturn(saved);

        MessageResponse response = new MessageResponse();
        response.setId(99L);
        given(messageMapper.toResponse(saved)).willReturn(response);

        MessageResponse result = service.sendDirectMessage(request);

        assertThat(result).isEqualTo(response);
        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        Message persisted = captor.getValue();
        assertThat(persisted.getContent()).isEqualTo("Hello");
        assertThat(persisted.getSender()).isEqualTo(sender);
        assertThat(persisted.getReceiverUser()).isEqualTo(receiver);
        assertThat(persisted.getReceiverGroup()).isNull();
    }

    @Test
    void sendDirectMessage_throwsIfReceiverMissing() {
        MessageRequest request = new MessageRequest();
        request.setContent("Hello");
        request.setSenderId(1L);
        request.setReceiverUserId(null);

        assertThatThrownBy(() -> service.sendDirectMessage(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("receiverUserId est obligatoire");
    }

    @Test
    void sendDirectMessage_throwsIfSenderNotFound() {
        MessageRequest request = new MessageRequest();
        request.setContent("Hello");
        request.setSenderId(10L);
        request.setReceiverUserId(20L);

        given(userRepository.findById(10L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.sendDirectMessage(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Sender not found");
    }

    @Test
    void getPrivateConversation_returnsMappedResponses() {
        DirectMessageService serviceWithRealMapper = new DirectMessageService(
                messageRepository, userRepository, new MessageMapper());

        User alice = new User();
        alice.setId(1L);
        alice.setUsername("alice");
        User bob = new User();
        bob.setId(2L);
        bob.setUsername("bob");

        Message first = new Message();
        first.setId(1L);
        first.setContent("Hello Bob");
        first.setTimestamp(LocalDateTime.of(2024, 1, 1, 10, 0));
        first.setSender(alice);
        first.setReceiverUser(bob);

        Message second = new Message();
        second.setId(2L);
        second.setContent("Hi Alice");
        second.setTimestamp(LocalDateTime.of(2024, 1, 1, 10, 5));
        second.setSender(bob);
        second.setReceiverUser(alice);

        given(messageRepository.findConversationBetweenUsers(1L, 2L))
                .willReturn(List.of(first, second));

        List<MessageResponse> responses = serviceWithRealMapper.getPrivateConversation(1L, 2L);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
        assertThat(responses.get(0).getContent()).isEqualTo("Hello Bob");
        assertThat(responses.get(0).getSenderUsername()).isEqualTo("alice");
        assertThat(responses.get(0).getReceiverUsername()).isEqualTo("bob");
        assertThat(responses.get(0).getMessageType()).isEqualTo("DM");

        assertThat(responses.get(1).getId()).isEqualTo(2L);
        assertThat(responses.get(1).getContent()).isEqualTo("Hi Alice");
        assertThat(responses.get(1).getSenderUsername()).isEqualTo("bob");
        assertThat(responses.get(1).getReceiverUsername()).isEqualTo("alice");
        assertThat(responses.get(1).getMessageType()).isEqualTo("DM");

        verify(messageRepository).findConversationBetweenUsers(1L, 2L);
    }
}
