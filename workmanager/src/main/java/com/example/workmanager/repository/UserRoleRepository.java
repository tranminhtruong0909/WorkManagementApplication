package com.example.workmanager.repository;

import com.example.workmanager.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    // ✅ Lấy vai trò của user trong 1 board
    Optional<UserRole> findByUserIdAndBoardId(Long userId, Long boardId);

    // ✅ Lấy tất cả user trong 1 board
    List<UserRole> findAllByBoardId(Long boardId);

    // ✅ Lấy tất cả board mà user tham gia
    List<UserRole> findAllByUserId(Long userId);
}
