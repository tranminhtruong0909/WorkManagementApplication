package com.example.workmanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

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

    // ✅ THÊM FIELD CHO MULTIPLE ASSIGNEES
    private List<Integer> assigneeIds;
}