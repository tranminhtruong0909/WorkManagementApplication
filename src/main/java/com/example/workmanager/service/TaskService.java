package com.example.workmanager.service;

import com.example.workmanager.dto.response.TaskResponse;
import com.example.workmanager.dto.request.TaskSearchRequest;
import com.example.workmanager.model.*;
import com.example.workmanager.repository.*;
import com.example.workmanager.repository.specification.TaskSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepo;
    private final BoardRepository boardRepository;
    private final PermissionFacade permissionFacade;

    public List<Task> getAllTaskEntities() {
        return taskRepository.findAll();
    }

    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
    }

    public Optional<Task> getTaskEntityById(Integer id) {
        return taskRepository.findById(id);
    }

    public long countTasksUnified(TaskSearchRequest searchRequest, User user) {
        TaskStatus status = convertStringToTaskStatus(searchRequest.getStatus());

        Specification<Task> spec = TaskSpecification.searchTasks(
                searchRequest.getName(),
                status,
                searchRequest.getGroupId(),
                searchRequest.getAssigneeId(),
                searchRequest.getSearchDate(),
                searchRequest.getStartDate(),
                searchRequest.getEndDate()
        );

        return taskRepository.count(spec);
    }

    public Page<TaskResponse> searchTasksUnified(TaskSearchRequest searchRequest, User user) {
        TaskStatus status = convertStringToTaskStatus(searchRequest.getStatus());

        Sort sort = Sort.by(
                searchRequest.getSortDirection().equalsIgnoreCase("desc")
                        ? Sort.Direction.DESC : Sort.Direction.ASC,
                getSortField(searchRequest.getSortBy())
        );
        Pageable pageable = PageRequest.of(searchRequest.getPage(), searchRequest.getSize(), sort);

        Specification<Task> spec = TaskSpecification.searchTasks(
                searchRequest.getName(),
                status,
                searchRequest.getGroupId(),
                searchRequest.getAssigneeId(),
                searchRequest.getSearchDate(),
                searchRequest.getStartDate(),
                searchRequest.getEndDate()
        );

        Page<Task> taskPage = taskRepository.findAll(spec, pageable);

        return taskPage.map(TaskResponse::new);
    }

    private TaskStatus convertStringToTaskStatus(String statusStr) {
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            try {
                return TaskStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        return null;
    }

    private String getSortField(String sortBy) {
        Map<String, String> sortFieldMap = new HashMap<>();
        sortFieldMap.put("id", "id");
        sortFieldMap.put("name", "name");
        sortFieldMap.put("status", "status");
        sortFieldMap.put("dueDate", "dueDate");
        sortFieldMap.put("timelineStart", "timelineStart");
        sortFieldMap.put("timelineEnd", "timelineEnd");
        sortFieldMap.put("notes", "notes");
        sortFieldMap.put("groupName", "group.name");
        sortFieldMap.put("groupId", "group.id");
        sortFieldMap.put("assigneeName", "assignee.name");
        sortFieldMap.put("assigneeId", "assignee.id");

        return sortFieldMap.getOrDefault(sortBy, "id");
    }

    public Task updateTask(Integer taskId, String status, String notes, User user) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy task"));

        Integer boardId = task.getGroup().getBoard().getId();
        if (!permissionFacade.canEditTask(user.getId(), boardId, task)) {
            throw new RuntimeException("Bạn không có quyền cập nhật task này");
        }

        if (status != null) task.setStatus(TaskStatus.valueOf(status.toUpperCase()));
        if (notes != null) task.setNotes(notes);

        return taskRepository.save(task);
    }

    public Task updateTaskFullWithPermission(Integer id, String name, String status,
                                             LocalDate dueDate, LocalDate timelineStart,
                                             LocalDate timelineEnd, String notes, User user) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task không tồn tại"));

        Integer boardId = task.getGroup().getBoard().getId();
        if (!permissionFacade.canEditTask(user.getId(), boardId, task)) {
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

    public void deleteTask(Integer id) {
        taskRepository.deleteById(id);
    }

    public List<TaskResponse> getTasksByGroupId(Integer groupId) {
        return taskRepository.findByGroupId(groupId)
                .stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public void assignMemberRoleIfNeeded(Integer userId, Integer boardId) {
        boolean hasRole = userRoleRepo.findByUserIdAndBoardId(userId, boardId).isPresent();

        if (!hasRole) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            Board board = boardRepository.findById(boardId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy board"));

            UserRole memberRole = new UserRole();
            memberRole.setUser(user);
            memberRole.setBoard(board);
            memberRole.setRole(Role.MEMBER);
            userRoleRepo.save(memberRole);
        }
    }


    @Transactional
    public Task addAssigneeToTask(Integer taskId, Integer userId, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy task"));

        User userToAdd = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        Integer boardId = task.getGroup().getBoard().getId();

        assignMemberRoleIfNeeded(userId, boardId);

        if (!permissionFacade.canChangeAssignee(currentUser.getId(), boardId, userId)) {
            throw new RuntimeException("Bạn không có quyền thêm assignee này");
        }

        if (!task.getAssignees().contains(userToAdd)) {
            task.getAssignees().add(userToAdd);
            return taskRepository.save(task);
        }

        return task;
    }

    @Transactional
    public Task addMultipleAssigneesToTask(Integer taskId, List<Integer> userIds, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy task"));

        Integer boardId = task.getGroup().getBoard().getId();

        for (Integer userId : userIds) {
            User userToAdd = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));

            assignMemberRoleIfNeeded(userId, boardId);

            if (!permissionFacade.canChangeAssignee(currentUser.getId(), boardId, userId)) {
                throw new RuntimeException("Bạn không có quyền thêm assignee với ID: " + userId);
            }

            if (!task.getAssignees().contains(userToAdd)) {
                task.getAssignees().add(userToAdd);
            }
        }
        return taskRepository.save(task);
    }

    @Transactional
    public Task removeAssigneeFromTask(Integer taskId, Integer userId, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy task"));

        User userToRemove = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        Integer boardId = task.getGroup().getBoard().getId();

        if (!permissionFacade.canChangeAssignee(currentUser.getId(), boardId, userId)) {
            throw new RuntimeException("Bạn không có quyền xoá assignee này");
        }

        task.getAssignees().remove(userToRemove);
        return taskRepository.save(task);
    }

    public List<User> getTaskAssignees(Integer taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy task"));
        return task.getAssignees();
    }

    @Transactional
    public Task createTask(String name, Integer groupId, String status,
                           LocalDate dueDate, LocalDate timelineStart,
                           LocalDate timelineEnd, String notes, List<Integer> assigneeIds, User creator) {

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
        task.setCreator(creator);

        Integer boardId = group.getBoard().getId();

        if (assigneeIds != null && !assigneeIds.isEmpty()) {
            List<User> assignees = userRepository.findAllById(assigneeIds);
            for (User assignee : assignees) {
                assignMemberRoleIfNeeded(assignee.getId(), boardId);
            }
            task.setAssignees(assignees);
        }

        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTaskFullWithPermission(Integer id, String name, String status,
                                             LocalDate dueDate, LocalDate timelineStart,
                                             LocalDate timelineEnd, String notes,
                                             List<Integer> assigneeIds, User user) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task không tồn tại"));

        Integer boardId = task.getGroup().getBoard().getId();
        if (!permissionFacade.canEditTask(user.getId(), boardId, task)) {
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

        if (assigneeIds != null) {
            List<User> assignees = userRepository.findAllById(assigneeIds);
            for (User assignee : assignees) {
                assignMemberRoleIfNeeded(assignee.getId(), boardId);
            }
            task.setAssignees(assignees);
        }

        return taskRepository.save(task);
    }

    @Transactional
    public Task updateTaskAssignees(Integer taskId, List<Integer> assigneeIds, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy task"));

        Integer boardId = task.getGroup().getBoard().getId();

        if (!permissionFacade.canChangeAssignee(currentUser.getId(), boardId)) {
            throw new RuntimeException("Bạn không có quyền quản lý assignees của task này");
        }

        if (assigneeIds != null) {
            for (Integer userId : assigneeIds) {
                assignMemberRoleIfNeeded(userId, boardId);
            }
        }

        List<User> assignees = userRepository.findAllById(assigneeIds);
        task.setAssignees(assignees);

        return taskRepository.save(task);
    }

    public boolean isUserAssignee(Integer taskId, Integer userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy task"));

        return task.getAssignees().stream()
                .anyMatch(user -> user.getId().equals(userId));
    }

    public List<TaskResponse> getTasksByAssignee(Integer userId) {
        return taskRepository.findByAssigneesId(userId)
                .stream()
                .map(TaskResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
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


}