package com.example.workmanager.repository;

import com.example.workmanager.model.UserRole;
import com.example.workmanager.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Integer> {

    // ✅ Lấy vai trò của user trong 1 board
    Optional<UserRole> findByUserIdAndBoardId(Integer userId, Integer boardId);


    // ✅ Lấy tất cả user trong 1 board
    List<UserRole> findAllByBoardId(Integer boardId);

    // ✅ Lấy tất cả board mà user tham gia
    List<UserRole> findAllByUserId(Integer userId);


    // ✅ Tìm tất cả UserRole theo boardId
    List<UserRole> findByBoardId(Integer boardId);

    // ✅ Tìm tất cả UserRole theo userId
    List<UserRole> findByUserId(Integer userId);

    // ✅ Đếm số MANAGER trong board
    @Query("SELECT COUNT(ur) FROM UserRole ur WHERE ur.board.id = :boardId AND ur.role = 'MANAGER'")
    long countManagersInBoard(@Param("boardId") Integer boardId);

    // ✅ Tìm tất cả UserRole theo boardId và role
    List<UserRole> findByBoardIdAndRole(Integer boardId, Role role);

}
