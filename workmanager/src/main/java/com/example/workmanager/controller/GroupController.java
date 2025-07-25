package com.example.workmanager.controller;

import com.example.workmanager.dto.GroupResponse;
import com.example.workmanager.dto.TaskResponse;
import com.example.workmanager.model.Group;
import com.example.workmanager.service.GroupService;
import com.example.workmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // ✅ Tạo group mới
    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody Map<String, Object> request) {
        try {
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

            Group group = groupService.createGroup(name, boardId);
            return ResponseEntity.ok(new GroupResponse(group));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // ✅ Lấy tất cả group
    @GetMapping
    public ResponseEntity<List<GroupResponse>> getAllGroups() {
        List<Group> groups = groupService.getAllGroups();
        List<GroupResponse> responses = groups.stream()
                .map(GroupResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    // ✅ Lấy group theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getGroupById(@PathVariable Integer id) {
        return groupService.getGroupById(id)
                .map(group -> ResponseEntity.ok(new GroupResponse(group)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Cập nhật group
    @PutMapping("/{id}")
    public ResponseEntity<?> updateGroup(@PathVariable Integer id, @RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            if (name == null) {
                return ResponseEntity.badRequest().body("Missing 'name'");
            }

            Group group = groupService.updateGroup(id, name);
            return ResponseEntity.ok(new GroupResponse(group));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ Xoá group
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable Integer id) {
        try {
            groupService.deleteGroup(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();

        }
    }

    // ✅ Lấy danh sách task trong group
    @GetMapping("/{id}/tasks")
    public ResponseEntity<List<TaskResponse>> getTasksByGroupId(@PathVariable Integer id) {
        List<TaskResponse> tasks = taskService.getTasksByGroupId(id);
        return ResponseEntity.ok(tasks);
    }
}
