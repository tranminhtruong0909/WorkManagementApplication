package com.example.workmanager.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TokenRefreshException extends RuntimeException {
    private final HttpStatus status;
    private final String errorCode;

    public TokenRefreshException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status = status;
        this.errorCode = errorCode;
    }
}