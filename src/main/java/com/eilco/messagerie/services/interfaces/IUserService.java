package com.eilco.messagerie.services.interfaces;

import com.eilco.messagerie.models.request.UserRequest;
import com.eilco.messagerie.models.response.UserResponse;
import java.util.List;

public interface IUserService {
    UserResponse create(UserRequest request);

    UserResponse getById(Long id);

    List<UserResponse> getAll();

    UserResponse update(Long id, UserRequest request);

    void delete(Long id);

    List<UserResponse> searchByUsername(String username);

    List<UserResponse> searchByFirstName(String firstname);

}
