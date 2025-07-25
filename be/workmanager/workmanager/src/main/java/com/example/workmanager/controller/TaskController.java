package com.example.workmanager.controller;

import com.example.workmanager.dto.TaskRequest;
import com.example.workmanager.dto.TaskResponse;
import com.example.workmanager.model.Task;
import com.example.workmanager.model.TaskStatus;
import com.example.workmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<?> createTask(@Valid @RequestBody TaskRequest request) {
        try {
            TaskStatus status = request.getStatus() != null
                    ? TaskStatus.valueOf(request.getStatus().toUpperCase())
                    : TaskStatus.TODO;

            Task task = taskService.createTask(
                    request.getName(),
                    request.getGroupId(),
                    status.name(),
                    request.getDueDate() != null ? LocalDate.parse(request.getDueDate()) : null,
                    request.getTimelineStart() != null ? LocalDate.parse(request.getTimelineStart()) : null,
                    request.getTimelineEnd() != null ? LocalDate.parse(request.getTimelineEnd()) : null,
                    request.getNotes()
            );
            return ResponseEntity.ok(new TaskResponse(task));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Trạng thái không hợp lệ");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }

    // Các phương thức khác giữ nguyên...
}