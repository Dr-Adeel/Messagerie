package com.eilco.messagerie.controllers;

import com.eilco.messagerie.models.request.LoginRequest;
import com.eilco.messagerie.models.request.UserRequest;
import com.eilco.messagerie.models.response.JwtResponse;
import com.eilco.messagerie.models.response.UserResponse;
import com.eilco.messagerie.security.jwt.JwtUtils;
import com.eilco.messagerie.services.interfaces.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {

    private final IUserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public AuthController(IUserService userService,
                          AuthenticationManager authenticationManager,
                          JwtUtils jwtUtils) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@RequestBody UserRequest userRequest) {
        log.info("[AUTH CONTROLLER] Register request received for username='{}'",
                userRequest.getUsername());

        UserResponse registeredUser = userService.registerUser(userRequest);

        log.info("[AUTH CONTROLLER] User '{}' registered successfully with ID={}",
                registeredUser.getUsername(), registeredUser.getId());

        return ResponseEntity.ok(registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> loginUser(@RequestBody LoginRequest loginRequest) {
        log.info("[AUTH CONTROLLER] Login attempt for username='{}'",
                loginRequest.username());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.username(),
                        loginRequest.password())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        var user = userService.findByUsername(userDetails.getUsername()).orElseThrow();

        log.info("[AUTH CONTROLLER] User '{}' authenticated successfully", user.getUsername());

        return ResponseEntity.ok(
                new JwtResponse(
                        jwt,
                        user.getId(),
                        user.getUsername(),
                        user.getFirstName(),
                        user.getLastName()
                )
        );
    }
}
