package com.example.workmanager.controller;

import com.example.workmanager.dto.request.ChangePasswordRequest;
import com.example.workmanager.dto.request.LoginRequest;
import com.example.workmanager.dto.request.RegisterRequest;
import com.example.workmanager.dto.request.UpdateProfileRequest;
import com.example.workmanager.dto.response.UserResponse;
import com.example.workmanager.exceptions.TokenRefreshException;
import com.example.workmanager.model.CustomUserDetails;
import com.example.workmanager.model.User;
import com.example.workmanager.service.AuthService;
import com.example.workmanager.config.JwtUtil;
import com.example.workmanager.service.CookieService;
import com.example.workmanager.service.CustomUserDetailsService;
import com.example.workmanager.util.PermissionValidator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
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
                                   HttpServletResponse response) {
        Map<String, Object> result = authService.login(loginRequest);

        UserResponse userResponse = (UserResponse) result.get("user");
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(userResponse.getEmail());

        String accessToken = jwtUtil.generateAccessToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        cookieService.setAuthCookie(response, accessToken);
        cookieService.setRefreshCookie(response, refreshToken);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request,
                                          HttpServletResponse response) {
        String refreshToken = cookieService.getCookieValue(request, "refresh_token");

        if (refreshToken == null) {
            throw new TokenRefreshException("Refresh token is missing",
                    HttpStatus.UNAUTHORIZED,
                    "REFRESH_TOKEN_MISSING");
        }

        String username = jwtUtil.extractUsername(refreshToken);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

        if (!jwtUtil.isRefreshTokenValid(refreshToken, userDetails)) {
            throw new TokenRefreshException("Invalid refresh token",
                    HttpStatus.UNAUTHORIZED,
                    "INVALID_REFRESH_TOKEN");
        }

        String newAccessToken = jwtUtil.generateAccessToken(userDetails);
        cookieService.setAuthCookie(response, newAccessToken);

        return ResponseEntity.ok("Access token refreshed successfully!");
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        Map<String, Object> response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        cookieService.clearAuthCookie(response);
        cookieService.clearRefreshCookie(response);
        return ResponseEntity.ok("Logged out successfully!");
    }
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);
        User user = userDetails.getUser();

        return ResponseEntity.ok(new UserResponse(user));
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