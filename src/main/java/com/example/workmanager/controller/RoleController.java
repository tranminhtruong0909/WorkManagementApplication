package com.example.workmanager.controller;

import com.example.workmanager.model.Board;
import com.example.workmanager.model.Role;
import com.example.workmanager.model.User;
import com.example.workmanager.model.UserRole;
import com.example.workmanager.repository.BoardRepository;
import com.example.workmanager.repository.UserRepository;
import com.example.workmanager.repository.UserRoleRepository;
import com.example.workmanager.model.CustomUserDetails;
import com.example.workmanager.service.PermissionFacade;
import com.example.workmanager.exceptions.PermissionDeniedException;
import com.example.workmanager.exceptions.ResourceNotFoundException;
import com.example.workmanager.exceptions.BusinessLogicException;
import com.example.workmanager.util.PermissionValidator;

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
    private final PermissionFacade permissionFacade;
    private final PermissionValidator permissionValidator;

    @PostMapping("/user/{userId}/board/{boardId}")
    public ResponseEntity<?> addUserToBoard(@PathVariable Integer userId, @PathVariable Integer boardId,
                                            @RequestBody Map<String, String> req,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {

        permissionValidator.validateUserAuthentication(userDetails);

        User currentUser = userDetails.getUser();
        permissionValidator.validateBoardUserManagePermissions(currentUser.getId(), boardId);

        String roleStr = req.get("role").toString().toUpperCase();
        Role role;
        try {
            role = Role.valueOf(roleStr);
        }
        catch (IllegalArgumentException e) {
            throw new BusinessLogicException("Vai trò không hợp lệ: "+ roleStr, "INVALID_ROLE");
        }

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user"));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy board"));

        Optional<UserRole> existing = userRoleRepository.findByUserIdAndBoardId(userId, boardId);
        UserRole userRole = existing.orElseGet(UserRole::new);

        userRole.setUser(targetUser);
        userRole.setRole(role);
        userRole.setBoard(board);

        return ResponseEntity.ok(userRoleRepository.save(userRole));

    }

    @GetMapping("/board/{boardId}")
    public ResponseEntity<?> getBoard(@PathVariable Integer boardId,
                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();
        permissionValidator.validateBoardUserManagePermissions(user.getId(), boardId);

        List<UserRole> roles = userRoleRepository.findAllByBoardId(boardId);
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<UserRole>> getAllBroadsOfUser(@PathVariable Integer userId,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();
        if ( !user.isSystemAdmin() && !user.getId().equals(userId)){
            throw new PermissionDeniedException("Bạn không có quyền xem thông tin này!", "VIEW_USER_PERMISSION");
        }

        List<UserRole> roles = userRoleRepository.findAllByUserId(user.getId());
        return ResponseEntity.ok(roles);
    }

    @DeleteMapping("/user/{userId}/board/{boardId}")
    public ResponseEntity<?> removeUserFromBoard(@PathVariable Integer userId,
                                                 @PathVariable Integer boardId,
                                                 @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        User currentUser = userDetails.getUser();
        permissionValidator.validateBoardUserManagePermissions(currentUser.getId(), boardId);

        UserRole userRole = userRoleRepository.findByUserIdAndBoardId(userId, boardId)
                .orElseThrow(() -> new ResourceNotFoundException("User không có trong board này !"));

        userRoleRepository.delete(userRole);

        return ResponseEntity.ok(Map.of(
                "message", "Đã xóa thành công user khỏi board này thaành công !",
                "userId", userId,
                "boardId", boardId));
    }

    @PostMapping("/seed")
    public ResponseEntity<?> seedUserRole(@RequestBody Map<String, Object> req,
                                          @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        User user = userDetails.getUser();
        Integer boardId = Integer.valueOf(req.get("boardId").toString());

        permissionValidator.validateBoardUserManagePermissions(user.getId(), boardId);

        try {
            Integer userId = Integer.valueOf(req.get("userId").toString());
            String roleStr = req.get("role").toString().toUpperCase();

            Role role = Role.valueOf(roleStr);

            User targetUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

            Board board = boardRepository.findById(boardId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy board"));

            Optional<UserRole> existing = userRoleRepository.findByUserIdAndBoardId(userId, boardId);
            UserRole userRole = existing.orElseGet(UserRole::new);

            userRole.setUser(targetUser);
            userRole.setRole(role);
            userRole.setBoard(board);

            return ResponseEntity.ok(userRoleRepository.save(userRole));
        } catch (Exception e) {
            throw new BusinessLogicException("Lỗi phân quyền: " + e.getMessage(), "ROLE_ASSIGNMENT_ERROR");
        }
    }

    @PostMapping("/test-make-manager")
    public ResponseEntity<?> makeUserManager(@RequestBody Map<String, Object> req,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);

        User currentUser = userDetails.getUser();
        Integer targetUserId = Integer.valueOf(req.get("UserId").toString());
        Integer boardId = Integer.valueOf(req.get("BoardId").toString());

        permissionValidator.validateBoardUserManagePermissions(currentUser.getId(), boardId);

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy User với ID: "+ targetUserId));
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy board với ID: " + boardId));

        Optional<UserRole> existing = userRoleRepository.findByUserIdAndBoardId(currentUser.getId(), boardId);
        UserRole userRole;

        if(existing.isPresent()){
            userRole = existing.get();
            userRole.setRole(Role.MANAGER);
        }else {
            userRole = new UserRole();
            userRole.setUser(targetUser);
            userRole.setBoard(board);
            userRole.setRole(Role.MANAGER);
        }

        UserRole saveUserRole = userRoleRepository.save(userRole);

        return ResponseEntity.ok(Map.of(
                "message", "Đã gán quyền manager thành công!",
                "userEmail", targetUser.getEmail(),
                "boardName",  board.getName(),
                "Role", "MANAGER",
                "userRole", saveUserRole
        ));
    }

    @PostMapping("/test-create-board-and-manager")
    public ResponseEntity<?> createBoardAndMakeManager(@RequestBody Map<String, Object> req,
                                                       @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);
        permissionValidator.validateSystemAdminPermissions(userDetails.getUser().isSystemAdmin());

        Integer targetUserId = Integer.valueOf(req.get("userId").toString());
        String boardName = req.get("boardName").toString();

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy user với ID: " + targetUserId));

        Board newBoard = new Board();
        newBoard.setName(boardName);
        newBoard.setDescription("Board được tạo tự động để test");
        Board savedBoard = boardRepository.save(newBoard);

        UserRole userRole = new UserRole();
        userRole.setUser(targetUser);
        userRole.setBoard(savedBoard);
        userRole.setRole(Role.MANAGER);
        UserRole savedUserRole = userRoleRepository.save(userRole);

        return ResponseEntity.ok(Map.of(
                "message", "Đã tạo board và gán quyền MANAGER thành công!",
                "userEmail", targetUser.getEmail(),
                "boardName", boardName,
                "boardId", savedBoard.getId(),
                "role", "MANAGER",
                "userRole", savedUserRole
            ));

    }

    @PostMapping("/test-make-system-admin")
    public ResponseEntity<?> makeSystemAdmin(@RequestBody Map<String, Object> req,
                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);
        permissionValidator.validateSystemAdminPermissions(userDetails.getUser().isSystemAdmin());

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
    }
}