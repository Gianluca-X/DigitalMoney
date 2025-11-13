package com.example.userservice.exceptions;

public class DniAlreadyExistsException extends RuntimeException {
    public DniAlreadyExistsException(String message) { super(message);
    }
}
