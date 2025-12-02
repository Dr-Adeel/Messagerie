package com.eilco.messagerie.controllers;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.eilco.messagerie.models.request.UserRequest;
import com.eilco.messagerie.models.response.UserResponse;
import com.eilco.messagerie.services.interfaces.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private IUserService userService;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        UserController userController = new UserController(userService);
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    @Test
    void getUser_returnsOkWhenFound() throws Exception {
        UserResponse response = UserResponse.builder()
                .id(1L)
                .username("john")
                .firstName("John")
                .lastName("Doe")
                .build();
        given(userService.getById(1L)).willReturn(response);

        mockMvc.perform(get("/api/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("john"));

        verify(userService).getById(1L);
    }

    @Test
    void getUser_returns404WhenServiceThrows() throws Exception {
        given(userService.getById(1L)).willThrow(new RuntimeException("introuvable"));

        mockMvc.perform(get("/api/user/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUsers_returnsCollection() throws Exception {
        given(userService.getAll()).willReturn(List.of(UserResponse.builder().id(2L).username("amy").build()));

        mockMvc.perform(get("/api/user/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("amy"));

        verify(userService).getAll();
    }

    @Test
    void createUser_returnsCreated() throws Exception {
        UserRequest request = buildUserRequest();
        UserResponse created = UserResponse.builder().id(10L).username("neo").build();
        given(userService.create(any(UserRequest.class))).willReturn(created);

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L));

        verify(userService).create(any(UserRequest.class));
    }

    @Test
    void createUser_returnsBadRequestWhenServiceFails() throws Exception {
        UserRequest request = buildUserRequest();
        given(userService.create(any(UserRequest.class))).willThrow(new RuntimeException("duplicate"));

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateUser_returnsOk() throws Exception {
        UserRequest request = buildUserRequest();
        UserResponse updated = UserResponse.builder().id(1L).username("updated").build();
        given(userService.update(eq(1L), any(UserRequest.class))).willReturn(updated);

        mockMvc.perform(put("/api/user/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("updated"));

        verify(userService).update(eq(1L), any(UserRequest.class));
    }

    @Test
    void updateUser_returns404WhenServiceThrows() throws Exception {
        UserRequest request = buildUserRequest();
        given(userService.update(eq(42L), any(UserRequest.class))).willThrow(new RuntimeException("missing"));

        mockMvc.perform(put("/api/user/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/user/7"))
                .andExpect(status().isNoContent());

        verify(userService).delete(7L);
    }

    @Test
    void deleteUser_returns404WhenServiceThrows() throws Exception {
        doThrow(new RuntimeException("missing")).when(userService).delete(7L);

        mockMvc.perform(delete("/api/user/7"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchByFirstName_returnsBadRequestWhenQueryBlank() throws Exception {
        mockMvc.perform(get("/api/user/search").param("name", " "))
                .andExpect(status().isBadRequest());

        verify(userService, never()).searchByFirstName(any());
    }

    @Test
    void searchByUsername_returnsBadRequestWhenQueryBlank() throws Exception {
        mockMvc.perform(get("/api/user/search").param("username", ""))
                .andExpect(status().isBadRequest());

        verify(userService, never()).searchByUsername(any());
    }

    private UserRequest buildUserRequest() {
        return UserRequest.builder()
                .username("johnny")
                .password("password")
                .firstName("John")
                .lastName("Doe")
                .groupId(1L)
                .build();
    }
}
