package com.example.workmanager.controller;

import com.example.workmanager.dto.TaskResponse;
import com.example.workmanager.model.Group;
import com.example.workmanager.service.GroupService;
import com.example.workmanager.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GroupController {

    private final GroupService groupService;
    private final TaskService taskService;

    // ✅ Tạo group mới
    @PostMapping
    public ResponseEntity<Group> createGroup(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            Integer boardId = (Integer) request.get("boardId");

            Group group = groupService.createGroup(name, boardId);
            return ResponseEntity.ok(group);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ✅ Lấy tất cả group
    @GetMapping
    public ResponseEntity<List<Group>> getAllGroups() {
        List<Group> groups = groupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    // ✅ Lấy group theo ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getGroupById(@PathVariable Integer id) {
        return groupService.getGroupById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Cập nhật group
    @PutMapping("/{id}")
    public ResponseEntity<Group> updateGroup(@PathVariable Integer id, @RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            Group group = groupService.updateGroup(id, name);
            return ResponseEntity.ok(group);
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
