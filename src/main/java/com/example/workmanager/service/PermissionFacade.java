package com.example.workmanager.service;

import com.example.workmanager.model.Task;
import com.example.workmanager.service.permission.BoardPermissionService;
import com.example.workmanager.service.permission.GroupPermissionService;
import com.example.workmanager.service.permission.TaskPermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PermissionFacade {

    @Autowired
    private BoardPermissionService boardPermissionService;

    @Autowired
    private GroupPermissionService groupPermissionService;

    @Autowired
    private TaskPermissionService taskPermissionService;

    // Board Permissions
    public boolean canManageBoard(Integer userId, Integer boardId) {
        return boardPermissionService.canManageBoard(userId, boardId);
    }

    public boolean canViewBoard(Integer userId, Integer boardId) {
        return boardPermissionService.canViewBoard(userId, boardId);
    }

    public boolean canManageBoardUsers(Integer userId, Integer boardId) {
        return boardPermissionService.canManageBoardUsers(userId, boardId);
    }

    public boolean canEditBoardInfo(Integer userId, Integer boardId) {
        return boardPermissionService.canEditBoardInfo(userId, boardId);
    }

    // Group Permissions
    public boolean canManageGroup(Integer userId, Integer boardId) {
        return groupPermissionService.canManageGroup(userId, boardId);
    }

    public boolean canViewGroup(Integer userId, Integer boardId) {
        return groupPermissionService.canViewGroup(userId, boardId);
    }

    public boolean canEditGroup(Integer userId, Integer boardId) {
        return groupPermissionService.canEditGroup(userId, boardId);
    }

    public boolean canDeleteGroup(Integer userId, Integer boardId) {
        return groupPermissionService.canDeleteGroup(userId, boardId);
    }

    // Task Permissions
    public boolean canCreateTask(Integer userId, Integer boardId) {
        return taskPermissionService.canCreateTask(userId, boardId);
    }

    public boolean canEditTask(Integer userId, Integer boardId, Task task) {
        return taskPermissionService.canEditTask(userId, boardId, task);
    }

    public boolean canDeleteTask(Integer userId, Integer boardId) {
        return taskPermissionService.canDeleteTask(userId, boardId);
    }

    public boolean canCommentTask(Integer userId, Integer boardId) {
        return taskPermissionService.canCommentTask(userId, boardId);
    }

    public boolean canChangeAssignee(Integer userId, Integer boardId) {
        return taskPermissionService.canChangeAssignee(userId, boardId);
    }

    public boolean canChangeAssignee(Integer userId, Integer boardId, Integer targetUserId) {
        return taskPermissionService.canChangeAssignee(userId, boardId, targetUserId);
    }

    public boolean canViewTask(Integer userId, Integer boardId) {
        return taskPermissionService.canViewTask(userId, boardId);
    }
}