package com.example.workmanager.service;

import com.example.workmanager.model.*;
import com.example.workmanager.repository.UserRoleRepository;
import com.example.workmanager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PermissionService {
    @Autowired
    private UserRoleRepository userRoleRepository;
    @Autowired
    private UserRepository userRepository;

    // Lấy role của user trên board (ADMIN là toàn hệ thống, các role khác theo từng board)
    public Role getRoleOnBoard(Integer userId, Integer boardId) {
        Optional<UserRole> ur = userRoleRepository.findByUserIdAndBoardId(userId, boardId);
        return ur.map(UserRole::getRole).orElse(null);
    }

    // Kiểm tra user có phải ADMIN toàn hệ thống không
    public boolean isAdmin(Integer userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.isSystemAdmin()) return true;
        // Logic cũ: kiểm tra nếu user có role ADMIN ở bất kỳ board nào
        return userRoleRepository.findAllByUserId(userId).stream()
                .anyMatch(ur -> ur.getRole() == Role.ADMIN);
    }

    // Kiểm tra user có quyền quản lý board (ADMIN hoặc MANAGER trên board đó)
    public boolean canManageBoard(Integer userId, Integer boardId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.isSystemAdmin()) return true;
        Role role = getRoleOnBoard(userId, boardId);
        return role == Role.ADMIN || role == Role.MANAGER;
    }

    // Kiểm tra user có quyền chỉnh sửa task (ADMIN, MANAGER hoặc MEMBER được gán task)
    public boolean canEditTask(Integer userId, Integer boardId, Task task) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.isSystemAdmin()) return true;
        Role role = getRoleOnBoard(userId, boardId);
        if (role == Role.ADMIN || role == Role.MANAGER) return true;
        if (role == Role.MEMBER && task.getAssignee() != null && task.getAssignee().getId().equals(userId)) return true;
        return false;
    }

    // Kiểm tra user có quyền xem board (có bất kỳ vai trò nào trên board đó)
    public boolean canViewBoard(Integer userId, Integer boardId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.isSystemAdmin()) return true;
        Role role = getRoleOnBoard(userId, boardId);
        return role != null; // Có bất kỳ vai trò nào đều xem được
    }
}
