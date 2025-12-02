package com.eilco.messagerie.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.eilco.messagerie.repositories.GroupRepository;
import com.eilco.messagerie.repositories.MessageRepository;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.Message;
import com.eilco.messagerie.repositories.entities.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupMessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GroupMessageService groupMessageService;

    @Test
    void sendMessageGroup_savesMessageWithGroupReceiver() {
        User sender = new User();
        sender.setId(1L);
        Group group = new Group();
        group.setId(2L);

        given(userRepository.findById(1L)).willReturn(Optional.of(sender));
        given(groupRepository.findById(2L)).willReturn(Optional.of(group));
        given(messageRepository.save(any(Message.class))).willAnswer(invocation -> invocation.getArgument(0));

        Message saved = groupMessageService.sendMessageGroup(1L, 2L, "Hello team");

        ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
        verify(messageRepository).save(captor.capture());
        Message stored = captor.getValue();
        assertThat(stored.getSender()).isEqualTo(sender);
        assertThat(stored.getReceiverGroup()).isEqualTo(group);
        assertThat(stored.getReceiverUser()).isNull();
        assertThat(saved.getContent()).isEqualTo("Hello team");
    }

    @Test
    void sendMessageGroup_throwsWhenUserMissing() {
        given(userRepository.findById(1L)).willReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> groupMessageService.sendMessageGroup(1L, 2L, "Hello"));
    }

    @Test
    void getGroupMessages_returnsHistoryForExistingUser() {
        User user = new User();
        user.setId(5L);
        given(userRepository.findById(5L)).willReturn(Optional.of(user));

        List<Message> messages = List.of(new Message(), new Message());
        given(messageRepository.findByReceiverGroupIdOrderByTimestampAsc(3L)).willReturn(messages);

        List<Message> result = groupMessageService.getGroupMessages(3L, 5L);

        assertThat(result).hasSize(2);
        verify(messageRepository).findByReceiverGroupIdOrderByTimestampAsc(3L);
    }

    @Test
    void getGroupMessages_throwsWhenUserMissing() {
        given(userRepository.findById(5L)).willReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> groupMessageService.getGroupMessages(3L, 5L));
    }

}
