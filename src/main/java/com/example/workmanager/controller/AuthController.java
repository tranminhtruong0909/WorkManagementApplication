package com.example.workmanager.controller;

import com.example.workmanager.dto.LoginRequest;
import com.example.workmanager.dto.RegisterRequest;
import com.example.workmanager.dto.UserResponse;
import com.example.workmanager.model.User;
import com.example.workmanager.repository.UserRepository;
import com.example.workmanager.service.AuthService;
import com.example.workmanager.config.JwtUtil;
import com.example.workmanager.service.CustomUserDetailsService;
import com.example.workmanager.service.PermissionService;
import com.example.workmanager.repository.UserRoleRepository;
import com.example.workmanager.model.UserRole;
import com.example.workmanager.model.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.workmanager.model.CustomUserDetails;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")

public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final PermissionService permissionService;
    private final UserRoleRepository userRoleRepository;

    @CrossOrigin
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            System.out.println("DEBUG: Login attempt for email: " + request.getEmail());

            // Validate input
            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Email không được để trống",
                        "errorCode", "EMPTY_EMAIL"
                ));
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Mật khẩu không được để trống",
                        "errorCode", "EMPTY_PASSWORD"
                ));
            }

            User user = authService.login(request);
            System.out.println("DEBUG: User found: " + user.getId() + " - " + user.getEmail());

            UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
            System.out.println("DEBUG: UserDetails loaded: " + userDetails.getUsername());

            String token = jwtUtil.generateToken(userDetails);
            System.out.println("DEBUG: Token generated: " + token.substring(0, Math.min(token.length(), 20)) + "...");

            // 🔍 Lấy thông tin phân quyền của user
            List<UserRole> userRoles = userRoleRepository.findAllByUserId(user.getId());
            List<Map<String, Object>> permissions = userRoles.stream()
                    .map(ur -> {
                        Map<String, Object> permission = new HashMap<>();
                        permission.put("boardId", ur.getBoard().getId());
                        permission.put("boardName", ur.getBoard().getName());
                        permission.put("role", ur.getRole().name());
                        permission.put("canManageBoard", permissionService.canManageBoard(user.getId(), ur.getBoard().getId()));
                        permission.put("canViewBoard", permissionService.canViewBoard(user.getId(), ur.getBoard().getId()));
                        return permission;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", new UserResponse(user));
            response.put("token", token);
            response.put("isSystemAdmin", user.isSystemAdmin());
            response.put("permissions", permissions);
            response.put("totalBoards", permissions.size());
            response.put("message", "Đăng nhập thành công! Bạn có " + permissions.size() + " board.");

            // 🔍 In ra thông tin phân quyền trong console
            System.out.println("🔍 USER PERMISSIONS:");
            System.out.println("User ID: " + user.getId());
            System.out.println("User Email: " + user.getEmail());
            System.out.println("Is System Admin: " + permissionService.isAdmin(user.getId()));
            System.out.println("Total Boards: " + permissions.size());

            if (permissions.isEmpty()) {
                System.out.println("⚠️ User chưa có quyền trên board nào!");
            } else {
                System.out.println("📋 Board Permissions:");
                permissions.forEach(permission -> {
                    System.out.println("  - Board: " + permission.get("boardName") +
                            " (ID: " + permission.get("boardId") + ")" +
                            " | Role: " + permission.get("role") +
                            " | Can Manage: " + permission.get("canManageBoard") +
                            " | Can View: " + permission.get("canViewBoard"));
                });
            }
            System.out.println("🔍 END PERMISSIONS");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            System.out.println("DEBUG: Login error: " + e.getMessage());

            String errorMessage = e.getMessage();
            String errorCode = "LOGIN_FAILED";

            // Phân loại lỗi cụ thể
            if (errorMessage.contains("Email không tồn tại")) {
                errorCode = "EMAIL_NOT_FOUND";
            } else if (errorMessage.contains("Mật khẩu không chính xác") || errorMessage.contains("Mật khẩu không đúng")) {
                errorCode = "INVALID_PASSWORD";
            } else if (errorMessage.contains("Tài khoản đã bị khóa")) {
                errorCode = "ACCOUNT_LOCKED";
            }

            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", errorMessage,
                    "errorCode", errorCode,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            System.out.println("DEBUG: Unexpected error: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Có lỗi hệ thống xảy ra. Vui lòng thử lại sau",
                    "errorCode", "SYSTEM_ERROR",
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            System.out.println("DEBUG: Register attempt for email: " + request.getEmail());

            // Validate input
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Tên không được để trống",
                        "errorCode", "EMPTY_NAME"
                ));
            }

            if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Email không được để trống",
                        "errorCode", "EMPTY_EMAIL"
                ));
            }

            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Mật khẩu không được để trống",
                        "errorCode", "EMPTY_PASSWORD"
                ));
            }

            if (request.getPassword().length() < 6) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Mật khẩu phải có ít nhất 6 ký tự",
                        "errorCode", "PASSWORD_TOO_SHORT"
                ));
            }

            // Validate email format (basic check)
            if (!request.getEmail().matches("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "message", "Email không hợp lệ",
                        "errorCode", "INVALID_EMAIL_FORMAT"
                ));
            }

            User user = authService.register(request);
            System.out.println("DEBUG: User registered successfully: " + user.getId() + " - " + user.getEmail());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("user", new UserResponse(user));
            response.put("message", "Đăng ký thành công! Bạn có thể đăng nhập ngay bây giờ.");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            System.out.println("DEBUG: Register error: " + e.getMessage());

            String errorMessage = e.getMessage();
            String errorCode = "REGISTER_FAILED";

            // Phân loại lỗi cụ thể
            if (errorMessage.contains("Email đã tồn tại")) {
                errorCode = "EMAIL_ALREADY_EXISTS";
            } else if (errorMessage.contains("Email không hợp lệ")) {
                errorCode = "INVALID_EMAIL";
            } else if (errorMessage.contains("Mật khẩu phải có ít nhất")) {
                errorCode = "PASSWORD_TOO_SHORT";
            } else if (errorMessage.contains("Tên không được để trống")) {
                errorCode = "EMPTY_NAME";
            }

            return ResponseEntity.status(400).body(Map.of(
                    "success", false,
                    "message", errorMessage,
                    "errorCode", errorCode,
                    "timestamp", System.currentTimeMillis()
            ));
        } catch (Exception e) {
            System.out.println("DEBUG: Unexpected register error: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "message", "Có lỗi hệ thống xảy ra. Vui lòng thử lại sau",
                    "errorCode", "SYSTEM_ERROR",
                    "timestamp", System.currentTimeMillis()
            ));
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Integer id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(new UserResponse(user)))
                .orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("No authentication found");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Đăng xuất thành công! Hãy xóa token ở phía client.");

        return ResponseEntity.ok(response);
    }


    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("No authentication found");
        }
        return ResponseEntity.ok(new UserResponse(userDetails.getUser()));
    }

    @GetMapping("/test-auth")
    public ResponseEntity<?> testAuth(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "authenticated", false,
                    "message", "No authentication token found"
            ));
        }
        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "userId", userDetails.getUser().getId(),
                "email", userDetails.getUser().getEmail(),
                "name", userDetails.getUser().getName()

        ));
    }

    // 🔍 API để xem phân quyền của user hiện tại
    @GetMapping("/my-permissions")
    public ResponseEntity<?> getMyPermissions(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("No authentication found");
        }

        User user = userDetails.getUser();
        List<UserRole> userRoles = userRoleRepository.findAllByUserId(user.getId());

        List<Map<String, Object>> permissions = userRoles.stream()
                .map(ur -> {
                    Map<String, Object> permission = new HashMap<>();
                    permission.put("boardId", ur.getBoard().getId());
                    permission.put("boardName", ur.getBoard().getName());
                    permission.put("role", ur.getRole().name());
                    permission.put("canManageBoard", permissionService.canManageBoard(user.getId(), ur.getBoard().getId()));
                    permission.put("canViewBoard", permissionService.canViewBoard(user.getId(), ur.getBoard().getId()));
                    return permission;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("userId", user.getId());
        response.put("userEmail", user.getEmail());
        response.put("userName", user.getName());
        response.put("isSystemAdmin", user.isSystemAdmin());
        response.put("permissions", permissions);
        response.put("totalBoards", permissions.size());

        return ResponseEntity.ok(response);
    }

    // 🔍 API để xem tất cả user và phân quyền (chỉ ADMIN mới xem được)
    @GetMapping("/all-users-permissions")
    public ResponseEntity<?> getAllUsersPermissions(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("No authentication found");
        }

        User currentUser = userDetails.getUser();
        if (!permissionService.isAdmin(currentUser.getId())) {
            return ResponseEntity.status(403).body("Chỉ ADMIN mới có quyền xem thông tin này");
        }

        List<User> allUsers = userRepository.findAll();
        List<Map<String, Object>> usersWithPermissions = allUsers.stream()
                .map(user -> {
                    List<UserRole> userRoles = userRoleRepository.findAllByUserId(user.getId());
                    List<Map<String, Object>> permissions = userRoles.stream()
                            .map(ur -> {
                                Map<String, Object> permission = new HashMap<>();
                                permission.put("boardId", ur.getBoard().getId());
                                permission.put("boardName", ur.getBoard().getName());
                                permission.put("role", ur.getRole().name());
                                // ✅ Thêm thông tin user vào từng quyền
                                permission.put("userName", ur.getUser().getName());
                                permission.put("userEmail", ur.getUser().getEmail());
                                return permission;
                            })
                            .collect(Collectors.toList());

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

        return ResponseEntity.ok(response);
    }


    // 🔍 API để xem user cụ thể và phân quyền (chỉ ADMIN mới xem được)
    @GetMapping("/user-permissions/{userId}")
    public ResponseEntity<?> getUserPermissions(@PathVariable Integer userId,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("No authentication found");
        }

        User currentUser = userDetails.getUser();
        if (!permissionService.isAdmin(currentUser.getId())) {
            return ResponseEntity.status(403).body("Chỉ ADMIN mới có quyền xem thông tin này");
        }

        User targetUser = userRepository.findById(userId).orElse(null);
        if (targetUser == null) {
            return ResponseEntity.status(404).body("User không tồn tại");
        }

        List<UserRole> userRoles = userRoleRepository.findAllByUserId(userId);
        List<Map<String, Object>> permissions = userRoles.stream()
                .map(ur -> {
                    Map<String, Object> permission = new HashMap<>();
                    permission.put("boardId", ur.getBoard().getId());
                    permission.put("boardName", ur.getBoard().getName());
                    permission.put("role", ur.getRole().name());
                    permission.put("canManageBoard", permissionService.canManageBoard(userId, ur.getBoard().getId()));
                    permission.put("canViewBoard", permissionService.canViewBoard(userId, ur.getBoard().getId()));
                    return permission;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("userId", targetUser.getId());
        response.put("userEmail", targetUser.getEmail());
        response.put("userName", targetUser.getName());
        response.put("isSystemAdmin", targetUser.isSystemAdmin());
        response.put("permissions", permissions);
        response.put("totalBoards", permissions.size());

        return ResponseEntity.ok(response);
    }
}