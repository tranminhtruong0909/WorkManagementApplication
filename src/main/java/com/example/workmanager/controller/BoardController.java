package com.example.workmanager.controller;

import com.example.workmanager.dto.request.AssignRoleRequest;
import com.example.workmanager.dto.response.BoardResponse;
import com.example.workmanager.dto.response.GroupResponse;
import com.example.workmanager.model.Board;
import com.example.workmanager.model.CustomUserDetails;
import com.example.workmanager.model.Group;
import com.example.workmanager.model.User;
import com.example.workmanager.repository.UserRepository;
import com.example.workmanager.service.BoardService;
import com.example.workmanager.service.PermissionFacade;
import com.example.workmanager.exceptions.ResourceNotFoundException;
import com.example.workmanager.util.PermissionValidator;

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
    private final PermissionValidator permissionValidator;
    private final PermissionFacade permissionFacade;

    @GetMapping
    public ResponseEntity<List<BoardResponse>> getAllBoards(@AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);
        User user = userDetails.getUser();

        List<Board> allBoards = boardService.getAllBoards();

        List<BoardResponse> accessibleBoards = allBoards.stream()
                .filter(board -> permissionFacade.canViewBoard(user.getId(), board.getId()))
                .map(BoardResponse::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(accessibleBoards);
    }

    @GetMapping("/{boardId}")
    public ResponseEntity<BoardResponse> getBoardById(@PathVariable Integer boardId,
                                                      @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);
        User user = userDetails.getUser();
        permissionValidator.validateBoardViewPermission(user.getId(), boardId);

        Board board = boardService.getBoardById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board không tồn tại"));

        return ResponseEntity.ok(new BoardResponse(board));
    }

    @PostMapping
    public ResponseEntity<BoardResponse> createBoard(@RequestBody Board board,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);
        User user = userDetails.getUser();

        Board created = boardService.createBoard(board, user);
        return ResponseEntity.ok(new BoardResponse(created));
    }

    @PutMapping("/{boardId}")
    public ResponseEntity<BoardResponse> updateBoard(@PathVariable Integer boardId,
                                                     @RequestBody Board board,
                                                     @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);
        User user = userDetails.getUser();
        permissionValidator.validateBoardEditPermission(user.getId(), boardId);

        Board updated = boardService.updateBoard(boardId, board, user);
        return ResponseEntity.ok(new BoardResponse(updated));
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<?> deleteBoard(@PathVariable Integer boardId,
                                         @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);
        User user = userDetails.getUser();
        permissionValidator.validateBoardManagePermission(user.getId(), boardId);

        boardService.deleteBoard(boardId, user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{boardId}/groups")
    public ResponseEntity<List<GroupResponse>> getGroupsByBoardId(@PathVariable Integer boardId,
                                                                  @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);
        User user = userDetails.getUser();
        permissionValidator.validateBoardViewPermission(user.getId(), boardId);

        List<Group> groups = boardService.getGroupsByBoardId(boardId, user);
        List<GroupResponse> response = groups.stream()
                .map(GroupResponse::new)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{boardId}/assign-role")
    public ResponseEntity<?> assignRole(@PathVariable Integer boardId,
                                        @RequestBody AssignRoleRequest req,
                                        @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);
        User user = userDetails.getUser();
        permissionValidator.validateBoardUserManagePermission(user.getId(), boardId);

        User targetUser = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User không tồn tại"));

        boardService.assignUserRole(user, boardId, req);
        return ResponseEntity.ok("Phân quyền thành công");
    }

    @GetMapping("/{boardId}/my-role")
    public ResponseEntity<?> getMyRoleOnBoard(@PathVariable Integer boardId,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        permissionValidator.validateUserAuthentication(userDetails);
        return ResponseEntity.badRequest().body("Chức năng đang được phát triển");
    }
}