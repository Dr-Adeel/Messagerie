package com.eilco.messagerie.exceptions;

public class UserAlreadyInGroupException extends RuntimeException {
      public UserAlreadyInGroupException(String message) {
            super(message);
      }
}
