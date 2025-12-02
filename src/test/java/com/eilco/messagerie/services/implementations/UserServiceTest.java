package com.eilco.messagerie.services.implementations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.eilco.messagerie.mappers.UserMapper;
import com.eilco.messagerie.models.request.UserRequest;
import com.eilco.messagerie.models.response.UserResponse;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.User;
import com.eilco.messagerie.services.interfaces.IGroupService;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private IGroupService groupService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @SuppressWarnings("unused")
    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, groupService, new UserMapper(), passwordEncoder);
    }

    @Test
    void create_throwsWhenUsernameExists() {
        UserRequest request = UserRequest.builder()
            .username("john")
            .build();
        given(userRepository.existsByUsername("john")).willReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.create(request));
        assertThat(exception.getMessage()).contains("existe déjà");
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_savesUserWithoutGroup() {
        UserRequest request = UserRequest.builder()
            .username("john")
            .password("pwd")
            .firstName("John")
            .lastName("Doe")
            .build();

        given(userRepository.existsByUsername("john")).willReturn(false);
        given(passwordEncoder.encode("pwd")).willReturn("encoded");

        User saved = new User();
        saved.setId(1L);
        saved.setUsername("john");
        saved.setPassword("encoded");
        given(userRepository.save(any(User.class))).willReturn(saved);

        UserResponse response = userService.create(request);

        assertThat(response.getId()).isEqualTo(1L);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getGroup()).isNull();
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded");
    }

    @Test
    void create_addsUserToGroupWhenGroupProvided() {
        Group group = new Group();
        group.setId(5L);

        UserRequest request = UserRequest.builder()
                .username("member")
                .password("pwd")
                .firstName("Mem")
                .lastName("Ber")
                .groupId(5L)
                .build();

        given(userRepository.existsByUsername("member")).willReturn(false);
        given(passwordEncoder.encode("pwd")).willReturn("encoded");
        given(groupService.getById(5L)).willReturn(group);

        User persisted = new User();
        persisted.setId(2L);
        persisted.setUsername("member");
        persisted.setGroup(group);
        given(userRepository.save(any(User.class))).willReturn(persisted);

        UserResponse response = userService.create(request);

        assertEquals(5L, response.getGroupId());
        verify(groupService).getById(5L);
    }

    @Test
    void create_throwsWhenGroupLookupFails() {
        UserRequest request = UserRequest.builder()
                .username("member")
                .password("pwd")
                .firstName("Mem")
                .lastName("Ber")
                .groupId(5L)
                .build();

        given(userRepository.existsByUsername("member")).willReturn(false);
        given(passwordEncoder.encode("pwd")).willReturn("encoded");
        given(groupService.getById(5L)).willThrow(new RuntimeException("Groupe introuvable"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.create(request));
        assertThat(exception.getMessage()).contains("Groupe introuvable");
        verify(userRepository, never()).save(any());
    }

    @Test
    void getById_checksPermissionAndReturnsResponse() {
        User user = new User();
        user.setId(10L);
        user.setUsername("john");
        given(userRepository.findById(10L)).willReturn(Optional.of(user));

        UserResponse response = userService.getById(10L);

        assertThat(response.getUsername()).isEqualTo("john");
    }

    @Test
    void getAll_returnsMappedUsers() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        given(userRepository.findAll()).willReturn(List.of(user));

        List<UserResponse> responses = userService.getAll();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUsername()).isEqualTo("alice");
    }

    @Test
    void update_updatesFieldsAndPasswordAndGroup() {
        Group group = new Group();
        group.setId(3L);
        User user = new User();
        user.setId(1L);
        user.setPassword("old");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(passwordEncoder.encode("newPass")).willReturn("encoded");
        given(groupService.getById(3L)).willReturn(group);

        UserRequest request = UserRequest.builder()
            .username("bob")
            .password("newPass")
            .firstName("Bob")
            .lastName("Lee")
            .groupId(3L)
            .build();

        userService.update(1L, request);

        verify(userRepository).save(user);
        assertThat(user.getPassword()).isEqualTo("encoded");
        assertThat(user.getGroup()).isEqualTo(group);
        assertThat(user.getUsername()).isEqualTo("bob");
    }

    @Test
    void update_keepsPasswordWhenBlank() {
        User user = new User();
        user.setId(1L);
        user.setPassword("existing");
        given(userRepository.findById(1L)).willReturn(Optional.of(user));

        UserRequest request = UserRequest.builder()
            .username("bob")
            .password(" ")
            .firstName("Bob")
            .lastName("Lee")
            .build();

        userService.update(1L, request);

        assertThat(user.getPassword()).isEqualTo("existing");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void delete_throwsWhenUserMissing() {
        given(userRepository.existsById(8L)).willReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.delete(8L));
        assertThat(exception.getMessage()).contains("introuvable");
    }

    @Test
    void delete_deletesExistingUser() {
        given(userRepository.existsById(8L)).willReturn(true);

        userService.delete(8L);

        verify(userRepository).deleteById(8L);
    }

    @Test
    void searchByUsername_invokesPermissionAndMaps() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        given(userRepository.findByUsernameContainingIgnoreCase("ali")).willReturn(List.of(user));

        List<UserResponse> responses = userService.searchByUsername("ali");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getUsername()).isEqualTo("alice");
    }

    @Test
    void searchByFirstName_invokesPermissionAndMaps() {
        User user = new User();
        user.setId(1L);
        user.setFirstName("Alice");
        given(userRepository.findByFirstNameContainingIgnoreCase("ali")).willReturn(List.of(user));

        List<UserResponse> responses = userService.searchByFirstName("ali");

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getFirstName()).isEqualTo("Alice");
    }
}
