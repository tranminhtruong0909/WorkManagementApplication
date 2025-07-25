package com.example.workmanager.controller;

import com.example.workmanager.model.Board;
import com.example.workmanager.model.Group;
import com.example.workmanager.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BoardController {

    private final BoardService boardService;

    @GetMapping
    public ResponseEntity<List<Board>> getAllBoards() {
        List<Board> boards = boardService.getAllBoards();
        return ResponseEntity.ok(boards);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Board> getBoardById(@PathVariable Integer id) {
        return boardService.getBoardById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Board> createBoard(@RequestBody Board board) {
        try {
            Board createdBoard = boardService.createBoard(board);
            return ResponseEntity.ok(createdBoard);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Board> updateBoard(@PathVariable Integer id, @RequestBody Board board) {
        try {
            Board updatedBoard = boardService.updateBoard(id, board);
            return ResponseEntity.ok(updatedBoard);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBoard(@PathVariable Integer id) {
        try {
            boardService.deleteBoard(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/groups")
    public ResponseEntity<List<Group>> getGroupsByBoardId(@PathVariable Integer id) {
        List<Group> groups = boardService.getGroupsByBoardId(id);
        return ResponseEntity.ok(groups);
    }
}