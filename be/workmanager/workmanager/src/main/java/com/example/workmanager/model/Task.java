package com.example.workmanager.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private LocalDate dueDate;
    private LocalDate timelineStart;
    private LocalDate timelineEnd;

    private String notes;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getTimelineStart() {
        return timelineStart;
    }

    public void setTimelineStart(LocalDate timelineStart) {
        this.timelineStart = timelineStart;
    }

    public LocalDate getTimelineEnd() {
        return timelineEnd;
    }

    public void setTimelineEnd(LocalDate timelineEnd) {
        this.timelineEnd = timelineEnd;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }
}
