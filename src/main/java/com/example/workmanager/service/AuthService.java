package com.example.workmanager.service;

import com.example.workmanager.dto.request.ChangePasswordRequest;
import com.example.workmanager.dto.request.LoginRequest;
import com.example.workmanager.dto.request.RegisterRequest;
import com.example.workmanager.dto.request.UpdateProfileRequest;
import com.example.workmanager.dto.response.UserResponse;
import com.example.workmanager.exceptions.ValidationException;
import com.example.workmanager.model.User;
import com.example.workmanager.model.UserRole;
import com.example.workmanager.repository.UserRepository;
import com.example.workmanager.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionFacade permissionFacade;

    public UserResponse getUserResponse(User user) {
        return new UserResponse(user);
    }

    @Transactional
    public Map<String, Object> login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ValidationException("Thông tin tài khoản mật khẩu không chính xác", "INVALID_CREDENTIALS"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ValidationException("Mật khẩu không chính xác", "INVALID_PASSWORD");
        }

        List<UserRole> userRoles = userRoleRepository.findAllByUserId(user.getId());
        List<Map<String, Object>> permissions = buildUserPermissions(user.getId(), userRoles);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", new UserResponse(user));
        response.put("isSystemAdmin", user.isSystemAdmin());
        response.put("permissions", permissions);
        response.put("totalBoards", permissions.size());
        response.put("message", "Đăng nhập thành công! Bạn có " + permissions.size() + " role");

        return response;
    }

    @Transactional
    public Map<String, Object> register(RegisterRequest request) {
        validateRegisterRequest(request);

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ValidationException("Email đã tồn tại", "EMAIL_ALREADY_EXISTS");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAvatarUrl(request.getAvatarUrl());
        user.setRole("USER");

        User savedUser = userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", new UserResponse(savedUser));
        response.put("message", "Đăng ký tài khoản thành công! Bạn có thể đăng nhập ngay bây giờ");

        return response;
    }

    @Transactional
    public Map<String, Object> updateProfile(User currentUser, UpdateProfileRequest request) {
        validateUpdateProfileRequest(currentUser, request);

        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            currentUser.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            currentUser.setEmail(request.getEmail());
        }
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().trim().isEmpty()) {
            currentUser.setAvatarUrl(request.getAvatarUrl());
        }

        User updatedUser = userRepository.save(currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("user", new UserResponse(updatedUser));
        response.put("message", "Cập nhật thông tin cá nhân thành công!");

        return response;
    }

    @Transactional
    public Map<String, Object> changePassword(User currentUser, ChangePasswordRequest request) {
        validateChangePasswordRequest(currentUser, request);

        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(currentUser);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Đổi mật khẩu thành công!");

        return response;
    }

    public Map<String, Object> getUserPermissions(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ValidationException("User không tồn tại", "USER_NOT_FOUND"));

        List<UserRole> userRoles = userRoleRepository.findAllByUserId(userId);
        List<Map<String, Object>> permissions = buildUserPermissions(userId, userRoles);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("userEmail", user.getEmail());
        response.put("userName", user.getName());
        response.put("isSystemAdmin", user.isSystemAdmin());
        response.put("permissions", permissions);
        response.put("totalBoards", permissions.size());

        return response;
    }

    public Map<String, Object> getAllUsersPermissions() {
        List<User> allUsers = userRepository.findAll();
        List<Map<String, Object>> usersWithPermissions = allUsers.stream()
                .map(user -> {
                    List<UserRole> userRoles = userRoleRepository.findAllByUserId(user.getId());
                    List<Map<String, Object>> permissions = buildUserPermissions(user.getId(), userRoles);

                    Map<String, Object> userInfo = new HashMap<>();
                    userInfo.put("userId", user.getId());
                    userInfo.put("userEmail", user.getEmail());
                    userInfo.put("userName", user.getName());
                    userInfo.put("isSystemAdmin", user.isSystemAdmin());
                    userInfo.put("permissions", permissions);
                    userInfo.put("totalBoards", permissions.size());
                    return userInfo;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("totalUsers", allUsers.size());
        response.put("users", usersWithPermissions);

        return response;
    }

    private List<Map<String, Object>> buildUserPermissions(Integer userId, List<UserRole> userRoles) {
        return userRoles.stream()
                .map(ur -> {
                    Map<String, Object> permission = new HashMap<>();
                    permission.put("boardId", ur.getBoard().getId());
                    permission.put("boardName", ur.getBoard().getName());
                    permission.put("role", ur.getRole().name());

                    boolean isAdmin = "ADMIN".equalsIgnoreCase(ur.getRole().name());
                    boolean isMember = "MEMBER".equalsIgnoreCase(ur.getRole().name());
                    boolean canView = isAdmin || isMember;

                    permission.put("canManageBoard", isAdmin);
                    permission.put("canViewBoard", canView);
                    permission.put("canManageBoardUsers", isAdmin);
                    permission.put("canEditBoardInfo", isAdmin);
                    permission.put("canManageGroup", isAdmin);
                    permission.put("canViewGroup", canView);
                    permission.put("canCreateTask", canView);
                    permission.put("canViewTask", canView);

                    return permission;
                })
                .collect(Collectors.toList());
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (request.getName() == null || request.getName().isEmpty()) {
            throw new ValidationException("Tên không được để trống", "EMPTY_NAME");
        }
        if (request.getEmail() == null || request.getEmail().isEmpty()) {
            throw new ValidationException("Email không được để trống", "EMPTY_EMAIL");
        }
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new ValidationException("Mật khẩu không được để trống", "EMPTY_PASSWORD");
        }
        if (request.getPassword().length() < 6) {
            throw new ValidationException("Mật khẩu phải có ít nhất 6 ký tự", "PASSWORD_TOO_SHORT");
        }
        if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
            throw new ValidationException("Email không hợp lệ", "INVALID_EMAIL_FORMAT");
        }
    }

    private void validateUpdateProfileRequest(User currentUser, UpdateProfileRequest request) {
        if (request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new ValidationException("Vui lòng nhập mật khẩu để xác thực", "PASSWORD_REQUIRED");
        }

        if (!passwordEncoder.matches(request.getPassword(), currentUser.getPassword())) {
            throw new ValidationException("Mật khẩu không đúng", "INVALID_PASSWORD");
        }

        if (request.getName() != null && request.getName().isEmpty()) {
            throw new ValidationException("Tên không được để trống", "EMPTY_NAME");
        }

        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String newEmail = request.getEmail().trim();

            if (!newEmail.equals(currentUser.getEmail())) {
                if (!newEmail.matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
                    throw new ValidationException("Email không hợp lệ", "INVALID_EMAIL_FORMAT");
                }
                if (userRepository.existsByEmailAndIdNot(newEmail, currentUser.getId())) {
                    throw new ValidationException("Email đã được tài khoản khác sử dụng", "EMAIL_ALREADY_EXISTS");
                }
            }
        }
    }

    private void validateChangePasswordRequest(User currentUser, ChangePasswordRequest request) {
        if (request.getCurrentPassword() == null || request.getCurrentPassword().isEmpty()) {
            throw new ValidationException("Vui lòng nhập mật khẩu hiện tại", "CURRENT_PASSWORD_REQUIRED");
        }
        if (request.getNewPassword() == null || request.getNewPassword().isEmpty()) {
            throw new ValidationException("Vui lòng nhập mật khẩu mới", "NEW_PASSWORD_REQUIRED");
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new ValidationException("Mật khẩu hiện tại không đúng", "INVALID_CURRENT_PASSWORD");
        }
        if (request.getNewPassword().length() < 6) {
            throw new ValidationException("Mật khẩu phải có ít nhất 6 ký tự", "NEW_PASSWORD_TOO_SHORT");
        }
        if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
            throw new ValidationException("Mật khẩu mới phải khác mật khẩu hiện tại", "SAME_PASSWORD");
        }
    }
}