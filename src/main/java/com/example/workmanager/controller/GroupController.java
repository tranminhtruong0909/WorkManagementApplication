package com.example.workmanager.controller;

import com.example.workmanager.dto.GroupResponse;
import com.example.workmanager.dto.TaskResponse;
import com.example.workmanager.model.CustomUserDetails;
import com.example.workmanager.model.Group;
import com.example.workmanager.model.User;
import com.example.workmanager.service.GroupService;
import com.example.workmanager.service.PermissionService;
import com.example.workmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GroupController {

    private final GroupService groupService;
    private final TaskService taskService;
    private final PermissionService permissionService;

    // ✅ Tạo group mới
    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody Map<String, Object> request,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.getUser();
            String name = (String) request.get("name");
            Object boardIdObj = request.get("boardId");

            if (name == null || boardIdObj == null) {
                return ResponseEntity.badRequest().body("Missing 'name' or 'boardId'");
            }

            Integer boardId;
            if (boardIdObj instanceof Integer) {
                boardId = (Integer) boardIdObj;
            } else if (boardIdObj instanceof Number) {
                boardId = ((Number) boardIdObj).intValue();
            } else {
                return ResponseEntity.badRequest().body("Invalid 'boardId' format");
            }

            if (!permissionService.canManageBoard(user.getId(), boardId)) {
                return ResponseEntity.status(403).body("Bạn không có quyền thực hiện chức năng này!");
            }

            Group group = groupService.createGroup(name, boardId);
            return ResponseEntity.ok(new GroupResponse(group));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // ✅ Lấy tất cả group
    @GetMapping
    public ResponseEntity<List<GroupResponse>> getAllGroups(@AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        List<Group> groups = groupService.getAllGroups();
        List<GroupResponse> responses = groups.stream()
                .filter(group -> permissionService.canViewBoard(user.getId(), group.getBoard().getId()))
                .map(GroupResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // ✅ Lấy group theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getGroupById(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        var groupOpt = groupService.getGroupById(id);
        if (groupOpt.isEmpty()) return ResponseEntity.notFound().build();
        Group group = groupOpt.get();
        Integer boardId = group.getBoard().getId();
        if (!permissionService.canViewBoard(user.getId(), boardId)) {
            return ResponseEntity.status(403).body("Bạn không có quyền thực hiện chức năng này!");
        }
        return ResponseEntity.ok(new GroupResponse(group));
    }

    // ✅ Cập nhật group
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGroup(@PathVariable Integer id, @RequestBody Map<String, Object> request,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.getUser();
            Group group = groupService.getGroupById(id).orElse(null);
            if (group == null) return ResponseEntity.notFound().build();
            Integer boardId = group.getBoard().getId();
            if (!permissionService.canManageBoard(user.getId(), boardId)) {
                return ResponseEntity.status(403).body("Bạn không có quyền thực hiện chức năng này!");
            }
            String name = (String) request.get("name");
            if (name == null) {
                return ResponseEntity.badRequest().body("Missing 'name'");
            }

            Group updatedGroup = groupService.updateGroup(id, name);
            return ResponseEntity.ok(new GroupResponse(updatedGroup));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ Xoá group
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable Integer id,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.getUser();
            Group group = groupService.getGroupById(id).orElse(null);
            if (group == null) return ResponseEntity.notFound().build();
            Integer boardId = group.getBoard().getId();
            if (!permissionService.canManageBoard(user.getId(), boardId)) {
                return ResponseEntity.status(403).body("Bạn không có quyền thực hiện chức năng này!");
            }
            groupService.deleteGroup(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();

        }
    }

    // ✅ Lấy danh sách task trong group
    @GetMapping("/{id}/tasks")
    public ResponseEntity<?> getTasksByGroupId(@PathVariable Integer id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        var groupOpt = groupService.getGroupById(id);
        if (groupOpt.isEmpty()) return ResponseEntity.notFound().build();
        Group group = groupOpt.get();
        Integer boardId = group.getBoard().getId();
        if (!permissionService.canViewBoard(user.getId(), boardId)) {
            return ResponseEntity.status(403).body("Bạn không có quyền thực hiện chức năng này!");
        }
        List<TaskResponse> tasks = taskService.getTasksByGroupId(id);
        return ResponseEntity.ok(tasks);
    }
}
