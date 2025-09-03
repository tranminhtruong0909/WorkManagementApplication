package com.example.workmanager.dto;

import com.example.workmanager.dto.response.TaskResponse;
import lombok.Data;
import java.util.List;

@Data
public class TaskSearchResult {
    private List<TaskResponse> tasks;     // Danh sách task trong page hiện tại
    private long totalElements;           // Tổng số task trong DB (sau khi filter)
    private int totalPages;              // Tổng số trang
    private int currentPage;             // Trang hiện tại
    private int pageSize;                // Kích thước page
    private boolean hasMore;             // Còn trang tiếp theo không
    private boolean isFirst;             // Trang đầu tiên?
    private boolean isLast;              // Trang cuối cùng?

    public TaskSearchResult(List<TaskResponse> tasks, long totalElements,
                            int currentPage, int pageSize) {
        this.tasks = tasks;
        this.totalElements = totalElements;
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.totalPages = pageSize > 0 ? (int) Math.ceil((double) totalElements / pageSize) : 1;
        this.hasMore = currentPage < totalPages - 1;
        this.isFirst = currentPage == 0;
        this.isLast = currentPage >= totalPages - 1;
    }
}