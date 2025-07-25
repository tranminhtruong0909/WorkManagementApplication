package com.example.workmanager.service;

import com.example.workmanager.dto.AssignRoleRequest;
import com.example.workmanager.model.*;
import com.example.workmanager.repository.BoardRepository;
import com.example.workmanager.repository.GroupRepository;
import com.example.workmanager.repository.UserRepository;
import com.example.workmanager.repository.UserRoleRepository;
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

    // ✅ Lấy toàn bộ board
    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    // ✅ Lấy board theo ID
    public Optional<Board> getBoardById(Integer id) {
        return boardRepository.findById(id);
    }

    // ✅ Tạo board mới
    public Board createBoard(Board board) {
        return boardRepository.save(board);
    }

    // ✅ Cập nhật board
    public Board updateBoard(Integer id, Board boardDetails) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + id));

        board.setName(boardDetails.getName());
        board.setDescription(boardDetails.getDescription());

        return boardRepository.save(board);
    }

    // ✅ Xoá board
    public void deleteBoard(Integer id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + id));
        boardRepository.delete(board);
    }

    // ✅ Lấy group theo board
    public List<Group> getGroupsByBoardId(Integer boardId) {
        return groupRepository.findByBoardId(boardId);
    }

    // ✅ Phân quyền user trong board
    public void assignUserRole(User currentUser, Integer boardId, AssignRoleRequest req) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy board"));

        // Kiểm tra currentUser có phải ADMIN không
        UserRole selfRole = userRoleRepo.findByUserIdAndBoardId(currentUser.getId().longValue(), boardId.longValue())
                .orElseThrow(() -> new RuntimeException("Bạn không thuộc board này"));

        if (selfRole.getRole() != Role.ADMIN) {
            throw new RuntimeException("Bạn không có quyền phân quyền người khác");
        }

        // Gán quyền cho user khác
        Role newRole = Role.valueOf(req.getRole().toUpperCase());

        User userToUpdate = userRepository.findById(req.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        Optional<UserRole> existing = userRoleRepo.findByUserIdAndBoardId(userToUpdate.getId().longValue(), boardId.longValue());

        if (existing.isPresent()) {
            UserRole role = existing.get();
            role.setRole(newRole);
            userRoleRepo.save(role);
        } else {
            UserRole role = new UserRole();
            role.setUser(userToUpdate);
            role.setBoard(board);
            role.setRole(newRole);
            userRoleRepo.save(role);
        }
    }
}
