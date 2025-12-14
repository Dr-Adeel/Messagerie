package com.eilco.messagerie.exceptions;

public class UserNotFoundException extends RuntimeException {
      public UserNotFoundException(String message) {
            super(message);
      }
}
