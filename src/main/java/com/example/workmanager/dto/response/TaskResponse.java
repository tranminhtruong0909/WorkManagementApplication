package com.example.workmanager.dto.response;

import com.example.workmanager.model.Task;
import com.example.workmanager.model.User;
import lombok.Data;


import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class TaskResponse {

    private Integer id;

    private String name;

    private Integer groupId;

    private String groupName;

    private String status;

    private LocalDate dueDate;

    private LocalDate timelineStart;

    private LocalDate timelineEnd;

    private String notes;

    private List<Integer> assigneeIds;

    private List<String> assigneeNames;

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