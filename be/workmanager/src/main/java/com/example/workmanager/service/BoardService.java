package com.example.workmanager.service;

import com.example.workmanager.model.Board;
import com.example.workmanager.model.Group;
import com.example.workmanager.repository.BoardRepository;
import com.example.workmanager.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final GroupRepository groupRepository;

    public List<Board> getAllBoards() {
        return boardRepository.findAll();
    }

    public Optional<Board> getBoardById(Integer id) {
        return boardRepository.findById(id);
    }

    public Board createBoard(Board board) {
        return boardRepository.save(board);
    }

    public Board updateBoard(Integer id, Board boardDetails) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + id));

        board.setName(boardDetails.getName());
        board.setDescription(boardDetails.getDescription());

        return boardRepository.save(board);
    }

    public void deleteBoard(Integer id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + id));
        boardRepository.delete(board);
    }

    public List<Group> getGroupsByBoardId(Integer boardId) {
        return groupRepository.findByBoardId(boardId);
    }
}