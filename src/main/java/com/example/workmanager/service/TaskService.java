package com.example.workmanager.service;

import com.example.workmanager.dto.TaskResponse;
import com.example.workmanager.dto.TaskSearchRequest;
import com.example.workmanager.model.*;
import com.example.workmanager.repository.GroupRepository;
import com.example.workmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

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

    // Method đếm tổng số task (để tính phân trang)
    public long countTasksUnified(TaskSearchRequest searchRequest, User user) {
        TaskStatus status = null;
        if (searchRequest.getStatus() != null && !searchRequest.getStatus().trim().isEmpty()) {
            try {
                status = TaskStatus.valueOf(searchRequest.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                return 0;
            }
        }

        // Đếm từ database
        long totalCount;
        if (!searchRequest.hasAnyFilter()) {
            totalCount = taskRepository.count();
        } else {
            // Cần thêm method count trong repository
            totalCount = taskRepository.countSearchTasksUnified(
                    searchRequest.getName(),
                    status,
                    searchRequest.getGroupId(),
                    searchRequest.getAssigneeId(),
                    searchRequest.getSearchDate(),
                    searchRequest.getStartDate(),
                    searchRequest.getEndDate()
            );
        }

        return totalCount;
    }

    // Method tìm kiếm thống nhất (giữ nguyên method cũ)
    public List<TaskResponse> searchTasksUnified(TaskSearchRequest searchRequest, User user) {
        TaskStatus status = null;
        if (searchRequest.getStatus() != null && !searchRequest.getStatus().trim().isEmpty()) {
            try {
                status = TaskStatus.valueOf(searchRequest.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Trạng thái không hợp lệ: " + searchRequest.getStatus());
            }
        }

        // Tạo Pageable cho sắp xếp và phân trang
        Sort sort = Sort.by(
                searchRequest.getSortDirection().equalsIgnoreCase("desc") ?
                        Sort.Direction.DESC : Sort.Direction.ASC,
                getSortField(searchRequest.getSortBy())
        );
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);

        // Lấy data với phân trang từ DB
        Page<Task> taskPage;
        if (!searchRequest.hasAnyFilter()) {
            taskPage = taskRepository.findAll(pageable);
        } else {
            taskPage = taskRepository.searchTasksUnified(
                    searchRequest.getName(),
                    status,
                    searchRequest.getGroupId(),
                    searchRequest.getAssigneeId(),
                    searchRequest.getSearchDate(),
                    searchRequest.getStartDate(),
                    searchRequest.getEndDate(),
                    pageable
            );
        }

        // Lọc theo quyền và convert sang TaskResponse
        return taskPage.getContent().stream()
                .filter(task -> permissionService.canViewBoard(user.getId(), task.getGroup().getBoard().getId()))
                .map(TaskResponse::new)
                .collect(Collectors.toList());
    }

    // Helper method để map sort field với đúng tên field trong entity
    private String getSortField(String sortBy) {
        Map<String, String> sortFieldMap = new HashMap<>();
        sortFieldMap.put("id", "id");
        sortFieldMap.put("name", "name");
        sortFieldMap.put("status", "status");
        sortFieldMap.put("dueDate", "dueDate");
        sortFieldMap.put("timelineStart", "timelineStart");
        sortFieldMap.put("timelineEnd", "timelineEnd");
        sortFieldMap.put("notes", "notes");
        sortFieldMap.put("groupName", "group.name");  // Đúng với @ManyToOne field "group"
        sortFieldMap.put("groupId", "group.id");
        sortFieldMap.put("assigneeName", "assignee.name"); // Nếu có assignee
        sortFieldMap.put("assigneeId", "assignee.id");

        return sortFieldMap.getOrDefault(sortBy, "id");
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
