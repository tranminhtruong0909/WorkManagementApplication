package com.example.workmanager.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomAuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(CustomAuthenticationException ex) {
        Map<String , Object> respone = new HashMap<>();
        respone.put("success" , false);
        respone.put("message", ex.getMessage());
        respone.put("errorCode", ex.getErrorCode());
        respone.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(ex.getStatus()).body(respone);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String , Object> respone = new HashMap<>();
        respone.put("success" , false);
        respone.put("message", ex.getMessage());
        respone.put("errorCode", "RESOURCE_NOT_FOUND");
        respone.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(respone);
    }

    @ExceptionHandler(PermissionDeniedException.class)
    public ResponseEntity<Map<String, Object>> handlePermissionDeniedException(PermissionDeniedException ex) {
        Map<String , Object> respone = new HashMap<>();
        respone.put("success" , false);
        respone.put("message", ex.getMessage());
        respone.put("errorCode", "PERMISSION_DENIED");
        respone.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(respone);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException ex) {
        Map<String , Object> respone = new HashMap<>();
        respone.put("success" , false);
        respone.put("message", ex.getMessage());
        respone.put("errorCode", "VALIDATION_FAILED");
        respone.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respone);
    }

    @ExceptionHandler(BusinessLogicException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessLogicException(BusinessLogicException ex) {
        Map<String , Object> respone = new HashMap<>();
        respone.put("success" , false);
        respone.put("message", ex.getMessage());
        respone.put("errorCode", "BUSINESS_EXCEPTION");
        respone.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(respone);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleException(Exception ex) {
        Map<String , Object> respone = new HashMap<>();
        respone.put("success" , false);
        respone.put("message", "Có lỗi h thống xảy ra. Vui lòng thử lại sau");
        respone.put("errorCode", "EXCEPTION");
        respone.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respone);
    }

    @ExceptionHandler(JwtTokenException.class)
    public ResponseEntity<Map<String, Object>> handleJwtTokenException(JwtTokenException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", ex.getMessage());
        response.put("errorCode", ex.getErrorCode());
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<Map<String, Object>> handleTokenRefreshException(TokenRefreshException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", ex.getMessage());
        response.put("errorCode", ex.getErrorCode());
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

}
