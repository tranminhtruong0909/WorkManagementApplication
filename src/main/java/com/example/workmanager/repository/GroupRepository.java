package com.example.workmanager.repository;

import com.example.workmanager.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {

    // ✅ Tìm groups theo boardId
    List<Group> findByBoardId(Integer boardId);

    // ✅ Các phương thức khác nếu cần
    List<Group> findByBoardIdAndNameContaining(Integer boardId, String name);
    boolean existsByBoardIdAndName(Integer boardId, String name);
}