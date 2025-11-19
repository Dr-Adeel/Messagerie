package com.eilco.messagerie.services;


import com.eilco.messagerie.models.request.UserRequest;
import com.eilco.messagerie.models.response.UserResponse;

public interface IUserService {
    UserResponse saveUser(UserRequest request);

    void deleteUser(Long id);
    UserResponse updateUser(Long id, UserRequest request);
}
