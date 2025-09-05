package com.example.workmanager.controller;

import com.example.workmanager.dto.response.GroupResponse;
import com.example.workmanager.dto.response.TaskResponse;
import com.example.workmanager.exceptions.ResourceNotFoundException;
import com.example.workmanager.model.CustomUserDetails;
import com.example.workmanager.model.Group;
import com.example.workmanager.model.User;
import com.example.workmanager.service.GroupService;
import com.example.workmanager.service.TaskService;
import com.example.workmanager.service.PermissionFacade;
import com.example.workmanager.util.PermissionValidator;

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
    private final PermissionFacade permissionFacade;
    private final PermissionValidator permissionValidator;

    @PostMapping
    public ResponseEntity<GroupResponse> createGroup(@RequestBody Map<String, Object> request,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();
        String name = (String) request.get("name");
        Object boardIdObj = request.get("boardId");

        if(name == null || boardIdObj == null){
            throw new ResourceNotFoundException("Missing 'name' or 'boarId'");
        }

        Integer boarId;
        if (boardIdObj instanceof Integer) {
            boarId = (Integer) boardIdObj;
        } else if (boardIdObj instanceof Number) {
            boarId = ((Number) boardIdObj).intValue();
        } else {
            throw new ResourceNotFoundException("Invalid 'boarId' format");
        }

        permissionValidator.validateGroupManagePermission(user.getId(), boarId);

        Group group = groupService.createGroup(name, boarId);
        return ResponseEntity.ok(new GroupResponse(group));
    }

    @GetMapping
    public ResponseEntity<List<GroupResponse>> getAllGroups(@AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();
        List<Group> groups = groupService.getAllGroup();

        List<GroupResponse> responses = groups.stream()
                .filter(group -> {
                    try {
                        permissionValidator.validateGroupViewPermission(user.getId(), group.getBoard().getId());
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(GroupResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> getGroupById(@PathVariable Integer groupId,
                                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();
        Group group = groupService.getGroupById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        Integer boardId = group.getBoard().getId();
        permissionValidator.validateGroupViewPermission(user.getId(), boardId);

        return ResponseEntity.ok(new GroupResponse(group));
    }

    @PutMapping("/{groupId}")
    public ResponseEntity<GroupResponse> updateGroup(@PathVariable Integer groupId,
                                                     @RequestBody Map<String, Object> request,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();
        Group group = groupService.getGroupById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        Integer boardId = group.getBoard().getId();
        permissionValidator.validateGroupEditPermission(user.getId(), boardId);

        String name = (String) request.get("name");
        if (name == null) {
            throw new ResourceNotFoundException("Missing 'name'");
        }

        Group updatedGroup = groupService.updateGroup(groupId, name);
        return ResponseEntity.ok(new GroupResponse(updatedGroup));
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<?> deleteGroup(@PathVariable Integer groupId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();
        Group group = groupService.getGroupById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        Integer boardId = group.getBoard().getId();
        permissionValidator.validateGroupDeletePermission(user.getId(), boardId);

        groupService.deleteGroup(groupId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{groupId}/tasks")
    public ResponseEntity<List<TaskResponse>> getTaskByGroupId(@PathVariable Integer groupId,
                                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();
        Group group = groupService.getGroupById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        Integer boardId = group.getBoard().getId();
        permissionValidator.validateGroupViewPermission(user.getId(), boardId);

        List<TaskResponse> tasks = taskService.getTasksByGroupId(groupId);
        return ResponseEntity.ok(tasks);
    }
}