package com.eilco.messagerie.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.eilco.messagerie.mappers.MessageMapper;
import com.eilco.messagerie.models.request.MessageRequest;
import com.eilco.messagerie.models.response.MessageResponse;
import com.eilco.messagerie.repositories.entities.Message;
import com.eilco.messagerie.service.IGroupMessageService;
import com.eilco.messagerie.services.interfaces.IDirectMessageService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class MessageControllerTest {

    @Mock
    private IGroupMessageService groupMessageService;

    @Mock
    private IDirectMessageService directMessageService;

    @Mock
    private MessageMapper messageMapper;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MessageController controller = new MessageController(groupMessageService, directMessageService, messageMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void getPrivateConversation_returnsResponses() throws Exception {
        MessageResponse response = new MessageResponse(1L, "Hello", LocalDateTime.now(), 1L,
                "alice", 2L, "bob", null, null, "DM");
        given(directMessageService.getPrivateConversation(1L, 2L)).willReturn(List.of(response));

        mockMvc.perform(get("/api/message/direct/1/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Hello"));

        verify(directMessageService).getPrivateConversation(1L, 2L);
    }

    @Test
    void sendDirectMessage_delegatesToService() throws Exception {
        MessageRequest request = buildDirectMessageRequest();
        MessageResponse response = new MessageResponse(5L, "Hi", LocalDateTime.now(), 1L,
                "alice", 2L, "bob", null, null, "DM");
        given(directMessageService.sendDirectMessage(any(MessageRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/message/direct")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5L));

        verify(directMessageService).sendDirectMessage(any(MessageRequest.class));
    }

    @Test
    void getGroupConversation_mapsMessagesToResponses() throws Exception {
        Message entity = new Message();
        MessageResponse mapped = new MessageResponse(9L, "Group msg", LocalDateTime.now(), 1L,
                "alice", null, null, 3L, "team", "GROUP");
        given(groupMessageService.getGroupMessages(4L, 1L)).willReturn(List.of(entity));
        given(messageMapper.toResponse(entity)).willReturn(mapped);

        mockMvc.perform(get("/api/message/group/1/4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].messageType").value("GROUP"));

        verify(groupMessageService).getGroupMessages(4L, 1L);
    }

    @Test
    void sendGroupMessage_requiresGroupId() throws Exception {
        MessageRequest request = new MessageRequest();
        request.setContent("team msg");
        request.setSenderId(1L);

        mockMvc.perform(post("/api/message/group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sendGroupMessage_mapsResponse() throws Exception {
        MessageRequest request = new MessageRequest();
        request.setContent("team msg");
        request.setSenderId(1L);
        request.setReceiverGroupId(3L);

        Message message = new Message();
        MessageResponse response = new MessageResponse(11L, "team msg", LocalDateTime.now(), 1L,
                "alice", null, null, 3L, "team", "GROUP");
        given(groupMessageService.sendMessageGroup(1L, 3L, "team msg")).willReturn(message);
        given(messageMapper.toResponse(message)).willReturn(response);

        mockMvc.perform(post("/api/message/group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(11L));

        verify(groupMessageService).sendMessageGroup(1L, 3L, "team msg");
        verify(messageMapper).toResponse(message);
    }

    private MessageRequest buildDirectMessageRequest() {
        MessageRequest request = new MessageRequest();
        request.setContent("Hi");
        request.setSenderId(1L);
        request.setReceiverUserId(2L);
        return request;
    }
}
