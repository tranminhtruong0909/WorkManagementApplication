package com.example.workmanager.service.permission;

import com.example.workmanager.model.Role;
import com.example.workmanager.model.Task;
import com.example.workmanager.model.User;
import com.example.workmanager.model.UserRole;
import com.example.workmanager.repository.UserRepository;
import com.example.workmanager.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TaskPermissionService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    private boolean isSystemAdmin(Integer userId) {
        return userRepository.findById(userId)
                .map(User::isSystemAdmin)
                .orElse(false);
    }

    private Role getRoleOnBoard(Integer userId, Integer boardId) {
        return userRoleRepository.findByUserIdAndBoardId(userId, boardId)
                .map(UserRole::getRole)
                .orElse(null);
    }

    public boolean canCreateTask(Integer userId, Integer boardId) {
        if (isSystemAdmin(userId)) return true;
        Role role = getRoleOnBoard(userId, boardId);
        return role == Role.MANAGER || role == Role.MEMBER;
    }

    public boolean canEditTask(Integer userId, Integer boardId, Task task) {
        if (isSystemAdmin(userId)) return true;
        Role role = getRoleOnBoard(userId, boardId);
        if (role == Role.MANAGER) return true;
        if (role == Role.MEMBER) {
            boolean isAssignee = task.getAssignees() != null &&
                    task.getAssignees().stream()
                            .anyMatch(user -> user.getId().equals(userId));
            boolean isCreator = task.getCreator() != null &&
                    task.getCreator().getId().equals(userId);
            return isAssignee || isCreator;
        }

        return false;
    }

    public boolean canDeleteTask(Integer userId, Integer boardId) {
        if (isSystemAdmin(userId)) return true;
        Role role = getRoleOnBoard(userId, boardId);
        return role == Role.MANAGER;
    }

    public boolean canCommentTask(Integer userId, Integer boardId) {
        if (isSystemAdmin(userId)) return true;
        Role role = getRoleOnBoard(userId, boardId);
        return role == Role.MANAGER || role == Role.MEMBER;
    }

    public boolean canChangeAssignee(Integer userId, Integer boardId) {
        if (isSystemAdmin(userId)) return true;
        return getRoleOnBoard(userId, boardId) == Role.MANAGER;
    }

    public boolean canChangeAssignee(Integer userId, Integer boardId, Integer targetUserId) {
        if (isSystemAdmin(userId)) return true;
        Role role = getRoleOnBoard(userId, boardId);
        if (role == Role.MANAGER) return true;
        if (role == Role.MEMBER && userId.equals(targetUserId)) {
            return true;
        }

        return false;
    }

    public boolean canViewTask(Integer userId, Integer boardId) {
        if (isSystemAdmin(userId)) return true;
        return getRoleOnBoard(userId, boardId) != null;
    }
}