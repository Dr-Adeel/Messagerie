package com.eilco.messagerie.controllers;

import com.eilco.messagerie.models.request.UserRequest;
import com.eilco.messagerie.models.response.UserResponse;
import com.eilco.messagerie.repositories.UserRepository;
import com.eilco.messagerie.services.IUserService;
import com.eilco.messagerie.services.JWTService;
import com.eilco.messagerie.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;


import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
public class SecurityController {

    final private JWTService jwtService;

    final private  IUserService userService;



    @PostMapping("/login")
    public String getToken(Authentication authentication) {
        return jwtService.generateToken(authentication);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.saveUser(request));
    }


}