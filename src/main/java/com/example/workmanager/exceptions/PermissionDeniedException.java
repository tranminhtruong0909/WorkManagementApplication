package com.example.workmanager.exceptions;

import lombok.Getter;

@Getter
public class PermissionDeniedException extends RuntimeException{

    private final String permissionType;

    public PermissionDeniedException(String message , String permissionType) {
        super(message);
        this.permissionType = permissionType;
    }

}
