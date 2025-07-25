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
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BoardController {

    private final BoardService boardService;
    private final UserRepository userRepository;

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
    public ResponseEntity<BoardResponse> getBoardById(@PathVariable Integer id) {
        return boardService.getBoardById(id)
                .map(board -> ResponseEntity.ok(new BoardResponse(board)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ Tạo mới board
    @PostMapping
    public ResponseEntity<BoardResponse> createBoard(@RequestBody Board board) {
        try {
            Board created = boardService.createBoard(board);
            return ResponseEntity.ok(new BoardResponse(created));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // ✅ Cập nhật thông tin board
    @PutMapping("/{id}")
    public ResponseEntity<BoardResponse> updateBoard(@PathVariable Integer id, @RequestBody Board board) {
        try {
            Board updated = boardService.updateBoard(id, board);
            return ResponseEntity.ok(new BoardResponse(updated));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ✅ Xoá board
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBoard(@PathVariable Integer id) {
        try {
            boardService.deleteBoard(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
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
                                        @RequestBody AssignRoleRequest req) {
        try {
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


}
