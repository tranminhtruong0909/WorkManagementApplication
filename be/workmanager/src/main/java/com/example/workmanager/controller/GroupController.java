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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteGroup(@PathVariable Integer id) {
        try {
            groupService.deleteGroup(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/tasks")
    public ResponseEntity<List<TaskResponse>> getTasksByGroupId(@PathVariable Integer id) {
        List<TaskResponse> tasks = taskService.getTasksByGroupId(id);
        return ResponseEntity.ok(tasks);
    }
}