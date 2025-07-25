package com.example.workmanager.controller;

import com.example.workmanager.model.Board;
import com.example.workmanager.model.Role;
import com.example.workmanager.model.User;
import com.example.workmanager.model.UserRole;
import com.example.workmanager.repository.BoardRepository;
import com.example.workmanager.repository.UserRepository;
import com.example.workmanager.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    // ✅ Lấy role của user trong 1 board
    @GetMapping("/user/{userId}/board/{boardId}")
    public ResponseEntity<?> getUserRoleInBoard(@PathVariable Long userId,
                                                @PathVariable Long boardId) {
        return userRoleRepository.findByUserIdAndBoardId(userId, boardId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Lấy danh sách tất cả user và vai trò trong 1 board
    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<UserRole>> getAllUsersInBoard(@PathVariable Long boardId) {
        List<UserRole> roles = userRoleRepository.findAllByBoardId(boardId);
        return ResponseEntity.ok(roles);
    }

    // ✅ Lấy tất cả board mà user tham gia
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserRole>> getAllBoardsOfUser(@PathVariable Long userId) {
        List<UserRole> roles = userRoleRepository.findAllByUserId(userId);
        return ResponseEntity.ok(roles);
    }

    // ✅ Dùng để gán nhanh 1 role cho user (chỉ để test, không kiểm tra quyền)
    @PostMapping("/seed")
    public ResponseEntity<?> seedUserRole(@RequestBody Map<String, Object> req) {
        try {
            Long userId = Long.valueOf(req.get("userId").toString());
            Long boardId = Long.valueOf(req.get("boardId").toString());
            String roleStr = req.get("role").toString().toUpperCase();

            Role role = Role.valueOf(roleStr);

            User user = userRepository.findById(userId.intValue())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            Board board = boardRepository.findById(boardId.intValue())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy board"));

            // Nếu đã tồn tại thì cập nhật, không tạo mới
            Optional<UserRole> existing = userRoleRepository.findByUserIdAndBoardId(userId, boardId);
            UserRole userRole = existing.orElseGet(UserRole::new);

            userRole.setUser(user);
            userRole.setBoard(board);
            userRole.setRole(role);

            return ResponseEntity.ok(userRoleRepository.save(userRole));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }
}
