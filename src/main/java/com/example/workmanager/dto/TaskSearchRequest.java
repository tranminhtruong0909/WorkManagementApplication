package com.example.workmanager.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskSearchRequest {
    // Tìm kiếm cơ bản
    private String name;           // Tìm kiếm theo tên (partial match)
    private String status;         // Tìm kiếm theo trạng thái
    private Integer groupId;       // Tìm kiếm trong group cụ thể
    private Integer assigneeId;    // Tìm kiếm theo người được giao

    // Tìm kiếm theo thời gian
    private LocalDate searchDate;  // Ngày cụ thể để tìm task đang hoạt động
    private LocalDate startDate;   // Ngày bắt đầu khoảng tìm kiếm
    private LocalDate endDate;     // Ngày kết thúc khoảng tìm kiếm

    // Sắp xếp và phân trang
    private String sortBy = "id";           // Sắp xếp theo field nào
    private String sortDirection = "asc";   // asc hoặc desc
    private int page = 0;                   // Trang hiện tại
    private int size = 15;                  // Số lượng item per page

    // Constructor mặc định
    public TaskSearchRequest() {}

    // Constructor đơn giản
    public TaskSearchRequest(String name, String status, LocalDate searchDate) {
        this.name = name;
        this.status = status;
        this.searchDate = searchDate;
    }

    // Kiểm tra có filter nào không
    public boolean hasAnyFilter() {
        return name != null || status != null || groupId != null ||
                assigneeId != null || searchDate != null ||
                startDate != null || endDate != null;
    }
}