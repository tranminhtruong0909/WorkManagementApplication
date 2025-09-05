package com.example.workmanager.service;

import com.example.workmanager.dto.request.AssignRoleRequest;
import com.example.workmanager.exceptions.ValidationException;
import com.example.workmanager.model.*;
import com.example.workmanager.repository.BoardRepository;
import com.example.workmanager.repository.GroupRepository;
import com.example.workmanager.repository.UserRepository;
import com.example.workmanager.repository.UserRoleRepository;
import com.example.workmanager.exceptions.ResourceNotFoundException;
import com.example.workmanager.exceptions.PermissionDeniedException;
import com.example.workmanager.exceptions.BusinessLogicException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final GroupRepository groupRepository;
    private final UserRoleRepository userRoleRepo;
    private final UserRepository userRepository;
    private final PermissionFacade permissionFacade;

    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    public Optional<Board> getBoardById(Integer id) {
        return boardRepository.findById(id);
    }

//    public Board createBoard(Board board) {
//        return boardRepository.save(board);
//    }

    public Board createBoard(Board board, User creator) {
        Board savedBoard = boardRepository.save(board);

        UserRole creatorRole = new UserRole();
        creatorRole.setUser(creator);
        creatorRole.setBoard(savedBoard);
        creatorRole.setRole(Role.MANAGER);
        userRoleRepo.save(creatorRole);

        return savedBoard;
    }

//    public Board updateBoard(Integer id, Board boardDetails) {
//        Board board = boardRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));
//
//        board.setName(boardDetails.getName());
//        board.setDescription(boardDetails.getDescription());
//
//        return boardRepository.save(board);
//    }

    public Board updateBoard(Integer id, Board boardDetails, User editor) {
        // Kiểm tra cả quyền System Admin
        if (!editor.isSystemAdmin() && !permissionFacade.canEditBoardInfo(editor.getId(), id)) {
            throw new ValidationException("Bạn không có quyền chỉnh sửa board này!", "EDIT_BOARD");
        }

        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));

        board.setName(boardDetails.getName());
        board.setDescription(boardDetails.getDescription());

        return boardRepository.save(board);
    }

//    public void deleteBoard(Integer id) {
//        Board board = boardRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));
//        boardRepository.delete(board);
//    }

    public void deleteBoard(Integer id, User deleter) {
        if (!permissionFacade.canManageBoard(deleter.getId(), id)) {
            throw new ValidationException("Bạn không có quyền xóa board này!", "DELETE_BOARD");
        }
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found with id: " + id));

        boardRepository.delete(board);
    }

//    public List<Group> getGroupByBoardId(Integer id) {
//        return groupRepository.findByBoardId(id);
//    }

    public List<Group> getGroupsByBoardId(Integer boardId, User viewer) {
        if (!permissionFacade.canViewBoard(viewer.getId(), boardId)) {
            throw new PermissionDeniedException("Bạn không có quyền xem board này", "VIEW_BOARD");
        }
        return groupRepository.findByBoardId(boardId);
    }


        public void assignUserRole(User currentUser, Integer broadId, AssignRoleRequest req) {
        Board board = boardRepository.findById(broadId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy board!"));
        if (!permissionFacade.canManageBoardUsers(currentUser.getId(), broadId)) {
            throw new PermissionDeniedException("Bạn không có quyền phân quyền người khác trong board này ", "MANAGE_BOARD_USER");
        }
        Role newRole;
        try {
            newRole = Role.valueOf(req.getRole().toUpperCase());

        } catch (IllegalArgumentException e) {
            throw new BusinessLogicException("Vai trò không hợp lệ: " + req.getRole() + "Vai trò hợp lệ: MANAGER, MEMBER, VIEWER", "INVALID_ROLE");

        }
        User userToUpdate = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy USER"));

        if (currentUser.getId().equals(userToUpdate.getId()) && newRole != Role.MANAGER) {
            long managerCount = userRoleRepo.countManagersInBoard(broadId);
            if (managerCount <= 1) {
                throw new BusinessLogicException("Bạn không thể tự hạ thấp vai trò của mình vì bạn là manager duy nhất trong board này ", "CANNOT_DEMOTE_SEFT");

            }
        }

        Optional<UserRole> existingRoleOpt = userRoleRepo.findByUserIdAndBoardId(userToUpdate.getId(), broadId);
        if (existingRoleOpt.isPresent()) {
            UserRole existingRole = existingRoleOpt.get();
            existingRole.setBoard(board);
            userRoleRepo.save(existingRole);
        } else {
            UserRole newUserRole = new UserRole();
            newUserRole.setUser(userToUpdate);
            newUserRole.setBoard(board);
            newUserRole.setRole(newRole);
            userRoleRepo.save(newUserRole);

        }

    }

//    public List<UserRole> getBoardUser(Integer boardid) {
//        return userRoleRepo.findByBoardId(boardid);
//    }

    public List<UserRole> getBoardUser(Integer boardid, User requester) {
        if (!permissionFacade.canViewBoard(requester.getId(), boardid)) {
            throw new PermissionDeniedException("Bạn không có quyền xem danh sách user của board này ", "VIEW_BOARD_USER");
        }
        return userRoleRepo.findByBoardId(boardid);
    }
}