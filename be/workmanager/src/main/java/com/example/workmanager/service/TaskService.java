package com.example.workmanager.service;

import com.example.workmanager.dto.TaskResponse;
import com.example.workmanager.model.Group;
import com.example.workmanager.model.Task;
import com.example.workmanager.model.TaskStatus;
import com.example.workmanager.repository.GroupRepository;
import com.example.workmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final GroupRepository groupRepository;

    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public Task createTask(String name, Integer groupId, String status,
                           LocalDate dueDate, LocalDate timelineStart,
                           LocalDate timelineEnd, String notes) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));

        Task task = new Task();
        task.setName(name);
        task.setGroup(group);

        // Convert String to Enum (safe)
        if (status != null) {
            task.setStatus(TaskStatus.valueOf(status.toUpperCase()));
        } else {
            task.setStatus(TaskStatus.TODO); // default status
        }

        task.setDueDate(dueDate);
        task.setTimelineStart(timelineStart);
        task.setTimelineEnd(timelineEnd);
        task.setNotes(notes);

        return taskRepository.save(task);
    }

    public Task updateTask(Integer id, String status, String notes) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        if (status != null) {
            task.setStatus(TaskStatus.valueOf(status.toUpperCase()));
        }

        if (notes != null) {
            task.setNotes(notes);
        }

        return taskRepository.save(task);
    }

    public Task updateTaskFull(Integer id, String name, String status,
                               LocalDate dueDate, LocalDate timelineStart,
                               LocalDate timelineEnd, String notes) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));

        if (name != null) task.setName(name);
        if (status != null) task.setStatus(TaskStatus.valueOf(status.toUpperCase()));
        if (dueDate != null) task.setDueDate(dueDate);
        if (timelineStart != null) task.setTimelineStart(timelineStart);
        if (timelineEnd != null) task.setTimelineEnd(timelineEnd);
        if (notes != null) task.setNotes(notes);

        return taskRepository.save(task);
    }

    public void deleteTask(Integer id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        taskRepository.delete(task);
    }

    public Optional<TaskResponse> getTaskById(Integer id) {
        return taskRepository.findById(id).map(this::convertToResponse);
    }

    public List<TaskResponse> getTasksByGroupId(Integer groupId) {
        return taskRepository.findByGroupId(groupId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private TaskResponse convertToResponse(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getName(),
                task.getGroup().getId(),
                task.getGroup().getName(),
                task.getStatus().name(), // Convert enum to string
                task.getDueDate(),
                task.getTimelineStart(),
                task.getTimelineEnd(),
                task.getNotes()
        );
    }
}
