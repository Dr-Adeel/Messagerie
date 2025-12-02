package com.eilco.messagerie.services.implementations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.eilco.messagerie.mappers.UserMapper;
import com.eilco.messagerie.models.request.UserRequest;
import com.eilco.messagerie.models.response.UserResponse;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.User;
import com.eilco.messagerie.services.interfaces.IGroupService;
import com.eilco.messagerie.services.security.AuthorizationService;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private IGroupService groupService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthorizationService authorizationService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, groupService, new UserMapper(), passwordEncoder, authorizationService);
    }

    @Test
    void create_throwsWhenUsernameExists() {
        UserRequest request = new UserRequest();
        request.setUsername("john");
        given(userRepository.existsByUsername("john")).willReturn(true);

        assertThrows(RuntimeException.class, () -> userService.create(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void create_savesUserWithoutGroup() {
        UserRequest request = new UserRequest();
        request.setUsername("john");
        request.setPassword("pwd");
        request.setFirstName("John");
        request.setLastName("Doe");

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
    void create_addsUserToGroupWhenAuthorized() {
        Group group = new Group();
        group.setId(5L);
        User admin = new User();
        admin.setId(99L);

        UserRequest request = new UserRequest();
        request.setUsername("member");
        request.setPassword("pwd");
        request.setFirstName("Mem");
        request.setLastName("Ber");
        request.setGroupId(5L);

        given(userRepository.existsByUsername("member")).willReturn(false);
        given(passwordEncoder.encode("pwd")).willReturn("encoded");
        given(groupService.getById(5L)).willReturn(group);
        given(authorizationService.getCurrentUser()).willReturn(admin);
        given(groupService.isAdminOfGroup(admin, group)).willReturn(true);

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
    void create_deniesWhenCurrentUserNotAdminOfGroup() {
        Group group = new Group();
        group.setId(5L);
        User current = new User();
        current.setId(4L);

        UserRequest request = new UserRequest();
        request.setUsername("member");
        request.setPassword("pwd");
        request.setFirstName("Mem");
        request.setLastName("Ber");
        request.setGroupId(5L);

        given(userRepository.existsByUsername("member")).willReturn(false);
        given(groupService.getById(5L)).willReturn(group);
        given(authorizationService.getCurrentUser()).willReturn(current);
        given(groupService.isAdminOfGroup(current, group)).willReturn(false);

        assertThrows(RuntimeException.class, () -> userService.create(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void getById_checksPermissionAndReturnsResponse() {
        User user = new User();
        user.setId(10L);
        user.setUsername("john");
        given(userRepository.findById(10L)).willReturn(Optional.of(user));

        UserResponse response = userService.getById(10L);

        verify(authorizationService).checkPermission("USER_READ");
        assertThat(response.getUsername()).isEqualTo("john");
    }

    @Test
    void getAll_returnsMappedUsers() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        given(userRepository.findAll()).willReturn(List.of(user));

        List<UserResponse> responses = userService.getAll();

        verify(authorizationService).checkPermission("USER_READ");
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

        UserRequest request = new UserRequest();
        request.setUsername("bob");
        request.setPassword("newPass");
        request.setFirstName("Bob");
        request.setLastName("Lee");
        request.setGroupId(3L);

        userService.update(1L, request);

        verify(authorizationService).checkPermission("USER_UPDATE");
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

        UserRequest request = new UserRequest();
        request.setUsername("bob");
        request.setPassword(" ");
        request.setFirstName("Bob");
        request.setLastName("Lee");

        userService.update(1L, request);

        assertThat(user.getPassword()).isEqualTo("existing");
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void delete_throwsWhenUserMissing() {
        given(userRepository.existsById(8L)).willReturn(false);

        assertThrows(RuntimeException.class, () -> userService.delete(8L));
    }

    @Test
    void delete_deletesExistingUser() {
        given(userRepository.existsById(8L)).willReturn(true);

        userService.delete(8L);

        verify(authorizationService).checkPermission("USER_DELETE");
        verify(userRepository).deleteById(8L);
    }

    @Test
    void searchByUsername_invokesPermissionAndMaps() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        given(userRepository.findByUsernameContainingIgnoreCase("ali")).willReturn(List.of(user));

        List<UserResponse> responses = userService.searchByUsername("ali");

        verify(authorizationService).checkPermission("USER_SEARCH");
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

        verify(authorizationService).checkPermission("USER_SEARCH");
        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getFirstName()).isEqualTo("Alice");
    }
}
