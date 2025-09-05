package com.example.workmanager.controller;

import com.example.workmanager.dto.request.TaskRequest;
import com.example.workmanager.dto.response.TaskResponse;
import com.example.workmanager.dto.request.TaskSearchRequest;
import com.example.workmanager.dto.response.TaskSearchResult;
import com.example.workmanager.exceptions.PermissionDeniedException;
import com.example.workmanager.exceptions.ResourceNotFoundException;
import com.example.workmanager.model.Task;
import com.example.workmanager.model.TaskStatus;
import com.example.workmanager.model.User;
import com.example.workmanager.model.CustomUserDetails;
import com.example.workmanager.service.TaskService;
import com.example.workmanager.service.GroupService;
import com.example.workmanager.service.PermissionFacade;
import com.example.workmanager.util.PermissionValidator;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.workmanager.model.Group;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TaskController {

    private final TaskService taskService;
    private final PermissionFacade permissionFacade;
    private final GroupService groupService;
    private final PermissionValidator permissionValidator;

    @PostMapping
    public ResponseEntity<?> createTask(@Valid @RequestBody TaskRequest request,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {

        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();

        Group group = groupService.getGroupById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group không tồn tại!"));
        Integer broadId = group.getBoard().getId();
        permissionValidator.validateTaskCreatePermission(user.getId(), broadId);

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
    }

    @PostMapping("/with-assignees")
    public ResponseEntity<?> createTaskWithAssignees(@Valid @RequestBody TaskRequest request,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {

        permissionValidator.validateUserAuthentication(userDetails);
        User user = userDetails.getUser();
        Group group = groupService.getGroupById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Group không tồn tại!"));

        Integer broadId = group.getBoard().getId();
        permissionValidator.validateTaskCreatePermission(user.getId(), broadId);

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
                request.getNotes(),
                request.getAssigneeIds(),
                user
        );
        return ResponseEntity.ok(new TaskResponse(task));
    }

    @GetMapping
    public ResponseEntity<?> getAllTasks(@AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();

        var allTasks = taskService.getAllTaskEntities();
        var filtered = allTasks.stream()
                .filter(task -> permissionFacade.canViewTask(user.getId(), task.getGroup().getBoard().getId()))
                .map(TaskResponse::new)
                .toList();
        return ResponseEntity.ok(filtered);
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchTasks(@ModelAttribute TaskSearchRequest searchRequest,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);
        User user = userDetails.getUser();

        if (searchRequest.getId() != null) {
            return searchById(searchRequest, user);
        }

        Page<TaskResponse> pageResult = taskService.searchTasksUnified(searchRequest, user);

        TaskSearchResult response = new TaskSearchResult(
                pageResult.getContent(),
                pageResult.getNumber(),
                pageResult.getSize(),
                pageResult.getTotalElements(),
                pageResult.getTotalPages(),
                pageResult.getContent().size(),
                pageResult.hasNext(),
                pageResult.isFirst(),
                pageResult.isLast()
        );

        return ResponseEntity.ok(response);
    }

    private ResponseEntity<?> searchById(TaskSearchRequest searchRequest, User user) {
        Task task = taskService.getTaskEntityById(searchRequest.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại"));

        Integer boardId = task.getGroup().getBoard().getId();
        permissionValidator.validateTaskCreatePermission(user.getId(), boardId);

        return ResponseEntity.ok(new TaskResponse(task));
    }


    @PutMapping("/{taskId}")
    public ResponseEntity<?> updateTaskFull(@PathVariable Integer taskId,
                                            @Valid @RequestBody TaskRequest request,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {

        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();
        Task task = taskService.getTaskEntityById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại!"));

        Integer broadId = task.getGroup().getBoard().getId();
        permissionValidator.validateTaskEditPermission(user.getId(), broadId,  task);

        Task updatedTask = taskService.updateTaskFullWithPermission(
                taskId,
                request.getName(),
                request.getStatus(),
                request.getDueDate() != null ? LocalDate.parse(request.getDueDate()) : null,
                request.getTimelineStart() != null ? LocalDate.parse(request.getTimelineStart()) : null,
                request.getTimelineEnd() != null ? LocalDate.parse(request.getTimelineEnd()) : null,
                request.getNotes(),
                user
        );
        return ResponseEntity.ok(new TaskResponse(updatedTask));
    }

    @PutMapping("/{taskId}/with-assignees")
    public ResponseEntity<?> updateTaskFullWithAssignees(@PathVariable Integer taskId,
                                                         @Valid @RequestBody TaskRequest request,
                                                         @AuthenticationPrincipal CustomUserDetails userDetails) {

        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();
        Task task = taskService.getTaskEntityById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại!")) ;

        Integer boardId = task.getGroup().getBoard().getId();
        permissionValidator.validateTaskEditPermission(user.getId(), boardId, task);

        Task updatedTask = taskService.updateTaskFullWithPermission(
                taskId,
                request.getName(),
                request.getStatus(),
                request.getDueDate() != null ? LocalDate.parse(request.getDueDate()) : null,
                request.getTimelineStart() != null ? LocalDate.parse(request.getTimelineStart()) : null,
                request.getTimelineEnd() != null ? LocalDate.parse(request.getTimelineEnd()) : null,
                request.getNotes(),
                request.getAssigneeIds(),
                user
        );
        return ResponseEntity.ok(new TaskResponse(updatedTask));
    }

    @PatchMapping("/{taskId}")
    public ResponseEntity<?> updateTaskStatusNotes(@PathVariable Integer taskId,
                                                   @RequestParam(required = false) String status,
                                                   @RequestParam(required = false) String notes,
                                                   @AuthenticationPrincipal CustomUserDetails userDetails) {

        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();
        Task task = taskService.getTaskEntityById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task này không tồn tại!"));

        Integer boardId = task.getGroup().getBoard().getId();
        permissionValidator.validateTaskEditPermission(user.getId(), boardId, task);

        Task updatedTask = taskService.updateTask(taskId, status, notes, user);
        return ResponseEntity.ok(new TaskResponse(updatedTask));
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<?> deleteTask(@PathVariable Integer taskId,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();
        Task task = taskService.getTaskEntityById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại"));

        Integer boardId = task.getGroup().getBoard().getId();
        permissionValidator.validateTaskDeletePermission(user.getId(), boardId);

        taskService.deleteTask(taskId);
        return ResponseEntity.ok("Đã xoá task có ID " + taskId);
    }

    @PostMapping("/{taskId}/assignees/{userId}")
    public ResponseEntity<?> addAssignee(@PathVariable Integer taskId,
                                         @PathVariable Integer userId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();
        Task task = taskService.addAssigneeToTask(taskId, userId, user);
        return ResponseEntity.ok(new TaskResponse(task));
    }

    @PostMapping("/{taskId}/assignees")
    public ResponseEntity<?> addMultipleAssignees(@PathVariable Integer taskId,
                                                  @RequestBody List<Integer> userIds,
                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();
        Task task = taskService.addMultipleAssigneesToTask(taskId, userIds, user);
        return ResponseEntity.ok(new TaskResponse(task));
    }

    @DeleteMapping("/{taskId}/assignees/{userId}")
    public ResponseEntity<?> removeAssignee(@PathVariable Integer taskId,
                                            @PathVariable Integer userId,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();
        Task task = taskService.removeAssigneeFromTask(taskId, userId, user);
        return ResponseEntity.ok(new TaskResponse(task));
    }

    @GetMapping("/{taskId}/assignees")
    public ResponseEntity<?> getTaskAssignees(@PathVariable Integer taskId,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();
        Task task = taskService.getTaskEntityById(taskId)
                .orElseThrow(()-> new ResourceNotFoundException("Task không tồn tại!"));

        Integer boardId = task.getGroup().getBoard().getId();
        permissionValidator.validateTaskCreatePermission(user.getId(), boardId);

        List<User> assignees = taskService.getTaskAssignees(taskId);
        return ResponseEntity.ok(assignees);
    }

    @PutMapping("/{taskId}/assignees")
    public ResponseEntity<?> updateTaskAssignees(@PathVariable Integer taskId,
                                                 @RequestBody List<Integer> assigneeIds,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();
        Task task = taskService.updateTaskAssignees(taskId, assigneeIds, user);
        return ResponseEntity.ok(new TaskResponse(task));
    }

    @GetMapping("/assignee/{userId}")
    public ResponseEntity<?> getTasksByAssignee(@PathVariable Integer userId,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();
        if (!user.getId().equals(userId)) {
            throw new PermissionDeniedException("Bạn không có quyền xem task của người khác !", "VIEW_OTHER_USER_TASKS");
        }

        List<TaskResponse> tasks = taskService.getTasksByAssignee(userId);
        return ResponseEntity.ok(tasks);

    }

    @GetMapping("/{taskId}/is-assignee/{userId}")
    public ResponseEntity<?> isUserAssignee(@PathVariable Integer taskId,
                                            @PathVariable Integer userId,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();
        Task task = taskService.getTaskEntityById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task không tồn tại"));

        Integer boardId = task.getGroup().getBoard().getId();
        permissionValidator.validateTaskViewPermission(taskId, boardId);

        boolean isAssignee = taskService.isUserAssignee(taskId, userId);
        return ResponseEntity.ok(isAssignee);
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getTasksByGroupId(@PathVariable Integer groupId,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();
        Group group = groupService.getGroupById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group không tồn tại!"));

        Integer boardId = group.getBoard().getId();
        permissionValidator.validateTaskViewPermission(groupId, boardId);

        List<TaskResponse> tasks = taskService.getTasksByGroupId(groupId);
        return ResponseEntity.ok(tasks);
    }
}