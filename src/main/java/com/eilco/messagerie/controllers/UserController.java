package com.eilco.messagerie.controllers;

import com.eilco.messagerie.models.response.UserResponse;
import com.eilco.messagerie.services.interfaces.IUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

      private final IUserService userService;

      public UserController(IUserService userService) {
            this.userService = userService;
      }

      @GetMapping
      public ResponseEntity<List<UserResponse>> getAllUsers() {
            return ResponseEntity.ok(userService.findAllUsers());
      }
}
