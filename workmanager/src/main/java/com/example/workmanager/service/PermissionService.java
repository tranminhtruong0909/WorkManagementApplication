package com.example.workmanager.service;

import com.example.workmanager.model.*;
import com.example.workmanager.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    @Autowired
    private UserRoleRepository userRoleRepository;

    public Role getUserRoleInBoard(Long userId, Long boardId) {
        return userRoleRepository.findByUserIdAndBoardId(userId, boardId)
                .map(UserRole::getRole)
                .orElse(null);
    }

    public boolean canEditTask(Long userId, Task task) {
        Long boardId = task.getGroup().getBoard().getId().longValue(); // ép kiểu rõ ràng
        Role role = getUserRoleInBoard(userId, boardId);

        return role == Role.ADMIN ||
                role == Role.MANAGER ||
                (role == Role.MEMBER && task.getAssignee().getId().longValue() == userId);
    }


    public boolean canManageBoard(Long userId, Long boardId) {
        Role role = getUserRoleInBoard(userId, boardId);
        return role == Role.ADMIN || role == Role.MANAGER;
    }

    public boolean canViewBoard(Long userId, Long boardId) {
        return getUserRoleInBoard(userId, boardId) != null;
    }
}
