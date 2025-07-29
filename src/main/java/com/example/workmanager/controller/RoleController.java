package com.example.workmanager.controller;

import com.example.workmanager.model.Board;
import com.example.workmanager.model.Role;
import com.example.workmanager.model.User;
import com.example.workmanager.model.UserRole;
import com.example.workmanager.repository.BoardRepository;
import com.example.workmanager.repository.UserRepository;
import com.example.workmanager.repository.UserRoleRepository;
import com.example.workmanager.model.CustomUserDetails;
import com.example.workmanager.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RoleController {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final BoardRepository boardRepository;
    private final PermissionService permissionService;

    // ✅ Lấy role của user trong 1 board
    @GetMapping("/user/{userId}/board/{boardId}")
    public ResponseEntity<?> getUserRoleInBoard(@PathVariable Integer userId,
                                                @PathVariable Integer boardId,
                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        // Chỉ ADMIN hoặc MANAGER board mới được xem role
        if (!permissionService.canManageBoard(user.getId(), boardId)) {
            return ResponseEntity.status(403).body("Bạn không có quyền thực hiện chức năng này!");
        }
        return userRoleRepository.findByUserIdAndBoardId(userId, boardId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Lấy danh sách tất cả user và vai trò trong 1 board
    @GetMapping("/board/{boardId}")
    public ResponseEntity<?> getAllUsersInBoard(@PathVariable Integer boardId,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        if (!permissionService.canManageBoard(user.getId(), boardId)) {
            return ResponseEntity.status(403).body("Bạn không có quyền thực hiện chức năng này!");
        }
        List<UserRole> roles = userRoleRepository.findAllByBoardId(boardId);
        return ResponseEntity.ok(roles);
    }

    // ✅ Lấy tất cả board mà user tham gia
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getAllBoardsOfUser(@PathVariable Integer userId,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        // Chỉ ADMIN mới được xem tất cả board của user khác
        if (!permissionService.isAdmin(user.getId()) && !user.getId().equals(userId)) {
            return ResponseEntity.status(403).body("Bạn không có quyền thực hiện chức năng này!");
        }
        List<UserRole> roles = userRoleRepository.findAllByUserId(userId);
        return ResponseEntity.ok(roles);
    }

    // ✅ Dùng để gán nhanh 1 role cho user (chỉ để test, không kiểm tra quyền)
    @PostMapping("/seed")
    public ResponseEntity<?> seedUserRole(@RequestBody Map<String, Object> req,
                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        Integer boardId = Integer.valueOf(req.get("boardId").toString());
        if (!permissionService.canManageBoard(user.getId(), boardId)) {
            return ResponseEntity.status(403).body("Bạn không có quyền thực hiện chức năng này!");
        }
        try {
            Integer userId = Integer.valueOf(req.get("userId").toString());
            String roleStr = req.get("role").toString().toUpperCase();

            Role role = Role.valueOf(roleStr);

            User targetUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            Board board = boardRepository.findById(boardId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy board"));

            // Nếu đã tồn tại thì cập nhật, không tạo mới
            Optional<UserRole> existing = userRoleRepository.findByUserIdAndBoardId(userId, boardId);
            UserRole userRole = existing.orElseGet(UserRole::new);

            userRole.setUser(targetUser);
            userRole.setRole(role);
            userRole.setBoard(board);

            return ResponseEntity.ok(userRoleRepository.save(userRole));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // 🔧 API để test gán quyền ADMIN cho user (không kiểm tra permission - chỉ để test)
    @PostMapping("/test-make-admin")
    public ResponseEntity<?> makeUserAdmin(@RequestBody Map<String, Object> req,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // Lấy thông tin từ request
            Integer targetUserId = Integer.valueOf(req.get("userId").toString());
            Integer boardId = Integer.valueOf(req.get("boardId").toString());

            // Tìm user và board
            User targetUser = userRepository.findById(targetUserId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + targetUserId));

            Board board = boardRepository.findById(boardId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy board với ID: " + boardId));

            // Kiểm tra xem đã có UserRole cho user này trên board này chưa
            Optional<UserRole> existing = userRoleRepository.findByUserIdAndBoardId(targetUserId, boardId);
            UserRole userRole;

            if (existing.isPresent()) {
                // Cập nhật role hiện tại thành ADMIN
                userRole = existing.get();
                userRole.setRole(Role.ADMIN);
                System.out.println("🔧 Updating existing role to ADMIN for user " + targetUserId + " on board " + boardId);
            } else {
                // Tạo mới UserRole với quyền ADMIN
                userRole = new UserRole();
                userRole.setUser(targetUser);
                userRole.setBoard(board);
                userRole.setRole(Role.ADMIN);
                System.out.println("🔧 Creating new ADMIN role for user " + targetUserId + " on board " + boardId);
            }

            UserRole savedUserRole = userRoleRepository.save(userRole);

            System.out.println("✅ Successfully assigned ADMIN role to user " + targetUser.getEmail() +
                    " on board " + board.getName());

            return ResponseEntity.ok(Map.of(
                    "message", "Đã gán quyền ADMIN thành công!",
                    "userEmail", targetUser.getEmail(),
                    "boardName", board.getName(),
                    "role", "ADMIN",
                    "userRole", savedUserRole
            ));

        } catch (Exception e) {
            System.out.println("❌ Error making user admin: " + e.getMessage());
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // 🔧 API để tạo board và gán quyền ADMIN cho user (test purpose)
    @PostMapping("/test-create-board-and-admin")
    public ResponseEntity<?> createBoardAndMakeAdmin(@RequestBody Map<String, Object> req,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            Integer targetUserId = Integer.valueOf(req.get("userId").toString());
            String boardName = req.get("boardName").toString();

            // Tìm user
            User targetUser = userRepository.findById(targetUserId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + targetUserId));

            // Tạo board mới
            Board newBoard = new Board();
            newBoard.setName(boardName);
            newBoard.setDescription("Board được tạo tự động để test");
            Board savedBoard = boardRepository.save(newBoard);

            // Gán quyền ADMIN cho user trên board mới
            UserRole userRole = new UserRole();
            userRole.setUser(targetUser);
            userRole.setBoard(savedBoard);
            userRole.setRole(Role.ADMIN);
            UserRole savedUserRole = userRoleRepository.save(userRole);

            System.out.println("✅ Created board '" + boardName + "' and assigned ADMIN role to user " + targetUser.getEmail());

            return ResponseEntity.ok(Map.of(
                    "message", "Đã tạo board và gán quyền ADMIN thành công!",
                    "userEmail", targetUser.getEmail(),
                    "boardName", boardName,
                    "boardId", savedBoard.getId(),
                    "role", "ADMIN",
                    "userRole", savedUserRole
            ));

        } catch (Exception e) {
            System.out.println("❌ Error creating board and making admin: " + e.getMessage());
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }

    // 🔧 API để gán user thành system admin (chỉ dùng cho dev/test)
    @PostMapping("/test-make-system-admin")
    public ResponseEntity<?> makeSystemAdmin(@RequestBody Map<String, Object> req) {
        try {
            Integer userId = Integer.valueOf(req.get("userId").toString());
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user với ID: " + userId));
            user.setSystemAdmin(true);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of(
                    "message", "Đã gán quyền SYSTEM ADMIN cho user!",
                    "userId", user.getId(),
                    "userEmail", user.getEmail(),
                    "isSystemAdmin", user.isSystemAdmin()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}
