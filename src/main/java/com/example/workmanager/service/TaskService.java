package com.example.workmanager.service;

import com.example.workmanager.dto.TaskResponse;
import com.example.workmanager.model.*;
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
    private final PermissionService permissionService;

    // ✅ getAllTasks - trả về Task entity
    public List<Task> getAllTaskEntities() {
        return taskRepository.findAll();
    }

    // ✅ getAllTasks - trả về TaskResponse (giữ nguyên cho backward compatibility)
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
    }

    // ✅ getTaskById - trả về Task entity
    public Optional<Task> getTaskEntityById(Integer id) {
        return taskRepository.findById(id);
    }

    // ✅ getTaskById - trả về TaskResponse (giữ nguyên cho backward compatibility)
    public Optional<TaskResponse> getTaskById(Integer id) {
        return taskRepository.findById(id).map(TaskResponse::new);
    }

    // ✅ updateTask (PATCH: status + notes)
    public Task updateTask(Integer taskId, String status, String notes, User user) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy task"));

        Integer boardId = task.getGroup().getBoard().getId();
        if (!permissionService.canEditTask(user.getId(), boardId, task)) {
            throw new RuntimeException("Bạn không có quyền cập nhật task này");
        }

        if (status != null) task.setStatus(TaskStatus.valueOf(status.toUpperCase()));
        if (notes != null) task.setNotes(notes);

        return taskRepository.save(task);
    }

    // ✅ updateTaskFullWithPermission (PUT: toàn bộ task)
    public Task updateTaskFullWithPermission(Integer id, String name, String status,
                                             LocalDate dueDate, LocalDate timelineStart,
                                             LocalDate timelineEnd, String notes, User user) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task không tồn tại"));

        Integer boardId = task.getGroup().getBoard().getId();
        if (!permissionService.canEditTask(user.getId(), boardId, task)) {
            throw new RuntimeException("Bạn không có quyền chỉnh sửa task này");
        }

        task.setName(name != null ? name : task.getName());

        if (status != null) {
            task.setStatus(TaskStatus.valueOf(status.toUpperCase()));
        }

        task.setDueDate(dueDate);
        task.setTimelineStart(timelineStart);
        task.setTimelineEnd(timelineEnd);
        task.setNotes(notes);

        return taskRepository.save(task);
    }

    // ✅ createTask (giữ nguyên như cũ)
    public Task createTask(String name, Integer groupId, String status,
                           LocalDate dueDate, LocalDate timelineStart,
                           LocalDate timelineEnd, String notes) {

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy group"));

        Task task = new Task();
        task.setName(name);
        task.setGroup(group);
        task.setStatus(TaskStatus.valueOf(status.toUpperCase()));
        task.setDueDate(dueDate);
        task.setTimelineStart(timelineStart);
        task.setTimelineEnd(timelineEnd);
        task.setNotes(notes);

        return taskRepository.save(task);
    }

    // ✅ deleteTask
    public void deleteTask(Integer id) {
        taskRepository.deleteById(id);
    }
    public List<TaskResponse> getTasksByGroupId(Integer groupId) {
        return taskRepository.findByGroupId(groupId)
                .stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
    }

}
