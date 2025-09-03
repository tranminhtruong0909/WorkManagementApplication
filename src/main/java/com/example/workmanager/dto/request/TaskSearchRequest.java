package com.example.workmanager.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class TaskSearchRequest {
    private Integer id;
    private String name;
    private String status;
    private Integer groupId;
    private Integer assigneeId;
    private Integer userId;

    private LocalDate searchDate;
    private LocalDate startDate;
    private LocalDate endDate;

    private String sortBy = "id";
    private String sortDirection = "asc";
    private int page = 0;
    private int size = 15;

    public boolean hasAnyFilter() {
        return name != null || status != null || groupId != null ||
                assigneeId != null || searchDate != null ||
                startDate != null || endDate != null;
    }
}