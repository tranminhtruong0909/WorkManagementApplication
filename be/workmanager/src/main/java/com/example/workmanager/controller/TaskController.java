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

    // ✅ Tạo task mới
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

    // ✅ Lấy toàn bộ task
    @GetMapping
    public ResponseEntity<?> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    // ✅ Lấy task theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Integer id) {
        return taskService.getTaskById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 🆕 Cập nhật toàn bộ task (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTaskFull(@PathVariable Integer id, @Valid @RequestBody TaskRequest request) {
        try {
            Task task = taskService.updateTaskFull(
                    id,
                    request.getName(),
                    request.getStatus(),
                    request.getDueDate() != null ? LocalDate.parse(request.getDueDate()) : null,
                    request.getTimelineStart() != null ? LocalDate.parse(request.getTimelineStart()) : null,
                    request.getTimelineEnd() != null ? LocalDate.parse(request.getTimelineEnd()) : null,
                    request.getNotes()
            );
            return ResponseEntity.ok(new TaskResponse(task));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật task: " + e.getMessage());
        }
    }

    // 🆕 Cập nhật 1 phần task (status + notes)
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateTaskStatusNotes(@PathVariable Integer id,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) String notes) {
        try {
            Task task = taskService.updateTask(id, status, notes);
            return ResponseEntity.ok(new TaskResponse(task));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật task: " + e.getMessage());
        }
    }

    // 🆕 Xoá task
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Integer id) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.ok("Đã xoá task có ID " + id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi xoá task: " + e.getMessage());
        }
    }
}
