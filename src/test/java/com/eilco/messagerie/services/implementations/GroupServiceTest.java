package com.eilco.messagerie.services.implementations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.eilco.messagerie.mappers.GroupMapper;
import com.eilco.messagerie.mappers.UserMapper;
import com.eilco.messagerie.models.request.GroupRequest;
import com.eilco.messagerie.models.response.GroupResponse;
import com.eilco.messagerie.repositories.GroupRepository;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.repositories.entities.Group;
import com.eilco.messagerie.repositories.entities.User;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private UserRepository userRepository;

    private GroupService groupService;

    @BeforeEach
    void setUp() {
        GroupMapper groupMapper = new GroupMapper(new UserMapper());
        groupService = new GroupService(groupRepository, userRepository, groupMapper);
    }

    @Test
    void createGroup_persistsGroupAndAssignsCreator() {
        User creator = new User();
        creator.setId(1L);
        given(userRepository.findById(1L)).willReturn(Optional.of(creator));

        Group saved = new Group();
        saved.setId(2L);
        saved.setName("Team");
        saved.setCreator(creator);
        given(groupRepository.save(any(Group.class))).willReturn(saved);

        GroupRequest request = new GroupRequest();
        request.setName("Team");
        request.setCreatorId(1L);

        GroupResponse response = groupService.createGroup(request);

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getName()).isEqualTo("Team");
        assertThat(response.getCreatorId()).isEqualTo(1L);
        verify(userRepository).save(creator);
    }

    @Test
    void createGroup_throwsWhenCreatorAlreadyInGroup() {
        User creator = new User();
        creator.setId(1L);
        creator.setGroup(new Group());
        given(userRepository.findById(1L)).willReturn(Optional.of(creator));

        GroupRequest request = new GroupRequest();
        request.setName("Team");
        request.setCreatorId(1L);

        assertThrows(IllegalStateException.class, () -> groupService.createGroup(request));
    }

    @Test
    void createGroup_throwsWhenCreatorMissing() {
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        GroupRequest request = new GroupRequest();
        request.setName("Team");
        request.setCreatorId(99L);

        assertThrows(IllegalArgumentException.class, () -> groupService.createGroup(request));
    }

    @Test
    void deleteGroup_clearsUsersAndDeletesGroup() {
        Group group = new Group();
        group.setId(5L);
        given(groupRepository.findById(5L)).willReturn(Optional.of(group));

        User member = new User();
        member.setId(10L);
        member.setGroup(group);

        given(userRepository.findAllByGroup(group)).willReturn(List.of(member));

        groupService.deleteGroup(5L);

        assertThat(member.getGroup()).isNull();
        verify(userRepository).saveAll(List.of(member));
        verify(groupRepository).delete(group);
    }

    @Test
    void addMember_requiresCreator() {
        Group group = new Group();
        User creator = new User();
        creator.setId(1L);
        group.setCreator(creator);
        given(groupRepository.findById(7L)).willReturn(Optional.of(group));

        User requester = new User();
        requester.setId(2L);
        given(userRepository.findById(2L)).willReturn(Optional.of(requester));

        User newMember = new User();
        newMember.setId(3L);
        given(userRepository.findById(3L)).willReturn(Optional.of(newMember));

        assertThrows(RuntimeException.class, () -> groupService.addMember(7L, 3L, 2L));
    }

    @Test
    void addMember_throwsWhenUserAlreadyInGroup() {
        Group group = new Group();
        User creator = new User();
        creator.setId(1L);
        group.setCreator(creator);
        given(groupRepository.findById(7L)).willReturn(Optional.of(group));

        given(userRepository.findById(1L)).willReturn(Optional.of(creator));

        User newMember = new User();
        newMember.setId(3L);
        newMember.setGroup(new Group());
        given(userRepository.findById(3L)).willReturn(Optional.of(newMember));

        assertThrows(RuntimeException.class, () -> groupService.addMember(7L, 3L, 1L));
    }

    @Test
    void addMember_assignsGroupWhenAuthorized() {
        Group group = new Group();
        User creator = new User();
        creator.setId(1L);
        group.setCreator(creator);
        given(groupRepository.findById(7L)).willReturn(Optional.of(group));

        given(userRepository.findById(1L)).willReturn(Optional.of(creator));

        User newMember = new User();
        newMember.setId(3L);
        given(userRepository.findById(3L)).willReturn(Optional.of(newMember));

        groupService.addMember(7L, 3L, 1L);

        assertThat(newMember.getGroup()).isEqualTo(group);
        verify(userRepository).save(newMember);
    }

    @Test
    void removeMember_requiresAdmin() {
        Group group = new Group();
        User creator = new User();
        creator.setId(1L);
        group.setCreator(creator);
        given(groupRepository.findById(9L)).willReturn(Optional.of(group));

        User requester = new User();
        requester.setId(2L);
        given(userRepository.findById(2L)).willReturn(Optional.of(requester));

        User member = new User();
        member.setId(3L);
        member.setGroup(group);
        given(userRepository.findById(3L)).willReturn(Optional.of(member));

        assertThrows(RuntimeException.class, () -> groupService.removeMember(9L, 3L, 2L));
    }

    @Test
    void removeMember_failsWhenUserNotInGroup() {
        Group group = new Group();
        User creator = new User();
        creator.setId(1L);
        group.setCreator(creator);
        given(groupRepository.findById(9L)).willReturn(Optional.of(group));
        given(userRepository.findById(1L)).willReturn(Optional.of(creator));

        User member = new User();
        member.setId(3L);
        member.setGroup(null);
        given(userRepository.findById(3L)).willReturn(Optional.of(member));

        assertThrows(RuntimeException.class, () -> groupService.removeMember(9L, 3L, 1L));
    }

    @Test
    void removeMember_preventsRemovingCreator() {
        Group group = new Group();
        User creator = new User();
        creator.setId(1L);
        group.setCreator(creator);
        creator.setGroup(group);
        given(groupRepository.findById(9L)).willReturn(Optional.of(group));
        given(userRepository.findById(1L)).willReturn(Optional.of(creator));
        given(userRepository.findById(1L)).willReturn(Optional.of(creator));

        assertThrows(RuntimeException.class, () -> groupService.removeMember(9L, 1L, 1L));
    }

    @Test
    void removeMember_removesWhenAuthorized() {
        Group group = new Group();
        User creator = new User();
        creator.setId(1L);
        group.setCreator(creator);
        given(groupRepository.findById(9L)).willReturn(Optional.of(group));
        given(userRepository.findById(1L)).willReturn(Optional.of(creator));

        User member = new User();
        member.setId(3L);
        member.setGroup(group);
        given(userRepository.findById(3L)).willReturn(Optional.of(member));

        groupService.removeMember(9L, 3L, 1L);

        assertThat(member.getGroup()).isNull();
        verify(userRepository).save(member);
    }

    @Test
    void getById_returnsGroup() {
        Group group = new Group();
        given(groupRepository.findById(4L)).willReturn(Optional.of(group));

        Group result = groupService.getById(4L);

        assertThat(result).isEqualTo(group);
    }

    @Test
    void getById_throwsWhenMissing() {
        given(groupRepository.findById(4L)).willReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> groupService.getById(4L));
    }

    @Test
    void isAdminOfGroup_handlesNullsAndEquality() {
        Group group = new Group();
        User creator = new User();
        creator.setId(1L);
        group.setCreator(creator);

        User other = new User();
        other.setId(2L);

        assertThat(groupService.isAdminOfGroup(null, group)).isFalse();
        assertThat(groupService.isAdminOfGroup(creator, group)).isTrue();
        assertThat(groupService.isAdminOfGroup(other, group)).isFalse();
    }
}
