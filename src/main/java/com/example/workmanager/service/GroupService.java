package com.example.workmanager.service;

import com.example.workmanager.model.Board;
import com.example.workmanager.model.Group;
import com.example.workmanager.model.Task;
import com.example.workmanager.repository.BoardRepository;
import com.example.workmanager.repository.GroupRepository;
import com.example.workmanager.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final BoardRepository boardRepository;
    private final TaskRepository taskRepository;

    // ✅ Tạo group mới
    public Group createGroup(String name, Integer boardId) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new RuntimeException("Board not found with id: " + boardId));

        Group group = new Group();
        group.setName(name);
        group.setBoard(board);

        return groupRepository.save(group);
    }

    // ✅ Cập nhật group
    public Group updateGroup(Integer id, String name) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + id));

        group.setName(name);
        return groupRepository.save(group);
    }

    // ✅ Xoá group
    public void deleteGroup(Integer id) {
        Group group = groupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + id));
        groupRepository.delete(group);
    }

    // ✅ Lấy group theo ID
    public Optional<Group> getGroupById(Integer id) {
        return groupRepository.findById(id);
    }

    // ✅ Lấy toàn bộ group
    public List<Group> getAllGroups() {
        return groupRepository.findAll();
    }

    // ✅ Lấy task trong group (nếu cần dùng trực tiếp tại đây)
    public List<Task> getTasksByGroupId(Integer groupId) {
        return taskRepository.findByGroupId(groupId);
    }
}
