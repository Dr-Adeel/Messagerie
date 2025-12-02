package com.eilco.messagerie.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.eilco.messagerie.models.request.GroupRequest;
import com.eilco.messagerie.models.response.GroupResponse;
import com.eilco.messagerie.repositories.entities.User;
import com.eilco.messagerie.services.implementations.CurrentUserService;
import com.eilco.messagerie.services.interfaces.IGroupService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class GroupControllerTest {

    @Mock
    private IGroupService groupService;

    @Mock
    private CurrentUserService currentUserService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        GroupController controller = new GroupController(groupService, currentUserService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void createGroup_returnsCreated() throws Exception {
        GroupRequest request = buildGroupRequest();
        GroupResponse response = new GroupResponse();
        response.setId(5L);
        response.setName("Team");
        given(groupService.createGroup(any(GroupRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5L));

        verify(groupService).createGroup(any(GroupRequest.class));
    }

    @Test
    void createGroup_returnsBadRequestWhenServiceFails() throws Exception {
        GroupRequest request = buildGroupRequest();
        given(groupService.createGroup(any(GroupRequest.class))).willThrow(new RuntimeException("invalid"));

        mockMvc.perform(post("/api/group")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteGroup_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/group/7"))
                .andExpect(status().isNoContent());

        verify(groupService).deleteGroup(7L);
    }

    @Test
    void deleteGroup_returnsNotFoundWhenServiceThrows() throws Exception {
        doThrow(new RuntimeException("missing")).when(groupService).deleteGroup(99L);

        mockMvc.perform(delete("/api/group/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void addMember_returnsOkAndDelegates() throws Exception {
        User current = new User();
        current.setId(1L);
        given(currentUserService.getCurrentUser()).willReturn(current);

        mockMvc.perform(post("/api/group/3/member/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Member added successfully!"));

        verify(groupService).addMember(3L, 10L, 1L);
    }

    @Test
    void addMember_returnsBadRequestWhenServiceThrows() throws Exception {
        User current = new User();
        current.setId(2L);
        given(currentUserService.getCurrentUser()).willReturn(current);
        doThrow(new RuntimeException("forbidden")).when(groupService).addMember(eq(4L), eq(8L), eq(2L));

        mockMvc.perform(post("/api/group/4/member/8"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteMember_returnsNoContent() throws Exception {
        User current = new User();
        current.setId(5L);
        given(currentUserService.getCurrentUser()).willReturn(current);

        mockMvc.perform(delete("/api/group/6/member/12"))
                .andExpect(status().isNoContent());

        verify(groupService).removeMember(6L, 12L, 5L);
    }

    @Test
    void deleteMember_returnsBadRequestWhenServiceThrows() throws Exception {
        User current = new User();
        current.setId(3L);
        given(currentUserService.getCurrentUser()).willReturn(current);
        doThrow(new RuntimeException("bad"))
                .when(groupService).removeMember(6L, 9L, 3L);

        mockMvc.perform(delete("/api/group/6/member/9"))
                .andExpect(status().isBadRequest());
    }

    private GroupRequest buildGroupRequest() {
        GroupRequest request = new GroupRequest();
        request.setName("Team");
        request.setCreatorId(1L);
        return request;
    }
}
