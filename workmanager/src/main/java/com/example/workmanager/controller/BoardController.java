package com.example.workmanager.controller;

import com.example.workmanager.dto.AssignRoleRequest;
import com.example.workmanager.dto.BoardResponse;
import com.example.workmanager.dto.GroupResponse;
import com.example.workmanager.model.Board;
import com.example.workmanager.model.CustomUserDetails;
import com.example.workmanager.model.Group;
import com.example.workmanager.model.User;
import com.example.workmanager.repository.UserRepository;
import com.example.workmanager.service.BoardService;
import com.example.workmanager.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.example.workmanager.model.Role;
import java.util.Map;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BoardController {

    private final BoardService boardService;
    private final UserRepository userRepository;
    private final PermissionService permissionService;

    // ✅ Lấy danh sách tất cả board
    @GetMapping
    public ResponseEntity<List<BoardResponse>> getAllBoards() {
        List<Board> boards = boardService.getAllBoards();
        List<BoardResponse> response = boards.stream()
                .map(BoardResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // ✅ Lấy 1 board theo id (kèm danh sách group)
    @GetMapping("/{id}")
    public ResponseEntity<BoardResponse> getBoardById(@PathVariable Integer id,
                                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        User user = userDetails.getUser();
        if (!permissionService.canViewBoard(user.getId(), id)) {
            return ResponseEntity.status(403).body("Bạn không có quyền thực hiện chức năng này!");
        }
        return boardService.getBoardById(id)
                .map(board -> ResponseEntity.ok(new BoardResponse(board)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Tạo mới board
    @PostMapping
    public ResponseEntity<BoardResponse> createBoard(@RequestBody Board board,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // Kiểm tra authentication
            if (userDetails == null) {
                System.out.println("DEBUG: No authentication found");
                return ResponseEntity.status(401).build();
            }
            
            User user = userDetails.getUser();
            System.out.println("DEBUG: User " + user.getId() + " creating board: " + board.getName());
            
            Board created = boardService.createBoard(board);
            System.out.println("DEBUG: Board created with ID: " + created.getId());
            
            // Tự động gán quyền ADMIN cho user tạo board
            AssignRoleRequest assignRequest = new AssignRoleRequest();
            assignRequest.setUserId(user.getId());
            assignRequest.setRole("ADMIN");
            System.out.println("DEBUG: Assigning ADMIN role to user " + user.getId() + " on board " + created.getId());
            boardService.assignUserRole(user, created.getId(), assignRequest);
            
            System.out.println("DEBUG: Board creation completed successfully");
            return ResponseEntity.ok(new BoardResponse(created));
        } catch (Exception e) {
            System.out.println("DEBUG: Error creating board: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // ✅ Cập nhật thông tin board
    @PutMapping("/{id}")
    public ResponseEntity<BoardResponse> updateBoard(@PathVariable Integer id, @RequestBody Board board,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.getUser();
            if (!permissionService.canManageBoard(user.getId(), id)) {
                return ResponseEntity.status(403).body("Bạn không có quyền thực hiện chức năng này!");
            }
            Board updated = boardService.updateBoard(id, board);
            return ResponseEntity.ok(new BoardResponse(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ Xoá board
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBoard(@PathVariable Integer id,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.getUser();
            System.out.println("DEBUG: User " + user.getId() + " trying to delete board " + id);
            
            // Kiểm tra board có tồn tại không
            Optional<Board> boardOpt = boardService.getBoardById(id);
            if (boardOpt.isEmpty()) {
                System.out.println("DEBUG: Board " + id + " not found");
                return ResponseEntity.notFound().build();
            }
            
            if (!permissionService.canManageBoard(user.getId(), id)) {
                System.out.println("DEBUG: User " + user.getId() + " doesn't have permission to delete board " + id);
                return ResponseEntity.status(403).body("Bạn không có quyền thực hiện chức năng này!");
            }
            
            System.out.println("DEBUG: Deleting board " + id);
            boardService.deleteBoard(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            System.out.println("DEBUG: RuntimeException when deleting board " + id + ": " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.out.println("DEBUG: Exception when deleting board " + id + ": " + e.getMessage());
            return ResponseEntity.internalServerError().body("Lỗi server: " + e.getMessage());
        }
    }

    // ✅ Lấy danh sách group thuộc 1 board
    @GetMapping("/{id}/groups")
    public ResponseEntity<List<GroupResponse>> getGroupsByBoardId(@PathVariable Integer id) {
        List<Group> groups = boardService.getGroupsByBoardId(id);
        List<GroupResponse> response = groups.stream()
                .map(GroupResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/{boardId}/assign-role")
    public ResponseEntity<?> assignRole(@PathVariable Integer boardId,
                                        @RequestBody AssignRoleRequest req,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.getUser();
            if (!permissionService.canManageBoard(user.getId(), boardId)) {
                return ResponseEntity.status(403).body("Bạn không có quyền thực hiện chức năng này!");
            }
            // Lấy userId từ request body
            Integer userId = req.getUserId();
            User currentUser = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User không tồn tại"));

            boardService.assignUserRole(currentUser, boardId, req);
            return ResponseEntity.ok("Phân quyền thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi phân quyền: " + e.getMessage());
        }
    }

    // ✅ Kiểm tra role của user trên board (để debug)
    @GetMapping("/{boardId}/my-role")
    public ResponseEntity<?> getMyRoleOnBoard(@PathVariable Integer boardId,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            User user = userDetails.getUser();
            Role role = permissionService.getRoleOnBoard(user.getId(), boardId);
            return ResponseEntity.ok(Map.of(
                "userId", user.getId(),
                "boardId", boardId,
                "role", role != null ? role.name() : "NO_ROLE"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi: " + e.getMessage());
        }
    }


}
