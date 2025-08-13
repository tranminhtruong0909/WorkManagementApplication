package com.example.workmanager.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TaskStatus {
    TODO("TODO)"),
    IN_PROGRESS("IN_PROGRESS"),
    DONE("DONE"),
    EXPIRED("EXPIRED"),
    WORKINGONIT("WORKINGONIT");


    private final String value;

    TaskStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static TaskStatus fromValue(String value) {
        for (TaskStatus status : TaskStatus.values()) {
            if (status.value.equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        throw new IllegalArgumentException("Trạng thái không hợp lệ");
    }
}
