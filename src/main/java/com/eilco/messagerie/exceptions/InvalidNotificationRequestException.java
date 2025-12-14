package com.eilco.messagerie.exceptions;

public class InvalidNotificationRequestException extends RuntimeException {
    public InvalidNotificationRequestException(String message) {
        super(message);
    }
}
