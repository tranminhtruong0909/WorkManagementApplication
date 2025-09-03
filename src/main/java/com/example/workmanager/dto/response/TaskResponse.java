package com.example.workmanager.dto;

import com.example.workmanager.model.Task;
import com.example.workmanager.model.User;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class TaskResponse {

    @Setter
    @Getter
    private Integer id;

    @Setter
    @Getter
    private String name;

    @Setter
    @Getter
    private Integer groupId;

    @Setter
    @Getter
    private String groupName;

    @Setter
    @Getter
    private String status;

    @Setter
    @Getter
    private LocalDate dueDate;

    @Setter
    @Getter
    private LocalDate timelineStart;
    @Setter
    @Getter
    private LocalDate timelineEnd;

    @Setter
    @Getter
    private String notes;
    // GETTERS cho các field mới
    @Getter
    private List<Integer> assigneeIds; // ✅ THÊM FIELD MỚI
    @Getter
    private List<String> assigneeNames; // ✅ THÊM FIELD MỚI

    public TaskResponse(Task task) {
        this.id = task.getId();
        this.name = task.getName();
        this.groupId = task.getGroup().getId();
        this.groupName = task.getGroup().getName();
        this.status = task.getStatus().name();
        this.dueDate = task.getDueDate();
        this.timelineStart = task.getTimelineStart();
        this.timelineEnd = task.getTimelineEnd();
        this.notes = task.getNotes();

        // ✅ THÊM: Lấy thông tin assignees
        if (task.getAssignees() != null) {
            this.assigneeIds = task.getAssignees().stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
            this.assigneeNames = task.getAssignees().stream()
                    .map(User::getName)
                    .collect(Collectors.toList());
        }
    }

}