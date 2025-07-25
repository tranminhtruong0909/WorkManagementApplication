package com.example.workmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskRequest {

    @NotBlank(message = "Tên task không được để trống")
    private String name;

    @NotNull(message = "Group ID không được để trống")
    private Integer groupId;

    private String status;
    private String dueDate;
    private String timelineStart;
    private String timelineEnd;
    private String notes;
}