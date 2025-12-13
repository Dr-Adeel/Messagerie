package com.eilco.messagerie.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

      @ExceptionHandler(UserNotFoundException.class)
      public ResponseEntity<Map<String, String>> handleUserNotFoundException(UserNotFoundException ex) {
            Map<String, String> response = new HashMap<>();
            response.put("error", ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
      }

      @ExceptionHandler(GroupNotFoundException.class)
      public ResponseEntity<Map<String, String>> handleGroupNotFoundException(GroupNotFoundException ex) {
            Map<String, String> response = new HashMap<>();
            response.put("error", ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
      }

      @ExceptionHandler(UserNotMemberException.class)
      public ResponseEntity<Map<String, String>> handleUserNotMemberException(UserNotMemberException ex) {
            Map<String, String> response = new HashMap<>();
            response.put("error", ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
      }

      @ExceptionHandler(BadCredentialsException.class)
      public ResponseEntity<Map<String, String>> handleBadCredentialsException(BadCredentialsException ex) {
            Map<String, String> response = new HashMap<>();
            response.put("error", "Invalid username or password");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
      }

      @ExceptionHandler(Exception.class)
      public ResponseEntity<Map<String, String>> handleGlobalException(Exception ex) {
            Map<String, String> response = new HashMap<>();
            response.put("error", ex.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
      }
}
