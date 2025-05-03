package com.backendfitnessapp.exceptions;

public class UserAlreadyExitsException extends RuntimeException {
    public UserAlreadyExitsException(String message) {
        super(message);
    }
}
