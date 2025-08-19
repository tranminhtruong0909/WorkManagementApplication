package com.example.workmanager.repository;

import com.example.workmanager.model.Task;
import com.example.workmanager.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Integer> {
    List<Task> findByGroupId(Integer groupId);

    // Query thống nhất cho tất cả các trường hợp tìm kiếm
    @Query("SELECT t FROM Task t WHERE " +
            // Tìm theo tên
            "(:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +

            // Tìm theo status
            "(:status IS NULL OR t.status = :status) AND " +

            // Tìm theo group (field name là "group" trong entity)
            "(:groupId IS NULL OR t.group.id = :groupId) AND " +

            // Tìm theo assignee (field name là "assignee" trong entity)
            "(:assigneeId IS NULL OR t.assignee.id = :assigneeId) AND " +

            // Tìm task đang hoạt động trong ngày cụ thể
            "(:searchDate IS NULL OR " +
            " ((:searchDate >= t.timelineStart AND :searchDate <= t.timelineEnd) OR " +
            "  (t.dueDate IS NOT NULL AND :searchDate <= t.dueDate AND t.status != 'COMPLETED') OR " +
            "  (t.timelineStart IS NULL AND t.timelineEnd IS NULL AND " +
            "   (t.dueDate IS NULL OR :searchDate <= t.dueDate) AND t.status != 'COMPLETED'))) AND " +

            // Tìm trong khoảng thời gian
            "(:startDate IS NULL OR :endDate IS NULL OR " +
            " ((t.timelineStart IS NOT NULL AND t.timelineStart <= :endDate AND " +
            "   (t.timelineEnd IS NULL OR t.timelineEnd >= :startDate)) OR " +
            "  (t.dueDate IS NOT NULL AND t.dueDate >= :startDate AND t.dueDate <= :endDate)))")
    Page<Task> searchTasksUnified(@Param("name") String name,
                                  @Param("status") TaskStatus status,
                                  @Param("groupId") Integer groupId,
                                  @Param("assigneeId") Integer assigneeId,
                                  @Param("searchDate") LocalDate searchDate,
                                  @Param("startDate") LocalDate startDate,
                                  @Param("endDate") LocalDate endDate,
                                  Pageable pageable);

    // Version không phân trang cho các trường hợp đặc biệt
    @Query("SELECT t FROM Task t WHERE " +
            "(:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:groupId IS NULL OR t.group.id = :groupId) AND " +
            "(:assigneeId IS NULL OR t.assignee.id = :assigneeId) AND " +
            "(:searchDate IS NULL OR " +
            " ((:searchDate >= t.timelineStart AND :searchDate <= t.timelineEnd) OR " +
            "  (t.dueDate IS NOT NULL AND :searchDate <= t.dueDate AND t.status != 'COMPLETED') OR " +
            "  (t.timelineStart IS NULL AND t.timelineEnd IS NULL AND " +
            "   (t.dueDate IS NULL OR :searchDate <= t.dueDate) AND t.status != 'COMPLETED'))) AND " +
            "(:startDate IS NULL OR :endDate IS NULL OR " +
            " ((t.timelineStart IS NOT NULL AND t.timelineStart <= :endDate AND " +
            "   (t.timelineEnd IS NULL OR t.timelineEnd >= :startDate)) OR " +
            "  (t.dueDate IS NOT NULL AND t.dueDate >= :startDate AND t.dueDate <= :endDate)))")
    List<Task> searchTasksUnifiedList(@Param("name") String name,
                                      @Param("status") TaskStatus status,
                                      @Param("groupId") Integer groupId,
                                      @Param("assigneeId") Integer assigneeId,
                                      @Param("searchDate") LocalDate searchDate,
                                      @Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);
    // Method count cho phân trang
    @Query("SELECT COUNT(t) FROM Task t WHERE " +
            "(:name IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
            "(:status IS NULL OR t.status = :status) AND " +
            "(:groupId IS NULL OR t.group.id = :groupId) AND " +
            "(:assigneeId IS NULL OR t.assignee.id = :assigneeId) AND " +
            "(:searchDate IS NULL OR " +
            " ((:searchDate >= t.timelineStart AND :searchDate <= t.timelineEnd) OR " +
            "  (t.dueDate IS NOT NULL AND :searchDate <= t.dueDate AND t.status != 'COMPLETED') OR " +
            "  (t.timelineStart IS NULL AND t.timelineEnd IS NULL AND " +
            "   (t.dueDate IS NULL OR :searchDate <= t.dueDate) AND t.status != 'COMPLETED'))) AND " +
            "(:startDate IS NULL OR :endDate IS NULL OR " +
            " ((t.timelineStart IS NOT NULL AND t.timelineStart <= :endDate AND " +
            "   (t.timelineEnd IS NULL OR t.timelineEnd >= :startDate)) OR " +
            "  (t.dueDate IS NOT NULL AND t.dueDate >= :startDate AND t.dueDate <= :endDate)))")
    long countSearchTasksUnified(@Param("name") String name,
                                 @Param("status") TaskStatus status,
                                 @Param("groupId") Integer groupId,
                                 @Param("assigneeId") Integer assigneeId,
                                 @Param("searchDate") LocalDate searchDate,
                                 @Param("startDate") LocalDate startDate,
                                 @Param("endDate") LocalDate endDate);
}