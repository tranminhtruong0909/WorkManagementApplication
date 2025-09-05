package com.example.workmanager.service.permission;

import com.example.workmanager.model.Role;
import com.example.workmanager.model.User;
import com.example.workmanager.model.UserRole;
import com.example.workmanager.repository.UserRepository;
import com.example.workmanager.repository.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BoardPermissionService {

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

    public boolean canManageBoard(Integer userId, Integer boardId) {
        if (isSystemAdmin(userId)) return true;
        return getRoleOnBoard(userId, boardId) == Role.MANAGER;
    }

    public boolean canViewBoard(Integer userId, Integer boardId) {
        if (isSystemAdmin(userId)) return true;
        return getRoleOnBoard(userId, boardId) != null;
    }

    public boolean canManageBoardUsers(Integer userId, Integer boardId) {
        if (isSystemAdmin(userId)) return true;
        return getRoleOnBoard(userId, boardId) == Role.MANAGER;
    }

    public boolean canEditBoardInfo(Integer userId, Integer boardId) {
        return canManageBoard(userId, boardId);
    }
}