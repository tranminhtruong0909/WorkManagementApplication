package com.example.workmanager.controller;

import com.example.workmanager.dto.TaskRequest;
import com.example.workmanager.dto.TaskResponse;
import com.example.workmanager.model.Task;
import com.example.workmanager.model.TaskStatus;
import com.example.workmanager.model.User;
import com.example.workmanager.model.CustomUserDetails;
import com.example.workmanager.service.TaskService;
import com.example.workmanager.service.PermissionService;
import com.example.workmanager.service.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import com.example.workmanager.model.Group;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;
    private final PermissionService permissionService;
    private final GroupService groupService;

    // ✅ Tạo task mới
    @PostMapping
    public ResponseEntity<?> createTask(@Valid @RequestBody TaskRequest request,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.getUser();
            // Lấy group để xác định board
            var groupOpt = groupService.getGroupById(request.getGroupId());
            if (groupOpt.isEmpty()) return ResponseEntity.badRequest().body("Group không tồn tại");
            Group group = groupOpt.get();
            Integer boardId = group.getBoard().getId();
            if (!permissionService.canManageBoard(user.getId(), boardId)) {
                return ResponseEntity.status(403).body("Bạn không có quyền thực hiện chức năng này!");
            }
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
    public ResponseEntity<?> getAllTasks(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        // Lọc chỉ trả về các task thuộc các board mà user có quyền xem
        var allTasks = taskService.getAllTaskEntities();
        var filtered = allTasks.stream()
            .filter(task -> permissionService.canViewBoard(user.getId(), task.getGroup().getBoard().getId()))
            .map(TaskResponse::new)
            .toList();
        return ResponseEntity.ok(filtered);
    }

    // ✅ Lấy task theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        var taskOpt = taskService.getTaskEntityById(id);
        if (taskOpt.isEmpty()) return ResponseEntity.notFound().build();
        Task task = taskOpt.get();
        Integer boardId = task.getGroup().getBoard().getId();
        if (!permissionService.canViewBoard(user.getId(), boardId)) {
            return ResponseEntity.status(403).body("Bạn không có quyền thực hiện chức năng này!");
        }
        return ResponseEntity.ok(new TaskResponse(task));
    }

    // ✅ Cập nhật toàn bộ task (PUT) - có kiểm tra quyền
    @PutMapping("/{id}")
    public ResponseEntity<?> updateTaskFull(@PathVariable Integer id,
                                            @Valid @RequestBody TaskRequest request,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.getUser();
            Task task = taskService.getTaskEntityById(id).orElse(null);
            if (task == null) return ResponseEntity.notFound().build();
            Integer boardId = task.getGroup().getBoard().getId();
            if (!permissionService.canEditTask(user.getId(), boardId, task)) {
                return ResponseEntity.status(403).body("Bạn không có quyền thực hiện chức năng này!");
            }
            Task updatedTask = taskService.updateTaskFullWithPermission(
                    id,
                    request.getName(),
                    request.getStatus(),
                    request.getDueDate() != null ? LocalDate.parse(request.getDueDate()) : null,
                    request.getTimelineStart() != null ? LocalDate.parse(request.getTimelineStart()) : null,
                    request.getTimelineEnd() != null ? LocalDate.parse(request.getTimelineEnd()) : null,
                    request.getNotes(),
                    user
            );
            return ResponseEntity.ok(new TaskResponse(updatedTask));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật task: " + e.getMessage());
        }
    }

    // ✅ Cập nhật 1 phần task (PATCH) - có kiểm tra quyền
    @PatchMapping("/{id}")
    public ResponseEntity<?> updateTaskStatusNotes(@PathVariable Integer id,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) String notes,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.getUser();
            Task task = taskService.getTaskEntityById(id).orElse(null);
            if (task == null) return ResponseEntity.notFound().build();
            Integer boardId = task.getGroup().getBoard().getId();
            if (!permissionService.canEditTask(user.getId(), boardId, task)) {
                return ResponseEntity.status(403).body("Bạn không có quyền thực hiện chức năng này!");
            }
            Task updatedTask = taskService.updateTask(id, status, notes, user);
            return ResponseEntity.ok(new TaskResponse(updatedTask));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi cập nhật task: " + e.getMessage());
        }
    }

    // ✅ Xoá task
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Integer id,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.getUser();
            Task task = taskService.getTaskEntityById(id).orElse(null);
            if (task == null) return ResponseEntity.notFound().build();
            Integer boardId = task.getGroup().getBoard().getId();
            if (!permissionService.canManageBoard(user.getId(), boardId)) {
                return ResponseEntity.status(403).body("Bạn không có quyền thực hiện chức năng này!");
            }
            taskService.deleteTask(id);
            return ResponseEntity.ok("Đã xoá task có ID " + id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi xoá task: " + e.getMessage());
        }
    }
}
