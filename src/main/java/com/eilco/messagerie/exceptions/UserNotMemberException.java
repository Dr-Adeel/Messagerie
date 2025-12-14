package com.eilco.messagerie.exceptions;

public class UserNotMemberException extends RuntimeException {
      public UserNotMemberException(String message) {
            super(message);
      }
}
