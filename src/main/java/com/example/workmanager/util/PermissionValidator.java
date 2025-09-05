package com.example.workmanager.util;

import com.example.workmanager.exceptions.CustomAuthenticationException;
import com.example.workmanager.exceptions.PermissionDeniedException;
import com.example.workmanager.model.CustomUserDetails;
import com.example.workmanager.model.Task;
import com.example.workmanager.service.PermissionFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PermissionValidator {

    private final PermissionFacade permissionFacade;

    public void validateUserAuthentication(CustomUserDetails userDetails) {
        if (userDetails == null) {
            throw new CustomAuthenticationException("Unauthorized", HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
    }

    // Board Permissions
    public void validateBoardViewPermission(Integer userId, Integer boardId) {
        if (!permissionFacade.canViewBoard(userId, boardId)) {
            throw new PermissionDeniedException("Bạn không có quyền xem board này!", "VIEW_BOARD");
        }
    }

    public void validateBoardEditPermission(Integer userId, Integer boardId) {
        if (!permissionFacade.canEditBoardInfo(userId, boardId)) {
            throw new PermissionDeniedException("Bạn không có quyền chỉnh sửa board này!", "EDIT_BOARD");
        }
    }

    public void validateBoardManagePermission(Integer userId, Integer boardId) {
        if (!permissionFacade.canManageBoard(userId, boardId)) {
            throw new PermissionDeniedException("Bạn không có quyền quản lý board này!", "MANAGE_BOARD");
        }
    }

    public void validateBoardUserManagePermission(Integer userId, Integer boardId) {
        if (!permissionFacade.canManageBoardUsers(userId, boardId)) {
            throw new PermissionDeniedException("Bạn không có quyền phân quyền trên board này!", "MANAGE_BOARD_USERS");
        }
    }

    // Group Permissions
    public void validateGroupViewPermission(Integer userId, Integer boardId) {
        if (!permissionFacade.canViewGroup(userId, boardId)) {
            throw new PermissionDeniedException("Bạn không có quyền xem group này!", "VIEW_GROUP");
        }
    }

    public void validateGroupManagePermission(Integer userId, Integer boardId) {
        if (!permissionFacade.canManageGroup(userId, boardId)) {
            throw new PermissionDeniedException("Bạn không có quyền quản lý group này!", "MANAGE_GROUP");
        }
    }

    public void validateGroupEditPermission(Integer userId, Integer boardId) {
        if (!permissionFacade.canEditGroup(userId, boardId)) {
            throw new PermissionDeniedException("Bạn không có quyền chỉnh sửa group này!", "EDIT_GROUP");
        }
    }

    public void validateGroupDeletePermission(Integer userId, Integer boardId) {
        if (!permissionFacade.canDeleteGroup(userId, boardId)) {
            throw new PermissionDeniedException("Bạn không có quyền xóa group này!", "DELETE_GROUP");
        }
    }

    // Task Permissions
    public void validateTaskCreatePermission(Integer userId, Integer boardId) {
        if (!permissionFacade.canCreateTask(userId, boardId)) {
            throw new PermissionDeniedException("Bạn không có quyền tạo task trong board này!", "CREATE_TASK");
        }
    }

    public void validateTaskEditPermission(Integer userId, Integer boardId, Task task) {
        if (!permissionFacade.canEditTask(userId, boardId, task)) {
            throw new PermissionDeniedException("Bạn không có quyền sửa task này!", "EDIT_TASK");
        }
    }

    public void validateTaskDeletePermission(Integer userId, Integer boardId) {
        if (!permissionFacade.canDeleteTask(userId, boardId)) {
            throw new PermissionDeniedException("Bạn không có quyền xóa task này!", "DELETE_TASK");
        }
    }

    public void validateTaskViewPermission(Integer userId, Integer boardId) {
        if (!permissionFacade.canViewTask(userId, boardId)) {
            throw new PermissionDeniedException("Bạn không có quyền xem task này!", "VIEW_TASK");
        }
    }

    public void validateBoardUserManagePermissions(Integer userId, Integer boardId) {
        if (!permissionFacade.canManageBoardUsers(userId, boardId)) {
            throw new PermissionDeniedException("Bạn không có quyền quản lý user trong board này!", "MANAGE_BOARDS_USER");
        }
    }

    public void validateSystemAdminPermissions(boolean isSystemAdmin) {
        if (!isSystemAdmin) {
            throw new PermissionDeniedException("Chỉ có admin hệ thống mới có quyền thực hiện hành động này!", "REQUIRE_SYSTEM_ADMIN");
        }
    }
}