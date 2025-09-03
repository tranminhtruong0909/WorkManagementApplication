package com.example.workmanager.controller;

import com.example.workmanager.dto.request.ChangePasswordRequest;
import com.example.workmanager.dto.request.LoginRequest;
import com.example.workmanager.dto.request.RegisterRequest;
import com.example.workmanager.dto.request.UpdateProfileRequest;
import com.example.workmanager.dto.response.UserResponse;
import com.example.workmanager.model.CustomUserDetails;
import com.example.workmanager.service.AuthService;
import com.example.workmanager.config.JwtUtil;
import com.example.workmanager.service.CookieService;
import com.example.workmanager.service.CustomUserDetailsService;
import com.example.workmanager.util.PermissionValidator;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5175", allowCredentials = "true")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;
    private final PermissionValidator permissionValidator;
    private final CookieService cookieService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest,
                                   HttpServletResponse httpServletResponse) {
        Map<String, Object> response = authService.login(loginRequest);

        UserResponse userResponse = (UserResponse) response.get("user");
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(userResponse.getEmail());

        String token = jwtUtil.generateToken(userDetails);
        cookieService.setAuthCookie(httpServletResponse, token);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        Map<String, Object> response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse httpServletResponse) {
        cookieService.clearAuthCookie(httpServletResponse);

        return ResponseEntity.ok(Map.of(
                "message", "Đăng xuất thành công! Cookie đã được xóa!"
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);
        return ResponseEntity.ok(authService.getUserResponse(userDetails.getUser()));
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateCurrentUser(@RequestBody UpdateProfileRequest request,
                                               @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        Map<String, Object> response = authService.updateProfile(userDetails.getUser(), request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        Map<String, Object> response = authService.changePassword(userDetails.getUser(), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/test-auth")
    public ResponseEntity<?> testAuth(@AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "authenticated", false,
                    "message", "No authentication found"
            ));
        }

        return ResponseEntity.ok(Map.of(
                "authenticated", true,
                "userId", userDetails.getUser().getId(),
                "name", userDetails.getUser().getName(),
                "email", userDetails.getUser().getEmail()
        ));
    }

    @GetMapping("/my-permissions")
    public ResponseEntity<?> getMyPermissions(@AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        Map<String, Object> response = authService.getUserPermissions(userDetails.getUser().getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/all-users-permissions")
    public ResponseEntity<?> getAllUsersPermissions(@AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);
        permissionValidator.validateSystemAdminPermissions(userDetails.getUser().isSystemAdmin());

        Map<String, Object> response = authService.getAllUsersPermissions();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user-permissions/{userId}")
    public ResponseEntity<?> getUserPermissions(@PathVariable Integer userId,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);
        permissionValidator.validateSystemAdminPermissions(userDetails.getUser().isSystemAdmin());

        Map<String, Object> response = authService.getUserPermissions(userId);
        return ResponseEntity.ok(response);
    }

}