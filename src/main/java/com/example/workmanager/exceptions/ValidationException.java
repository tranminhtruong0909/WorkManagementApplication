package com.example.workmanager.exceptions;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException{

    private final String errorCode;

    public ValidationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
