package com.example.workmanager.dto;

import com.example.workmanager.model.Task;
import java.time.LocalDate;

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

    public TaskResponse(Integer id, String name, Integer groupId, String groupName, String status,
                        LocalDate dueDate, LocalDate timelineStart, LocalDate timelineEnd, String notes) {
        this.id = id;
        this.name = name;
        this.groupId = groupId;
        this.groupName = groupName;
        this.status = status;
        this.dueDate = dueDate;
        this.timelineStart = timelineStart;
        this.timelineEnd = timelineEnd;
        this.notes = notes;
    }

    public TaskResponse(Task task) {
        this(
                task.getId(),
                task.getName(),
                task.getGroup().getId(),
                task.getGroup().getName(),
                task.getStatus().name(),  // convert enum to String
                task.getDueDate(),
                task.getTimelineStart(),
                task.getTimelineEnd(),
                task.getNotes()
        );
    }

    // Getters (có thể thêm setters nếu cần)
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getTimelineStart() {
        return timelineStart;
    }

    public LocalDate getTimelineEnd() {
        return timelineEnd;
    }

    public String getNotes() {
        return notes;
    }
}
